package semester7.llk;

import semester7.Lab3.*;
import semester7.Lab5.AnalyzeError;
import org.apache.commons.lang3.SerializationUtils;
import static semester7.llk.Table.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Stack;

public class LLkAnalyzer {
	
	private static final boolean DEBUG = false;
	
	private final Table controlTable;
	private final Scanner scanner;
	
	public LLkAnalyzer(File table, Scanner source) throws Exception {
		controlTable = SerializationUtils.deserialize(new FileInputStream(table));
		scanner = source;
	}

	public void tProgram() {
		Stack<Element> stack = new Stack<>();
		stack.add(controlTable.getEndTerminal());
		stack.add(controlTable.getAxiom());
		
		Lexeme lexeme = scanner.next();
		for(;;) {
			if(DEBUG) System.out.format("%-17s %-2d %s\n", lexeme.type.name(), lexeme.line + 1, stack);
			Element current = stack.pop();
			
			if(current instanceof Terminal) {
				Terminal terminal = (Terminal)current;
				
				if(lexeme.type != terminal.type) {
					String line1 = "expected " + terminal.type, line2 = "but found " + lexeme.type;
					throw new AnalyzeError(scanner, lexeme, line1, line2);
				}
				if(lexeme.type == Type.T_EOF) break;
				lexeme = scanner.next();
			} else {
				NonTerminal nonTerminal = (NonTerminal)current;
				
				Cell cell = controlTable.get(nonTerminal, lexeme.type);
				if(cell.isEmpty()) {
					String line1 = "wrong character " + lexeme.type, line2 = "when analyzing " + nonTerminal;
					throw new AnalyzeError(scanner, lexeme, line1, line2);
				}
				stack.addAll(cell.get(0));
			}
		}
	}

}
