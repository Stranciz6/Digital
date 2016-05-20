package de.neemann.digital.core.basic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class NotTest extends TestCase {

    public void testNot() throws Exception {
        ObservableValue a = new ObservableValue("a", 2);

        Model model = new Model();
        Not out = model.add(new Not(new ElementAttributes().setBits(2)));
        out.setInputs(new ObservableValues(a));

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(out.getOutputs());
        sc.check(0, 3);
        sc.check(1, 2);
        sc.check(2, 1);
        sc.check(3, 0);
    }
}