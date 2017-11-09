package Lab7;

import Lab3.Lexeme;

public class Tree {
    private final int MAXK = 1000;

    private Node n;
    private Tree up, left, right;

    Tree(Tree u, Tree l, Tree r, Node d) {
        n = d;
        up = u;
        left = l;
        right = r;
    }

    Tree() {
        n = new Node();
    }

    public void setLeft(Node data) {
        left = new Tree(this, null, null, data);
    }

    public void setRight(Node data) {
        right = new Tree(this, null, null, data);
    }

    public void setLeft(Tree tree) {
        left = tree;
    }

    public void setRight(Tree tree) {
        right = tree;
    }

    public Tree findUp(String id) {
        if (n.id.equals(id))
            return this;

        if (up == null)
            return null;

        return up.findUp(id);
    }

    public Tree findLeft(String id) {
        if (n.id.equals(id))
            return this;

        if (left == null)
            return null;

        return left.findLeft(id);
    }

    public void print() {
        System.out.print("Вершина с данными "+n.id+" ----->");
        if (left != null)
            System.out.print(" данные слева " + left.n.id);
        if (right != null)
            System.out.print(" данные справа " + right.n.id);
        System.out.println();

        if (left != null)
            left.print();

        if (right != null)
            right.print();
    }
}
