import java.util.ArrayList;

/**
 * Created by Alexandr on 05.10.2017.
 */
public class Lab2 {
    static String what = "int n = 1";
    static String cur = "";
    static int ind = 0;

    static String alph = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static String numbers = "0123456789";
    static String oneToNine = "123456789";
    static String AToF = "ABCDEF";

    public static void main(String[] args) {
        int n = 1;
    }

    public void buildAutomato(Node start){
        //Идентификатор и ключевое слово
        Node from = start;
        Node to = new Node(true, "Идентификатор/Ключевле слово");
        from.addEdge(new Edge(alph, from, to));

        from = to;
        from.addEdge(new Edge(alph, from, to));
        from.addEdge(new Edge(numbers, from, to));

        //Числа
        from = start;
        to = new Node(true,"Ноль");
        from.addEdge(new Edge("0", from, to));

        Node zero = to;
        to = new Node(true, "Число 10 с/с");
        from = zero;
        from.addEdge(new Edge(numbers, from, to));

        from = start;
        from.addEdge(new Edge(oneToNine, from, to));

        from = to;
        from.addEdge(new Edge(numbers, from, to));

        from = zero;
        to = new Node(false,"0x");
        from.addEdge(new Edge("x", from, to));

        from = to;
        to = new Node(true, "Число 16 с/с");
        from.addEdge(new Edge(numbers, from, to));
        from.addEdge(new Edge(AToF, from, to));

        from = to;
        from.addEdge(new Edge(numbers, from, to));
        from.addEdge(new Edge(AToF, from, to));

        from = start;
        to = new Node(true, "=");
        from.addEdge(new Edge("=", from, to));

        from = to;
        to = new Node(true, "==");
        from.addEdge(new Edge("=", from, to));

        from = start;
        to = new Node(true, ">");
        from.addEdge(new Edge(">", from, to));

        from = to;
        to = new Node(true, ">=");
        from.addEdge(new Edge("=", from, to));

        from = start;
        to = new Node(true, "<");
        from.addEdge(new Edge("<", from, to));

        from = to;
        to = new Node(true, "<=");
        from.addEdge(new Edge("=", from, to));

        from = start;
        to = new Node(true, "!");
        from.addEdge(new Edge("!", from, to));

        from = to;
        to = new Node(true, "!=");
        from.addEdge(new Edge("=", from, to));

        from = start;
        to = new Node(false, "|");
        from.addEdge(new Edge("|", from, to));

        from = to;
        to = new Node(true, "||");
        from.addEdge(new Edge("|", from, to));

        from = start;
        to = new Node(false, "&");
        from.addEdge(new Edge("&", from, to));

        from = to;
        to = new Node(true, "&&");
        from.addEdge(new Edge("&", from, to));

        from = start;
        to = new Node(true, "(");
        from.addEdge(new Edge("(", from, to));

        from = start;
        to = new Node(true, ")");
        from.addEdge(new Edge(")", from, to));

        from = start;
        to = new Node(true, "[");
        from.addEdge(new Edge("[", from, to));

        from = start;
        to = new Node(true, "]");
        from.addEdge(new Edge("]", from, to));

        from = start;
        to = new Node(true, "{");
        from.addEdge(new Edge("{", from, to));

        from = start;
        to = new Node(true, "}");
        from.addEdge(new Edge("}", from, to));

        from = start;
        to = new Node(true, ".");
        from.addEdge(new Edge(".", from, to));

        from = start;
        to = new Node(true, ",");
        from.addEdge(new Edge(",", from, to));

        from = start;
        to = new Node(true, ";");
        from.addEdge(new Edge(";", from, to));
    }

    static void buildAnotherAutomato(Node start){
        Node from = start;
        Node to = start;
        from.addEdge(new Edge("\n\t", from, to));

        to = new Node(false, "/");
        from.addEdge(new Edge("/", from, to));

        Node cross = to;
        from = to;
        to = new Node(false, "/");
        from.addEdge(new Edge("/", from, to));

        from = to;
        from.addEdge(new Edge("\n", from, to, 1));

        to = start;
        from.addEdge(new Edge("\n", from, to));

        from = cross;
        to = new Node(false, "/*");
        from.addEdge(new Edge("*", from, to));

        to = from;
        from.addEdge(new Edge("*", from, to, 1));

        to = new Node(false, "/**");
        to.addEdge(new Edge("/*",to, from));

        from = to;
        from.addEdge(new Edge("*",from, to));

        to = start;
        from.addEdge(new Edge("/",from, to));
    }

    static class Edge{
        String path;
        Node from;
        Node to;
        boolean revers = false;

        Edge(String p, Node f, Node t){
            path = p;
            from = f;
            to = t;
        }

        Edge(String p, Node f, Node t, int n){
            path = p;
            from = f;
            to = t;
            revers = true;
        }
    }

    static class Node{
        boolean fina;
        String meaning;
        ArrayList<Edge> to = new ArrayList<>();

        Node(boolean _f, String m){
            fina = _f;
            meaning = m;
        }

        void addEdge(Edge e){
            to.add(e);
        }
    }
}
