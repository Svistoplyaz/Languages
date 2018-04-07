package semester8.Lab2;

import javafx.util.Pair;
import semester7.Lab3.Lexeme;
import semester7.Lab3.Position;
import semester7.Lab3.Scanner;
import semester7.Lab3.Type;
import semester7.Lab5.AnalyzeError;
import semester7.Lab5.DataType;

import java.io.FileReader;
import java.util.ArrayList;

import static semester7.Lab3.Type.*;

public class SyntaxAnalyzer3 {
    private Scanner sc;
    private Interpreter inter;

    public SyntaxAnalyzer3(FileReader in) {
        sc = new Scanner(in);
        inter = new Interpreter(sc);
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

            inter.addVariable(t, id);

            Position pos = sc.getCurPosition();
            Lexeme assignment = sc.next();
            if (assignment.type == T_assign) {
                DataType dataType = DataType.tArray;
                if (t.type == T_int) dataType = DataType.tInt;
                if (t.type == T_int64) dataType = DataType.tInt64;
                Pair<DataType, Long> pair = tA0();
                inter.checkAssignment(assignment, dataType, pair.getKey());

                inter.putValueInId(id, pair.getKey(), pair.getValue());
            } else sc.setCurPosition(pos);

            lexeme = sc.next();
        } while (lexeme.type == T_colon);

        if (lexeme.type != T_semicolon)
            throw new AnalyzeError(sc, lexeme, T_semicolon);
    }

    private void tMain() {
        scanAndCheck(T_int);
        Lexeme main = scanAndCheck(T_main);
        inter.addMain(main);
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
        long size;
        switch (number.type) {
            case T_const10:
            case T_const16:
            case T_id:
                size = inter.getConstNumber(number);
                break;
            default:
                throw new AnalyzeError(sc, number, T_const10, T_const16);
        }
        scanAndCheck(T_rsbracket);

        inter.addType(ancestor, id, (int) size);

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
                inter.addConst(inter.getConstType(number), id);
                break;
            default:
                throw new AnalyzeError(sc, number, T_const10, T_const16);
        }

        scanAndCheck(T_semicolon);
    }

    //Sem done?
    private void tBlock() {
        scanAndCheck(T_lbracket);
        inter.startBlock();
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
        inter.goToParentLevel();
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

    private Pair<DataType, Long> workWithIdOrArray() {
        Position pos = sc.getCurPosition();
        Lexeme identifier = scanAndCheck(T_id);
        inter.dropIfType(identifier);
        Lexeme skobka = sc.nextWithBackup();

        Pair<DataType, Long> pair;
        if (skobka.type == T_lsbracket) {
            sc.setCurPosition(pos);
            pair = tArrayElement();
        } else {
            inter.dropIfArray(identifier);
            pair = new Pair<>(inter.getVariableType(identifier), inter.getId(identifier));
        }

        return new Pair<>(pair.getKey(), pair.getValue());
    }

    private Pair<DataType, Long> tArrayElement() {
        Lexeme lex = scanAndCheck(T_id);
        Pair<DataType, Integer> arrInfo = inter.getArrayType(lex);

        Position pos = sc.getCurPosition();
        Lexeme sbracket = scanAndCheck(T_lsbracket);
        sc.setCurPosition(pos);
        int inc = 0;
        Pair<DataType, Long> pair;
        ArrayList<Integer> arr = new ArrayList<>();
        while (sbracket.type == T_lsbracket) {
            sc.next();
            inc++;
            pair = tA0();
            scanAndCheck(T_rsbracket);
            sbracket = sc.nextWithBackup();
            arr.add(pair.getValue().intValue());
        }

        if (inc > arrInfo.getValue())
            throw new AnalyzeError(sc, lex, "Array type expected");
        else if (inc < arrInfo.getValue())
            throw new AnalyzeError(sc, lex, "Can't cast this type");

        int[] a = new int[arrInfo.getValue()];
        for (int i = 0; i < a.length; i++) {
            a[i] = arr.get(i);
        }

        pair = new Pair<>(arrInfo.getKey(), inter.getElement(lex, a));

        return pair;
    }

    private Pair<Lexeme, Pair<DataType, Long>> workWithIdOrArrayAssign() {
        Position pos = sc.getCurPosition();
        Lexeme identifier = scanAndCheck(T_id);
        inter.dropIfType(identifier);
        Lexeme skobka = sc.nextWithBackup();

        Pair<DataType, Long> pair;
        if (skobka.type == T_lsbracket) {
            sc.setCurPosition(pos);
            pair = tArrayElementAssign();
        } else {
            inter.dropIfArray(identifier);
            pair = new Pair<>(inter.getVariableType(identifier), (long) -1);
        }

        return new Pair<>(identifier, new Pair<>(pair.getKey(), pair.getValue()));
    }

    private Pair<DataType, Long> tArrayElementAssign() {
        Lexeme lex = scanAndCheck(T_id);
        Pair<DataType, Integer> arrInfo = inter.getArrayType(lex);

        Position pos = sc.getCurPosition();
        Lexeme sbracket = scanAndCheck(T_lsbracket);
        sc.setCurPosition(pos);
        int inc = 0;
        Pair<DataType, Long> pair;
        ArrayList<Integer> arr = new ArrayList<>();
        while (sbracket.type == T_lsbracket) {
            sc.next();
            inc++;
            pair = tA0();
            scanAndCheck(T_rsbracket);
            sbracket = sc.nextWithBackup();
            arr.add(pair.getValue().intValue());
        }

        if (inc > arrInfo.getValue())
            throw new AnalyzeError(sc, lex, "Array type expected");
        else if (inc < arrInfo.getValue())
            throw new AnalyzeError(sc, lex, "Can't cast this type");

        int[] a = new int[arrInfo.getValue()];
        for (int i = 0; i < a.length; i++) {
            a[i] = arr.get(i);
        }

        pair = new Pair<>(arrInfo.getKey(), inter.getGreatIndex(lex, a));

        return pair;
    }

    private void tAssignment(boolean inFor) {
        Pair<Lexeme, Pair<DataType, Long>> leftPair = workWithIdOrArrayAssign();

        Lexeme eq = scanAndCheck(T_assign);
        Pair<DataType, Long> rightPair = tA0();
        inter.checkAssignment(eq, leftPair.getValue().getKey(), rightPair.getKey());

        //Вставить присваивание
        if (inter.interpret)
            inter.putValueIn(leftPair.getKey(), leftPair.getValue().getKey(), leftPair.getValue().getValue().intValue(), rightPair.getValue());

        if (!inFor)
            scanAndCheck(T_semicolon);
    }

    private void tFor() {
        boolean local = inter.interpret;

        Position check, inc, beg, end;
        scanAndCheck(T_for);
        scanAndCheck(T_lparenthesis);
        tDataDescription();
        check = sc.getCurPosition();
        Pair<DataType, Long> pair = tA0();
        scanAndCheck(T_semicolon);
        inc = sc.getCurPosition();
        inter.interpret = false;
        tAssignment(true);
//        inter.interpret = local;

        if(pair.getValue() != 0)
            inter.interpret = local;
        else
            inter.interpret = false;

        scanAndCheck(T_rparenthesis);
        beg = sc.getCurPosition();

        Node cur = inter.getCurrent();

        do {

            sc.setCurPosition(beg);
            Lexeme block = sc.nextWithBackup();

            switch (block.type) {
                case T_lbracket:
                    inter.setCurrent(cur);
                    cur.left = null;
                    tBlock();
                    break;
                default:
                    tPartOfBlock();
            }
            end = sc.getCurPosition();

            if(inter.interpret) {
                //увеличить
                sc.setCurPosition(inc);
                tAssignment(true);

                //проверить
                sc.setCurPosition(check);
                if (!inter.forCheck(tA0())) {
                    sc.setCurPosition(end);
                    break;
                }
            }else
                break;
        }while(true);
        inter.interpret = local;
    }

    private Pair<DataType, Long> tA0() {
        Pair<DataType, Long> pair = A1();
        DataType type = pair.getKey();
        long ans = pair.getValue();

        Position pos = sc.getCurPosition();
        Lexeme or = sc.next();
        while (or.type == T_or) {
            pair = A1();
            ans = inter.calculateA0(ans, pair.getValue());

            type = DataType.tInt;
            pos = sc.getCurPosition();
            or = sc.next();
        }

        sc.setCurPosition(pos);
        return new Pair<>(type, ans);
    }

    private Pair<DataType, Long> A1() {
        Pair<DataType, Long> pair = A2();
        DataType type = pair.getKey();
        long ans = pair.getValue();

        Position pos = sc.getCurPosition();
        Lexeme and = sc.next();
        while (and.type == T_and) {
            pair = A2();
            ans = inter.calculateA1(ans, pair.getValue());

            type = DataType.tInt;
            pos = sc.getCurPosition();
            and = sc.next();
        }

        sc.setCurPosition(pos);
        return new Pair<>(type, ans);
    }

    private Pair<DataType, Long> A2() {
        Pair<DataType, Long> pair = A3();
        DataType type = pair.getKey();
        long ans = pair.getValue();

        Position pos = sc.getCurPosition();
        Lexeme comp = sc.next();
        while (comp.type == T_less || comp.type == T_leq || comp.type == T_more || comp.type == T_meq || comp.type == T_eqaul || comp.type == T_neq) {
            pair = A3();
            ans = inter.calculateA2(ans, pair.getValue(), comp.type);

            type = DataType.tInt;
            pos = sc.getCurPosition();
            comp = sc.next();
        }

        sc.setCurPosition(pos);
        return new Pair<>(type, ans);
    }

    private Pair<DataType, Long> A3() {
        Pair<DataType, Long> pair = A4();
        DataType type = pair.getKey();
        long ans = pair.getValue();

        Position pos = sc.getCurPosition();
        Lexeme plus = sc.next();
        while (plus.type == T_plus || plus.type == T_minus) {
            pair = A4();
            type = inter.cast(type, pair.getKey());
            ans = inter.calculateA3(ans, pair.getValue(), plus.type);
            if (type == DataType.tInt)
                ans = (int) ans;

            pos = sc.getCurPosition();
            plus = sc.next();
        }

        sc.setCurPosition(pos);
        return new Pair<>(type, ans);
    }

    private Pair<DataType, Long> A4() {
        Pair<DataType, Long> pair = A5();
        DataType type = pair.getKey();
        long ans = pair.getValue();

        Position pos = sc.getCurPosition();
        Lexeme mult = sc.next();
        while (mult.type == T_multiply || mult.type == T_division || mult.type == T_mod) {
            pair = A5();
            type = inter.cast(type, pair.getKey());
            ans = inter.calculateA4(ans, pair.getValue(), mult.type);
            if (type == DataType.tInt)
                ans = (int) ans;

            pos = sc.getCurPosition();
            mult = sc.next();
        }

        sc.setCurPosition(pos);
        return new Pair<>(type, ans);
    }

    private Pair<DataType, Long> A5() {
        Position pos = sc.getCurPosition();
        Lexeme mult = sc.next();
        int inc = 0;

        while (mult.type == T_not) {
            pos = sc.getCurPosition();
            mult = sc.next();
            inc++;
        }
        sc.setCurPosition(pos);

        Pair<DataType, Long> pair = A6();
        long ans;
        if (inc != 0) {
            ans = inter.calculateA5(pair.getValue(), inc);
            return new Pair<>(DataType.tInt, ans);
        }
        else {
            ans = pair.getValue();
            return new Pair<>(pair.getKey(), ans);
        }
    }

    private Pair<DataType, Long> A6() {
        Position pos = sc.getCurPosition();
        Lexeme next = sc.next();
        DataType cur;
        switch (next.type) {
            case T_const10:
            case T_const16:
                cur = inter.getConstType(next);
                if (cur == DataType.tInt)
                    return new Pair<>(cur, (long) (int) (inter.getConstNumber(next)));
                else
                    return new Pair<>(cur, inter.getConstNumber(next));
            case T_lparenthesis:
                Pair<DataType, Long> pair = tA0();
                scanAndCheck(T_rparenthesis);
                return pair;
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
