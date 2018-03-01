package semester7.Lab5;



import semester7.Lab3.Lexeme;
import semester7.Lab3.Scanner;
import semester7.Lab3.Type;

import java.util.function.Supplier;

public class AnalyzeError extends RuntimeException {
	
	private final String message;
	private final String additional;
	
	public AnalyzeError(Scanner scanner, Lexeme lexeme, Type... expected) {
		this(scanner, lexeme, ((Supplier<String>)(() -> {
			StringBuilder builder = new StringBuilder();
			builder.append("Found ");
			if(lexeme.type == Type.T_EOF) {
				builder.append("end of file");
			} else {
				if(lexeme.type == Type.T_err) builder.append("invalid ");
				builder.append("character \"");
				builder.append(lexeme.value);
				builder.append("\"");
			}
			return builder.toString();
		})).get(), "expected " + asList(expected));
	}
	
	public AnalyzeError(Scanner scanner, Lexeme lexeme, String first, String... lines) {
		StringBuilder builder = new StringBuilder();
//		builder.append(scanner.getSourceName());
		builder.append("(line ");
		builder.append(lexeme.line);
		builder.append(", ");
		builder.append(lexeme.ptr);
		builder.append("): ");
		int length = builder.length();
		
		builder.append(first);
		message = builder.toString();
		builder = new StringBuilder();
		builder.append("\n");
		
		for(String s : lines) {
			for(int i = 0; i < length; i++) builder.append(" ");
			builder.append(s);
			builder.append("\n");
		}
//		builder.append("\n");
//
//		if(lexeme.type != Type.T_EOF) {
//			builder.append(scanner.getLine(lexeme.line));
//			for(int i = 0; i < lexeme.character; i++) builder.append(" ");
//			builder.append("^");
//		}

		additional = builder.toString();
	}
	
	@Override
	public String getMessage() {
		return message;
	}
	
	public String getDisplayMessage() {
		return message + additional;
	}

	private static String asList(Type[] types) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < types.length - 1; i++) {
			if(builder.length() == 0) {
				builder.append(types[i]);
			} else {
				builder.append(", ");
				builder.append(types[i]);
			}
		}
		if(types.length > 1) builder.append(" or ");
		builder.append(types[types.length - 1]);
		
		return builder.toString();
	}

}