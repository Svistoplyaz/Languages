package Lab5;

import java.math.BigInteger;

import Lab3.Scanner;
import Lab3.Lexeme;
import Lab3.Type;

import java.math.BigInteger;

public class SemanticAnalyzer {



    private final Scanner scanner;

    private final Node root = new Node(null, null, null);
    private Node current = root;

    private Node functionPointer, functionArgumentPointer;

    public SemanticAnalyzer(Scanner s) {
        scanner = s;
    }

    public void addVariable(DataType type, Lexeme identifier) {
        Node node = findScope(identifier.value);
        if (node != null) alreadyDefined(identifier, node);

        left(type, identifier);
    }

    public void startFunction(DataType type, Lexeme identifier) {
        Node node = findScope(identifier.value);
        if (node != null) alreadyDefined(identifier, node);

        left(type, identifier);
        right();
    }

    private void alreadyDefined(Lexeme identifier, Node found) {
        String s1 = "Identifier '" + identifier.value + "' is already defined in the scope";
        String s2 = "Previous declaration at line " + (found.lexeme.line + 1);
        throw new AnalyzeError(scanner, identifier, s1, s2);
    }

    public void startBlock() {
        left(DataType.tBlock, null);
        right();
    }

    public void goToParentLevel() {
        while (current.type != null) current = current.parent;
        current = current.parent;
    }

    private void left(DataType type, Lexeme lexeme) {
        current.left = new Node(current, type, lexeme);
        current = current.left;
    }

    private void right() {
        current.right = new Node(current, null, null);
        current = current.right;
    }

    public Node findScope(String identifier) {
        Node node = current;
        while (node.lexeme != null) {
            if (node.lexeme.value.equals(identifier)) return node;
            node = node.parent;
        }
        return null;
    }

    public Node find(String identifier) {
        Node node = current;
        do {
            if (node.lexeme != null && node.lexeme.value.equals(identifier)) return node;
            node = node.parent;
        } while (node != null);
        return null;
    }

    public DataType cast(DataType a, DataType b) {
        if (a == DataType.tLongLongInt || b == DataType.tLongLongInt) return DataType.tLongLongInt;
        if (a == DataType.tLongInt || b == DataType.tLongInt) return DataType.tLongInt;
        if (a == DataType.tInt || b == DataType.tInt) return DataType.tInt;
        return null;
    }

    public DataType getConstType(Lexeme lexeme) {
        BigInteger value = new BigInteger(lexeme.value, lexeme.type == Type.T_const10 ? 10 : 16);
        if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) return DataType.tInt;
        if (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) return DataType.tLongLongInt;

        throw new AnalyzeError(scanner, lexeme, "Integer constant is too big");
    }

    public DataType getVariableType(Lexeme lexeme) {
        Node node = find(lexeme.value);
        if (node == null)
            throw new AnalyzeError(scanner, lexeme, "Variable '" + lexeme.value + "' is not defined in the scope");
        if (node.right != null)
            throw new AnalyzeError(scanner, lexeme, "'" + lexeme.value + "' is not a variable");
        return node.type;
    }

    public void checkAssignment(Lexeme lexeme, DataType variable, DataType expression) {
        if (cast(variable, expression) != variable) {
            String line1 = "Incompatibility of types during operation";
            String line2 = "'" + variable + "' = '" + expression + "'";
            throw new AnalyzeError(scanner, lexeme, line1, line2);
        }
    }

    public void checkReturnType(Lexeme lexeme, DataType ret) {
        Node node = current;
        while (node.right == null || node.type == null || node.type == DataType.tBlock) node = node.parent;
        DataType required = node.type;

        if (cast(required, ret) != required) {
            String message = "Incompatible return type '" + ret + "' when required '" + required + "'";
            throw new AnalyzeError(scanner, lexeme, message);
        }
    }

    public void startFunctionCall(Lexeme lexeme) {
        Node node = find(lexeme.value);
        if (node == null)
            throw new AnalyzeError(scanner, lexeme, "Function '" + lexeme.value + "' is not defined in the scope");
        if (node.right == null)
            throw new AnalyzeError(scanner, lexeme, "'" + lexeme.value + "' is not a function");

        functionPointer = node;
        functionArgumentPointer = node.right.left;
    }

    public void checkFunctionArgument(Lexeme lexeme, DataType type) {
        if (functionArgumentPointer == null || functionArgumentPointer.type == DataType.tBlock)
            throw new AnalyzeError(scanner, lexeme, "Too many arguments for function");

        DataType required = functionArgumentPointer.type;
        if (cast(required, type) != required) {
            String message = "Incompatible argument type '" + type + "' when required '" + required + "'";
            throw new AnalyzeError(scanner, lexeme, message);
        }
        functionArgumentPointer = functionArgumentPointer.left;
    }

    public DataType finishFunctionCall(Lexeme lexeme) {
        if (functionArgumentPointer != null && functionArgumentPointer.type != DataType.tBlock)
            throw new AnalyzeError(scanner, lexeme, "Not enough arguments for function");
        return functionPointer.type;
    }

    public String treeToString() {
        StringBuilder builder = new StringBuilder();
        printTree(builder, root, 0);
        return builder.toString();
    }

    private void printTree(StringBuilder builder, Node node, int level) {
        if (node == null) return;

        for (int i = 0; i < level; i++) {
            builder.append("|");
            for (int j = 0; j < 6; j++) builder.append(" ");
        }

        String value = node.lexeme == null ? null : node.lexeme.value;
        builder.append(node.type == null ? "♦" : (node.type + " " + value));
        if (node == current) builder.append("  <--");

        builder.append("\n");
        printTree(builder, node.right, level + 1);
        printTree(builder, node.left, level);
    }

    private static class Node {

        public final Node parent;
        public Node left, right;

        public Lexeme lexeme;
        public DataType type;

        public Node(Node p, DataType t, Lexeme l) {
            parent = p;
            type = t;
            lexeme = l;
        }

    }

}

