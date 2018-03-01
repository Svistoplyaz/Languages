package semester7.precedence;

import semester7.llk.Grammar;
import semester7.llk.Grammar.Rule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.*;

public class Helper {
	
	private static Grammar grammar;
	private static ArrayList<String> last;
	private static ArrayList<Pair<String, String>> pairs;
	
	public static void main(String[] args) throws Exception {
		grammar = new Grammar(new File("grammar2.txt"));
		grammar.removeEps();

		for(Map.Entry<String, ArrayList<Rule>> e : grammar.map.entrySet()) {
			System.out.println(e.getKey() + " -> " + e.getValue());
		}
		System.out.println();
		
		grammar.rules().forEach((rule) -> {
			for(int i = 0; i < rule.size() - 1; i++)
				if(grammar.isNonTerminal(rule.get(i)) && grammar.isNonTerminal(rule.get(i + 1)))
					System.out.println("Neighbour non-terminals in rule ? -> " + rule);
			
			grammar.rules().forEach((other) -> {
				if(other != rule && Grammar.isRulesEquals(other, rule))
					System.out.println("Found rules with equals right parts " + rule + " " + other);
			});
		});

		// Определение символов, стоящих только в конце правил
		last = new ArrayList<>();
		last.addAll(grammar.terminals.keySet());
		last.addAll(grammar.nonTerminals);

		grammar.rules().forEach((rule) -> {
			for(int i = 0; i < rule.size() - 1; i++) last.remove(rule.get(i));
		});
		
		// Определение пар соседних символов при выводе из #S#
		pairs = new ArrayList<>();
		HashSet<Pair<String, String>> used = new HashSet<>();
		
		findPairs("#", grammar.getAxiom(), used);
		findPairs(grammar.getAxiom(), "#", used);
		
		System.out.println(pairs.size());
		System.out.println(pairs);

		
		// Таблица предшествования
		Table table = new Table(grammar);
		for(Pair<String, String> pair : pairs)
			if(grammar.isNonTerminal(pair.getKey()) && !grammar.isNonTerminal(pair.getValue())) {
				String A = pair.getKey(), m = pair.getValue();
				// Построение отношений >
				for(Rule rule : grammar.map.get(A)) table.addRelation(rule.get(rule.size() - 1), '>', m);
			} else if(grammar.isNonTerminal(pair.getValue())) {
				String m = pair.getKey(), A = pair.getValue();
				// Построение отношений <
				for(Rule rule : grammar.map.get(A)) table.addRelation(m, '<', rule.get(0));
			}

		// Построение отношений =
		grammar.rules().forEach((rule) -> {
			for(int i = 0; i < rule.size() - 1; i++) table.addRelation(rule.get(i), '=', rule.get(i + 1));
		});
		
		Table tmp = buildTable();
		for(int i = 0; i < tmp.getSize(); i++)
			for(int j = 0; j < tmp.getSize(); j++) {
				Table.Cell cell = tmp.getCell(i, j);
				String a = tmp.getCharacter(i), b = tmp.getCharacter(j);
				
				// Устранение конфликтов <= и >= в ячейках таблицы
				if(checkConflict(cell.relations, '<', '=') || checkConflict(cell.relations, '>', '='))
					grammar.rules().forEach((r) -> {
						for(int k = 0; k < r.size() - 1; k++)
							// A -> xaby
							if(r.get(k).equals(a) && r.get(k + 1).equals(b)) {
								//String surrogate = grammar.createSurrogateNonTerminal();
								
								// Левая часть конфликтного правила
								ArrayList<String> list1 = new ArrayList<>();
								for(int l = 0; l <= k; l++) list1.add(r.get(l));
								
								// Правая часть конфликтного правила
								ArrayList<String> list2 = new ArrayList<>();
								for(int l = k + 1; l < r.size(); l++) list2.add(r.get(l));
								
								ArrayList<String> surrogate = checkConflict(cell.relations, '<', '=') ? list2 : list1;
								String T = null;// grammar.hasRule(surrogate);
								if(T == null) {
									T = grammar.createSurrogateNonTerminal();
									
									for(Map.Entry<String, ArrayList<Rule>> e : grammar.map.entrySet())
										for(Rule rule : e.getValue())
											if(Grammar.isRulesEquals(rule, surrogate)) {
												rule.clear();
												rule.add(T);
											}
									grammar.addRule(T, surrogate);
									//}
								}
								
								if(checkConflict(cell.relations, '<', '=')) {
										// A -> xaT, T -> by
									r.clear(); r.addAll(list1); r.add(T);
								} else {
									// A -> Tby, T -> xa
									r.clear(); r.add(T); r.addAll(list2);
								}
								
								break;
							}
					});
				//else if(cell.relations.size() > 1)
				//	throw new RuntimeException("Detected conflict " + cell.relations + " between " + a + " and " + b);
			}
		
		//final Table	table = buildTable();
		
		// Проверка на грамматику слабого предшествования
		for(Map.Entry<String, ArrayList<Rule>> e : grammar.map.entrySet())
			for(Rule rule : e.getValue()) {
				//System.out.println(rule);
				grammar.rules().forEach((other) -> {
					if(other.size() > rule.size()) {
						boolean endsWith = true;
						int offset = other.size() - rule.size();
						
						for(int i = 0; i < rule.size(); i++)
							if(!other.get(offset + i).equals(rule.get(i))) {
								endsWith = false;
								break;
							}
						
						if(endsWith) {
							String check = other.get(offset - 1);
							
							//System.out.println(">> " + other);
							for(String lastY : last1(check, new HashSet<>())) {
								Table.Cell cell = table.getCell(table.getIndex(lastY), table.getIndex(e.getKey()));
								if(cell.relations.size() != 0) {
									System.out.print("Detected relation " + cell.relations + " ");
									System.out.println("between " + lastY + " and " + e.getKey());
									System.out.print("When analyzing rules " + e.getKey() + " -> " + rule + " ");
									System.out.println("and ? --> " + other + "\n");
								}
							}
							//System.out.println("   " + check + " " + last1(check, new HashSet<>()));
						}
					}
				});
			}
		
		// Построение функций предшествования
		int[][] matrix = table.getMatrix();
		int[] f = table.getF(), g = table.getG();
		
		for(int i = 0; i < matrix.length; i++) {
			int val = calcValue(matrix, i, new boolean[matrix.length]);
			if(i >= table.getSize()) g[i - table.getSize()] = val; else f[i] = val;
		}
		
		// Сохранение таблицы и функций предшествования
		table.exportToExcel(new File("table.xls"));
		System.out.println();

		// Сериализация полученной таблицы
		File file = new File("data.prc");
		FileUtils.writeByteArrayToFile(file, SerializationUtils.serialize(table.toData()));
		System.out.println("Precedence table serialized and saved to " + file);
	}
	
	private static int calcValue(int[][] matrix, int i, boolean[] visited) {
		if(visited[i]) return 0;
		visited[i] = true;
		
		int max = 0;
		for(int j = 0; j < matrix[i].length; j++)
			if(matrix[i][j] != -1) max = Math.max(max, matrix[i][j] + calcValue(matrix, j, visited));
		
		visited[i] = false;
		return max;
	}
	
	private static HashSet<String> last1(String check, HashSet<String> visited) {
		HashSet<String> result = new HashSet<>();
		
		if(visited.contains(check)) return result;
		visited.add(check);
		
		if(!grammar.isNonTerminal(check))
			result.add(check);
		else
			for(Rule rule : grammar.map.get(check)) result.addAll(last1(rule.get(rule.size() - 1), visited));
		
		return result;
	}
	
	private static Table buildTable() {
		// Определение символов, стоящих только в конце правил
		last = new ArrayList<>();
		last.addAll(grammar.terminals.keySet());
		last.addAll(grammar.nonTerminals);
		
		grammar.rules().forEach((rule) -> {
			for(int i = 0; i < rule.size() - 1; i++) last.remove(rule.get(i));
		});
		
		// Определение пар соседних символов при выводе из #S#
		pairs = new ArrayList<>();
		HashSet<Pair<String, String>> used = new HashSet<>();
		
		findPairs("#", grammar.getAxiom(), used);
		findPairs(grammar.getAxiom(), "#", used);
		
		// Таблица предшествования
		Table table = new Table(grammar);
		for(Pair<String, String> pair : pairs)
			if(grammar.isNonTerminal(pair.getKey()) && !grammar.isNonTerminal(pair.getValue())) {
				String A = pair.getKey(), m = pair.getValue();
				// Построение отношений >
				for(Rule rule : grammar.map.get(A)) table.addRelation(rule.get(rule.size() - 1), '>', m);
			} else if(grammar.isNonTerminal(pair.getValue())) {
				String m = pair.getKey(), A = pair.getValue();
				// Построение отношений <
				for(Rule rule : grammar.map.get(A)) table.addRelation(m, '<', rule.get(0));
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
