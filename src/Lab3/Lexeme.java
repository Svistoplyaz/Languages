package Lab3;

public class Lexeme {

    public final Type type;
    public final String value;
    public final int line;

    public Lexeme(Type t, String v, int l) {
        type = t;
        value = v;
        line = l;
    }

    @Override
    public boolean equals(Object other){
        Lexeme lex = (Lexeme) other;
        return this.type == lex.type && this.value.equals(lex.value);
    }
}
