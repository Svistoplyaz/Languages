import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;

public class Scanner {
	
	enum Type {
		Identifier,
		KeyInt, KeyLong, KeyMain, KeyReturn, KeyDo, KeyWhile,
		ParenthesisOpen, ParenthesisClose, BraceOpen, BraceClose,
		Semicolon, Comma,
		Assignment, EQ, NE, LT, LTE, GT, GTE, Plus, Minus, Multiplication, Division, Mod,
		ConstInt10, ConstInt16,
		End, Error
	}
	
	private static final HashMap<String, Type> keywords = new HashMap<>();
	static {
		keywords.put("int",	 	Type.KeyInt);
		keywords.put("long", 	Type.KeyLong);
		keywords.put("main", 	Type.KeyMain);
		keywords.put("return",	Type.KeyReturn);
		keywords.put("do", 		Type.KeyDo);
		keywords.put("while", 	Type.KeyWhile);
	}
	
	private final BufferedReader reader;
	private char[] line;
	private int ptr, currentLine = 0;
	
	private StringBuilder current = new StringBuilder();
	
	public Scanner(Reader reader) {
		this.reader = new BufferedReader(reader);
		readNextLine();
	}
	
	public Lexeme next() {
		skip();
		if(line == null) return finishLexeme(Type.End);
		
		if(isLetter(line[ptr])) {
			do {
				current.append(line[ptr]);
				ptr++;
			} while(isLetter(line[ptr]) || isNumber(line[ptr]));
			
			Type type = keywords.get(current.toString().toLowerCase());
			return finishLexeme(type == null ? Type.Identifier : type);
		}
		
		if(isNumber(line[ptr])) {
			if(line[ptr] == '0' && line[ptr + 1] == 'x') {
				ptr += 2;
				
				do {
					current.append(line[ptr]);
					ptr++;
				} while(isNumber16(line[ptr]));
				return finishLexeme(Type.ConstInt16);
			}
			
			do {
				current.append(line[ptr]);
				ptr++;
			} while(isNumber(line[ptr]));
			return finishLexeme(Type.ConstInt10);
		} 
		
		current.append(line[ptr]);
		ptr++;
		
		switch(line[ptr - 1]) {
			case '(':
				return finishLexeme(Type.ParenthesisOpen);
			case ')':
				return finishLexeme(Type.ParenthesisClose);
			case '{':
				return finishLexeme(Type.BraceOpen);
			case '}':
				return finishLexeme(Type.BraceClose);
			case '=':
				return checkTwoChars(Type.Assignment, '=', Type.EQ);
			case '!':
				return checkTwoChars(Type.Error, '=', Type.NE);
			case '<':
				return checkTwoChars(Type.LT, '=', Type.LTE);
			case '>':
				return checkTwoChars(Type.GT, '=', Type.GTE);
			case '+':
				return finishLexeme(Type.Plus);
			case '-':
				return finishLexeme(Type.Minus);
			case '*':
				return finishLexeme(Type.Multiplication);
			case '/':
				return finishLexeme(Type.Division);
			case '%':
				return finishLexeme(Type.Mod);
			case ';':
				return finishLexeme(Type.Semicolon);
			case ',':
				return finishLexeme(Type.Comma);
		}
		
		return finishLexeme(Type.Error);
	}
	
	private void skip() {
		for(;;) {
			if(line == null) return;

			if(line[ptr] == ' ' || line[ptr] == '\t') {
				ptr++;
			} else if(line[ptr] == '\n') {
				readNextLine();
			} else if(line[ptr] == '/' && line[ptr + 1] == '/') {
				readNextLine();
			} else if(line[ptr] == '/' && line[ptr + 1] == '*') {
				while(line != null && !(line[ptr] == '*' && line[ptr + 1] == '/')) {
					ptr++;
					if(line[ptr] == '\n') readNextLine();
				}
				ptr += 2;
			} else break;
		}
	}
	
	private void readNextLine() {
		try {
			String readed = reader.readLine();
			if(readed != null) {
				line = (readed + "\n").toCharArray();
				ptr = 0; currentLine++;
			} else line = null;
		} catch(Exception e) {
			line = null;
		}
	}
	
	private Lexeme checkTwoChars(Type type, char c, Type type2) {
		if(line[ptr] != c) return finishLexeme(type);
		current.append(line[ptr]); ptr++;
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
		public final String value;
		public final int line;
		
		private Lexeme(Type t, String v, int l) {
			type = t; value = v; line = l;
		}
		
	}
	
}
