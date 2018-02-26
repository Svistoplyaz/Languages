package semester8.Lab2;

import javafx.util.Pair;
import semester7.Lab3.Lexeme;
import semester7.Lab3.Scanner;
import semester7.Lab3.Type;
import semester7.Lab5.AnalyzeError;
import semester7.Lab5.DataType;

import java.math.BigInteger;

import static semester7.Lab3.Type.T_int;
import static semester7.Lab3.Type.T_int64;

public class Interpreter {
    private final Scanner sc;

    private final Node root = new Node(null, null, null);
    private Node current = root;
    private boolean haveMain = false;

//    private Node functionPointer, functionArgumentPointer;

    public Interpreter(Scanner s) {
        sc = s;
    }


    public void addMain(Lexeme identifier) {
        if(haveMain)
            throw new AnalyzeError(sc, identifier, "main is already defined");
        else haveMain = true;
    }

    public void addVariable(Lexeme type, Lexeme identifier) {
        if(type.type == T_int || type.type == T_int64) {
            Node node = findScope(identifier.value);
            if (node != null) alreadyDefined(identifier, node);

            DataType dataType = type.type == T_int ? DataType.tInt : DataType.tInt64;
            current.left = new Node(current, dataType, identifier);
            current = current.left;
        }else{
            addArray(type, identifier);
        }
    }

    public void addConst(DataType type, Lexeme identifier) {
        Node node = findScope(identifier.value);
        if (node != null) alreadyDefined(identifier, node);

        current.left = new Node(current, type, identifier, true);
        current = current.left;
    }

    public void addArray(Lexeme typedef, Lexeme identifier) {
        Node node = findScope(identifier.value);
        if (node != null) alreadyDefined(identifier, node);

        Node typeN = find(typedef.value);
        if(typeN == null)
            throw new AnalyzeError(sc, typedef, typedef.value + " Isn't defined as Type");
        if (!typeN.isType)
            throw new AnalyzeError(sc, typedef, "Isn't a type");

        current.left = new Node(current, typeN.type, identifier, typeN.allsizes, true);
        current = current.left;
    }

    public void addType(Lexeme ancestor, Lexeme identifier, int size) {
        Node node = findScope(identifier.value);
        if (node != null) alreadyDefined(identifier, node);

        int[] newsize;
        DataType dataType;
        if (ancestor.type != T_int && ancestor.type != T_int64) {
            Node typeN = find(ancestor.value);
            if(typeN == null)
                throw new AnalyzeError(sc, ancestor, ancestor.value + " Isn't defined as a Type");
            if (!typeN.isType)
                throw new AnalyzeError(sc, ancestor, "Isn't a type");

            int len = typeN.allsizes.length;
            newsize = new int[len + 1];
            System.arraycopy(typeN.allsizes, 0, newsize, 0, len);
            newsize[len] = size;
            dataType = typeN.type;
        } else {
            newsize = new int[1];
            newsize[0] = size;
            dataType = (ancestor.type == T_int) ? DataType.tInt : DataType.tInt64;
        }

        current.left = new Node(current, dataType, identifier, true, newsize);
        current = current.left;
    }

    private void alreadyDefined(Lexeme identifier, Node found) {
        String s1 = "Identifier '" + identifier.value + "' is already defined in the scope";
        String s2 = "Previous declaration at line " + (found.lexeme.line);
        throw new AnalyzeError(sc, identifier, s1, s2);
    }

    public void startBlock() {
        current.left = new Node(current, DataType.tBlock, null);
        current = current.left;

        right();
    }

    public void goToParentLevel() {
        while (current.type != null) current = current.parent;
        current = current.parent;
    }

    private void right() {
        current.right = new Node(current, null, null);
        current = current.right;
    }

//    public Node findScope(String identifier) {
//        Node node = current;
//        while (node.lexeme != null) {
//            if (node.lexeme.value.equals(identifier)) return node;
//            node = node.parent;
//        }
//        return null;
//    }

    public Node findScope(String identifier) {
        Node node = current;
        if(current == root)
            return null;
        while (node != node.parent.right) {
            if (node.lexeme != null && node.lexeme.value.equals(identifier)) return node;
            node = node.parent;
            if(node.equals(root))
                return null;
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

    public Pair<DataType, Integer> getArrayType(Lexeme array) {
        Node arr = find(array.value);

        if(arr == null)
            throw new AnalyzeError(sc, array, "Variable '" + array.value + "' is not defined in the scope");
        if(!arr.isArray)
            throw new AnalyzeError(sc, array, "Not an array");
        return new Pair<>(arr.type, arr.allsizes.length);
    }

    public DataType cast(DataType a, DataType b) {
        if(a == DataType.tArray || b == DataType.tArray) return null;
        if (a == DataType.tInt64 || b == DataType.tInt64) return DataType.tInt64;
        if (a == DataType.tInt || b == DataType.tInt) return DataType.tInt;
        return null;
    }

    public DataType getConstType(Lexeme lexeme) {
        BigInteger value = new BigInteger(lexeme.value, lexeme.type == Type.T_const10 ? 10 : 16);
        if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) return DataType.tInt;
        if (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) return DataType.tInt64;

        throw new AnalyzeError(sc, lexeme, "Integer constant is too big");
    }

    public int getConstNumber(Lexeme lex) {
        switch (lex.type) {
            case T_const10:
            case T_const16:
                BigInteger value = new BigInteger(lex.value, lex.type == Type.T_const10 ? 10 : 16);
                if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0)
                    return value.intValue();
                break;
            case T_id:
                Node con = find(lex.value);
                if (con == null)
                    throw new AnalyzeError(sc, lex, "Not defined");
                if (!con.isConst)
                    throw new AnalyzeError(sc, lex, "Not constant");
                return Integer.parseInt(lex.value);
        }

        return 0;
    }

    public DataType getVariableType(Lexeme lexeme) {
        Node node = find(lexeme.value);
        if (node == null)
            throw new AnalyzeError(sc, lexeme, "Variable '" + lexeme.value + "' is not defined in the scope");
//        if (node.right != null)
//            throw new AnalyzeError(sc, lexeme, "'" + lexeme.value + "' is not a variable");
        return node.type;
    }

    public void checkAssignment(Lexeme lexeme, DataType variable, DataType expression) {
        if (cast(variable, expression) != variable) {
            String line1 = "Incompatibility of types during operation";
            String line2 = "'" + variable + "' = '" + expression + "'";
            throw new AnalyzeError(sc, lexeme, line1, line2);
        }
    }

    public void dropIfArray(Lexeme lexeme) {
        Node node = find(lexeme.value);
        if(node == null)
            return;
        if(node.isArray)
            throw new AnalyzeError(sc, lexeme, "It is array");
    }

//    public void checkReturnType(Lexeme lexeme, DataType ret) {
//        Node node = current;
//        while (node.right == null || node.type == null || node.type == DataType.tBlock) node = node.parent;
//        DataType required = node.type;
//
//        if (cast(required, ret) != required) {
//            String message = "Incompatible return type '" + ret + "' when required '" + required + "'";
//            throw new AnalyzeError(sc, lexeme, message);
//        }
//    }

//    public void startFunctionCall(Lexeme lexeme) {
//        Node node = find(lexeme.value);
//        if (node == null)
//            throw new AnalyzeError(sc, lexeme, "Function '" + lexeme.value + "' is not defined in the scope");
//        if (node.right == null)
//            throw new AnalyzeError(sc, lexeme, "'" + lexeme.value + "' is not a function");
//
//        functionPointer = node;
//        functionArgumentPointer = node.right.left;
//    }
//
//    public void checkFunctionArgument(Lexeme lexeme, DataType type) {
//        if (functionArgumentPointer == null || functionArgumentPointer.type == DataType.tBlock)
//            throw new AnalyzeError(sc, lexeme, "Too many arguments for function");
//
//        DataType required = functionArgumentPointer.type;
//        if (cast(required, type) != required) {
//            String message = "Incompatible argument type '" + type + "' when required '" + required + "'";
//            throw new AnalyzeError(sc, lexeme, message);
//        }
//        functionArgumentPointer = functionArgumentPointer.left;
//    }
//
//    public DataType finishFunctionCall(Lexeme lexeme) {
//        if (functionArgumentPointer != null && functionArgumentPointer.type != DataType.tBlock)
//            throw new AnalyzeError(sc, lexeme, "Not enough arguments for function");
//        return functionPointer.type;
//    }

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

    public void dropIfType(Lexeme id) {
        Node node = find(id.value);
        if(node == null)
            return;
        if(node.isType)
            throw new AnalyzeError(sc, id, "It is type");
    }

    private static class Node {

        public final Node parent;
        public Node left, right;

        public Lexeme lexeme;
        public DataType type;

        public boolean isType;

        public boolean isConst;

        public boolean isArray;
        public int[] allsizes;

        public Node(Node p, DataType t, Lexeme l) {
            parent = p;
            type = t;
            lexeme = l;
        }

        public Node(Node p, DataType t, Lexeme l, boolean isC) {
            parent = p;
            type = t;
            lexeme = l;
            isConst = isC;
        }

        public Node(Node p, DataType t, Lexeme l, boolean isT, int[] sizes) {
            parent = p;
            type = t;
            lexeme = l;
            isType = isT;
            allsizes = sizes;
        }

        public Node(Node p, DataType t, Lexeme l, int[] sizes, boolean isA) {
            parent = p;
            type = t;
            lexeme = l;
            isArray = isA;
            allsizes = sizes;
        }
    }

}
