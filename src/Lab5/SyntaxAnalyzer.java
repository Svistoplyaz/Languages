package Lab5;

import Lab3.Scanner;
import Lab3.Scanner.Lexeme;

import java.io.FileReader;

/**
 * Created by Alexander on 18.10.2017.
 */
public class SyntaxAnalyzer {
    public Scanner sc;

    public void checkFile(FileReader in){
        Scanner sc = new Scanner(in);
    }

    public int GetUK(){
        return sc.getGlobalPtr();
    }

    public void PutUK(int uk){
        sc.setLocalPtr(uk);
    }

    public void tProgram(){
        int uk1 = GetUK();
        Lexeme t = sc.next(); PutUK(uk1);
        while ( t == <first(A)> )
        {

                uk1 = GetUK(); t= Scaner (lex); PutUK(uk1);
        }
    }

}
