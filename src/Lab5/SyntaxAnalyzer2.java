package Lab5;

import Lab3.Lexeme;
import Lab3.Position;
import Lab3.Scanner;
import Lab3.Type;
import javafx.util.Pair;

import java.io.FileReader;

import static Lab3.Type.*;

public class SyntaxAnalyzer2 {
    private Scanner sc;
    private SemanticAnalyzer sem;

    public SyntaxAnalyzer2(FileReader in) {
        sc = new Scanner(in);
        sem = new SemanticAnalyzer(sc);
    }

    private void tProgram() throws Exception {

        Lexeme t = sc.nextWithBackup();

        while (t.type != T_EOF) {
            tElement();

            t = sc.nextWithBackup();
        }
    }

    private void tElement() {
        Position pos = sc.getCurPosition();
        Lexeme t = sc.nextWithBackup();

        switch (t.type) {
            case T_const:
                tConst();
                break;
            case T_typedef:
                tTypedef();
                break;
            default:
                switch (tType()) {
                    case T_int:
                        if (sc.nextWithBackup().type == T_main) {
                            sc.setCurPosition(pos);
                            tMain();
                        } else {
                            sc.setCurPosition(pos);
                            tDataDescription();
                        }
                        break;
                    default:
                        sc.setCurPosition(pos);
                        tDataDescription();
                }
        }
    }

    private Type tType() {
        Lexeme type = sc.next();
        switch (type.type) {
            case T_int:
            case T_int64:
                return type.type;
            case T_id:
                //Вставить проверку на то что есть такой тип
                return T_id;
            default:
                throw new AnalyzeError(sc, type, T_id);
        }
    }

    private void tDataDescription() {
        Type t = tType();

        //Запись в дерево

        Lexeme lexeme;
        do {
            Lexeme id = sc.next();
            if (id.type != T_id)
                throw new AnalyzeError(sc, id, T_id);

            //semantic.addVariable(t, id);

            Position pos = sc.getCurPosition();
            Lexeme assignment = sc.next();
            if (assignment.type == T_assign) {
                //semantic.checkAssignment(assignment, type, A1());
            } else sc.setCurPosition(pos);

            lexeme = sc.next();
        } while (lexeme.type == T_colon);

        if (lexeme.type != T_semicolon)
            throw new AnalyzeError(sc, lexeme, T_semicolon);
    }

    private void tMain() {
        sc.next();
        sc.next();

        scanAndCheck(T_lparenthesis);
        scanAndCheck(T_rparenthesis);
        tBlock();
//        semantic.goToParentLevel();
    }

    private void tTypedef() {
        scanAndCheck(T_typedef);
        tType();
        scanAndCheck(T_lsbracket);

        Lexeme number = sc.next();
        switch (number.type) {
            case T_const10:
            case T_const16:
//                return semantic.getConstType(next);
                break;
            case T_id:
//                check if const
                break;
            default:
                throw new AnalyzeError(sc, number, T_const10, T_const16);
        }

        scanAndCheck(T_rsbracket);
        Lexeme id = sc.next();
        switch (id.type) {
            case T_id:
                //check
                break;
            default:
                throw new AnalyzeError(sc, id, T_id);
        }
        scanAndCheck(T_semicolon);
    }

    private void tConst() {
        scanAndCheck(T_const);
        tType();

        Lexeme id = sc.next();
        switch (id.type) {
            case T_id:
                //check
                break;
            default:
                throw new AnalyzeError(sc, id, T_id);
        }

        Lexeme number = sc.next();
        switch (number.type) {
            case T_const10:
            case T_const16:
//                return semantic.getConstType(next);
                break;
            default:
                throw new AnalyzeError(sc, number, T_const10, T_const16);
        }

        scanAndCheck(T_semicolon);
    }

    private void tBlock() {
        scanAndCheck(T_lbracket);
//        semantic.startBlock();
        if (sc.nextWithBackup().type != T_rbracket) {
            Lexeme lexeme;
            do {
                Position pos = sc.getCurPosition();
                Lexeme next = sc.next();
                Lexeme id = sc.next();

                if (next.type == T_int || next.type == T_int64 || next.type == T_id && id.type == T_id) {
                    sc.setCurPosition(pos);
                    tDataDescription();
                } else tPartOfBlock();

                lexeme = sc.nextWithBackup();
            } while (lexeme.type != T_rbracket);
        }
        scanAndCheck(T_rbracket);
//        semantic.goToParentLevel();
    }

    private void tPartOfBlock() {
        Lexeme lexeme = sc.nextWithBackup();

        switch (lexeme.type) {
            case T_for:
                tFor();
                break;
            case T_lbracket:
                tBlock();
                break;
            case T_semicolon:
                sc.next();
                break;
            case T_id:
                tAssignment();
                break;
            default:
                throw new AnalyzeError(sc, lexeme, T_id, T_for, T_lbracket, T_semicolon);
        }
    }

    private void tAssignment() {
        Position pos = sc.getCurPosition();
        Lexeme identifier = scanAndCheck(T_id);
        Lexeme skobka = sc.next();
        if (skobka.type == T_lsbracket) {
            sc.setCurPosition(pos);
            tArrayElement();
        }
        DataType type = sem.getVariableType(identifier);

        Lexeme eq = scanAndCheck(T_assign);
        tA0();
//        semantic.checkAssignment(eq, type, A1());
        scanAndCheck(T_semicolon);
    }

    private void tFor() {
        scanAndCheck(T_for);
        scanAndCheck(T_lparenthesis);
        tDataDescription();
        tA0();
        scanAndCheck(T_semicolon);
        tA0();
        scanAndCheck(T_rparenthesis);

        Lexeme block = sc.nextWithBackup();
        switch (block.type) {
            case T_lbracket:
                tBlock();
                break;
            default:
                tPartOfBlock();
        }
    }

    private void tArrayElement() {
        scanAndCheck(T_id);

        Lexeme sbracket = scanAndCheck(T_lsbracket);
        while (sbracket.type == T_lsbracket) {
            tA0();
            scanAndCheck(T_rsbracket);
            sbracket = scanAndCheck(T_lsbracket);
        }
    }

    private void tA0() {
        A1();
        Position pos = sc.getCurPosition();
        Lexeme or = sc.next();
        while (or.type == T_or) {
            A1();
            pos = sc.getCurPosition();
            or = sc.next();
        }

        sc.setCurPosition(pos);
    }

    private void A1() {
        A2();
        Position pos = sc.getCurPosition();
        Lexeme and = sc.next();
        while (and.type == T_and) {
            A2();
            pos = sc.getCurPosition();
            and = sc.next();
        }

        sc.setCurPosition(pos);
    }

    private void A2() {
        A3();
        Position pos = sc.getCurPosition();
        Lexeme comp = sc.next();
        while (comp.type == T_less || comp.type == T_leq || comp.type == T_more || comp.type == T_meq || comp.type == T_eqaul || comp.type == T_neq) {
            A3();
            pos = sc.getCurPosition();
            comp = sc.next();
        }

        sc.setCurPosition(pos);
    }

    private void A3() {
        A4();
        Position pos = sc.getCurPosition();
        Lexeme plus = sc.next();
        while (plus.type == T_plus || plus.type == T_minus) {
            A4();
            pos = sc.getCurPosition();
            plus = sc.next();
        }

        sc.setCurPosition(pos);
    }

    private void A4() {
        A5();
        Position pos = sc.getCurPosition();
        Lexeme mult = sc.next();
        while (mult.type == T_multiply || mult.type == T_division || mult.type == T_mod) {
            A5();
            pos = sc.getCurPosition();
            mult = sc.next();
        }

        sc.setCurPosition(pos);
    }

    private void A5() {
        Position pos = sc.getCurPosition();
        Lexeme mult = sc.next();
        while (mult.type == T_not) {
            pos = sc.getCurPosition();
            mult = sc.next();
        }
        sc.setCurPosition(pos);
        A6();
    }

    private void A6() {
        Position pos = sc.getCurPosition();
        Lexeme next = sc.next();
        switch (next.type) {
            case T_const10:
            case T_const16:
//                return semantic.getConstType(next);
                break;
            case T_lbracket:
                tA0();
                scanAndCheck(T_rbracket);
                break;
            case T_id:
                if (sc.nextWithBackup().type == T_lsbracket) {
                    sc.setCurPosition(pos);
                    tArrayElement();
                }
                break;
            default:
                throw new AnalyzeError(sc, next, T_const10, T_const16, T_id);
        }
    }


    private Lexeme scanAndCheck(Type type) {
        Lexeme lexeme = sc.next();
        if (lexeme.type != type) throw new AnalyzeError(sc, lexeme, type);
        return lexeme;
    }
}
