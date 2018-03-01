package operatorPrecedence;

import llk.Grammar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Alexandr on 16.12.2017.
 */
public class Generator {
    //Здесь мы храним какие нетерминалы находятся в конце правил выбранного нетерминала
    public static HashMap<String, HashSet<String>> last = new HashMap<>();
    //Здесь мы храним какие нетерминалы находятся в начале правил выбранного нетерминала
    public static HashMap<String, HashSet<String>> first = new HashMap<>();

    //Храним терминалы которые в правилах стоят справа от выбранного нетерминала
    public static HashMap<String, HashSet<String>> right = new HashMap<>();
    //Храним терминалы которые в правилах стоят слева от выбранного нетерминала
    public static HashMap<String, HashSet<String>> left = new HashMap<>();

    //Храним терминалы которые в могут стоять справа от выбранного нетерминала
    public static HashMap<String, HashSet<String>> NonTerm_Term = new HashMap<>();
    //Храним терминалы которые в могут стоять слева от выбранного нетерминала
    public static HashMap<String, HashSet<String>> Term_NonTerm = new HashMap<>();

    //Храним терминалы которые являются первыми терминалами справа в правых частях выбранного нетерминала
    public static HashMap<String, HashSet<String>> endTerm = new HashMap<>();
    //Храним терминалы которые являются первыми терминалами слева в правых частях выбранного нетерминала
    public static HashMap<String, HashSet<String>> beginTerm = new HashMap<>();

    public static Trio[][] table;
    public static Grammar oper;

    public static void main(String[] args) throws Exception {
        oper = new Grammar(new File("grammar2.txt"));

        checkRulesforNoNeighbourNonterminals();

        for (String nonterm : oper.map.keySet()) {
            HashSet<String> forLastNonTerm = new HashSet<>();
            forLastNonTerm.add(nonterm);
            getLast(forLastNonTerm, nonterm);
            last.put(nonterm, forLastNonTerm);

            HashSet<String> forFirstNonTerm = new HashSet<>();
            forFirstNonTerm.add(nonterm);
            getFirst(forFirstNonTerm, nonterm);
            first.put(nonterm, forFirstNonTerm);
        }

        addToLeft(oper.getAxiom(), "#");
        addToRight(oper.getAxiom(), "#");
        for (String nonTerm : oper.map.keySet()) {
            for (Grammar.Rule rule : oper.map.get(nonTerm)) {
                int len = rule.size();
                for (int i = 0; i < len; i++) {
                    String subPart = rule.get(i);

                    if (oper.isNonTerminal(subPart)) {
                        if (i - 1 >= 0 && !oper.isNonTerminal(rule.get(i - 1))) {
                            addToLeft(subPart, rule.get(i - 1));
                        }

                        if (i + 1 < len && !oper.isNonTerminal(rule.get(i + 1))) {
                            addToRight(subPart, rule.get(i + 1));
                        }
                    }
                }
            }
        }

        formPairs();
        formBeginEndTerms();

        int n = oper.terminals.size() + 1;
        table = new Trio[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                table[i][j] = new Trio();

        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(oper.terminals.keySet());

        for (String nonTerm : oper.map.keySet()) {
            for (String rightTermInRule : endTerm.get(nonTerm))
                for (String rightTermInPair : NonTerm_Term.get(nonTerm)) {
                    table[indexOf(rightTermInRule, keys)][indexOf(rightTermInPair, keys)].add(Comparance.more);
                    if (rightTermInRule.equals("int") && rightTermInPair.equals(";"))
                        System.out.println(nonTerm + " more");
                }

            for (String leftTermInPair : Term_NonTerm.get(nonTerm))
                for (String leftTermInRule : beginTerm.get(nonTerm)) {
                    table[indexOf(leftTermInPair, keys)][indexOf(leftTermInRule, keys)].add(Comparance.less);
                    if (leftTermInPair.equals("int") && leftTermInRule.equals(";"))
                        System.out.println(nonTerm + " less");
                }

            for (Grammar.Rule rule : oper.map.get(nonTerm)) {
                int len = rule.size();

                String prev = null;
                String cur = null;
                for (int i = 0; i < len; i++) {
                    if (!oper.isNonTerminal(rule.get(i))) {
                        if (prev == null)
                            prev = rule.get(i);
                        else if (cur == null) {
                            cur = rule.get(i);
                            table[indexOf(prev, keys)][indexOf(cur, keys)].add(Comparance.equal);
                            prev = cur;
                            cur = null;
                        }
                    }
                }
            }
        }

        int result = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                Trio cur = table[i][j];
                if (cur.length() > 1) {
                    System.out.println(termOf(i, keys) + " " + cur.print() + " " + termOf(j, keys));
                    result++;
                }
            }
        System.out.println(result + "");

        print();
    }

    public static int indexOf(String str, ArrayList<String> keys) {
        if (str.equals("#"))
            return keys.size();
        else
            return keys.indexOf(str);
    }

    public static String termOf(int ind, ArrayList<String> keys) {
        if (ind >= keys.size())
            return "#";
        else
            return keys.get(ind);
    }

    public static void getLast(HashSet<String> prev, String nonTerm) {
        for (Grammar.Rule rule : oper.map.get(nonTerm)) {
            String lastTerm = rule.get(rule.size() - 1);
            if (oper.isNonTerminal(lastTerm) && !prev.contains(lastTerm)) {
                prev.add(lastTerm);
//                System.out.println(lastTerm);
                getLast(prev, lastTerm);
            }
        }
    }

    public static void getFirst(HashSet<String> prev, String nonTerm) {
        for (Grammar.Rule rule : oper.map.get(nonTerm)) {
            String firstTerm = rule.get(0);
            if (oper.isNonTerminal(firstTerm) && !prev.contains(firstTerm)) {
                prev.add(firstTerm);
//                System.out.println(firstTerm);
                getFirst(prev, firstTerm);
            }
        }
    }

    public static void addToLeft(String nonTerm, String term) {
        for (String str : first.get(nonTerm)) {
            left.putIfAbsent(str, new HashSet<>());
            left.get(str).add(term);
        }
    }

    public static void addToRight(String nonTerm, String term) {
        for (String str : last.get(nonTerm)) {
            right.putIfAbsent(str, new HashSet<>());
            right.get(str).add(term);
        }
    }

    public static void formPairs() {
        for (String key : oper.nonTerminals) {
            NonTerm_Term.put(key, new HashSet<>());
            Term_NonTerm.put(key, new HashSet<>());
        }

        //Проходим по всем нетерминалам
        for (String key : last.keySet()) {
            //Выбираем нетерминалы, которые могут быть в конце правил нетерминала выбранного перед этим
            for (String value : last.get(key)) {
                //Выбираем терминалы, которые могут стоять справа от нетерминала выбранного в первом for
                for (String term : right.get(key))
                    //Добавляем пару из нетерминала из второго for и терминала
                    NonTerm_Term.get(value).add(term);
            }
        }

        //Делаем то же самое только слева
        for (String key : first.keySet()) {
            //Выбираем нетерминалы, которые могут быть в конце правил нетерминала выбранного перед этим
            for (String value : first.get(key)) {
                //Выбираем терминалы, которые могут стоять справа от нетерминала выбранного в первом for
                for (String term : left.get(key))
                    //Добавляем пару из нетерминала из второго for и терминала
                    Term_NonTerm.get(value).add(term);
            }
        }
    }

    public static void formBeginEndTerms() {
        for (String key : oper.nonTerminals) {
            beginTerm.put(key, new HashSet<>());
            endTerm.put(key, new HashSet<>());
        }

        for (String nonTerm : oper.map.keySet()) {
            for (Grammar.Rule rule : oper.map.get(nonTerm)) {
                int len = rule.size();

                for (int i = 0; i < len; i++) {
                    if (!oper.isNonTerminal(rule.get(i))) {
                        beginTerm.get(nonTerm).add(rule.get(i));
                        break;
                    }
                }

                for (int i = len - 1; i >= 0; i--) {
                    if (!oper.isNonTerminal(rule.get(i))) {
                        endTerm.get(nonTerm).add(rule.get(i));
                        break;
                    }
                }
            }
        }
    }

    public static void checkRulesforNoNeighbourNonterminals() throws Exception {
        boolean clean = true;
        for (String nonTerm : oper.map.keySet()) {
            for (Grammar.Rule rule : oper.map.get(nonTerm)) {
                int len = rule.size();

                for (int i = 0; i < len - 1; i++) {
                    if (oper.isNonTerminal(rule.get(i)) && oper.isNonTerminal(rule.get(i + 1))) {
                        System.out.println(nonTerm + " -> " + rule.get(i) + " and " + rule.get(i + 1) + " are neighbours!!!");
//                        break;
                        clean = false;
                    }
                }
            }
        }

        if (!clean)
            throw new Exception();
    }

    public static void print() {
        for (String nonTerm : oper.map.keySet()) {
            System.out.print(nonTerm + "{");

            for (String rightTermInPair : NonTerm_Term.get(nonTerm)) {
                System.out.print(rightTermInPair + ", ");
            }
            System.out.print("} -> {");

            for (String rightTermInRule : endTerm.get(nonTerm)) {
                System.out.print(rightTermInRule + ", ");
            }

            System.out.print("} *> {");

            for (String rightTermInPair : NonTerm_Term.get(nonTerm)) {
                System.out.print(rightTermInPair + ", ");
            }
            System.out.print("}\n");

            System.out.print("{");

            for (String leftTermInPair : Term_NonTerm.get(nonTerm)) {
                System.out.print(leftTermInPair + ", ");
            }
            System.out.print("}" + nonTerm + " -> {");

            for (String leftTermInPair : Term_NonTerm.get(nonTerm)) {
                System.out.print(leftTermInPair + ", ");
            }

            System.out.print("} <* {");

            for (String leftTermInRule : beginTerm.get(nonTerm)) {
                System.out.print(leftTermInRule + ", ");
            }
            System.out.print("}\n\n");
        }
    }

    public static class Trio {
        HashSet<Comparance> first;

        Trio() {
            first = new HashSet<>();
        }

        void add(Comparance comp) {
            first.add(comp);
        }

        String print() {
            String ans = "";
            for (Comparance comp : first) {
                ans += " " + comp;
            }
            return ans;
        }

        int length() {
            return first.size();
        }
    }
}
