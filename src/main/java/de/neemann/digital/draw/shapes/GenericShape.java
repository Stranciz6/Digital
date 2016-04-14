package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

/**
 * Universal Shape. Used for most components.
 * Shows a simple Box with inputs at the left and outputs at the right.
 *
 * @author hneemann
 */
public class GenericShape implements Shape {
    /**
     * Half the size of the used raster
     */
    public static final int SIZE2 = 10;
    /**
     * The size of the used raster
     */
    public static final int SIZE = SIZE2 * 2;

    private final String name;
    private final PinDescription[] inputs;
    private final PinDescription[] outputs;
    private final int width;
    private final boolean symmetric;
    private boolean invert = false;
    private final String label;

    private transient Pins pins;
    private boolean showPinLabels;

    /**
     * Creates a new generic shape.
     *
     * @param name    the name shown in or below the shape
     * @param inputs  the used inputs
     * @param outputs the used outputs
     */
    public GenericShape(String name, PinDescription[] inputs, PinDescription[] outputs) {
        this(name, inputs, outputs, null, false);
    }

    /**
     * Creates a new generic shape.
     *
     * @param name          the name shown in or below the shape
     * @param inputs        the used inputs
     * @param outputs       the used outputs
     * @param label         the label shown above the shape
     * @param showPinLabels true if pin names visible
     */
    public GenericShape(String name, PinDescription[] inputs, PinDescription[] outputs, String label, boolean showPinLabels) {
        this(name, inputs, outputs, label, showPinLabels, inputs.length == 1 && outputs.length == 1 && !showPinLabels ? 1 : 3);
    }

    /**
     * Creates a new generic shape.
     *
     * @param name          the name shown in or below the shape
     * @param inputs        the used inputs
     * @param outputs       the used outputs
     * @param label         the label shown above the shape
     * @param showPinLabels true if pin names visible
     * @param width         the width of the box
     */
    public GenericShape(String name, PinDescription[] inputs, PinDescription[] outputs, String label, boolean showPinLabels, int width) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        if (label != null && label.length() == 0)
            label = null;
        this.label = label;
        this.showPinLabels = showPinLabels;
        this.width = width;
        symmetric = outputs.length == 1;
    }

    /**
     * Sets the invert flag.
     * If set true a little circle at the putput is shown.
     *
     * @param invert true is output is inverted
     * @return this for chaind calls
     */
    public GenericShape invert(boolean invert) {
        this.invert = invert;
        return this;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();

            int offs = symmetric ? inputs.length / 2 * SIZE : 0;

            for (int i = 0; i < inputs.length; i++) {
                int correct = 0;
                if (symmetric && ((inputs.length & 1) == 0) && i >= inputs.length / 2)
                    correct = SIZE;

                pins.add(new Pin(new Vector(0, i * SIZE + correct), inputs[i]));
            }


            if (invert) {
                for (int i = 0; i < outputs.length; i++)
                    pins.add(new Pin(new Vector(SIZE * (width + 1), i * SIZE + offs), outputs[i]));

            } else {
                for (int i = 0; i < outputs.length; i++)
                    pins.add(new Pin(new Vector(SIZE * width, i * SIZE + offs), outputs[i]));
            }
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        int max = Math.max(inputs.length, outputs.length);
        int height = (max - 1) * SIZE + SIZE2;

//        if (symmetric && state != null) {
//            graphic.drawText(new Vector(width * SIZE, 0), new Vector((width + 1) * SIZE, 0), Long.toString(state.getOutput(0).getValue()));
//        }


        if (symmetric && ((inputs.length & 1) == 0)) height += SIZE;

        graphic.drawPolygon(new Polygon(true)
                .add(1, -SIZE2)
                .add(SIZE * width - 1, -SIZE2)
                .add(SIZE * width - 1, height)
                .add(1, height), Style.NORMAL);

        if (invert) {
            int offs = symmetric ? inputs.length / 2 * SIZE : 0;
            for (int i = 0; i < outputs.length; i++)
                graphic.drawCircle(new Vector(SIZE * width + 1, i * SIZE - SIZE2 + 1 + offs),
                        new Vector(SIZE * (width + 1) - 1, i * SIZE + SIZE2 - 1 + offs), Style.NORMAL);

        }

        if (label != null) {
            Vector pos = new Vector(SIZE2 * width, -SIZE2 - 8);
            graphic.drawText(pos, pos.add(1, 0), label, Orientation.CENTERBOTTOM, Style.NORMAL);
        }

        if (showPinLabels) {
            for (Pin p : getPins()) {
                if (p.getDirection() == Pin.Direction.input)
                    graphic.drawText(p.getPos().add(4, 0), p.getPos().add(5, 0), p.getName(), Orientation.LEFTCENTER, Style.SHAPE_PIN);
                else
                    graphic.drawText(p.getPos().add(-4, 0), p.getPos().add(5, 0), p.getName(), Orientation.RIGHTCENTER, Style.SHAPE_PIN);
            }
        }
        if (name.length() <= 3 && !showPinLabels) {
            Vector pos = new Vector(SIZE2 * width, -SIZE2 + 4);
            graphic.drawText(pos, pos.add(1, 0), name, Orientation.CENTERTOP, Style.NORMAL);
        } else {
            Vector pos = new Vector(SIZE2 * width, height + 4);
            graphic.drawText(pos, pos.add(1, 0), name, Orientation.CENTERTOP, Style.SHAPE_PIN);
        }
    }

}
