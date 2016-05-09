package de.neemann.digital.draw.builder;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment describing a VisualElement
 *
 * @author hneemann
 */
public class FragmentVisualElement implements Fragment {

    private final ArrayList<Vector> inputs;
    private final ArrayList<Vector> outputs;
    private final VisualElement visualElement;
    private Vector pos;

    /**
     * Creates a new instance
     *
     * @param description  the elements description
     * @param shapeFactory the shapeFactory to use
     */
    public FragmentVisualElement(ElementTypeDescription description, ShapeFactory shapeFactory) {
        this(description, 1, shapeFactory);
    }

    /**
     * Creates a new instance
     *
     * @param description  the elements description
     * @param inputCount   number of inputs
     * @param shapeFactory the shapeFactory to use
     */
    public FragmentVisualElement(ElementTypeDescription description, int inputCount, ShapeFactory shapeFactory) {
        visualElement = new VisualElement(description.getName()).setShapeFactory(shapeFactory);
        visualElement.getElementAttributes().set(Keys.INPUT_COUNT, inputCount);
        Pins pins = visualElement.getShape().getPins();

        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        for (Pin p : pins) {
            if (p.getDirection().equals(PinDescription.Direction.input))
                inputs.add(p.getPos());
            else
                outputs.add(p.getPos());
        }
    }

    /**
     * Sets an attribute to this VisualElement
     *
     * @param key     the key
     * @param value   the value
     * @param <VALUE> the tyype of the value
     * @return this for call chaining
     */
    public <VALUE> FragmentVisualElement setAttr(Key<VALUE> key, VALUE value) {
        visualElement.getElementAttributes().set(key, value);
        return this;
    }

    @Override
    public Box doLayout() {
        GraphicMinMax mm = new GraphicMinMax();
        for (Vector p : inputs)
            mm.check(p);
        for (Vector p : outputs)
            mm.check(p);
        Vector delta = mm.getMax().sub(mm.getMin());
        return new Box(delta.x, delta.y);
    }

    @Override
    public void setPos(Vector pos) {
        this.pos = pos;
    }

    @Override
    public void addToCircuit(Vector offset, Circuit circuit) {
        visualElement.setPos(pos.add(offset));
        circuit.add(visualElement);
    }

    @Override
    public List<Vector> getInputs() {
        return Vector.add(inputs, pos);
    }

    @Override
    public List<Vector> getOutputs() {
        return Vector.add(outputs, pos);
    }
}
