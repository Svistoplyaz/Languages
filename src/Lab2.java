import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by Alexandr on 05.10.2017.
 */
public class Lab2 {
    static String what = "int n = 1";
    static String cur = "";
    static int pnt = 0;

    static String alph = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static String numbers = "0123456789";
    static String oneToNine = "123456789";
    static String AToF = "ABCDEF";

    static Node startnorm;
    static Node startign;

    static String errorbuff = "";

    static int strnum = 0;

    static BufferedWriter out;

    public static void main(String[] args) throws Exception{
        int n = 1;
        startnorm = new Node(false, "");
        buildAutomato(startnorm);

        startign = new Node(true, "");
        buildAnotherAutomato(startign);

        BufferedReader in = new BufferedReader(new FileReader("in.in"));
        out = new BufferedWriter(new FileWriter("out.out"));

        while (true){
            String st = in.readLine();
            strnum++;

            if(st == null) {
                try {
                    out.write(strnum + " EOF\n");
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }

            char[] cur = (st+"\n").toCharArray();
            next(cur);

        }

        out.close();
    }

    static boolean next(char[] string){
        pnt = 0;
        int len = string.length;

        while(pnt < len){
            pnt = passIgn(string);
            if(errorbuff.endsWith("/")){
                pnt *= -1;
            }
            else if(pnt < 0){
                try {
                    out.write(strnum+ " ERROR " + errorbuff +"\n");
                }catch (Exception e){
                    e.printStackTrace();
                }
                pnt *= -1;
            }

            String ans = findLex(string);
            try {
                out.write(strnum + " "+ ans + " " + errorbuff +"\n");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return true;
    }

    static String findLex(char[] str){
        boolean canmove = true;
        errorbuff = "";
        int len = str.length;

        Node cur = startnorm;
        while (canmove && pnt < len){
            canmove = false;
            for(Edge e : cur.to){
                if(e.path.indexOf(str[pnt]) != -1 || e.revers && e.path.indexOf(str[pnt]) == -1){
                    errorbuff += str[pnt];
                    pnt++;
                    cur = e.to;
                    canmove = true;
                    break;
                }
            }
        }

        if(cur.fina){
            return cur.meaning;
        }else{
            return "ERROR";
        }

    }

    private static int passIgn(char[] str){
        boolean canmove = true;
        errorbuff = "";
        int len = str.length;

        Node cur = startign;
        while (canmove && pnt < len){
            canmove = false;
            for(Edge e : cur.to){
                if(e.revers && e.path.indexOf(str[pnt]) == -1 || !e.revers && e.path.indexOf(str[pnt]) != -1){
                    errorbuff += str[pnt];
                    pnt++;
                    cur = e.to;
                    canmove = true;
                    break;
                }
            }
        }

        if(!cur.fina)
            pnt *= -1;

        return pnt;
    }

    private static void buildAutomato(Node start){
        //Идентификатор и ключевое слово
        Node from = start;
        Node to = new Node(true, "Идентификатор/Ключевое слово");
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

    private static void buildAnotherAutomato(Node start){
        Node from = start;
        Node to = start;
        from.addEdge(new Edge("\n\t ", from, to));

        to = new Node(false, "/");
        from.addEdge(new Edge("/", from, to));

        Node cross = to;
        from = to;
        to = new Node(false, "//");
        from.addEdge(new Edge("/", from, to));

        from = to;
        from.addEdge(new Edge("\n", from, to, 1));

        to = start;
        from.addEdge(new Edge("\n", from, to));

        from = cross;
        to = new Node(false, "/*");
        from.addEdge(new Edge("*", from, to));

        from = to;
        from.addEdge(new Edge("*", from, to, 1));

        to = new Node(false, "/**");
        to.addEdge(new Edge("/*",to, from));

        from = to;
        from.addEdge(new Edge("*",from, to));

        to = start;
        from.addEdge(new Edge("/",from, to));
    }

    private static class Edge{
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

    private static class Node{
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
