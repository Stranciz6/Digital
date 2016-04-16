package de.neemann.digital.core.memory;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import static de.neemann.digital.TestExecuter.HIGHZ;

/**
 * @author hneemann
 */
public class RAMDualPortTest extends TestCase {

    public void testRAM() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue d = new ObservableValue("d", 4);
        ObservableValue str = new ObservableValue("str", 1);
        ObservableValue clk = new ObservableValue("clk", 1);
        ObservableValue ld = new ObservableValue("ld", 1);

        Model model = new Model();
        RAMDualPort out = model.add(new RAMDualPort(
                new ElementAttributes()
                        .set(Keys.AddrBits, 4)
                        .setBits(4)));
        out.setInputs(a, d, str, clk, ld);

        TestExecuter sc = new TestExecuter(model).setInputs(a, d, str, clk, ld).setOutputs(out.getOutputs());
        //       A  D  ST C  LD
        sc.check(0, 0, 0, 0, 0, HIGHZ);  // def
        sc.check(0, 5, 1, 1, 0, HIGHZ);  // st  0->5
        sc.check(0, 0, 0, 0, 0, HIGHZ);  // def
        sc.check(1, 9, 1, 1, 0, HIGHZ);  // st  1->9
        sc.check(0, 0, 0, 0, 1, 5);      // rd  5
        sc.check(1, 0, 0, 0, 1, 9);      // rd  5
    }


}