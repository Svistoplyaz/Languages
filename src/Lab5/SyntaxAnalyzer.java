package Lab5;

import Lab3.*;
import Lab3.Type;
import Lab3.Scanner.Lexeme;

import java.io.FileReader;

import static Lab3.Type.*;

/**
 * Created by Alexander on 18.10.2017.
 */
public class SyntaxAnalyzer {
    public Scanner sc;

//    private Type[] main = {T_int, T_main, T_rbracket, T_lbracket};


    public void checkFile(FileReader in){
        Scanner sc = new Scanner(in);
    }

    public int getUK(){
        return sc.getGlobalPtr();
    }

    public void setUK(int uk){
        sc.setLocalPtr(uk);
    }

    public boolean tProgram(){
        while ( tElement() )
        {

        }

        return true;
    }

    public boolean tElement(){

        return true;
    }

    public boolean tSblock(){

        return true;
    }

    public boolean tMain(){

        return true;
    }

    public boolean tBlock(){

        return true;
    }

    public boolean tPartOfBlock(){

        return true;
    }

    public boolean tFor(){

        return true;
    }

    public boolean tTypedef(){

        return true;
    }

    public boolean tConst(){

        return true;
    }

    public boolean tArrayElement(){

        return true;
    }

    public boolean tDataDescription(){

        return true;
    }

    public boolean tAssignment(){

        return true;
    }

    public boolean tAssignment1(){

        return true;
    }

    public boolean tIdList(){
        int uk = getUK();
        Lexeme t = sc.next();
        setUK(uk);

        if(t.type == T_id){
            uk = getUK();
            t = sc.next();
            setUK(uk);

            if(t.type == T_assign){
                tAssignment1();
            }else{
                return true;
            }
        }

        uk = getUK();
        t = sc.next();
        setUK(uk);

        while (t.type == T_colon){
            sc.next();

            uk = getUK();
            t = sc.next();
            setUK(uk);

            if(t.type == T_id){
                uk = getUK();
                t = sc.next();
                setUK(uk);

                if(t.type == T_assign){
                    tAssignment1();
                }else{
                    sc.next();
                }
            }

            uk = getUK();
            t = sc.next();
            setUK(uk);
        }

        return true;
    }

    public boolean tA0(){
        int uk = getUK();
        Lexeme t = sc.next();
        setUK(uk);

        if(t.type == T_id){
            uk = getUK();
            t = sc.next();
            setUK(uk);

            if(t.type == T_lsbracket){
                tArrayElement();
            }

            uk = getUK();
            t = sc.next();
            setUK(uk);

            if(t.type == T_assign){
                tA1();
                return true;
            }
        }

        tA1();

        return true;
    }

    public boolean tA1(){
        tA2();

        int uk = getUK();
        Lexeme t = sc.next();
        setUK(uk);

        while (t.type == T_or){
            tA2();

            uk = getUK();
            t = sc.next();
            setUK(uk);
        }

        return true;
    }

    public boolean tA2(){
        tA3();

        int uk = getUK();
        Lexeme t = sc.next();
        setUK(uk);

        while (t.type == T_and){
            tA3();

            uk = getUK();
            t = sc.next();
            setUK(uk);
        }

        return true;
    }

    public boolean tA3(){
        tA4();

        int uk = getUK();
        Lexeme t = sc.next();
        setUK(uk);

        while (t.type == T_more || t.type == T_less || t.type == T_meq || t.type == T_leq || t.type == T_neq || t.type == T_eqaul){
            tA4();

            uk = getUK();
            t = sc.next();
            setUK(uk);
        }

        return true;
    }

    public boolean tA4(){
        tA5();

        int uk = getUK();
        Lexeme t = sc.next();
        setUK(uk);

        while (t.type == T_minus || t.type == T_plus){
            tA5();

            uk = getUK();
            t = sc.next();
            setUK(uk);
        }

        return true;
    }

    public boolean tA5(){
        tA6();

        int uk = getUK();
        Lexeme t = sc.next();
        setUK(uk);

        while (t.type == T_multiply || t.type == T_division || t.type == T_mod){
            tA6();

            uk = getUK();
            t = sc.next();
            setUK(uk);
        }

        return true;
    }

    public boolean tA6(){
        int uk = getUK();
        Lexeme t = sc.next();
        setUK(uk);

        while (t.type == T_not){
            uk = getUK();
            t = sc.next();
            setUK(uk);
        }

        tA7();

        return true;
    }

    public boolean tA7(){
        int uk = getUK();
        Lexeme t = sc.next();
        setUK(uk);

        if(t.type == T_const10 || t.type == T_const16)
            return true;

        else if(t.type == T_lparenthesis){
            tA0();

            uk = getUK();
            t = sc.next();
            setUK(uk);

            if(t.type != T_rparenthesis){
                return false;
            }
        }
        else if(t.type == T_id){
            uk = getUK();
            t = sc.next();
            setUK(uk);

            if(t.type == T_lsbracket){
                tArrayElement();
            }else{
                return true;
            }
        }

        return false;
    }


}
