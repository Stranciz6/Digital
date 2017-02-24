package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Observer;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.ModelEntry;

/**
 * Every element you can put on the circuit panel implements this interface.
 *
 * @author hneemann
 */
public interface Shape extends Drawable {

    /**
     * Puts the pins name and the pins x-y-position together!
     * This information is used to calculate the models connections from the wiring in the circuit.
     * Don't create your own {@link de.neemann.digital.core.element.PinInfo} instance! Try to use
     * the instances passed to the constructor of the shape via the {@link ShapeFactory}s {@link ShapeFactory#Creator} interface.
     *
     * @return the pins
     */
    Pins getPins();

    /**
     * This method call connects the created model element to the shape which represents the model node.
     * If the look of the shape depends on an inputs state, the shape has to register the guiObserver
     * to all of the inputs ObservableValues it depends on.
     * To access the actual state while drawing, the Shape needs to store the IOState or the needed inputs
     * in a member variable.
     * <p>
     * If the shape returns an interactor, this interactors clicked method is called if the
     * shape is clicked in running mode. If the shape is not interactive siply return null.
     *
     * @param ioState     the state of the element, never null
     * @param guiObserver can be used to update the GUI by calling hasChanged, maybe null
     * @return the interactor is called if the shape is clicked during running mode, maybe null
     */
    InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver);

    /**
     * Allows the shape to make its drawing dependent of the model by registering a Observer to the model.
     * It is used by {@link DataShape} to create and show the data graph.
     * This method is necessary if a shape does not depend only on its inputs or outputs but if it
     * depends on the global model state.
     *
     * @param modelCreator the models description
     * @param model        the model itself
     * @param element      the ModelElement this shape belongs to
     */
    default void registerModel(ModelCreator modelCreator, Model model, ModelEntry element) {
    }
}
