package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.shapes.Drawable;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class Circuit implements Drawable {
    private final ArrayList<VisualPart> visualParts;
    private ArrayList<Wire> wires;
    private transient boolean dotsPresent = false;
    private transient boolean modified = false;

    public Circuit() {
        visualParts = new ArrayList<>();
        wires = new ArrayList<>();
    }

    @Override
    public void drawTo(Graphic graphic) {
        if (!dotsPresent) {
            new DotCreator(wires).applyDots();
            dotsPresent = true;
        }

        for (Wire w : wires)
            w.drawTo(graphic);
        for (VisualPart p : visualParts)
            p.drawTo(graphic);
    }

    public void add(VisualPart visualPart) {
        visualParts.add(visualPart);
        modified();
    }

    public void add(Wire newWire) {
        if (newWire.p1.equals(newWire.p2))
            return;

        int len = wires.size();
        for (int i = 0; i < len; i++) {
            Wire present = wires.get(i);
            if (present.contains(newWire.p1)) {
                wires.set(i, new Wire(present.p1, newWire.p1));
                wires.add(new Wire(present.p2, newWire.p1));
            } else if (present.contains(newWire.p2)) {
                wires.set(i, new Wire(present.p1, newWire.p2));
                wires.add(new Wire(present.p2, newWire.p2));
            }
        }

        wires.add(newWire);
        WireConsistencyChecker checker = new WireConsistencyChecker(wires);
        wires = checker.check();

        dotsPresent = false;
        modified();
    }

    public ArrayList<VisualPart> getParts() {
        return visualParts;
    }

    public ArrayList<Moveable> getElementsToMove(Vector min, Vector max) {
        ArrayList<Moveable> m = new ArrayList<>();
        for (VisualPart vp : visualParts)
            if (vp.matches(min, max))
                m.add(vp);

        for (Wire w : wires) {
            if (w.p1.inside(min, max))
                m.add(w.p1);
            if (w.p2.inside(min, max))
                m.add(w.p2);
        }

        return m;
    }

    public ArrayList<Moveable> getElementsToCopy(Vector min, Vector max) {
        ArrayList<Moveable> m = new ArrayList<>();
        for (VisualPart vp : visualParts)
            if (vp.matches(min, max))
                m.add(new VisualPart(vp));

        for (Wire w : wires)
            if (w.p1.inside(min, max) && w.p2.inside(min, max))
                m.add(new Wire(w));

        return m;
    }


    public void delete(Vector min, Vector max) {
        {
            Iterator<VisualPart> it = visualParts.iterator();
            while (it.hasNext())
                if (it.next().matches(min, max))
                    it.remove();
        }
        {
            Iterator<Wire> it = wires.iterator();
            while (it.hasNext()) {
                Wire w = it.next();
                if (w.p1.inside(min, max) || w.p2.inside(min, max))
                    it.remove();
            }
        }
        dotsPresent = false;
        modified();
    }

    public void modified() {
        modified = true;
    }

    public ArrayList<Wire> getWires() {
        return wires;
    }

    public void clearState() {
        for (VisualPart vp : visualParts)
            vp.setState(null, null);
        for (Wire w : wires)
            w.setValue(null);
    }

    public boolean isModified() {
        return modified;
    }

    public void saved() {
        modified = false;
    }
}
