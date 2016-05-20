package de.neemann.digital.core.wiring;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Delay
 *
 * @author hneemann
 */
public class Delay extends Node implements Element {

    /**
     * The Delay description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Delay.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS);

    private final ObservableValue output;
    private final int bits;
    private ObservableValue input;
    private long value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Delay(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        output = new ObservableValue("out", bits);
    }

    @Override
    public void readInputs() throws NodeException {
        value = input.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).addObserverToValue(this).checkBits(bits, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(output);
    }

}
