package Lab3;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

public class Scanner {

	private static final HashMap<String, Type> voc = new HashMap<>();
	static {
		voc.put("int", Type.T_int);
		voc.put("__int64", Type.T_int64);
		voc.put("main", Type.T_main);
		voc.put("return", Type.T_return);
		voc.put("for", Type.T_for);
		voc.put("typedef", Type.T_typedef);
		voc.put("const", Type.T_const);
	}
	
	private final BufferedReader reader;
	private char[] line;
	public int ptr, currentLine = 0;
	private ArrayList<Integer> stringLengthes = new ArrayList<>();
	
	private StringBuilder current = new StringBuilder();

	private ArrayList<String> all_lines = new ArrayList<>();
	
	public Scanner(Reader reader) {

		this.reader = new BufferedReader(reader);

		while(true){
			try {
				String readed = this.reader.readLine();

				if(readed != null) {
					stringLengthes.add(readed.length());
					all_lines.add(readed);
				} else {
					line = null;
					break;
				}
			} catch(Exception e) {
				line = null;
				break;
			}
		}

	}

	public void readAllLexemes(){
		pickNextLine();

		while (true) {
			Lexeme cur = next();
			System.out.print(cur.line+" "+cur.type+" "+cur.value+"\n");
			if(cur.type == Type.T_EOF)
				break;
		}
	}

	public Pair<Integer,Integer> getGlobalPtr(){
//		int ans = 0;
//
//		for(int i = 1; i < currentLine; i++){
//			ans += stringLengthes.get(i-1);
//		}
//
//		return ans + ptr;
		return new Pair<>(currentLine, ptr);
	}

	public void setLocalPtr(Pair<Integer,Integer> global){
		currentLine = global.getKey();
		ptr = global.getValue();
	}

	public Lexeme next() {
		skip();
		if(line == null)
			return finishLexeme(Type.T_EOF);
		
		if(isLetter(line[ptr])) {
			do {
				current.append(line[ptr]);
				ptr++;
			} while(isLetter(line[ptr]) || isNumber(line[ptr]));
			
			Type type = voc.get(current.toString());
			return finishLexeme(type == null ? Type.T_id : type);
		}
		
		if(isNumber(line[ptr])) {
			if(line[ptr] == '0' && line[ptr + 1] == 'x') {

				if(line[ptr + 2] == '\n'){
					ptr+=2;
					current.append("0x");
					return finishLexeme(Type.T_err);
				}

				ptr += 2;

				do {
					current.append(line[ptr]);
					ptr++;
				} while(isNumber16(line[ptr]));
				return finishLexeme(Type.T_const16);
			}
			
			do {
				current.append(line[ptr]);
				ptr++;
			} while(isNumber(line[ptr]));
			return finishLexeme(Type.T_const10);
		} 
		
		current.append(line[ptr]);
		ptr++;
		
		switch(line[ptr - 1]) {
			case '(':
				return finishLexeme(Type.T_lparenthesis);
			case ')':
				return finishLexeme(Type.T_rparenthesis);
			case '{':
				return finishLexeme(Type.T_lbracket);
			case '}':
				return finishLexeme(Type.T_rbracket);
			case '[':
				return finishLexeme(Type.T_lsbracket);
			case ']':
				return finishLexeme(Type.T_rsbracket);
			case '|':
				return checkTwoChars(Type.T_err, '|', Type.T_or);
			case '&':
				return checkTwoChars(Type.T_err, '&', Type.T_and);
			case '=':
				return checkTwoChars(Type.T_assign, '=', Type.T_eqaul);
			case '!':
				return checkTwoChars(Type.T_not, '=', Type.T_neq);
			case '<':
				return checkTwoChars(Type.T_less, '=', Type.T_leq);
			case '>':
				return checkTwoChars(Type.T_more, '=', Type.T_meq);
			case '+':
				return finishLexeme(Type.T_plus);
			case '-':
				return finishLexeme(Type.T_minus);
			case '*':
				return finishLexeme(Type.T_multiply);
			case '/':
				return finishLexeme(Type.T_division);
			case '%':
				return finishLexeme(Type.T_mod);
			case ';':
				return finishLexeme(Type.T_semicolon);
			case ',':
				return finishLexeme(Type.T_colon);
		}
		
		return finishLexeme(Type.T_err);
	}
	
	private void skip() {
		while (true){
			if(line == null) return;

			if(line[ptr] == ' ' || line[ptr] == '\t') {
				ptr++;
			} else if(line[ptr] == '\n') {
				pickNextLine();
			} else if(ptr + 1 < line.length && line[ptr] == '/' && line[ptr + 1] == '/') {
				pickNextLine();
			} else if(ptr + 1 < line.length && line[ptr] == '/' && line[ptr + 1] == '*') {
				while(line != null && !(ptr + 1 < line.length && line[ptr] == '*' && line[ptr + 1] == '/')) {
					ptr++;

					if(line[ptr] == '\n')
						pickNextLine();
				}
				ptr += 2;
			} else break;
		}
	}
	
	public void pickNextLine() {
		try {
			String readed = all_lines.get(currentLine);
			if(readed != null) {
				line = (readed + "\n").toCharArray();
				ptr = 0;
				currentLine++;
			} else
				line = null;
		} catch(Exception e) {
			line = null;
		}
	}
	
	private Lexeme checkTwoChars(Type type, char c, Type type2) {
		if(line[ptr] != c)
			return finishLexeme(type);

		current.append(line[ptr]);
		ptr++;
		return finishLexeme(type2);
	}
	
	private boolean isLetter(char c) {
		return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == '_';
	}
	
	private boolean isNumber(char c) {
		return '0' <= c && c <= '9';
	}
	
	private boolean isNumber16(char c) {
		return isNumber(c) || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F';
	}
	
	private Lexeme finishLexeme(Type t) {
		Lexeme l = new Lexeme(t, current.toString(), currentLine);
		current = new StringBuilder();
		return l;
	}
	
	public static class Lexeme {
		
		public final Type type;
		final String value;
		final int line;
		
		private Lexeme(Type t, String v, int l) {
			type = t;
			value = v;
			line = l;
		}
		
	}
	
}
