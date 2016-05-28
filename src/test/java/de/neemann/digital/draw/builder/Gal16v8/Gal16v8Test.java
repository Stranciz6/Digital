package de.neemann.digital.draw.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * @author hneemann
 */
public class Gal16v8Test extends TestCase {

    // stepper control
    public void testWriteTo() throws Exception {
        Variable D = new Variable("D");

        Variable Q0 = new Variable("Q0");
        Variable Q1 = new Variable("Q1");
        Variable Q2 = new Variable("Q2");

        //Q0.d = !Q0;
        Expression Q0d = not(Q0);
        //Q1.d = !D & !Q1 & Q0 # !D & Q1 & !Q0 # D & !Q1 & !Q0 # D & Q1 & Q0;
        Expression Q1d = or(and(not(D), not(Q1), Q0), and(not(D), Q1, not(Q0)), and(D, not(Q1), not(Q0)), and(D, Q1, Q0));
        //Q2.d = !D & !Q2 & Q1 & Q0 #
        //       !D & Q2 & !Q1 #
        //       Q2 & Q1 & !Q0 #
        //       D & !Q2 & !Q1 & !Q0 #
        //       D & Q2 & Q0;
        Expression Q2d = or(
                and(not(D), not(Q2), Q1, Q0),
                and(not(D), Q2, not(Q1)),
                and(Q2, Q1, not(Q0)),
                and(D, not(Q2), not(Q1), not(Q0)),
                and(D, Q2, Q0));

        //P0 = !Q2 & !Q1 # Q2 & Q1 & Q0;
        Expression P0 = or(
                and(not(Q2), not(Q1)),
                and(Q2, Q1, Q0));
        //P1 = !Q2 & Q0 # !Q2 & Q1;
        Expression P1 = or(
                and(not(Q2), Q0),
                and(not(Q2), Q1));
        //P2 = !Q2 & Q1 & Q0 # Q2 & !Q1;
        Expression P2 = or(
                and(not(Q2), Q1, Q0),
                and(Q2, not(Q1)));
        //P3 = Q2 & Q0 # Q2 & Q1;
        Expression P3 = or(
                and(Q2, Q0),
                and(Q2, Q1));


        Gal16v8 gal = new Gal16v8()
                .setPin("D", 2)
                .setPin("Q0", 16)
                .setPin("Q1", 17)
                .setPin("Q2", 18)
                .setPin("P0", 12)
                .setPin("P1", 13)
                .setPin("P2", 14)
                .setPin("P3", 15);

        gal.getBuilder()
                .addState("Q0", Q0d)
                .addState("Q1", Q1d)
                .addState("Q2", Q2d)
                .addExpression("P0", P0)
                .addExpression("P1", P1)
                .addExpression("P2", P2)
                .addExpression("P3", P3);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gal.writeTo(baos);

        assertEquals("\u0002Digital assembler*\r\n" +
                "QF2194*\r\n" +
                "G0*\r\n" +
                "F0*\r\n" +
                "L256 10111110110111011111111111111111*\r\n" +
                "L288 10111101111011111111111111111111*\r\n" +
                "L320 01111110111011101111111111111111*\r\n" +
                "L352 01111101111111011111111111111111*\r\n" +
                "L384 11111101110111101111111111111111*\r\n" +
                "L512 10111111111011011111111111111111*\r\n" +
                "L544 10111111110111101111111111111111*\r\n" +
                "L576 01111111111011101111111111111111*\r\n" +
                "L608 01111111110111011111111111111111*\r\n" +
                "L768 11111111111111101111111111111111*\r\n" +
                "L1024 11111111111111111111111111111111*\r\n" +
                "L1056 11111101111111011111111111111111*\r\n" +
                "L1088 11111101110111111111111111111111*\r\n" +
                "L1280 11111111111111111111111111111111*\r\n" +
                "L1312 11111110110111011111111111111111*\r\n" +
                "L1344 11111101111011111111111111111111*\r\n" +
                "L1536 11111111111111111111111111111111*\r\n" +
                "L1568 11111110111111011111111111111111*\r\n" +
                "L1600 11111110110111111111111111111111*\r\n" +
                "L1792 11111111111111111111111111111111*\r\n" +
                "L1824 11111101110111011111111111111111*\r\n" +
                "L1856 11111110111011111111111111111111*\r\n" +
                "L2112 00000000100011111111111111111111*\r\n" +
                "L2144 11111111111111111111111111111111*\r\n" +
                "L2176 111111111111111101*\r\n" +
                "C56F7*\r\n" +
                "\u0003C4AA", baos.toString());

    }

}