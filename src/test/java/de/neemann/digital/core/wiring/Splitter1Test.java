package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class Splitter1Test extends TestCase {

    public void testBits() throws Exception {
        ObservableValue a = new ObservableValue("a", 1);
        ObservableValue b = new ObservableValue("b", 1);
        ObservableValue c = new ObservableValue("c", 1);
        ObservableValue d = new ObservableValue("d", 1);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "1,1,1,1")
                .set(Keys.OUTPUT_SPLIT, "4"));

        splitter.setInputs(a, b, c, d);
        assertEquals(1, a.observerCount());
        assertEquals(1, b.observerCount());
        assertEquals(1, c.observerCount());
        assertEquals(1, d.observerCount());

        ObservableValue[] outputs = splitter.getOutputs();
        assertEquals(1, outputs.length);

        TestExecuter sc = new TestExecuter().setInputs(a, b, c, d).setOutputsOf(splitter);
        sc.check(0, 0, 0, 0, 0);
        sc.check(1, 0, 0, 0, 1);
        sc.check(0, 1, 0, 0, 2);
        sc.check(0, 0, 1, 0, 4);
        sc.check(0, 0, 0, 1, 8);
        sc.check(1, 1, 0, 0, 3);
        sc.check(1, 1, 1, 0, 7);
        sc.check(1, 1, 1, 1, 15);
        sc.check(0, 0, 1, 1, 12);
        sc.check(0, 1, 1, 1, 14);
    }

    public void testMoreBits() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);
        ObservableValue c = new ObservableValue("c", 4);
        ObservableValue d = new ObservableValue("d", 4);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "4,4,4,4")
                .set(Keys.OUTPUT_SPLIT, "16"));

        splitter.setInputs(a, b, c, d);
        assertEquals(1, a.observerCount());
        assertEquals(1, b.observerCount());
        assertEquals(1, c.observerCount());
        assertEquals(1, d.observerCount());

        ObservableValue[] outputs = splitter.getOutputs();
        assertEquals(1, outputs.length);

        TestExecuter sc = new TestExecuter().setInputs(d, c, b, a).setOutputsOf(splitter);
        sc.check(0, 0, 0, 0, 0x0000);
        sc.check(0, 0, 0, 1, 0x0001);
        sc.check(0, 0, 1, 0, 0x0010);
        sc.check(0, 1, 0, 0, 0x0100);
        sc.check(1, 0, 0, 0, 0x1000);
        sc.check(0, 0, 0, 15, 0x000F);
        sc.check(0, 0, 15, 0, 0x00F0);
        sc.check(0, 15, 0, 0, 0x0F00);
        sc.check(15, 0, 0, 0, 0xF000);
    }

    public void testMoreBits2() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);
        ObservableValue c = new ObservableValue("c", 4);
        ObservableValue d = new ObservableValue("d", 4);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "4,4,4,4")
                .set(Keys.OUTPUT_SPLIT, "8,8"));

        splitter.setInputs(a, b, c, d);
        assertEquals(1, a.observerCount());
        assertEquals(1, b.observerCount());
        assertEquals(1, c.observerCount());
        assertEquals(1, d.observerCount());

        ObservableValue[] outputs = splitter.getOutputs();
        assertEquals(2, outputs.length);

        TestExecuter sc = new TestExecuter().setInputs(b, a, d, c).setOutputsOf(splitter);
        sc.check(0x0, 0x0, 0x0, 0x0, 0x00, 0x00);
        sc.check(0x1, 0x0, 0x0, 0x0, 0x10, 0x00);
        sc.check(0x0, 0x1, 0x0, 0x0, 0x01, 0x00);
        sc.check(0x0, 0x0, 0x1, 0x0, 0x00, 0x10);
        sc.check(0x0, 0x0, 0x0, 0x1, 0x00, 0x01);
        sc.check(0xf, 0x0, 0x0, 0x0, 0xf0, 0x00);
        sc.check(0x0, 0xf, 0x0, 0x0, 0x0f, 0x00);
        sc.check(0x0, 0x0, 0xf, 0x0, 0x00, 0xf0);
        sc.check(0x0, 0x0, 0x0, 0xf, 0x00, 0x0f);
        sc.check(0x1, 0x1, 0x0, 0x0, 0x11, 0x00);
        sc.check(0x0, 0x0, 0x1, 0x1, 0x00, 0x11);
        sc.check(0xf, 0xf, 0x0, 0x0, 0xff, 0x00);
        sc.check(0x0, 0x0, 0xf, 0xf, 0x00, 0xff);
        sc.check(0xf, 0xf, 0xf, 0xf, 0xff, 0xff);
    }


}