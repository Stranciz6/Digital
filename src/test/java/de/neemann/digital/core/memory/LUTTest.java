package de.neemann.digital.core.memory;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class LUTTest extends TestCase {

    public void testLut() throws Exception {
        ObservableValue a = new ObservableValue("a", 1);
        ObservableValue b = new ObservableValue("b", 1);
        ObservableValue c = new ObservableValue("c", 1);

        Model model = new Model();
        DataField data = new DataField(8, 8);
        data.setData(3, 1);
        data.setData(7, 1);
        LookUpTable out = model.add(new LookUpTable(
                new ElementAttributes()
                        .setBits(1)
                        .set(Keys.Data, data)));
        out.setInputs(a, b);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b, c).setOutputs(out.getOutputs());
        sc.check(0, 0, 0, 0);
        sc.check(1, 0, 0, 0);
        sc.check(0, 1, 0, 0);
        sc.check(1, 1, 0, 1);
        sc.check(0, 0, 1, 0);
        sc.check(1, 0, 1, 0);
        sc.check(0, 1, 1, 0);
        sc.check(1, 1, 1, 1);
    }
}