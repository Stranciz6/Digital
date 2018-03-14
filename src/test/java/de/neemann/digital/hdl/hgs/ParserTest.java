/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.hdl.hgs.function.FirstClassFunction;
import de.neemann.digital.hdl.hgs.function.FuncAdapter;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParserTest extends TestCase {

    public void testParseExpressionArith() throws IOException, ParserException, HGSEvalException {
        assertEquals(3L, new Parser("1+2").parseExpression().value(new Context()));
        assertEquals("HelloWorld", new Parser("\"Hello\"+\"World\"").parseExpression().value(new Context()));
        assertEquals(3L, new Parser("5-2").parseExpression().value(new Context()));
        assertEquals(10L, new Parser("5*2").parseExpression().value(new Context()));
        assertEquals(2L, new Parser("6/3").parseExpression().value(new Context()));
        assertEquals(8L, new Parser("1<<3").parseExpression().value(new Context()));
        assertEquals(2L, new Parser("16>>3").parseExpression().value(new Context()));
        assertEquals(4L, new Parser("9%5").parseExpression().value(new Context()));

        assertEquals(-5L, new Parser("-5").parseExpression().value(new Context()));
        assertEquals(6L, new Parser("2*(1+2)").parseExpression().value(new Context()));

        try {
            new Parser("1+").parseExpression().value(new Context());
            fail();
        } catch (ParserException e) {
            assertTrue(true);
        }

        assertEquals("Hallo4", new Parser("\"Hallo\" + (2*2)").parseExpression().value(new Context()));
        assertEquals("Hallo_true", new Parser("\"Hallo_\" + (1<2)").parseExpression().value(new Context()));
    }

    public void testParseExpressionCompare() throws IOException, ParserException, HGSEvalException {
        assertEquals(true, new Parser("5=5").parseExpression().value(new Context()));
        assertEquals(true, new Parser("\"Hello\"=\"Hello\"").parseExpression().value(new Context()));
        assertEquals(false, new Parser("\"Hello\"=\"World\"").parseExpression().value(new Context()));
        assertEquals(false, new Parser("5!=5").parseExpression().value(new Context()));
        assertEquals(false, new Parser("\"Hello\"!=\"Hello\"").parseExpression().value(new Context()));
        assertEquals(true, new Parser("\"Hello\"!=\"World\"").parseExpression().value(new Context()));

        assertEquals(false, new Parser("5<5").parseExpression().value(new Context()));
        assertEquals(true, new Parser("4<5").parseExpression().value(new Context()));
        assertEquals(false, new Parser("5>5").parseExpression().value(new Context()));
        assertEquals(false, new Parser("4>5").parseExpression().value(new Context()));
    }


    public void testParseExpressionBool() throws IOException, ParserException, HGSEvalException {
        assertEquals(3L, new Parser("1|2").parseExpression().value(new Context()));
        assertEquals(true, new Parser("a|b").parseExpression()
                .value(new Context()
                        .setVar("a", true)
                        .setVar("b", false)));
        assertEquals(0L, new Parser("1&2").parseExpression().value(new Context()));
        assertEquals(false, new Parser("a&b").parseExpression()
                .value(new Context()
                        .setVar("a", true)
                        .setVar("b", false)));
        assertEquals(3L, new Parser("1^2").parseExpression()
                .value(new Context()));
        assertEquals(true, new Parser("a^b").parseExpression()
                .value(new Context()
                        .setVar("a", true)
                        .setVar("b", false)));
        assertEquals(-2L, new Parser("~1").parseExpression().value(new Context()));
        assertEquals(false, new Parser("~a").parseExpression()
                .value(new Context()
                        .setVar("a", true)));

        assertEquals("true", exec("<? if (1) print(\"true\"); ?>").toString());
        assertEquals("", exec("<? if (0) print(\"true\"); ?>").toString());
        assertEquals("true", exec("<? if (1=1) print(\"true\"); ?>").toString());
        assertEquals("", exec("<? if (0=1) print(\"true\"); ?>").toString());
    }

    private Context exec(String code) throws IOException, ParserException, HGSEvalException {
        return exec(code, new Context());
    }

    private Context exec(String code, Context c) throws IOException, ParserException, HGSEvalException {
        new Parser(code).parse().execute(c);
        return c;
    }

    public void testParseTemplateSimple() throws IOException, ParserException, HGSEvalException {
        assertEquals("Hello World!", exec("Hello World!", new Context()).toString());
    }

    private Context exec(Statement s) throws HGSEvalException {
        return exec(s, new Context());
    }

    private Context exec(Statement s, Context c) throws HGSEvalException {
        s.execute(c);
        return c;
    }

    public void testParseTemplateTextOnly() throws IOException, ParserException, HGSEvalException {
        assertEquals("Hello World!", exec("Hello World!").toString());
        assertEquals("Hello < < World!", exec("Hello < < World!").toString());
    }

    public void testParseTemplateVariable() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? =a ?> World!", new Context().setVar("a", "My"));
        assertEquals("Hello My World!", c.toString());
    }

    public void testParseTemplateVariableEscape() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? ='a a' ?> World!", new Context().setVar("a a", "My"));
        assertEquals("Hello My World!", c.toString());
    }

    public void testParseTemplateCodeOnly() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? =a ?>", new Context().setVar("a", "My"));
        assertEquals("My", c.toString());
    }

    public void testParseTemplatePrint() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? print(\"My\"); ?> World!");
        assertEquals("Hello My World!", c.toString());
    }

    public void testParseTemplatePrintf() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? printf(\"-%d-%d-\", 4, 5); ?> World!");
        assertEquals("Hello -4-5- World!", c.toString());
    }

    public void testParseTemplateFor() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? for (i=0;i<10;i++) print(i); ?> World!");
        assertEquals("Hello 0123456789 World!", c.toString());
    }

    public void testParseTemplateForStatements() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? for (i=0;i<10;i++) { print(i, 9-i); } ?>");
        assertEquals("09182736455463728190", c.toString());
    }

    public void testParseTemplateForNested() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? for (i=0;i<10;i++) { ?>n<? } ?> World!");
        assertEquals("Hello nnnnnnnnnn World!", c.toString());
    }

    public void testParseTemplateForNested2() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? for (i=0;i<3;i++) { ?>(<? for(j=0;j<2;j++) { ?>:<? } ?>)<? } ?> World!");
        assertEquals("Hello (::)(::)(::) World!", c.toString());
    }

    public void testParseTemplateForNested3() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? for (i=1;i<4;i++) { ?>(<? for(j=1;j<3;j++) { print(i*j); } ?>)<? } ?> World!");
        assertEquals("Hello (12)(24)(36) World!", c.toString());
    }


    public void testParseTemplateElementAttibutes() throws IOException, ParserException, HGSEvalException {
        ElementAttributes attr = new ElementAttributes().set(Keys.BITS, 5);
        Context c = exec("bits=<?=elem.Bits?>;", new Context().setVar("elem", attr));
        assertEquals("bits=5;", c.toString());
    }

    public void testParseTemplateDataField() throws IOException, ParserException, HGSEvalException {
        DataField d = new DataField(5)
                .setData(0, 1)
                .setData(1, 7)
                .setData(2, 4)
                .setData(3, 8)
                .setData(4, 2);
        Context c = exec("(<? for(i=0;i<sizeOf(d);i++) { if (i>0) print(\"-\"); print(d[i]);} ?>)",
                new Context().setVar("d", d));
        assertEquals("(1-7-4-8-2)", c.toString());
    }

    public void testParseTemplateFormat() throws IOException, ParserException, HGSEvalException {
        ElementAttributes attr = new ElementAttributes().set(Keys.BITS, 17);
        Context c = new Context().setVar("elem", attr);
        exec("<? a=format(\"hex=%x;\",elem.Bits); print(a);?>", c);
        assertEquals("hex=11;", c.toString());
    }

    public void testParseTemplateArray() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? a=newList(); a[0]=1; a[1]=7; print(a[1], \",\" ,sizeOf(a)); ?>;");
        assertEquals("7,2;", c.toString());
        Object lo = c.getVar("a");
        assertTrue(lo instanceof List);
        List l = (List) lo;
        assertEquals(2, l.size());
        assertEquals(1L, l.get(0));
        assertEquals(7L, l.get(1));
    }

    public void testParseTemplateMap() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? m=newMap(); m.test=newMap(); m.test.val=7; print(m.test.val); ?>;");
        assertEquals("7;", c.toString());
        Object mo = c.getVar("m");
        assertTrue(mo instanceof Map);
        mo = ((Map) mo).get("test");
        assertTrue(mo instanceof Map);
        assertEquals(7L, ((Map) mo).get("val"));
    }

    public void testParseTemplateMapError() throws IOException, ParserException {
        try {
            exec("<? m=1; m.test=2; ?>;");
            fail();
        } catch (HGSEvalException e) {
            assertTrue(true);
        }
    }

    public void testParseTemplateArrayError() throws IOException, ParserException {
        try {
            exec("<? m=1; m[0]=2; ?>;");
            fail();
        } catch (HGSEvalException e) {
            assertTrue(true);
        }
    }

    public void testParseTemplateIsSet() throws IOException, ParserException, HGSEvalException {
        Statement t = new Parser("<? if (isPresent(m)) print(m); else print(\"false\"); ?>;").parse();
        assertEquals("false;", exec(t).toString());
        assertEquals("4;", exec(t, new Context().setVar("m", 4)).toString());
    }

    public void testComment() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? // comment\nprint(\"false\"); // zzz\n ?>;");
        assertEquals("false;", c.toString());
    }

    public void testAddFunction() throws IOException, ParserException, HGSEvalException {
        Statement s = new Parser("a : in <?=type(elem.Bits)?>;").parse();
        Context funcs = new Context().setVar("type", new FuncAdapter(1) {
            @Override
            protected Object f(Object... args) throws HGSEvalException {
                int n = Value.toInt(args[0]);
                if (n == 1)
                    return "std_logic";
                else
                    return "std_logic_vector(" + (n - 1) + " downto 0)";
            }
        });
        assertEquals("a : in std_logic;",
                exec(s, new Context(funcs)
                        .setVar("elem", new ElementAttributes())).toString());
        assertEquals("a : in std_logic_vector(5 downto 0);",
                exec(s, new Context(funcs)
                        .setVar("elem", new ElementAttributes().setBits(6))).toString());
    }

    int flag = 0;

    public void testFunctionAsStatement() throws IOException, ParserException, HGSEvalException {
        flag = 0;
        Statement s = new Parser("a : in <? type(7); ?>;").parse();

        Context c = new Context().addFunc("type", new FuncAdapter(1) {
            @Override
            protected Object f(Object... args) throws HGSEvalException {
                flag = Value.toInt(args[0]);
                return null;
            }
        });
        assertEquals("a : in ;", exec(s, c).toString());
        assertEquals(7, flag);
    }

    public void testStatic() throws IOException, ParserException {
        Parser p = new Parser("generic a; <? @gen[0]=\"a\"; ?>");
        final ArrayList<Object> generics = new ArrayList<>();
        p.getStaticContext().setVar("gen", generics);
        p.parse();

        assertEquals(1, generics.size());
        assertEquals("a", generics.get(0));
    }

    public void testFirstClassFunctionStatic() throws IOException, ParserException, HGSEvalException {
        Parser p = new Parser("<? @f=func(a){return=a*a+2;};  print(f(4));?>");
        p.parse();
        Object fObj = p.getStaticContext().getVar("f");
        assertTrue(fObj instanceof FirstClassFunction);
        FirstClassFunction f = (FirstClassFunction) fObj;
        assertEquals(11L, f.f(3));
    }

    public void testFirstClassFunction() throws IOException, ParserException, HGSEvalException {
        assertEquals("18", exec("<? f=func(a){return=a*a+2;};  print(f(4));?>").toString());
        assertEquals("5", exec("<? f=func(a,b){return=a+2*b;};  print(f(1,2));?>").toString());
        assertEquals("13", exec("<? f=func(a,b){return=a+2*b;};  print(f(1,a*2));?>",
                new Context().setVar("a", 3)).toString());

        assertEquals("18", exec("<? m=newMap(); m.f=func(a){return=newMap(); return.v=a*a+2;};  print(m.f(4).v);?>").toString());

    }

    public void testFirstClassFunctionOutput() throws IOException, ParserException, HGSEvalException {
        assertEquals("testtext12testtext15",
                exec("<? f=func(a){  ?>testtext<? print(a*3); return=output; };  print(f(4),f(5));?>").toString());
    }

}