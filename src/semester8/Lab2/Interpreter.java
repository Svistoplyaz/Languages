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
    private final int tooBig = 10000001;
    private long limit = 0;

    private final Node root = new Node(null, null, null);
    private Node current = root;
    private boolean haveMain = false;

//    private Node functionPointer, functionArgumentPointer;

    public Interpreter(Scanner s) {
        sc = s;
    }


    public void addMain(Lexeme identifier) {
        if (haveMain)
            throw new AnalyzeError(sc, identifier, "main is already defined");
        else haveMain = true;
    }

    /**
     * Creates new variable NodeId
     *
     * @param type
     * @param identifier
     */
    public void addVariable(Lexeme type, Lexeme identifier) {
        if (type.type == T_int || type.type == T_int64) {
            Node node = findScope(identifier.value);
            if (node != null) alreadyDefined(identifier, node);

            DataType dataType = type.type == T_int ? DataType.tInt : DataType.tInt64;
            current.left = new NodeId(current, dataType, identifier, false);
            current = current.left;
            increaseLimit(1, identifier);
        } else {
            addArray(type, identifier);
        }
    }

    /**
     * Creates new const NodeId
     *
     * @param type
     * @param identifier
     */
    public void addConst(DataType type, Lexeme identifier) {
        Node node = findScope(identifier.value);
        if (node != null) alreadyDefined(identifier, node);

        current.left = new NodeId(current, type, identifier, true);
        current = current.left;
        increaseLimit(1, identifier);
    }

    /**
     * Adds new NodeArray to your tree/branch
     *
     * @param typedef    type of new array
     * @param identifier id of new array
     */
    public void addArray(Lexeme typedef, Lexeme identifier) {
        Node node = findScope(identifier.value);
        if (node != null) alreadyDefined(identifier, node);

        Node typeN = find(typedef.value);
        if (typeN == null)
            throw new AnalyzeError(sc, typedef, typedef.value + " Isn't defined as Type");
        if (!(typeN instanceof NodeType))
            throw new AnalyzeError(sc, typedef, "Isn't a type");

        current.left = new NodeArray(current, (NodeType) typeN, identifier);
        current = current.left;
        increaseLimit(((NodeArray) current).length(), identifier);
    }

    /**
     * Adds new NodeType to your tree/branch
     *
     * @param ancestor   ancestor type(basic or created by typedef)
     * @param identifier id of new type
     * @param realSize   size of new dimension
     */
    public void addType(Lexeme ancestor, Lexeme identifier, long realSize) {
        if (realSize >= tooBig)
            throw new AnalyzeError(sc, ancestor, "Can't declare type, size is too big");

        int size = (int) realSize;
        Node node = findScope(identifier.value);
        if (node != null) alreadyDefined(identifier, node);

        int[] newsize;
        DataType dataType;
        //If ancestor is not basic type
        if (ancestor.type != T_int && ancestor.type != T_int64) {
            //Tries to find ancestor
            Node typeN = find(ancestor.value);
            if (typeN == null)
                throw new AnalyzeError(sc, ancestor, ancestor.value + " Isn't defined as a Type");
            if (!(typeN instanceof NodeType))
                throw new AnalyzeError(sc, ancestor, "Isn't a type");

            /*
            Вставить сюда проверку на длину массива
            */
            if (((NodeType) typeN).getComboLength() * size < tooBig)
                current.left = new NodeType(current, (NodeType) typeN, identifier, size);
            else
                throw new AnalyzeError(sc, ancestor, "Can't declare type, size is too big");
        } else {
            /*
            Вставить сюда проверку на длину массива
            */

            dataType = (ancestor.type == T_int) ? DataType.tInt : DataType.tInt64;
            current.left = new NodeType(current, dataType, identifier, size);
        }

        current = current.left;
    }

    /**
     * Throws excpetion that variable is already defined
     *
     * @param identifier
     * @param found
     */
    private void alreadyDefined(Lexeme identifier, Node found) {
        String s1 = "Identifier '" + identifier.value + "' is already defined in the scope";
        String s2 = "Previous declaration at line " + (found.lexeme.line);
        throw new AnalyzeError(sc, identifier, s1, s2);
    }

    /**
     * Creates NodeBlock as left of current and new right branch as right of this block,
     * sets head of created branch as current
     */
    public void startBlock() {
        current.left = new NodeBlock(current);
        current = current.left;

        right();
    }

    /**
     * Sets parent of branch head as current, we just close block
     */
    public void goToParentLevel() {
        while (current.type != null)
            current = current.parent;

        current = current.parent;
    }

    /**
     * Creates empty right Node for current as head of branch
     */
    private void right() {
        current.right = new Node(current, null, null);
        current = current.right;
    }

    /**
     * Tries to find node with identifier in current branch
     *
     * @param identifier Lexeme id of Node object
     * @return null if nothing was find
     * or Node object if current branch already has this id
     */
    public Node findScope(String identifier) {
        Node node = current;
        if (current == root)
            return null;
        //While we are in current branch
        while (node != node.parent.right) {
            //If we find lexeme with similar id then we return node with this id
            if (node.lexeme != null && node.lexeme.value.equals(identifier))
                return node;

            //Goind upwards
            node = node.parent;
            if (node.equals(root))
                return null;
        }
        return null;
    }

    /**
     * Tries to find node with identifier in all upward tree
     *
     * @param identifier Lexeme id of Node object
     * @return null if nothing was find
     * or Node object if current branch already has this id
     */
    public Node find(String identifier) {
        Node node = current;
        do {
            //If we find lexeme with similar id then we return node with this id
            if (node.lexeme != null && node.lexeme.value.equals(identifier))
                return node;

            node = node.parent;
        } while (node != null);
        return null;
    }

    public Pair<DataType, Integer> getArrayType(Lexeme array) {
        Node arr = find(array.value);

        if (arr == null)
            throw new AnalyzeError(sc, array, "Variable '" + array.value + "' is not defined in the scope");
        if (!(arr instanceof NodeArray))
            throw new AnalyzeError(sc, array, "Not an array");
        return new Pair<>(arr.type, ((NodeArray) arr).dimensions());
    }

    public DataType cast(DataType a, DataType b) {
        if (a == DataType.tArray || b == DataType.tArray) return null;
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

    public long getConstNumber(Lexeme lex) {
        switch (lex.type) {
            case T_const10:
            case T_const16:
                BigInteger value = new BigInteger(lex.value, lex.type == Type.T_const10 ? 10 : 16);
                if (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0)
                    return value.longValue();
                break;
            case T_id:
                Node con = find(lex.value);
                if (con == null)
                    throw new AnalyzeError(sc, lex, "Not defined");
                if (!(con instanceof NodeId) || !((NodeId) con).isConst)
                    throw new AnalyzeError(sc, lex, "Not constant");
                return ((NodeId) con).value;
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
        if (node == null)
            return;
        if (node instanceof NodeArray)
            throw new AnalyzeError(sc, lexeme, "It is array");
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

    public long calculateA0(long first, long second) {
        System.out.println("A0 first - " + first + "; second - " + second);

        boolean f, s;
        f = first != 0;
        s = second != 0;

        if (f || s)
            return 1;
        else
            return 0;
    }

    public long calculateA1(long first, long second) {
        System.out.println("A1 first - " + first + "; second - " + second);

        boolean f, s;
        f = first != 0;
        s = second != 0;

        if (f && s)
            return 1;
        else
            return 0;
    }

    public long calculateA2(long first, long second, Type type) {
        System.out.println("A2 first - " + first + "; second - " + second);

        boolean ans = false;
        switch (type) {
            case T_less:
                ans = first < second;
                break;
            case T_leq:
                ans = first <= second;
                break;
            case T_more:
                ans = first > second;
                break;
            case T_meq:
                ans = first >= second;
                break;
            case T_eqaul:
                ans = first >= second;
                break;
            case T_neq:
                ans = first >= second;
                break;
            default:
                try {
                    throw new Exception("SHITSTORM A2 !!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        if (ans)
            return 1;
        else
            return 0;
    }

    public long calculateA3(long first, long second, Type type) {
        System.out.println("A3 first - " + first + "; second - " + second);

        switch (type) {
            case T_plus:
                return first + second;
            case T_minus:
                return first - second;
            default:
                try {
                    throw new Exception("SHITSTORM A3 !!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        return 0;
    }

    public long calculateA4(long first, long second, Type type) {
        System.out.println("A4 first - " + first + "; second - " + second);

        switch (type) {
            case T_multiply:
                return first * second;
            case T_division:
                return first / second;
            case T_mod:
                return first % second;
            default:
                try {
                    throw new Exception("SHITSTORM A3 !!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        return 0;
    }

    public long calculateA5(long first, int inc) {
        boolean f = first != 0;

        if (inc % 2 == 1 && !f || inc % 2 == 0 && f)
            return 1;
        else
            return 0;
    }

    public void dropIfType(Lexeme id) {
        Node node = find(id.value);
        if (node == null)
            return;
        if (node instanceof NodeType)
            throw new AnalyzeError(sc, id, "It is type");
    }

    public void increaseLimit(long inc, Lexeme ancestor) {
        limit += inc;
        if (limit >= tooBig)
            throw new AnalyzeError(sc, ancestor, "Can't declare type, size is too big");
    }

    public long getId(Lexeme id) {
        Node node = find(id.value);

        return ((NodeId) node).value;
    }

    public long getElement(Lexeme id, int[] indexes) {
        NodeArray node = (NodeArray) find(id.value);

        return node.array[node.getGreatIndex(indexes)];
    }

    public void putValueIn(Lexeme lex, DataType key, int index, long value) {
        if (index == -1) {
            putValueInId(lex, key, value);
        } else {
            putValueInElement(lex, key, index, value);
        }
    }

    public void putValueInId(Lexeme id, DataType dataType, long value) {
        NodeId node = (NodeId) find(id.value);

        if (dataType == DataType.tInt)
            node.value = (int) value;

        node.value = value;
        System.out.println(id.value + " = " + node.value);
    }

    public void putValueInElement(Lexeme id, DataType dataType, int index, long value) {
        NodeArray node = (NodeArray) find(id.value);

        if (dataType == DataType.tInt)
            node.array[index] = (int) value;

        node.array[index] = value;
        System.out.println(id.value + "[] = " + node.array[index]);
    }

    public long getGreatIndex(Lexeme id, int[] a) {
        NodeArray node = (NodeArray) find(id.value);

        return node.getGreatIndex(a);
    }

    public boolean forCheck(Pair<DataType, Long> pair){
        return pair.getValue()!=0;
    }
}
