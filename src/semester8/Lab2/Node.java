package semester8.Lab2;

import semester7.Lab3.Lexeme;
import semester7.Lab5.DataType;

public class Node {
    public final Node parent;
    public Node left, right;

    public Lexeme lexeme;
    public DataType type;

    public int[] allsizes;

    public Node(Node p, DataType t, Lexeme l) {
        parent = p;
        type = t;
        lexeme = l;
    }
}
