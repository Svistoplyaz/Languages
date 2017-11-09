package Lab7;

public class Node {
    boolean full;
    String id;
    int dataType;

    Node(){
        full = false;
    }

    Node(String i, int d){
        id = i;
        dataType = d;
        full = true;
    }
}
