package Lab5;

import Lab3.*;
import Lab3.Type;
import Lab3.Lexeme;
import javafx.util.Pair;

import java.io.FileReader;

import static Lab3.Type.*;

/**
 * Created by Alexander on 18.10.2017.
 */
public class SyntaxAnalyzer {
    private Scanner sc;
    private Pair<Integer, Integer> errorPos;

//    private Lexeme[] main = {T_int, T_main, T_rbracket, T_lbracket};

    public SyntaxAnalyzer(FileReader in) {
        sc = new Scanner(in);
    }

    public void checkFile() throws Exception {
        tProgram();
//        sc.readAllLexemes();
    }

    private Pair<Integer, Integer> getUK() {
        return sc.getGlobalPtr();
    }

    private void setUK(Pair<Integer, Integer> uk) {
        sc.setLocalPtr(uk);
    }

    private void tProgram() throws Exception {
        sc.pickNextLine();
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        while (t.type != T_EOF) {
            tElement();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);
        }

    }

    private void tElement() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_const) {
            tConst();

        } else if (t.type == T_typedef) {
            tTypedef();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type != T_semicolon) {
                printError("Ожидалась ;");
            } else {
                sc.next();
            }
        } else if (t.type == T_int) {
            uk = getUK();

            sc.next();
            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_main) {
                tMain();
            } else
                tDataDescription();

        } else if (t.type == T_int64 || t.type == T_id)
            tDataDescription();
        else
            printError("Ожидалось main, typedef, описание данных или константы");
    }

    private void tSblock() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_lbracket) {
            sc.next();

            tBlock();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type != T_rbracket) {
                printError("Ожидалась }");
            } else {
                sc.next();
            }
        } else
            printError("Ожидалась {");

        return;
    }

    private void tMain() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_int) {
            sc.next();
            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_main) {
                sc.next();
                uk = getUK();

                t = sc.next();
                errorPos = getUK();
                setUK(uk);

                if (t.type == T_lparenthesis) {
                    sc.next();
                    uk = getUK();

                    t = sc.next();
                    errorPos = getUK();
                    setUK(uk);

                    if (t.type == T_rparenthesis) {
                        sc.next();
                        tSblock();
                    } else
                        printError("Ожидалась )");
                } else
                    printError("Ожидалась (");
            } else
                printError("Ожидалась main");
        } else
            printError("Ожидалась int");
    }

    private void tBlock() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        while (t.type != T_rbracket) {
            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_for || t.type == T_lbracket || t.type == T_semicolon) {
                tPartOfBlock();
            } else if(t.type == T_id || t.type == T_int || t.type == T_int64){
                uk = getUK();

                t = sc.next();
                Lexeme t1 = sc.next();
                errorPos = getUK();
                setUK(uk);

                if (t.type == T_id && t1.type == T_assign) {
                    tPartOfBlock();
                } else if (t.type == T_id || t.type == T_int || t.type == T_int64) {
                    tDataDescription();
                } else
                    printError("Ожидалось объявление данных");
            }else{
                printError("Ожидался for | ; | блок | присваивание | объявление данных");
            }

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);
        }
    }

    private void tPartOfBlock() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_for) {
            tFor();
        } else if (t.type == T_lbracket) {
            tSblock();
        } else if (t.type == T_semicolon) {
            sc.next();
        } else {
            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            Lexeme t1 = sc.next();
            setUK(uk);

            if (t.type == T_id || t.type == T_int || t.type == T_int64 && t1.type == T_assign) {
                tAssignment();

                uk = getUK();

                t = sc.next();
                errorPos = getUK();
                setUK(uk);

                if (t.type == T_semicolon) {
                    sc.next();
                } else
                    printError("Ожидалось ;");
            } else
                printError("Ожидалось объявление данных");
        }
    }

    private void tFor() throws Exception {
        sc.next();

        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_lparenthesis) {
            sc.next();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_int || t.type == T_int64 || t.type == T_id) {
                tDataDescription();

                tA0();

                uk = getUK();

                t = sc.next();
                errorPos = getUK();
                setUK(uk);

                if (t.type == T_semicolon) {
                    sc.next();

                    tA0();

                    uk = getUK();

                    t = sc.next();
                    errorPos = getUK();
                    setUK(uk);

                    if (t.type == T_rparenthesis) {
                        sc.next();
                        tPartOfBlock();
                    } else
                        printError("Ожидалась }");
                } else
                    printError("Ожидалось ;");
            } else
                printError("Ожидалась объявление данных");
        } else
            printError("Ожидалось (");

    }

    private void tTypedef() throws Exception {
        sc.next();

        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_int || t.type == T_int64 || t.type == T_id) {
            sc.next();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_lsbracket) {
                sc.next();

                uk = getUK();

                t = sc.next();
                errorPos = getUK();
                setUK(uk);

                if (t.type == T_const10 || t.type == T_const16 || t.type == T_id) {
                    sc.next();

                    uk = getUK();

                    t = sc.next();
                    errorPos = getUK();
                    setUK(uk);

                    if (t.type == T_rsbracket) {
                        sc.next();

                        uk = getUK();

                        t = sc.next();
                        errorPos = getUK();
                        setUK(uk);

                        if (t.type == T_id) {
                            sc.next();
                        } else
                            printError("Ожидался идентификатор");
                    } else
                        printError("Ожидалось ]");
                } else
                    printError("Ожидалось число");
            } else
                printError("Ожидалось [");
        } else
            printError("Ожидался тип");
    }

    private void tConst() throws Exception {
        sc.next();

        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_int || t.type == T_int64 || t.type == T_id) {
            sc.next();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_id) {
                sc.next();

                uk = getUK();

                t = sc.next();
                errorPos = getUK();
                setUK(uk);

                if (t.type == T_const10 || t.type == T_const16) {
                    sc.next();

                    uk = getUK();

                    t = sc.next();
                    errorPos = getUK();
                    setUK(uk);

                    if (t.type == T_semicolon) {
                        sc.next();
                    } else
                        printError("Ожидалось ;");
                } else
                    printError("Ожидалось число");
            } else
                printError("Ожидалось id");
        } else
            printError("Ожидался тип");
    }

    private void tArrayElement() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_lsbracket) {
            sc.next();

            tA0();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_rsbracket) {
                sc.next();
            } else
                printError("Ожидалась ]");
        } else
            printError("Ожидалось [");
    }

    private void tDataDescription() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_int || t.type == T_int64 || t.type == T_id) {
            sc.next();

            tIdList();
        }

        uk = getUK();

        t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_semicolon) {
            sc.next();
        } else
            printError("Ожидалась ;");
    }

    private void tAssignment() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_assign) {
            sc.next();
            tA0();
        } else
            printError("Ожидалось =");
    }

    private void tAssignment1() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_assign) {
            sc.next();
            tA0();
        } else
            printError("Ожидалось =");
    }

    private void tIdList() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_id) {
            sc.next();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_assign) {
                tAssignment1();
            }
        } else
            printError("Ожидался идентификатор");

        uk = getUK();

        t = sc.next();
        errorPos = getUK();
        setUK(uk);

        while (t.type == T_colon) {
            sc.next();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_id) {
                sc.next();

                uk = getUK();

                t = sc.next();
                errorPos = getUK();
                setUK(uk);

                if (t.type == T_assign) {
                    tAssignment1();
                }
            } else {
                printError("Ожидался идентификатор");
            }

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);
        }
    }

    private void tA0() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        boolean justA1 = true;
        Pair<Integer, Integer> IMPORTANTuk = getUK();


        if (t.type == T_id) {
            justA1 = false;
            uk = getUK();

            sc.next();
            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type != T_lsbracket) {
                uk = getUK();

                sc.next();
                t = sc.next();
                errorPos = getUK();
                setUK(uk);

                if (t.type == T_assign) {
                    sc.next();
                    tAssignment();
                } else {
                    justA1 = true;
                }
            } else {
                tArrayElement();

                uk = getUK();

                t = sc.next();
                errorPos = getUK();
                setUK(uk);

                if (t.type == T_assign) {
                    sc.next();
                    tAssignment();
                } else {
                    justA1 = true;
                }
            }
        }
        if (justA1) {
            setUK(IMPORTANTuk);
            tA1();
        }

    }

    private void tA1() throws Exception {
        tA2();

        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        while (t.type == T_or) {
            sc.next();

            tA2();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);
        }
    }

    private void tA2() throws Exception {
        tA3();

        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        while (t.type == T_and) {
            sc.next();

            tA3();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);
        }
    }

    private void tA3() throws Exception {
        tA4();

        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        while (t.type == T_more || t.type == T_less || t.type == T_meq || t.type == T_leq || t.type == T_neq || t.type == T_eqaul) {
            sc.next();

            tA4();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);
        }
    }

    private void tA4() throws Exception {
        tA5();

        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        while (t.type == T_minus || t.type == T_plus) {
            sc.next();

            tA5();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);
        }
    }

    private void tA5() throws Exception {
        tA6();

        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        while (t.type == T_multiply || t.type == T_division || t.type == T_mod) {
            sc.next();

            tA6();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);
        }
    }

    private void tA6() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        while (t.type == T_not) {
            sc.next();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);
        }

        tA7();
    }

    private void tA7() throws Exception {
        Pair<Integer, Integer> uk = getUK();

        Lexeme t = sc.next();
        errorPos = getUK();
        setUK(uk);

        if (t.type == T_const10 || t.type == T_const16) {
            sc.next();
        } else if (t.type == T_lparenthesis) {
            sc.next();

            tA0();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type != T_rparenthesis) {
                printError("Ожидалась )");
            } else
                sc.next();
        } else if (t.type == T_id) {
            sc.next();

            uk = getUK();

            t = sc.next();
            errorPos = getUK();
            setUK(uk);

            if (t.type == T_lsbracket) {
                tArrayElement();
            }
        }else
            printError("Ожидалось выражение");

    }

    private void printError(String end) throws Exception {
//        String ans = "" + (sc.currentLine) + " " + (sc.ptr + 1) + " " + end;
        String ans = "" + (errorPos.getKey()) + " " + (errorPos.getValue()+1) + " " + end;

        System.out.println(ans);

        throw new Exception();
    }
}
