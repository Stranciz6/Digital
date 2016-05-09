package de.neemann.digital.draw.builder;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;

import java.util.List;

/**
 * A fragemnt used to create a circuit from an expression
 *
 * @author hneemann
 */
public interface Fragment {

    /**
     * Layouts this fragment
     *
     * @return the width and height of this fragment
     */
    Box doLayout();

    /**
     * Sets the position of the fragment.
     * Called during layout
     *
     * @param pos the position
     */
    void setPos(Vector pos);

    /**
     * Fragment is asked to add itself to the given circuit
     *
     * @param pos     the absolute position of the fragment
     * @param circuit the circuit
     */
    void addToCircuit(Vector pos, Circuit circuit);

    /**
     * @return the input positions of this fragment
     */
    List<Vector> getInputs();

    /**
     * @return the output positions of this fragment
     */
    List<Vector> getOutputs();

}
