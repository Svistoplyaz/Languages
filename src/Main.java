import semester7.Lab3.Scanner;
import semester7.Lab5.AnalyzeError;
import semester7.precedence.PrecedenceAnalyzer;
import semester8.Lab2.SyntaxAnalyzer3;

import java.io.File;
import java.io.FileReader;

/**
 * Lab 5 - Syntax analyzer
 */
public class Main {
    public static void main(String[] args) throws Exception{
        SyntaxAnalyzer3 sa = new SyntaxAnalyzer3(new FileReader("in.in"));
//        LLkAnalyzer sa = new LLkAnalyzer(new File("table.semester7.llk"),new Scanner(new FileReader("in.in")));

        long[][][] i = new long[1000][1000][100];
//        PrecedenceAnalyzer sa = new PrecedenceAnalyzer(new File("data.prc"),new Scanner(new FileReader("in1.in")));

        try{
            sa.tProgram();
        }catch (AnalyzeError e){
//            e.printStackTrace();
            System.out.println(e.getDisplayMessage());
            return;
        }
    }
}
