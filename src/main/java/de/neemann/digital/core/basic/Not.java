package de.neemann.digital.core.basic;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Not
 *
 * @author hneemann
 */
public class Not extends Node implements Element {

    /**
     * The Not description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Not.class, input("in"))
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits);

    private final ObservableValue output;
    private final int bits;
    private ObservableValue input;
    private long value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Not(ElementAttributes attributes) {
        bits = attributes.get(AttributeKey.Bits);
        output = new ObservableValue("out", bits);
    }

    @Override
    public void readInputs() throws NodeException {
        value = input.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(~value);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        input = inputs[0].addObserverToValue(this).checkBits(bits, this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

}
