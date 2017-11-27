package llk;

import Lab3.Type;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Table implements Serializable {

	private final HashMap<String, NonTerminal> nonTerminalsMapping = new HashMap<>();
	private final HashMap<String, Terminal> terminalsMapping = new HashMap<>();
	private final HashMap<Type, Integer> typesMapping = new HashMap<>();
	
	private final Cell[][] cells;
	
	private final Terminal end;
	private final NonTerminal axiom;
	
	public Table(ArrayList<String> nonTerminals, HashMap<String, Type> terminals, String s) {
		cells = new Cell[nonTerminals.size()][terminals.size()];
		for(int i = 0; i < nonTerminals.size(); i++)
			for(int j = 0; j < terminals.size(); j++) cells[i][j] = new Cell();
		
		for(String nonTerminal : nonTerminals)
			nonTerminalsMapping.put(nonTerminal, new NonTerminal(nonTerminal, nonTerminalsMapping.size()));
		axiom = nonTerminalsMapping.get(s);
		
		Terminal tmpEnd = null;
		for(Map.Entry<String, Type> terminal : terminals.entrySet()) {
			String name = terminal.getKey();
			terminalsMapping.put(name, new Terminal(name, terminalsMapping.size(), terminal.getValue()));
			typesMapping.put(terminal.getValue(), typesMapping.size());
			
			if(terminal.getValue() == Type.T_EOF) tmpEnd = terminalsMapping.get(name);
		}
		end = tmpEnd;
	}
	
	public void add(String nonTerminal, String terminal, ArrayList<String> rule) {
		int a = nonTerminalsMapping.get(nonTerminal).index;
		int b = terminalsMapping.get(terminal).index;
		
		ArrayList<Element> result = new ArrayList<>();
		for(int i = rule.size() - 1; i >= 0; i--) {
			String v = rule.get(i);
			if(v.equals("#")) continue;
			result.add(nonTerminalsMapping.containsKey(v) ? nonTerminalsMapping.get(v) : terminalsMapping.get(v));
		}
		
		cells[a][b].add(result);
	}
	
	public Cell get(NonTerminal nonTerminal, Type type) {
		return cells[nonTerminal.index][typesMapping.get(type)];
	}
	
	public void exportToExcel(File file) throws Exception {
		System.out.println("Writing control table to " + file.getName() + "...");
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Control table");
		
		for(int i = 0; i < cells.length + 1; i++) {
			Row row = sheet.createRow(i);
			for(int j = 0; j < cells[0].length + 1; j++) row.createCell(j).getCellStyle().setWrapText(true);
		}
		
		for(Map.Entry<String, Terminal> e : terminalsMapping.entrySet())
			sheet.getRow(0).getCell(e.getValue().index + 1).setCellValue(e.getKey());
		
		for(Map.Entry<String, NonTerminal> e : nonTerminalsMapping.entrySet())
			sheet.getRow(e.getValue().index + 1).getCell(0).setCellValue(e.getKey());
		
		for(int i = 0; i < cells.length; i++)
			for(int j = 0; j < cells[i].length; j++) {
				if(cells[i][j].isEmpty()) continue;
				
				StringBuilder builder = new StringBuilder();
				for(ArrayList<Element> rule : cells[i][j]) {
					if(builder.length() != 0) builder.append("\n");
					
					for(int k = 0; k < rule.size(); k++) {
						if(k != 0) builder.append(" ");
						builder.append(rule.get(k));
					}
				}
				
				sheet.getRow(i + 1).getCell(j + 1).setCellValue(builder.toString());
			}
		
		for(int i = 0; i < cells[0].length + 1; i++) sheet.autoSizeColumn(i);
			
		FileOutputStream out = new FileOutputStream(file);
		workbook.write(out);
		out.close();
		
		System.out.println("Successfully finished!");
	}
	
	public NonTerminal getAxiom() {
		return axiom;
	}
	
	public Terminal getEndTerminal() {
		return end;
	}
	
	public static class Cell extends ArrayList<ArrayList<Element>> { }
	
	public static abstract class Element implements Serializable {
		
		public final String value;
		public final int index;
		
		public Element(String s, int i) {
			value = s; index = i;
		}
		
		@Override
		public String toString() {
			return value;
		}
		
	}
	
	public static class Terminal extends Element {
		
		public final Type type;
		
		public Terminal(String s, int i, Type t) {
			super(s, i);
			type = t;
		}
		
	}
	
	public static class NonTerminal extends Element {
		
		public NonTerminal(String s, int i) {
			super(s, i);
		}
		
	}
	
}
