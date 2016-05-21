package de.neemann.digital.draw.builder;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * @author hneemann
 */
public class BuilderTest extends TestCase {

    public void testBuilderCombinatorial() throws Exception {

        Variable a = new Variable("a");
        Variable b = new Variable("b");

        // xor
        Expression y = and(or(a, b), not(and(a, b)));

        ElementLibrary library = new ElementLibrary();
        Circuit circuit = new CircuitBuilder(new ShapeFactory(library))
                .addExpression("y", y)
                .createCircuit();

        ModelDescription m = new ModelDescription(circuit, library);

        TestExecuter te = new TestExecuter(m.createModel(false)).setUp(m);
        te.check(0, 0, 0);
        te.check(0, 1, 1);
        te.check(1, 0, 1);
        te.check(1, 1, 0);
    }

    public void testBuilderSequential() throws Exception {
        Variable y0 = new Variable("Y_0");
        Variable y1 = new Variable("Y_1");

        // counter
        Expression y0s = not(y0);
        Expression y1s = or(and(not(y0), y1), and(y0, not(y1)));

        ElementLibrary library = new ElementLibrary();
        Circuit circuit = new CircuitBuilder(new ShapeFactory(library))
                .addState("Y_0", y0s)
                .addState("Y_1", y1s)
                .addExpression("Y_0", y0)
                .addExpression("Y_1", y1)
                .createCircuit();

        ModelDescription m = new ModelDescription(circuit, library);
        TestExecuter te = new TestExecuter(m.createModel(false)).setUp(m);
        te.check(0, 0);
        te.checkC(1, 0);
        te.checkC(0, 1);
        te.checkC(1, 1);
        te.checkC(0, 0);
    }

}