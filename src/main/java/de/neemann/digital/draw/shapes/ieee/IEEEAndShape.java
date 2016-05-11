package de.neemann.digital.draw.shapes.ieee;

import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * IEEE Standard 91-1984 And Shape
 *
 * @author hneemann
 */
public class IEEEAndShape extends IEEEGenericShape {

    private static final Polygon POLYGON = createPoly();

    private static Polygon createPoly() {
        return new Polygon(true)
                .add(SIZE + SIZE2, SIZE * 2 + SIZE2)
                .add(1, SIZE * 2 + SIZE2)
                .add(1, -SIZE2)
                .add(SIZE + SIZE2, -SIZE2)
                .add(new Vector(SIZE * 2, -SIZE2), new Vector(SIZE * 3, 0), new Vector(SIZE * 3 - 1, SIZE))
                .add(new Vector(SIZE * 3 - 1, SIZE * 2), new Vector(SIZE * 2, SIZE * 2 + SIZE2), new Vector(SIZE + SIZE2, SIZE * 2 + SIZE2));
    }

    /**
     * Creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     * @param invert  true if NAnd
     */
    public IEEEAndShape(PinDescription[] inputs, PinDescription[] outputs, boolean invert) {
        super(inputs, outputs, invert);
    }

    @Override
    protected void drawIEEE(Graphic graphic) {
        graphic.drawPolygon(POLYGON, Style.NORMAL);
    }

}
