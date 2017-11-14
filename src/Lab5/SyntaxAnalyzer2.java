package Lab5;

import Lab3.Lexeme;
import Lab3.Position;
import Lab3.Scanner;
import Lab3.Type;
import javafx.geometry.Pos;
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

    public void tProgram() throws Exception {

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
            case T_int:
                sc.next();
                if (sc.nextWithBackup().type == T_main) {
                    sc.setCurPosition(pos);
                    tMain();
                } else {
                    sc.setCurPosition(pos);
                    tDataDescription();
                }
                break;
            case T_int64:
            case T_id:
                sc.setCurPosition(pos);
                tDataDescription();
                break;
            default:
                throw new AnalyzeError(sc, t, T_const, T_typedef, T_int, T_int64, T_id);
        }
    }

    private Lexeme tType() {
        Lexeme type = sc.next();
        switch (type.type) {
            case T_int:
            case T_int64:
            case T_id:
                return type;
            default:
                throw new AnalyzeError(sc, type, T_id);
        }
    }

    private void tDataDescription() {
        Lexeme t = tType();

        //Запись в дерево

        Lexeme lexeme;
        do {
            Lexeme id = sc.next();
            if (id.type != T_id)
                throw new AnalyzeError(sc, id, T_id);

            sem.addVariable(t, id);

            Position pos = sc.getCurPosition();
            Lexeme assignment = sc.next();
            if (assignment.type == T_assign) {
                DataType dataType = DataType.tArray;
                if (t.type == T_int) dataType = DataType.tInt;
                if (t.type == T_int64) dataType = DataType.tInt64;
                sem.checkAssignment(assignment, dataType, tA0());
            } else sc.setCurPosition(pos);

            lexeme = sc.next();
        } while (lexeme.type == T_colon);

        if (lexeme.type != T_semicolon)
            throw new AnalyzeError(sc, lexeme, T_semicolon);
    }

    private void tMain() {
        scanAndCheck(T_int);
        Lexeme main = scanAndCheck(T_main);
        sem.addMain(main);
        scanAndCheck(T_lparenthesis);
        scanAndCheck(T_rparenthesis);
        tBlock();
    }

    //Sem done?
    private void tTypedef() {
        scanAndCheck(T_typedef);
        Lexeme ancestor = tType();

        Lexeme id = sc.next();
        switch (id.type) {
            case T_id:
                break;
            default:
                throw new AnalyzeError(sc, id, T_id);
        }

        scanAndCheck(T_lsbracket);
        Lexeme number = sc.next();
        int size;
        switch (number.type) {
            case T_const10:
            case T_const16:
            case T_id:
                size = sem.getConstNumber(number);
                break;
            default:
                throw new AnalyzeError(sc, number, T_const10, T_const16);
        }
        scanAndCheck(T_rsbracket);

        sem.addType(ancestor, id, size);

        scanAndCheck(T_semicolon);
    }

    //Sem done?
    private void tConst() {
        scanAndCheck(T_const);
        tType();

        Lexeme id = sc.next();
        switch (id.type) {
            case T_id:
                break;
            default:
                throw new AnalyzeError(sc, id, T_id);
        }

        Lexeme number = sc.next();
        switch (number.type) {
            case T_const10:
            case T_const16:
                sem.addConst(sem.getConstType(number), id);
                break;
            default:
                throw new AnalyzeError(sc, number, T_const10, T_const16);
        }

        scanAndCheck(T_semicolon);
    }

    //Sem done?
    private void tBlock() {
        scanAndCheck(T_lbracket);
        sem.startBlock();
        if (sc.nextWithBackup().type != T_rbracket) {
            Lexeme lexeme;
            do {
                Position pos = sc.getCurPosition();
                Lexeme next = sc.next();
                Lexeme id = sc.next();

                sc.setCurPosition(pos);
                if (next.type == T_int || next.type == T_int64 || next.type == T_id && id.type == T_id) {
//                    sc.setCurPosition(pos);
                    tDataDescription();
                } else if (next.type == T_typedef) {
                    tTypedef();
                } else {
//                    sc.setCurPosition(pos);
                    tPartOfBlock();
                }

                lexeme = sc.nextWithBackup();
            } while (lexeme.type != T_rbracket);
        }
        scanAndCheck(T_rbracket);
        sem.goToParentLevel();
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
                tAssignment(false);
                break;
            default:
                throw new AnalyzeError(sc, lexeme, T_id, T_for, T_lbracket, T_semicolon);
        }
    }

    private DataType workWithIdOrArray() {
        Position pos = sc.getCurPosition();
        Lexeme identifier = scanAndCheck(T_id);
        sem.dropIfType(identifier);
        Lexeme skobka = sc.nextWithBackup();

        DataType dataType = null;
        if (skobka.type == T_lsbracket) {
            sc.setCurPosition(pos);
            dataType = tArrayElement();
        } else {
            sem.dropIfArray(identifier);
            dataType = sem.getVariableType(identifier);
        }

        return dataType;
    }

    private void tAssignment(boolean inFor) {
        DataType dataType = workWithIdOrArray();

        Lexeme eq = scanAndCheck(T_assign);
        sem.checkAssignment(eq, dataType, tA0());

        if (!inFor)
            scanAndCheck(T_semicolon);
    }

    private void tFor() {
        scanAndCheck(T_for);
        scanAndCheck(T_lparenthesis);
        tDataDescription();
        tA0();
        scanAndCheck(T_semicolon);
        tAssignment(true);
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

    private DataType tArrayElement() {
        Lexeme lex = scanAndCheck(T_id);
        Pair<DataType, Integer> arrInfo = sem.getArrayType(lex);

        Position pos = sc.getCurPosition();
        Lexeme sbracket = scanAndCheck(T_lsbracket);
        sc.setCurPosition(pos);
        int inc = 0;
        while (sbracket.type == T_lsbracket) {
            sc.next();
            inc++;
            tA0();
            scanAndCheck(T_rsbracket);
            sbracket = sc.nextWithBackup();
        }

        if (inc > arrInfo.getValue())
            throw new AnalyzeError(sc, lex, "Array type expected");
        else if (inc < arrInfo.getValue())
            throw new AnalyzeError(sc, lex, "Can't cast this type");

        return arrInfo.getKey();
    }

    private DataType tA0() {
        DataType type = A1();
        Position pos = sc.getCurPosition();
        Lexeme or = sc.next();
        while (or.type == T_or) {
            A1();
            type = DataType.tInt;
            pos = sc.getCurPosition();
            or = sc.next();
        }

        sc.setCurPosition(pos);
        return type;
    }

    private DataType A1() {
        DataType type = A2();
        Position pos = sc.getCurPosition();
        Lexeme and = sc.next();
        while (and.type == T_and) {
            A1();
            type = DataType.tInt;
            pos = sc.getCurPosition();
            and = sc.next();
        }

        sc.setCurPosition(pos);
        return type;
    }

    private DataType A2() {
        DataType type = A3();
        Position pos = sc.getCurPosition();
        Lexeme comp = sc.next();
        while (comp.type == T_less || comp.type == T_leq || comp.type == T_more || comp.type == T_meq || comp.type == T_eqaul || comp.type == T_neq) {
            A2();
            type = DataType.tInt;
            pos = sc.getCurPosition();
            comp = sc.next();
        }

        sc.setCurPosition(pos);
        return type;
    }

    private DataType A3() {
        DataType type = A4();
        Position pos = sc.getCurPosition();
        Lexeme plus = sc.next();
        while (plus.type == T_plus || plus.type == T_minus) {
            type = sem.cast(type, A3());
            pos = sc.getCurPosition();
            plus = sc.next();
        }

        sc.setCurPosition(pos);
        return type;
    }

    private DataType A4() {
        DataType type = A5();
        Position pos = sc.getCurPosition();
        Lexeme mult = sc.next();
        while (mult.type == T_multiply || mult.type == T_division || mult.type == T_mod) {
            type = sem.cast(type, A4());
            pos = sc.getCurPosition();
            mult = sc.next();
        }

        sc.setCurPosition(pos);
        return type;
    }

    private DataType A5() {
        DataType type = DataType.tInt;
        Position pos = sc.getCurPosition();
        Lexeme mult = sc.next();

        while (mult.type == T_not) {
            pos = sc.getCurPosition();
            mult = sc.next();
        }
        sc.setCurPosition(pos);
        return sem.cast(type, A6());
    }

    private DataType A6() {
        Position pos = sc.getCurPosition();
        Lexeme next = sc.next();
        switch (next.type) {
            case T_const10:
            case T_const16:
                return sem.getConstType(next);
            case T_lparenthesis:
                DataType type = tA0();
                scanAndCheck(T_rparenthesis);
                return type;
            case T_id:
                sc.setCurPosition(pos);
                return workWithIdOrArray();
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
