package Lab5;

import java.io.FileReader;

/**
 * Lab 5 - Syntax analyzer
 */
public class Main {
    public static void main(String[] args) throws Exception{
        SyntaxAnalyzer sa = new SyntaxAnalyzer(new FileReader("in.in"));

        try{
            sa.checkFile();
        }catch (Exception e){
            return;
        }
    }
}
