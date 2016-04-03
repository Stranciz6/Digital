package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class Splitter2Test extends TestCase {

    public void testBits() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(AttributeKey.InputSplit, "4")
                .set(AttributeKey.OutputSplit, "1,1,1,1"));

        splitter.setInputs(a);
        assertEquals(4, a.observerCount());

        ObservableValue[] outputs = splitter.getOutputs();
        assertEquals(4, outputs.length);

        TestExecuter sc = new TestExecuter().setInputs(a).setOutputsOf(splitter);
        sc.check(0, 0, 0, 0, 0);
        sc.check(1, 1, 0, 0, 0);
        sc.check(2, 0, 1, 0, 0);
        sc.check(4, 0, 0, 1, 0);
        sc.check(8, 0, 0, 0, 1);
        sc.check(15, 1, 1, 1, 1);
    }

    public void testMoreBits() throws Exception {
        ObservableValue a = new ObservableValue("a", 16);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(AttributeKey.InputSplit, "16")
                .set(AttributeKey.OutputSplit, "4,4,4,4"));

        splitter.setInputs(a);
        assertEquals(4, a.observerCount());

        ObservableValue[] outputs = splitter.getOutputs();
        assertEquals(4, outputs.length);

        TestExecuter sc = new TestExecuter().setInputs(a).setOutputsOf(splitter);
        sc.check(0x0000, 0x0, 0x0, 0x0, 0x0);
        sc.check(0x1000, 0x0, 0x0, 0x0, 0x1);
        sc.check(0x0100, 0x0, 0x0, 0x1, 0x0);
        sc.check(0x0010, 0x0, 0x1, 0x0, 0x0);
        sc.check(0x0001, 0x1, 0x0, 0x0, 0x0);
        sc.check(0xa000, 0x0, 0x0, 0x0, 0xa);
        sc.check(0x0b00, 0x0, 0x0, 0xb, 0x0);
        sc.check(0x00c0, 0x0, 0xc, 0x0, 0x0);
        sc.check(0x000d, 0xd, 0x0, 0x0, 0x0);
    }

    public void testMix() throws Exception {
        ObservableValue a = new ObservableValue("a", 8);
        ObservableValue b = new ObservableValue("b", 8);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(AttributeKey.InputSplit, "8,8")
                .set(AttributeKey.OutputSplit, "4,4,4,4"));

        splitter.setInputs(a, b);
        assertEquals(2, a.observerCount());
        assertEquals(2, b.observerCount());

        ObservableValue[] outputs = splitter.getOutputs();
        assertEquals(4, outputs.length);

        TestExecuter sc = new TestExecuter().setInputs(a, b).setOutputsOf(splitter);
        sc.check(0x00, 0x00, 0x0, 0x0, 0x0, 0x0);
        sc.check(0x10, 0x00, 0x0, 0x1, 0x0, 0x0);
        sc.check(0x01, 0x00, 0x1, 0x0, 0x0, 0x0);
        sc.check(0x00, 0x10, 0x0, 0x0, 0x0, 0x1);
        sc.check(0x00, 0x01, 0x0, 0x0, 0x1, 0x0);
        sc.check(0xf0, 0x00, 0x0, 0xf, 0x0, 0x0);
        sc.check(0x0f, 0x00, 0xf, 0x0, 0x0, 0x0);
        sc.check(0x00, 0xf0, 0x0, 0x0, 0x0, 0xf);
        sc.check(0x00, 0x0f, 0x0, 0x0, 0xf, 0x0);
        sc.check(0xff, 0xff, 0xf, 0xf, 0xf, 0xf);
    }


}