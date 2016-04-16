package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

import java.util.ArrayList;

/**
 * The And gate
 *
 * @author hneemann
 */
public class And extends Function {

    /**
     * The And description
     */
    public static final ElementTypeDescription DESCRIPTION = new FanInDescription(And.class);

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public And(ElementAttributes attributes) {
        super(attributes.get(AttributeKey.Bits));
    }

    @Override
    protected int calculate(ArrayList<ObservableValue> inputs) throws NodeException {
        int f = -1;
        for (ObservableValue i : inputs) {
            f &= i.getValue();
        }
        return f;
    }
}
