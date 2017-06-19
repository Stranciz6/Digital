package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * A seven seg display with seven single controllable inputs
 *
 * @author hneemann
 */
public class SevenSegShape extends SevenShape {
    private final PinDescriptions inputPins;
    private final boolean commonCatode;
    private final boolean persistence;
    private final boolean[] data;
    private ObservableValues inputs;
    private Pins pins;
    private ObservableValue ccin;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public SevenSegShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        super(attr);
        this.inputPins = inputs;
        commonCatode = attr.get(Keys.COMMON_CATHODE);
        persistence = attr.get(Keys.LED_PERSISTENCE);
        data = new boolean[8];
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();
            pins.add(new Pin(new Vector(0, 0), inputPins.get(0)));
            pins.add(new Pin(new Vector(SIZE, 0), inputPins.get(1)));
            pins.add(new Pin(new Vector(SIZE * 2, 0), inputPins.get(2)));
            pins.add(new Pin(new Vector(SIZE * 3, 0), inputPins.get(3)));
            pins.add(new Pin(new Vector(0, SIZE * HEIGHT), inputPins.get(4)));
            pins.add(new Pin(new Vector(SIZE, SIZE * HEIGHT), inputPins.get(5)));
            pins.add(new Pin(new Vector(SIZE * 2, SIZE * HEIGHT), inputPins.get(6)));
            pins.add(new Pin(new Vector(SIZE * 3, SIZE * HEIGHT), inputPins.get(7)));
            if (commonCatode)
                pins.add(new Pin(new Vector(SIZE * 4, SIZE * HEIGHT), inputPins.get(8)));
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        inputs = ioState.getInputs();
        for (ObservableValue o : inputs)
            o.addObserverToValue(guiObserver);
        if (commonCatode)
            ccin = inputs.get(8);
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        super.drawTo(graphic, highLight);
        if (commonCatode)
            graphic.drawLine(
                    new Vector(SIZE * 4 - SIZE2, SIZE * HEIGHT - 1),
                    new Vector(SIZE * 4, SIZE * HEIGHT - 1), Style.NORMAL);
    }

    @Override
    protected boolean getStyle(int i) {
        if (inputs == null)
            return true;

        if (persistence && commonCatode) {
            if (!ccin.isHighZ() && !ccin.getBool())
                data[i] = inputs.get(i).getValueIgnoreHighZ() > 0;
            return data[i];
        } else {
            if (commonCatode && (ccin.isHighZ() || ccin.getBool()))
                return false;

            return inputs.get(i).getValueIgnoreHighZ() > 0;
        }
    }

}
