package precedence;

import llk.Grammar;
import llk.Grammar.Rule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class Generator {

	private static Grammar grammar;
	private static ArrayList<String> last;
	private static ArrayList<Pair<String, String>> pairs;

	public static void main(String[] args) throws Exception {
		grammar = new Grammar(new File("grammar2.txt"));
		grammar.removeEps();

		// Определение символов, стоящих только в конце правил
		last = new ArrayList<>();
		last.addAll(grammar.terminals.keySet());
		last.addAll(grammar.nonTerminals);

		grammar.rules().forEach((rule) -> {
			for(int i = 0; i < rule.size() - 1; i++) last.remove(rule.get(i));
		});

		pairs = new ArrayList<>();
		HashSet<Pair<String, String>> used = new HashSet<>();

		findPairs("#", grammar.getAxiom(), used);
		findPairs(grammar.getAxiom(), "#", used);
		
		Table table = buildTable();
		for(int i = 0; i < table.getSize(); i++)
			for(int j = 0; j < table.getSize(); j++) {
				Table.Cell cell = table.getCell(i, j);
				String a = table.getCharacter(i), b = table.getCharacter(j);
				
				if(cell.relations.size() > 1)
					System.out.println("Detected conflict " + cell.relations + " between " + a + " and " + b);
			}
		
		for(int i = 0; i < table.getSize(); i++)
			for(int j = 0; j < table.getSize(); j++) {
				Table.Cell cell = table.getCell(i, j);
				String a = table.getCharacter(i), b = table.getCharacter(j);

				// Устранение конфликтов <= и >= в ячейках таблицы
				if(checkConflict(cell.relations, '<', '=') || checkConflict(cell.relations, '>', '='))
					for(Rule r : grammar.rules()) {
						for(int k = 0; k < r.size() - 1; k++)
							// A -> xaby
							if(r.get(k).equals(a) && r.get(k + 1).equals(b)) {
								// Левая часть конфликтного правила
								ArrayList<String> list1 = new ArrayList<>();
								for(int l = 0; l <= k; l++) list1.add(r.get(l));
								
								// Правая часть конфликтного правила
								ArrayList<String> list2 = new ArrayList<>();
								for(int l = k + 1; l < r.size(); l++) list2.add(r.get(l));
								
								ArrayList<String> surrogate = checkConflict(cell.relations, '<', '=') ? list2 : list1;
								String T = grammar.createSurrogateNonTerminal();
								grammar.addRule(T, surrogate);
								
								if(checkConflict(cell.relations, '<', '=')) {
									// A -> xaT, T -> by
									r.clear(); r.addAll(list1); r.add(T);
								} else {
									// A -> Tby, T -> xa
									r.clear(); r.add(T); r.addAll(list2);
								}
								
								break;
							}
					}
			}
		table = buildTable();
		
		// Сохранение таблицы и функций предшествования
		table.exportToExcel(new File("table.xls"));
		System.out.println();

		// Сериализация полученной таблицы
		File file = new File("data.prc");
		FileUtils.writeByteArrayToFile(file, SerializationUtils.serialize(table));
		System.out.println("Precedence table serialized and saved to " + file);
		System.out.println();
		
		//grammar.print();
		//System.out.println();
		
		HashSet<String> used1 = new HashSet<>();
		for(Rule r : grammar.rules()) {
			if(used1.contains(r.toString())) continue;

			ArrayList<Rule> eq = new ArrayList<>();
			for(Rule other : grammar.rules()) {
				if(other != r && Grammar.isRulesEquals(r, other)) eq.add(other);
			}

			if(eq.size() > 0) {
				System.out.println(r.from + " -> " + r);
				for(Rule other : eq) System.out.println("* " + other.from + " -> " + other);
				used1.add(r.toString());
			}
		}
	}
	
	private static Table buildTable() {
		Table table = new Table(grammar);
		for(Pair<String, String> pair : pairs)
			if(grammar.isNonTerminal(pair.getKey()) && !grammar.isNonTerminal(pair.getValue())) {
				String A = pair.getKey(), m = pair.getValue();
				// Построение отношений >
				for(Rule rule : grammar.map.get(A)) {
					//System.out.println("-- " + pair + " " + rule.get(rule.size() - 1) + " > " + m);
					table.addRelation(rule.get(rule.size() - 1), '>', m);
				}
			} else if(grammar.isNonTerminal(pair.getValue())) {
				String m = pair.getKey(), A = pair.getValue();
				if(last.contains(m)) continue;
				// Построение отношений <
				for(Rule rule : grammar.map.get(A)) {
					//System.out.println("-- " + pair + " " + m + " < " + rule.get(0));
					table.addRelation(m, '<', rule.get(0));
				}
			}

		// Построение отношений =
		grammar.rules().forEach((rule) -> {
			for(int i = 0; i < rule.size() - 1; i++) table.addRelation(rule.get(i), '=', rule.get(i + 1));
		});

		return table;
	}

	private static void findPairs(String a, String b, HashSet<Pair<String, String>> used) {
		Pair<String, String> pair = Pair.of(a, b);

		if(used.contains(pair)) return;
		used.add(pair);

		// Оставляем пары в которых есть хотя бы один нетерминал и первый символ не из списка list
		if(!grammar.isNonTerminal(a) && !grammar.isNonTerminal(b)) return;
		//if(last.contains(a)) return;
		pairs.add(pair);

		// Подставляем правила вместо первого нетерминала
		if(grammar.isNonTerminal(a))
			for(Rule rule : grammar.map.get(a)) {
				ArrayList<String> out = new ArrayList<>(rule);
				out.add(b);
				for(int i = 0; i < out.size() - 1; i++) findPairs(out.get(i), out.get(i + 1), used);
			}

		// Подставляем правила вместо второго нетерминала
		if(grammar.isNonTerminal(b))
			for(Rule rule : grammar.map.get(b)) {
				ArrayList<String> out = new ArrayList<>();
				out.add(a);
				out.addAll(rule);
				for(int i = 0; i < out.size() - 1; i++) findPairs(out.get(i), out.get(i + 1), used);
			}
	}

	private static boolean checkConflict(HashSet<Character> set, char... required) {
		if(set.size() != required.length) return false;
		for(char c : required) if(!set.contains(c)) return false;
		return true;
	}
	
}
