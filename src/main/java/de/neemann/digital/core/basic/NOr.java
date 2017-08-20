package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

import java.util.ArrayList;

/**
 * The NOr
 *
 * @author hneemann
 */
public class NOr extends Or {

    /**
     * The NOr description
     */
    public static final ElementTypeDescription DESCRIPTION = new FanInDescription(NOr.class);

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public NOr(ElementAttributes attributes) {
        super(attributes);
    }

    @Override
    protected int calculate(ArrayList<ObservableValue> inputs) throws NodeException {
        return ~super.calculate(inputs);
    }
}
