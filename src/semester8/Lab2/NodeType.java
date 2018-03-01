package semester8.Lab2;

import semester7.Lab3.Lexeme;
import semester7.Lab5.DataType;

public class NodeType extends Node {
    private int[] sizes;

    public NodeType(Node p, DataType t, Lexeme l, int size) {
        super(p, t, l);
        sizes = new int[1];
        sizes[0] = size;
    }

    public NodeType(Node p, NodeType t, Lexeme l, int size) {
        super(p, t.type, l);
        sizes = new int[t.sizes.length+1];
        System.arraycopy(t.allsizes, 0, sizes, 0, t.sizes.length);
        sizes[sizes.length-1] = size;
    }

    public int getComboLength(){
        int ans = 1;
        for (int size : sizes)
            ans *= size;

        return ans;
    }
}
