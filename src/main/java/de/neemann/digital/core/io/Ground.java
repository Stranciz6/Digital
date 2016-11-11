package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 * A constant
 *
 * @author hneemann
 */
public class Ground implements Element {

    /**
     * The Constant description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Ground.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS);

    private final ObservableValue output;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Ground(ElementAttributes attributes) {
        output = new ObservableValue("out", attributes.get(Keys.BITS));
        output.setValue(0);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"));
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
    }
}
