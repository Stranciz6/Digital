package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.ObservableValues.ovs;
import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A adder.
 *
 * @author hneemann
 */
public class Add extends Node implements Element {

    /**
     * The adders description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Add.class, input("a"), input("b"), input("c_i"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BITS);

    private final int bits;
    private final ObservableValue sum;
    private final ObservableValue cOut;
    private ObservableValue a;
    private ObservableValue b;
    private ObservableValue cIn;
    private Calc calc;
    private long aVal;
    private long bVal;
    private long cVal;

    /**
     * Create a new instance
     *
     * @param attributes the attributes
     */
    public Add(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);

        this.sum = new ObservableValue("s", bits).setPinDescription(DESCRIPTION);
        this.cOut = new ObservableValue("c_o", 1).setPinDescription(DESCRIPTION);

        calc = createCalculation(bits);
    }

    Calc createCalculation(int bits) {
        if (bits < 64) {   // simple and fast
            final long mask = Bits.up(1, bits);
            return (a, b, ci, s, co) -> {
                long value = a + b + ci;
                s.setValue(value);
                co.setBool((value & mask) != 0);
            };
        } else {           // complex 64 bit carry implementation
            return (a, b, ci, s, co) -> {
                long sum = a + b;
                s.setValue(sum + ci);
                co.setBool(addCarry(a, b) | addCarry(sum, ci));
            };
        }
    }

    private static final long LOWMASK = -1L >>> 1;
    private static final long CARRYMASK = 1L << 63;

    private static boolean addCarry(long x, long y) {
        boolean c = (((x & LOWMASK) + (y & LOWMASK)) & CARRYMASK) != 0;
        boolean a = (x & CARRYMASK) != 0;
        boolean b = (y & CARRYMASK) != 0;
        return (a & b) | ((a ^ b) & c);
    }

    @Override
    public void readInputs() throws NodeException {
        aVal = a.getValue();
        bVal = b.getValue();
        cVal = cIn.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        calc.calc(aVal, bVal, cVal, sum, cOut);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        a = inputs.get(0).addObserverToValue(this).checkBits(bits, this, 0);
        b = inputs.get(1).addObserverToValue(this).checkBits(bits, this, 1);
        cIn = inputs.get(2).addObserverToValue(this).checkBits(1, this, 2);

        if (bits > 64)
            throw new BitsException(Lang.get("err_toManyBits_Found_N0_maxIs_N1", bits, 63), this, 0, new ObservableValues(a, b));
    }

    @Override
    public ObservableValues getOutputs() {
        return ovs(sum, cOut);
    }

    interface Calc {
        void calc(long aVal, long bVal, long cVal, ObservableValue sum, ObservableValue cOut);
    }
}
