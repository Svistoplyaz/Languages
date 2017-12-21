package operatorPrecedence;

/**
 * Created by Alexandr on 16.12.2017.
 */
public enum Comparance {
    more(" > "), less(" < "), equal(" = ");

    String name;

    Comparance(String str) {
        name = str;
    }
}
