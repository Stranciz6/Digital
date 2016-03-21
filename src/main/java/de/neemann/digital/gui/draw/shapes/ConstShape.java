package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.elements.IOState;
import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.Pins;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Orientation;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;

/**
 * @author hneemann
 */
public class ConstShape implements Shape {

    private String value;

    public ConstShape(long value) {
        this.value = "0x" + Long.toHexString(value);
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), "out", Pin.Direction.output));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic) {
        Vector textPos = new Vector(-3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), value, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
