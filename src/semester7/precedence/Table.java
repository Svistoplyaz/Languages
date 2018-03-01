package semester7.precedence;

import semester7.Lab3.Type;
import semester7.llk.Grammar;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Table implements Serializable {
	
	private final Grammar grammar;
	
	private final HashMap<String, Integer> mapping = new HashMap<>();
	private final String[] reverseMapping;
	
	private final Cell[][] cells;

	private final HashMap<Type, String> reverse = new HashMap<>();
	
	private final int[] f, g;
	private int[][] matrix;
	
	public Table(Grammar gr) {
		grammar = gr;
		
		for(String s : grammar.nonTerminals) mapping.put(s, mapping.size());
		for(String s : grammar.terminals.keySet()) mapping.put(s, mapping.size());
		if(!mapping.containsKey("#")) mapping.put("#", mapping.size());
		
		reverseMapping = new String[mapping.size()];
		for(Map.Entry<String, Integer> e : mapping.entrySet()) reverseMapping[e.getValue()] = e.getKey();
		
		cells = new Cell[mapping.size()][mapping.size()];
		for(int i = 0; i < cells.length; i++)
			for(int j = 0; j < cells[i].length; j++) cells[i][j] = new Cell();

		for(Map.Entry<String, Type> e : grammar.terminals.entrySet()) reverse.put(e.getValue(), e.getKey());
		
		matrix = new int[mapping.size() * 2][mapping.size() * 2];
		for(int[] a : matrix) Arrays.fill(a, -1);
		
		f = new int[mapping.size()];
		g = new int[mapping.size()];
	}
	
	public Data toData() {
		return new Data(grammar, f, g, mapping);
	}
	
	public void addRelation(String a, char relation, String b) {
		int i = mapping.get(a), j = mapping.get(b);
		cells[i][j].relations.add(relation);
	}

	public int[][] getMatrix() {
		matrix = new int[mapping.size() * 2][mapping.size() * 2];
		for(int i = 0; i < cells.length; i++)
			for(int j = 0; j < cells[i].length; j++)
				if(cells[i][j].relations.size() >= 1)
					addRelationToMatrix(i, cells[i][j].relations.iterator().next(), j);
		return matrix;
	}
	
	public void addRelationToMatrix(int i, char relation, int j) {
		switch(relation) {
			case '=':
				matrix[i][getSize() + j] = 0;
				matrix[getSize() + j][i] = 0;
				break;
			case '>':
				matrix[i][getSize() + j] = 1;
				break;
			case '<':
				matrix[getSize() + j][i] = 1;
				break;
		}
	}
	
	public int getSize() {
		return mapping.size();
	}
	
	public Cell getCell(int i, int j) {
		return cells[i][j];
	}
	
	public String getCharacter(int i) {
		return reverseMapping[i];
	}
	
	public int getIndex(String s) {
		return mapping.get(s);
	}
	
	public Grammar getGrammar() {
		return grammar;
	}
	
	public String convertLexeme(Type type) {
		if(type == Type.T_EOF) return "#";
		int lel;
		if(reverse.get(type) == null)
			lel = 1;
		return reverse.get(type);
	}
	
	public int[] getF() {
		return f;
	}
	
	public int[] getG() {
		return g;
	}
	
	public void exportToExcel(File file) throws Exception {
		System.out.println("Writing semester7.precedence table to " + file.getName() + "...");
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Precedence table");
		
		for(int i = 0; i < cells.length + 2; i++) {
			Row row = sheet.createRow(i);
			for(int j = 0; j < cells[0].length + 2; j++) row.createCell(j).getCellStyle().setWrapText(true);
		}
		
		for(int i = 0; i < reverseMapping.length; i++)
			sheet.getRow(0).getCell(i + 1).setCellValue(reverseMapping[i]);
		
		for(int i = 0; i < reverseMapping.length; i++)
			sheet.getRow(i + 1).getCell(0).setCellValue(reverseMapping[i]);
		
		for(int i = 0; i < cells.length; i++)
			for(int j = 0; j < cells[i].length; j++) {
				if(cells[i][j].relations.isEmpty()) continue;
				
				StringBuilder builder = new StringBuilder();
				for(char c : cells[i][j].relations) {
					if(builder.length() != 0) builder.append("\n");
					builder.append(c);
				}
				
				sheet.getRow(i + 1).getCell(j + 1).setCellValue(builder.toString());
			}
		
		//for(int i = 0; i < f.length; i++) sheet.getRow(1 + i).getCell(cells.length + 1).setCellValue(f[i]);
		//for(int i = 0; i < g.length; i++) sheet.getRow(cells[0].length + 1).getCell(1 + i).setCellValue(g[i]);
			
		for(int i = 0; i < cells[0].length + 2; i++) sheet.autoSizeColumn(i);
		
		FileOutputStream out = new FileOutputStream(file);
		workbook.write(out);
		out.close();
		
		System.out.println("Successfully finished!");
	}
	
	public static class Cell implements Serializable {
		
		public final HashSet<Character> relations = new HashSet<>();
		
	}
	
}
