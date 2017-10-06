import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alexandr on 05.10.2017.
 */
public class Lab2 {
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

    static HashMap<String, String> voc = new HashMap<>();

    public static void main(String[] args) throws Exception{
        voc.put("for", "T_for");
        voc.put("indef", "T_indef");
        voc.put("const", "T_const");
        voc.put("int", "T_int");
        voc.put("__int64", "T_int64");

        startnorm = new Node(false, "");
        buildAutomato(startnorm);

        startign = new Node(true, "");
        buildAnotherAutomato(startign);

        BufferedReader in = new BufferedReader(new FileReader("in.in"));
        try {
            out = new BufferedWriter(new FileWriter("out.out"));
        }catch (Exception e){
            e.printStackTrace();
        }

        while (true){
            String st = in.readLine();
            strnum++;

            if(st == null) {
                try {
                    out.write(strnum + " T_EOF\n");
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

    private static boolean next(char[] string){
        pnt = 0;
        int len = string.length;

        while(pnt < len){
            boolean unchecked = true;
            while(unchecked) {
                unchecked = false;
                pnt = passIgn(string);

                if (errorbuff.endsWith(" /") || errorbuff.equals("/")) {
                    unchecked = true;
                    pnt *= -1;
                    try {
                        out.write(strnum + " T_division /\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (pnt < 0) {
                    unchecked = true;
                    if(errorbuff.endsWith("\n"))
                        errorbuff = errorbuff.substring(0,errorbuff.length()-1);
                    try {
                        out.write(strnum + " T_err " + errorbuff + "\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    pnt *= -1;
                }
            }

            if(pnt < len) {
                String ans = findLex(string);

                try {
                    if(ans.equals("T_id") && voc.get(errorbuff) != null)
                        out.write(strnum + " " + voc.get(errorbuff) + " " + errorbuff + "\n");
                    else
                        out.write(strnum + " " + ans + " " + errorbuff + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

        return true;
    }

    private static String findLex(char[] str){
        boolean canmove = true;
        errorbuff = "";
        int len = str.length;
        int prev = pnt;

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
            if(pnt == prev) {
                errorbuff += str[pnt];
                pnt++;
            }
            return "T_err";
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
        Node to = new Node(true, "T_id");
        from.addEdge(new Edge(alph, from, to));

        from = to;
        from.addEdge(new Edge(alph, from, to));
        from.addEdge(new Edge(numbers, from, to));

        //Числа
        from = start;
        to = new Node(true,"T_const10");
        from.addEdge(new Edge("0", from, to));

        Node zero = to;
        to = new Node(true, "T_const10");
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
        to = new Node(true, "T_const16");
        from.addEdge(new Edge(numbers, from, to));
        from.addEdge(new Edge(AToF, from, to));

        from = to;
        from.addEdge(new Edge(numbers, from, to));
        from.addEdge(new Edge(AToF, from, to));

        from = start;
        to = new Node(true, "T_assign");
        from.addEdge(new Edge("=", from, to));

        from = to;
        to = new Node(true, "T_equal");
        from.addEdge(new Edge("=", from, to));

        from = start;
        to = new Node(true, "T_more");
        from.addEdge(new Edge(">", from, to));

        from = to;
        to = new Node(true, "T_meq");
        from.addEdge(new Edge("=", from, to));

        from = start;
        to = new Node(true, "T_less");
        from.addEdge(new Edge("<", from, to));

        from = to;
        to = new Node(true, "T_leq");
        from.addEdge(new Edge("=", from, to));

        from = start;
        to = new Node(true, "T_not");
        from.addEdge(new Edge("!", from, to));

        from = to;
        to = new Node(true, "T_noteq");
        from.addEdge(new Edge("=", from, to));

        from = start;
        to = new Node(false, "|");
        from.addEdge(new Edge("|", from, to));

        from = to;
        to = new Node(true, "T_or");
        from.addEdge(new Edge("|", from, to));

        from = start;
        to = new Node(false, "&");
        from.addEdge(new Edge("&", from, to));

        from = to;
        to = new Node(true, "T_and");
        from.addEdge(new Edge("&", from, to));

        from = start;
        to = new Node(true, "T_lparenthesis");
        from.addEdge(new Edge("(", from, to));

        from = start;
        to = new Node(true, "T_rparenthesis");
        from.addEdge(new Edge(")", from, to));

        from = start;
        to = new Node(true, "T_lsbracket");
        from.addEdge(new Edge("[", from, to));

        from = start;
        to = new Node(true, "T_rsbracket");
        from.addEdge(new Edge("]", from, to));

        from = start;
        to = new Node(true, "T_lbracket");
        from.addEdge(new Edge("{", from, to));

        from = start;
        to = new Node(true, "T_rbracket");
        from.addEdge(new Edge("}", from, to));

        from = start;
        to = new Node(true, "T_dot");
        from.addEdge(new Edge(".", from, to));

        from = start;
        to = new Node(true, "T_colon");
        from.addEdge(new Edge(",", from, to));

        from = start;
        to = new Node(true, "T_semicolon");
        from.addEdge(new Edge(";", from, to));

        from = start;
        to = new Node(true, "T_plus");
        from.addEdge(new Edge("+", from, to));

        from = start;
        to = new Node(true, "T_minus");
        from.addEdge(new Edge("-", from, to));

        from = start;
        to = new Node(true, "T_division");
        from.addEdge(new Edge("/", from, to));

        from = start;
        to = new Node(true, "T_multiply");
        from.addEdge(new Edge("*", from, to));

        from = start;
        to = new Node(true, "T_remainder");
        from.addEdge(new Edge("%", from, to));
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
        to.addEdge(new Edge("/*",to, from, 1));

        from.addEdge(new Edge("*",from, to));

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
