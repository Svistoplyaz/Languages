package llk;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import static llk.Grammar.Rule;

import java.io.*;
import java.util.*;

public class Generator {
	
	private static Grammar grammar, grammar2;
	private static HashMap<String, HashSet<String>> follow = new HashMap<>();
	
	public static void main(String[] args) throws Exception {
		grammar = new Grammar(new File("grammar.txt"));
		
		// Вычисление first1 для всех правил грамматики
		HashMap<String, HashSet<String>> used = new HashMap<>();
		for(String a : grammar.nonTerminals) first1(a, used);
		
		// Приведение грамматики к неукорачивающей форме
		grammar2 = grammar.withoutEps();
		
		// Вычисление follow1 для всех правил грамматики
		for(String a : grammar.nonTerminals) follow.put(a, new HashSet<>());
		follow1(grammar.nonTerminals.get(0), "#", new HashSet<>());
		
		// Вывод полученных first1 и follow1 для всех нетерминалов
		for(String a : grammar.nonTerminals) {
			HashSet<String> first = new HashSet<>();
			for(Rule rule : grammar.map.get(a)) first.addAll(rule.first);
			
			System.out.format("%-3s %-38s %s\n", a, first, follow.get(a));
		}
		System.out.println();
		
		// Построение управляющей таблицы
		Table table = new Table(grammar.nonTerminals, grammar.terminals, grammar.nonTerminals.get(0));
		for(Map.Entry<String, ArrayList<Rule>> e : grammar.map.entrySet()) {
			for(Rule rule : e.getValue()) {
				HashSet<String> set = rule.first;
				if(set.size() == 1 && set.iterator().next().equals("#")) set = follow.get(e.getKey());
				
				for(String terminal : set) {
					//System.out.println("  T[" + e.getKey() + "][" + terminal + "] = " + rule);
					table.add(e.getKey(), terminal, rule);
				}
			}
		}
		table.exportToExcel(new File("table1.xls"));
		System.out.println();
		
		// Сериализация полученной таблицы
		File file = new File("table.llk");
		FileUtils.writeByteArrayToFile(file, SerializationUtils.serialize(table));
		System.out.println("Control table serialized and saved to " + file);
	}
	
	private static HashSet<String> first1(String a, HashMap<String, HashSet<String>> used) {
		if(used.containsKey(a)) return used.get(a);

		HashSet<String> result = new HashSet<>();
		if(!grammar.map.containsKey(a))
			result.add(a);
		else
			for(Rule rule : grammar.map.get(a)) {
				for(String s : first1(rule.get(0), used))
					if(s.equals("#") && rule.size() > 1)
						rule.first.addAll(first1(rule.get(1), used));
					else
						rule.first.add(s);
				result.addAll(rule.first);
			}
		return result;
	}
	
	private static void follow1(String a, String b, HashSet<String> used) {
		if(used.contains(a + "$" + b)) return;
		used.add(a + "$" + b);
		
		if(!grammar2.map.containsKey(b)) follow.get(a).add(b);
		
		for(ArrayList<String> l : grammar2.map.get(a)) {
			ArrayList<String> list = new ArrayList<>(l);
			list.add(b);
			for(int i = 0; i < list.size() - 1; i++)
				if(grammar2.map.containsKey(list.get(i))) follow1(list.get(i), list.get(i + 1), used);
		}
		
		if(grammar2.map.containsKey(b)) 
			for(ArrayList<String> l : grammar2.map.get(b)) {
				ArrayList<String> list = new ArrayList<>();
				list.add(a);
				list.addAll(l);
				for(int i = 0; i < list.size() - 1; i++)
					if(grammar2.map.containsKey(list.get(i))) follow1(list.get(i), list.get(i + 1), used);
			}
	}
	
}
