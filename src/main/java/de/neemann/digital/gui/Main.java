package de.neemann.digital.gui;

import de.neemann.digital.core.PartDescription;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Circuit;
import de.neemann.digital.gui.draw.parts.VisualPart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author hneemann
 */
public class Main extends JFrame {
    private final CircuitComponent circuitComponent;
    private final Circuit cr;

    public Main() {
        super("Digital");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cr = new Circuit();
        circuitComponent = new CircuitComponent(cr);
        getContentPane().add(circuitComponent);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        JMenuBar bar = new JMenuBar();

        JMenu parts = new JMenu("Parts");
        bar.add(parts);
        parts.add(createSimpleMenu("AND", inputs -> And.createFactory(1, inputs)));
        parts.add(createSimpleMenu("OR", inputs -> Or.createFactory(1, inputs)));
        parts.add(createSimpleMenu("NAND", inputs -> NAnd.createFactory(1, inputs)));
        parts.add(createSimpleMenu("NOR", inputs -> NOr.createFactory(1, inputs)));
        parts.add(new InsertAbstractAction("Not", Not.createFactory(1)));

        setJMenuBar(bar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private JMenu createSimpleMenu(String name, DescriptionFactory factory) {
        JMenu m = new JMenu(name);
        for (int i = 2; i < 16; i++) {
            m.add(new JMenuItem(new InsertAbstractAction(Integer.toString(i), factory.create(i))));
        }
        return m;
    }

    private interface DescriptionFactory {
        PartDescription create(int inputs);
    }

    private class InsertAbstractAction extends AbstractAction {
        private final PartDescription partDescription;

        public InsertAbstractAction(String name, PartDescription partDescription) {
            super(name);
            this.partDescription = partDescription;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            cr.add(new VisualPart(partDescription).setPos(new Vector(10, 10)));
        }
    }

}
