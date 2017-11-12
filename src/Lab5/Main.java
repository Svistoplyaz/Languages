package Lab5;

import java.io.FileReader;

/**
 * Lab 5 - Syntax analyzer
 */
public class Main {
    public static void main(String[] args) throws Exception{
        SyntaxAnalyzer2 sa = new SyntaxAnalyzer2(new FileReader("in.in"));

        try{
            sa.tProgram();
        }catch (AnalyzeError e){
//            e.printStackTrace();
            System.out.println(e.getDisplayMessage());
            return;
        }
    }
}
