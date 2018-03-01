package semester7.llk;

import semester7.Lab3.Scanner;
import semester7.Lab3.Type;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.util.*;

public class Grammar implements Serializable {
	
	public HashMap<String, ArrayList<Rule>> map = new HashMap<>();
	public ArrayList<String> nonTerminals = new ArrayList<>();
	public HashMap<String, Type> terminals = new HashMap<>();
	
	private int surrogates = 0;
	
	public Grammar(File file) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		String s;
		// Чтение и парсинг грамматики, определение нетерминалов
		while((s = in.readLine()) != null && !s.equals("")) {
			String[] data = s.split("->");
			String nt = data[0].trim();
			nonTerminals.add(nt);
			
			if(nt.startsWith("$")) surrogates = Math.max(surrogates, Integer.parseInt(nt.substring(1)) + 1);
			
			ArrayList<Rule> list = new ArrayList<>();
			for(String sr : data[1].trim().split("\\^")) {
				Rule rule = new Rule(nt);
				for(String so : sr.trim().split("\\s")) rule.add(so.trim());
				list.add(rule);
			}
			
			map.put(nt, list);
		}
		
		// Определение терминалов
		HashSet<String> terminalsSet = new HashSet<>();
		for(ArrayList<Rule> rules : map.values())
			for(Rule rule : rules)
				for(String t : rule) if(!map.containsKey(t)) terminalsSet.add(t);
		
		// Определение типов терминалов
		for(String t : terminalsSet) terminals.put(t, getTerminalType(t));
	}

	private Type getTerminalType(String t) throws Exception {
		switch(t) {
			case "#":
				return Type.T_EOF;
			case "N10":
				return Type.T_const10;
			case "N16":
				return Type.T_const16;
			default:
				InputStream stream = new ByteArrayInputStream(t.getBytes("UTF-8"));
				return (new Scanner(new InputStreamReader(stream))).next().type;
		}
	}
	
	public Grammar withoutEps() {
		Grammar grammar = SerializationUtils.clone(this);
		grammar.removeEps();
		return grammar;
	}
	
	public void removeEps() {
		removeEps("#", true, new HashSet<>());
		terminals.remove("#");
		//for(String a : nonTerminals) System.out.println(a + " -> " + map.get(a));
		//System.out.println();
	}
	
	public ArrayList<Rule> rules() {
		ArrayList<Rule> rules = new ArrayList<>();
		for(ArrayList<Rule> r : map.values()) rules.addAll(r);
		return rules;
	}
	
	private void removeEps(String a, boolean full, HashSet<String> used) {
		used.add(a);
		for(Map.Entry<String, ArrayList<Rule>> e : map.entrySet()) {
			if(used.contains(e.getKey())) continue;

			Iterator<Rule> i = e.getValue().iterator();
			ArrayList<Rule> add = new ArrayList<>();

			while(i.hasNext()) {
				Rule rule = i.next();
				if(rule.size() == 1 && rule.get(0).equals(a)) {
					if(full) i.remove();
					removeEps(e.getKey(), e.getValue().size() == 0, used);
				} else {
					boolean contains = false;
					for(String r : rule) if(r.equals(a)) contains = true;

					if(contains) {
						Rule nw = new Rule(e.getKey());
						for(String s : rule) if(!s.equals(a)) nw.add(s);
						add.add(nw);
					}
				}
			}

			e.getValue().addAll(add);
		}
	}
	
	public boolean isNonTerminal(String s) {
		return nonTerminals.contains(s);
	}
	
	public String getAxiom() {
		return nonTerminals.get(0);
	}
	
	public String createSurrogateNonTerminal() {
		String name = "$" + (surrogates++);
		nonTerminals.add(name);
		return name;
	}
	
	public void addRule(String nonTerminal, List<String> rule) {
		map.computeIfAbsent(nonTerminal, (e) -> new ArrayList<>()).add(new Rule(nonTerminal, rule));
	}
	
	public String hasRule(List<String> rule) {
		for(Map.Entry<String, ArrayList<Rule>> e : map.entrySet())
			for(Rule r : e.getValue())
				if(/*e.getKey().startsWith("$") && */isRulesEquals(r, rule)) return e.getKey();
		return null;
	}
	
	public static boolean isRulesEquals(List<String> rule, String... other) {
		return isRulesEquals(rule, Arrays.asList(other));
	}
	
	public static boolean isRulesEquals(List<String> rule, List<String> other) {
		if(rule.size() != other.size()) return false;
		for(int i = 0; i < rule.size(); i++)
			if(!rule.get(i).equals(other.get(i))) return false;
		return true;
	}
	
	public void print() {
		for(String s : nonTerminals) {
			ArrayList<Rule> rules = map.get(s);
			System.out.print(s + " -> ");
			for(int i = 0; i < rules.size(); i++) {
				if(i != 0) System.out.print(" | ");
				System.out.print(String.join(" ", rules.get(i)));
			}
			System.out.println();
		}
	}
	
	public static class Rule extends ArrayList<String> {
		
		public final String from;
		
		public Rule(String f) { super(); from = f; }
		public Rule(String f, Collection<? extends String> c) { super(c); from = f; }
		
		public final HashSet<String> first = new HashSet<>();
		
	}
	
}
