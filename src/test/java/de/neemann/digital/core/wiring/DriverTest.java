package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

import static de.neemann.digital.TestExecuter.HIGHZ;

/**
 * @author hneemann
 */
public class DriverTest extends TestCase {

    public void testDriver() throws Exception {
        ObservableValue a = new ObservableValue("a", 2);
        ObservableValue sel = new ObservableValue("sel", 1);

        Model model = new Model();
        Driver out = model.add(new Driver(new ElementAttributes().setBits(2)));
        out.setInputs(new ObservableValues(a, sel));

        TestExecuter sc = new TestExecuter(model).setInputs(a, sel).setOutputs(out.getOutputs());
        sc.check(0, 1, 0);
        sc.check(2, 1, 2);
        sc.check(2, 0, HIGHZ);
        sc.check(2, 1, 2);
    }

}