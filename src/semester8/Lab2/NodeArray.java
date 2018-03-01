package semester8.Lab2;

import semester7.Lab3.Lexeme;
import semester7.Lab5.DataType;

public class NodeArray extends Node {
    private NodeType nType;
    long[] array;

    public NodeArray(Node p, NodeType t, Lexeme l) {
        super(p, t.type, l);
        nType = t;
        array = new long[nType.getComboLength()];
    }

    int length(){
        return array.length;
    }

    int dimensions(){
        return nType.dimensions();
    }

    int getBigIndex(int cur, int num){
        int len = nType.sizes.length - num;
        int ans = 1;
        for(int i = 1; i < len; i++){
            ans *= nType.sizes[i];
        }
        return ans;
    }
}
