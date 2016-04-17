package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The driver shape
 *
 * @author hneemann
 */
public class DriverShape implements Shape {
    private final boolean bottom;
    private final PinDescription[] inputs;
    private final PinDescription[] outputs;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public DriverShape(ElementAttributes attr, PinDescription[] inputs, PinDescription[] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.bottom = attr.get(Keys.FLIP_SEL_POSITON);
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(-SIZE, 0), inputs[0]));
            pins.add(new Pin(new Vector(0, bottom ? SIZE : -SIZE), inputs[1]));
            pins.add(new Pin(new Vector(SIZE, 0), outputs[0]));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        graphic.drawPolygon(
                new Polygon(true)
                        .add(-SIZE + 1, -SIZE2 - 2)
                        .add(SIZE - 1, 0)
                        .add(-SIZE + 1, SIZE2 + 2), Style.NORMAL
        );
        if (bottom)
            graphic.drawLine(new Vector(0, SIZE), new Vector(0, 7), Style.NORMAL);
        else
            graphic.drawLine(new Vector(0, -SIZE), new Vector(0, -7), Style.NORMAL);
    }
}
