package de.neemann.digital.core.pld;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.*;

/**
 * Only a placeholder.
 * Has no connections to the model!
 *
 * @author hneemann
 */
public class PullUp implements Element {

    /**
     * The pull up description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("PullUp", PullUp.class)
            .addAttribute(Keys.ROTATE);

    private final ObservableValue output;

    /**
     * Creates a new pull up element
     *
     * @param attr the attributes
     */
    public PullUp(ElementAttributes attr) {
        int bits = attr.getBits();
        output = new PullDown.PullObservableValue(bits, PinDescription.PullResistor.pullUp);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
    }

}
