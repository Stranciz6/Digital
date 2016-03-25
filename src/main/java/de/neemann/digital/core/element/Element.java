package de.neemann.digital.core.element;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;

/**
 * A concrete element used for the simulation
 *
 * @author hneemann
 */
public interface Element {
    /**
     * Sets the inputs for this element.
     * This list contains the outputs of other elements which are connected to the
     * inputs of this element.
     *
     * @param inputs the list of <code>ObservableValue</code>s to use
     * @throws NodeException
     */
    void setInputs(ObservableValue... inputs) throws NodeException;

    /**
     * When the connections between the elements are build all outputs a collected
     * by calling this method. After the interconnection they are set to the inputs
     * by calling <code>setInputs()</code>
     *
     * @return the list of outputs wich are set by this element
     */
    ObservableValue[] getOutputs();

    /**
     * The element has to register itself to the model.
     *
     * @param model the model to register to
     */
    void registerNodes(Model model);

    /**
     * Is called after registerNodes is called on all Elements.
     */
    default void init() {
    }

}
