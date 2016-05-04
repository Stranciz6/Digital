package de.neemann.digital.analyse.expression;

import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 * @author hneemann
 */
public class OperationTest extends TestCase {

    public void testOr() throws Exception {
        Variable a = v("a");
        Variable b = v("b");
        Variable c = v("c");

        Expression i = or(a);
        assertTrue(i instanceof Variable);
        i = or(a, b);
        assertTrue(i instanceof Operation.Or);
        assertEquals(2, ((Operation.Or) i).getExpressions().size());
        i = or(c, i);
        assertEquals(3, ((Operation.Or) i).getExpressions().size());

        i = or(and(a, b), c);
        assertTrue(i instanceof Operation.Or);
        assertEquals(2, ((Operation.Or) i).getExpressions().size());

        i = or(not(a));
        assertTrue(i instanceof Not);
    }

    public void testAnd() throws Exception {
        Variable a = v("a");
        Variable b = v("b");
        Variable c = v("c");

        assertTrue(and(a) instanceof Variable);
        Expression i = and(a, b);
        assertTrue(i instanceof Operation.And);
        assertEquals(2, ((Operation.And) i).getExpressions().size());
        i = and(c, i);
        assertEquals(3, ((Operation.And) i).getExpressions().size());

        i = and(or(a, b), c);
        assertTrue(i instanceof Operation.And);
        assertEquals(2, ((Operation.And) i).getExpressions().size());

        i = and(not(a));
        assertTrue(i instanceof Not);
    }
}