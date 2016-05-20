package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The Tunnel shape
 *
 * @author hneemann
 */
public class TunnelShape implements Shape {

    private static final int HEIGHT = SIZE2 - 2;
    private static final int WIDTH = (int) Math.round(HEIGHT * Math.sqrt(3));

    private final PinDescription input;
    private final String label;
    //private ObservableValue inValue;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public TunnelShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        input = inputs.get(0);
        label = attr.get(Keys.NETNAME);
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), input));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        //inValue = ioState.getInput(0).addObserverToValue(guiObserver);
        return null;
    }

    @Override
    public void drawTo(Graphic gr, boolean highLight) {
//        if (inValue != null) {
//            Style style = Style.getWireStyle(inValue);
//            gr.drawPolygon(new Polygon(true)
//                    .add(1, 0)
//                    .add(SIZE - 1, SIZE2 - 4)
//                    .add(SIZE - 1, -SIZE2 + 4), style);
//        }

        gr.drawPolygon(new Polygon(true)
                .add(0, 0)
                .add(WIDTH, HEIGHT)
                .add(WIDTH, -HEIGHT), Style.NORMAL);
        Vector pos = new Vector(WIDTH + SIZE2 / 2, 0);
        gr.drawText(pos, pos.add(1, 0), label, Orientation.LEFTCENTER, Style.SHAPE_PIN);
    }
}
