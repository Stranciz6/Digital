package de.neemann.digital.core.flipflops;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class FlipflopT_Test extends TestCase {
    public void testFlipFlop() throws Exception {
        ObservableValue c = new ObservableValue("c", 1);

        Model model = new Model();
        FlipflopT out = model.add(new FlipflopT(new ElementAttributes().setBits(1)));
        out.setInputs(new ObservableValues(c));

        TestExecuter sc = new TestExecuter(model).setInputs(c).setOutputs(out.getOutputs());
        //       C  Q  ~Q
        sc.check(0, 0, 1);
        sc.check(1, 1, 0);
        sc.check(1, 1, 0);
        sc.check(0, 1, 0);
        sc.check(0, 1, 0);
        sc.check(1, 0, 1);
        sc.check(0, 0, 1);
    }
}