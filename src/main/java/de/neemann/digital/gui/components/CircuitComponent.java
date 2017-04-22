package de.neemann.digital.gui.components;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.ImmutableList;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.LibraryListener;
import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.sync.NoSync;
import de.neemann.digital.gui.sync.Sync;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.IconCreator;
import de.neemann.gui.StringUtils;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

/**
 * Component which shows the circuit.
 * ToDo: refactoring of repaint logic. Its to complex now.
 *
 * @author hneemann
 */
public class CircuitComponent extends JComponent implements Circuit.ChangedListener, LibraryListener {
    /**
     * The delete icon, also used from {@link de.neemann.digital.gui.components.terminal.TerminalDialog}
     */
    public static final Icon ICON_DELETE = IconCreator.create("delete.png");
    private static final String DEL_ACTION = "myDelAction";
    private static final String ESC_ACTION = "myEscAction";
    private static final int MOUSE_BORDER_SMALL = 10;
    private static final int MOUSE_BORDER_LARGE = 50;

    private final Main parent;
    private final ElementLibrary library;
    private final HashSet<Drawable> highLighted;
    private final ToolTipAction deleteAction;

    private final MouseController mouseNormal;
    private final MouseControllerInsertElement mouseInsertElement;
    private final MouseControllerMoveElement mouseMoveElement;
    private final MouseControllerMoveWire mouseMoveWire;
    private final MouseControllerWire mouseWire;
    private final MouseControllerSelect mouseSelect;
    private final MouseControllerMoveSelected mouseMoveSelected;
    private final MouseController mouseRun;
    private final MouseControllerInsertCopied mouseInsertList;
    private final Cursor moveCursor;
    private final AbstractAction copyAction;
    private final AbstractAction pasteAction;
    private final AbstractAction rotateAction;

    private Circuit circuit;
    private MouseController activeMouseController;
    private AffineTransform transform = new AffineTransform();
    private Observer manualChangeObserver;
    private Vector lastMousePos;
    private Sync modelSync;
    private boolean isManualScale;
    private boolean hasChanged = true;
    private boolean focusWasLost = false;


    /**
     * Creates a new instance
     *
     * @param parent       the parent window
     * @param library      the library used to edit the attributes of the elements
     * @param shapeFactory the shapeFactory used for copied elements
     */
    public CircuitComponent(Main parent, ElementLibrary library, ShapeFactory shapeFactory) {
        this.parent = parent;
        this.library = library;
        highLighted = new HashSet<>();

        rotateAction = new AbstractAction(Lang.get("menu_rotate")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeMouseController.rotate();
            }
        };
        rotateAction.setEnabled(false);


        copyAction = new AbstractAction(Lang.get("menu_copy")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (activeMouseController instanceof MouseControllerSelect) {
                    MouseControllerSelect mcs = ((MouseControllerSelect) activeMouseController);
                    ArrayList<Movable> elements = circuit.getElementsToCopy(Vector.min(mcs.corner1, mcs.corner2), Vector.max(mcs.corner1, mcs.corner2), shapeFactory);
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(new CircuitTransferable(elements), null);
                    removeHighLighted();
                    mouseNormal.activate();
                }
            }
        };
        copyAction.setEnabled(false);

        pasteAction = new AbstractAction(Lang.get("menu_paste")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                try {
                    Object data = clpbrd.getData(DataFlavor.stringFlavor);
                    if (data instanceof String) {
                        Vector posVector = getPosVector(lastMousePos.x, lastMousePos.y);
                        ArrayList<Movable> elements = CircuitTransferable.createList(data, shapeFactory, posVector);
                        if (elements != null) {
                            removeHighLighted();
                            mouseInsertList.activate(elements, posVector);
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_clipboardContainsNoImportableData")).setComponent(CircuitComponent.this));
                }
            }
        };

        deleteAction = new ToolTipAction(Lang.get("menu_delete"), ICON_DELETE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeMouseController.delete();
            }
        }.setToolTip(Lang.get("menu_delete_tt"));

        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeMouseController.escapePressed();
            }
        };

        AbstractAction programAction = new AbstractAction(Lang.get("menu_programDiode")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (activeMouseController instanceof MouseControllerNormal) {
                    programElementAt(getPosVector(lastMousePos.x, lastMousePos.y));
                }
            }
        };

        getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), ESC_ACTION);
        getActionMap().put(ESC_ACTION, escapeAction);
        getInputMap().put(KeyStroke.getKeyStroke("DELETE"), DEL_ACTION);
        getActionMap().put(DEL_ACTION, deleteAction);
        getInputMap().put(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "myCopy");
        getActionMap().put("myCopy", copyAction);
        getInputMap().put(KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "myPaste");
        getActionMap().put("myPaste", pasteAction);
        getInputMap().put(KeyStroke.getKeyStroke("R"), "myRotate");
        getActionMap().put("myRotate", rotateAction);
        getInputMap().put(KeyStroke.getKeyStroke("P"), "myProgram");
        getActionMap().put("myProgram", programAction);

        setFocusable(true);

        addMouseWheelListener(e -> {
            Vector pos = getPosVector(e);
            double f = Math.pow(0.9, e.getWheelRotation());
            transform.translate(pos.x, pos.y);
            transform.scale(f, f);
            transform.translate(-pos.x, -pos.y);
            isManualScale = true;
            hasChanged();
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (!isManualScale)
                    fitCircuit();
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                focusWasLost = true;
            }
        });

        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        moveCursor = new Cursor(Cursor.MOVE_CURSOR);
        mouseNormal = new MouseControllerNormal(normalCursor);
        mouseInsertElement = new MouseControllerInsertElement(normalCursor);
        mouseInsertList = new MouseControllerInsertCopied(normalCursor);
        mouseMoveElement = new MouseControllerMoveElement(normalCursor);
        mouseMoveWire = new MouseControllerMoveWire(normalCursor);
        mouseWire = new MouseControllerWire(normalCursor);
        mouseSelect = new MouseControllerSelect(new Cursor(Cursor.CROSSHAIR_CURSOR));
        mouseMoveSelected = new MouseControllerMoveSelected(moveCursor);
        mouseRun = new MouseControllerRun(normalCursor);

        setCircuit(new Circuit());

        MouseDispatcher disp = new MouseDispatcher();
        addMouseMotionListener(disp);
        addMouseListener(disp);

        setToolTipText("");
    }

    private void programElementAt(Vector pos) {
        VisualElement ve = circuit.getElementAt(pos);
        if (ve != null && library.isProgrammable(ve.getElementName())) {
            boolean blown = ve.getElementAttributes().get(Keys.BLOWN);
            ve.getElementAttributes().set(Keys.BLOWN, !blown);
            circuit.modified();
            hasChanged();
        }
    }

    /**
     * invalidates the image buffer and calls repaint();
     */
    public void hasChanged() {
        hasChanged = true;
        repaint();
    }

    /**
     * @return the main frame
     */
    public Main getMain() {
        return parent;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Vector pos = getPosVector(event);
        VisualElement ve = circuit.getElementAt(pos);
        if (ve == null) return null;

        Pin p = circuit.getPinAt(raster(pos), ve);
        if (p != null)
            return createPinToolTip(p);

        try {
            ElementTypeDescription etd = library.getElementType(ve.getElementName());
            return checkToolTip(etd.getDescription(ve.getElementAttributes()));
        } catch (ElementNotFoundException e) {
            return null;
        }
    }

    private String createPinToolTip(Pin p) {
        String text = p.getName();
        final String des = p.getDescription();
        if (des != null && des.length() > 0) {
            text += ": " + des;
        }
        return checkToolTip(text);
    }

    private String checkToolTip(String tt) {
        if (tt != null && tt.length() == 0)
            return null;
        else
            return StringUtils.textToHTML(tt);
    }

    /**
     * @return the delete action to put it to the toolbar
     */
    public ToolTipAction getDeleteAction() {
        return deleteAction;
    }

    /**
     * @return the copy action
     */
    public AbstractAction getCopyAction() {
        return copyAction;
    }

    /**
     * @return the paste action
     */
    public AbstractAction getPasteAction() {
        return pasteAction;
    }

    /**
     * @return the rotate action
     */
    public AbstractAction getRotateAction() {
        return rotateAction;
    }

    /**
     * Sets the observer to call if the user is clicking on elements while running.
     *
     * @param callOnManualChange the listener
     */
    public void setManualChangeObserver(Observer callOnManualChange) {
        this.manualChangeObserver = callOnManualChange;
    }

    /**
     * Sets the edit mode and resets the circuit
     *
     * @param runMode   true if running, false if editing
     * @param modelSync used to access the running model
     */
    public void setModeAndReset(boolean runMode, Sync modelSync) {
        this.modelSync = modelSync;
        if (runMode)
            mouseRun.activate();
        else {
            mouseNormal.activate();
            circuit.clearState();
        }
        requestFocusInWindow();
    }

    /**
     * @return the high lighted elements
     */
    public Collection<Drawable> getHighLighted() {
        return highLighted;
    }

    /**
     * Adds a drawable to the highlighted list
     *
     * @param drawable the drawable to add
     * @param <T>      type of drawable
     */
    public <T extends Drawable> void addHighLighted(T drawable) {
        if (drawable != null)
            highLighted.add(drawable);
    }

    /**
     * Add a list of drawables to high light
     *
     * @param drawables the list of drawables
     */
    public void addHighLighted(Collection<? extends Drawable> drawables) {
        if (drawables != null)
            highLighted.addAll(drawables);
    }

    /**
     * Adds all the wires representing the given value to the highlighted list
     *
     * @param values the value
     */
    public void addHighLightedWires(ImmutableList<ObservableValue> values) {
        if (values == null) return;

        HashSet<ObservableValue> ov = new HashSet<>();
        ov.addAll(values);
        for (Wire w : circuit.getWires())
            if (ov.contains(w.getValue()))
                addHighLighted(w);
    }

    /**
     * remove all highlighted elements
     */
    public void removeHighLighted() {
        highLighted.clear();
    }

    /**
     * Adds the given element to insert to the circuit
     *
     * @param element the element to insert
     */
    public void setPartToInsert(VisualElement element) {
        parent.stopModel();
        mouseInsertElement.activate(element);
        Point point = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(point, this);
        if (point.x < MOUSE_BORDER_LARGE || point.x > getWidth() - MOUSE_BORDER_SMALL
                || point.y < MOUSE_BORDER_LARGE || point.y > getHeight() - MOUSE_BORDER_SMALL) {

            if (point.x < MOUSE_BORDER_LARGE)
                point.x = MOUSE_BORDER_LARGE;
            else if (point.x > getWidth() - MOUSE_BORDER_SMALL)
                point.x = getWidth() - MOUSE_BORDER_SMALL;

            if (point.y < MOUSE_BORDER_LARGE)
                point.y = MOUSE_BORDER_LARGE;
            else if (point.y > getHeight() - MOUSE_BORDER_SMALL)
                point.y = getHeight() - MOUSE_BORDER_SMALL;

            SwingUtilities.convertPointToScreen(point, this);

            try {
                new Robot().mouseMove(point.x, point.y);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }


    private BufferedImage buffer;
    private int highlightedPaintedSize;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean needsNewBuffer = buffer == null
                || getWidth() != buffer.getWidth()
                || getHeight() != buffer.getHeight();

        if (needsNewBuffer && !isManualScale)
            fitCircuit();

        if (hasChanged
                || needsNewBuffer
                || highLighted.size() != highlightedPaintedSize) {

//            long time = System.currentTimeMillis();
            if (needsNewBuffer)
                buffer = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());

            Graphics2D gr2 = buffer.createGraphics();
            gr2.setColor(Color.WHITE);
            gr2.fillRect(0, 0, getWidth(), getHeight());
            gr2.transform(transform);

            GraphicSwing gr = new GraphicSwing(gr2, (int) (2 / transform.getScaleX()));
            circuit.drawTo(gr, highLighted, modelSync);
            highlightedPaintedSize = highLighted.size();
            hasChanged = false;
//            System.out.println("repaint: " + Long.toString(System.currentTimeMillis() - time) + "ms");
        }

        g.drawImage(buffer, 0, 0, null);

        Graphics2D gr2 = (Graphics2D) g;
        AffineTransform oldTrans = gr2.getTransform();
        gr2.transform(transform);
        GraphicSwing gr = new GraphicSwing(gr2, (int) (2 / transform.getScaleX()));
        activeMouseController.drawTo(gr);
        gr2.setTransform(oldTrans);
    }

    @Override
    public void circuitHasChanged() {
        hasChanged = true;
    }

    /**
     * forces a immediately repaint
     * Is called from {@link de.neemann.digital.gui.GuiModelObserver} if the models data has changed.
     * Therefore the double buffer is invalidated.
     */
    public void paintImmediately() {
        hasChanged = true;
        paintImmediately(0, 0, getWidth(), getHeight());
    }


    private Vector getPosVector(MouseEvent e) {
        return getPosVector(e.getX(), e.getY());
    }

    private Vector getPosVector(int x, int y) {
        try {
            Point2D.Double p = new Point2D.Double();
            transform.inverseTransform(new Point(x, y), p);
            return new Vector((int) Math.round(p.getX()), (int) Math.round(p.getY()));
        } catch (NoninvertibleTransformException e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * rounds the given vector to the raster
     *
     * @param pos the vector
     * @return pos round to raster
     */
    public static Vector raster(Vector pos) {
        return new Vector((int) Math.round((double) pos.x / SIZE) * SIZE,
                (int) Math.round((double) pos.y / SIZE) * SIZE);
    }

    /**
     * @return the circuit shown
     */
    public Circuit getCircuit() {
        return circuit;
    }

    /**
     * Sets a circuit to this component
     *
     * @param circuit the circuit
     */
    public void setCircuit(Circuit circuit) {

        if (this.circuit != null) {
            this.circuit.removeListener(this);
        }

        this.circuit = circuit;

        circuit.addListener(this);

        fitCircuit();
        setModeAndReset(false, NoSync.INST);
    }

    /**
     * maximizes the circuit shown
     */
    public void fitCircuit() {
        GraphicMinMax gr = new GraphicMinMax();
        circuit.drawTo(gr);

        AffineTransform newTrans = new AffineTransform();
        if (gr.getMin() != null && getWidth() != 0 && getHeight() != 0) {
            Vector delta = gr.getMax().sub(gr.getMin());
            double sx = ((double) getWidth()) / (delta.x + Style.NORMAL.getThickness() * 2);
            double sy = ((double) getHeight()) / (delta.y + Style.NORMAL.getThickness() * 2);
            double s = Math.min(sx, sy);


            newTrans.setToScale(s, s);  // set Scaling

            Vector center = gr.getMin().add(gr.getMax()).div(2);
            newTrans.translate(-center.x, -center.y);  // move drawing center to (0,0)

            Vector dif = new Vector(getWidth(), getHeight()).div(2);
            newTrans.translate(dif.x / s, dif.y / s);  // move drawing center to frame center
            isManualScale = false;
        } else {
            isManualScale = true;
        }
        if (!newTrans.equals(transform)) {
            transform = newTrans;
            hasChanged();
        }
    }

    /**
     * scales the circuit
     *
     * @param f factor to scale
     */
    public void scaleCircuit(double f) {
        Vector dif = getPosVector(getWidth() / 2, getHeight() / 2);
        transform.translate(dif.x, dif.y);
        transform.scale(f, f);
        transform.translate(-dif.x, -dif.y);
        isManualScale = true;
        hasChanged();
    }

    private void editAttributes(VisualElement vp, MouseEvent e) {
        String name = vp.getElementName();
        try {
            ElementTypeDescription elementType = library.getElementType(name);
            ArrayList<Key> list = elementType.getAttributeList();
            if (list.size() > 0) {
                Point p = new Point(e.getX(), e.getY());
                SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
                AttributeDialog attributeDialog = new AttributeDialog(this, p, list, vp.getElementAttributes());
                if (elementType instanceof ElementLibrary.ElementTypeDescriptionCustom) {
                    attributeDialog.addButton(Lang.get("attr_openCircuitLabel"), new ToolTipAction(Lang.get("attr_openCircuit")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            attributeDialog.dispose();
                            new Main.MainBuilder()
                                    .setParent(CircuitComponent.this)
                                    .setFileToOpen(((ElementLibrary.ElementTypeDescriptionCustom) elementType).getFile())
                                    .setLibrary(library)
                                    .denyMostFileActions()
                                    .openLater();
                        }
                    }.setToolTip(Lang.get("attr_openCircuit_tt")));
                }
                attributeDialog.addButton(new ToolTipAction(Lang.get("attr_help")) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        try {
                            new ElementHelpDialog(attributeDialog, elementType, vp.getElementAttributes()).setVisible(true);
                        } catch (PinException | NodeException e1) {
                            new ErrorMessage(Lang.get("msg_creatingHelp")).addCause(e1).show(CircuitComponent.this);
                        }
                    }
                }.setToolTip(Lang.get("attr_help_tt")));
                if (attributeDialog.showDialog()) {
                    circuit.modified();
                    hasChanged();
                }
            }
        } catch (ElementNotFoundException ex) {
            // do nothing if element not found!
        }
    }

    @Override
    public void libraryChanged(LibraryNode node) {
        circuit.clearState();
        hasChanged = true;
        repaint();
    }

    private class MouseDispatcher extends MouseAdapter implements MouseMotionListener {
        private Vector pos;
        private boolean isMoved;

        @Override
        public void mousePressed(MouseEvent e) {
            pos = new Vector(e.getX(), e.getY());
            isMoved = false;
            requestFocusInWindow();
            activeMouseController.pressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            activeMouseController.released(e);
            if (!(wasMoved(e) || isMoved))
                activeMouseController.clicked(e);
        }

        private boolean wasMoved(MouseEvent e) {
            Vector d = new Vector(e.getX(), e.getY()).sub(pos);
            return Math.abs(d.x) > SIZE2 || Math.abs(d.y) > SIZE2;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            lastMousePos = new Vector(e.getX(), e.getY());
            activeMouseController.moved(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            lastMousePos = new Vector(e.getX(), e.getY());
            activeMouseController.moved(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (wasMoved(e) || isMoved) {
                isMoved = true;
                if (!activeMouseController.dragged(e)) {
                    Vector newPos = new Vector(e.getX(), e.getY());
                    Vector delta = newPos.sub(pos);
                    double s = transform.getScaleX();
                    transform.translate(delta.x / s, delta.y / s);
                    pos = newPos;
                    isManualScale = true;
                    hasChanged();
                }
            }
        }

    }

    //MouseController can not be final because its overridden. Maybe checkstyle has a bug?
    //CHECKSTYLE.OFF: FinalClass
    private class MouseController {
        private final Cursor mouseCursor;

        private MouseController(Cursor mouseCursor) {
            this.mouseCursor = mouseCursor;
        }

        public void activate() {
            activeMouseController = this;
            deleteAction.setActive(false);
            copyAction.setEnabled(false);
            rotateAction.setEnabled(false);
            setCursor(mouseCursor);
            hasChanged();
        }

        void clicked(MouseEvent e) {
        }

        void pressed(MouseEvent e) {
        }

        void released(MouseEvent e) {
        }

        void moved(MouseEvent e) {
        }

        boolean dragged(MouseEvent e) {
            return false;
        }

        public void drawTo(Graphic gr) {
        }

        public void delete() {
        }

        public void rotate() {
        }

        public void escapePressed() {
        }
    }
    //CHECKSTYLE.ON: FinalClass

    private final class MouseControllerNormal extends MouseController {
        private Vector pos;
        private int downButton;

        private MouseControllerNormal(Cursor cursor) {
            super(cursor);
        }

        private VisualElement getVisualElement(Vector pos, boolean includeText) {
            VisualElement vp = null;
            List<VisualElement> list = circuit.getElementListAt(pos, includeText);
            if (list.size() == 1)
                vp = list.get(0);
            else if (list.size() > 1) {
                ItemPicker<VisualElement> picker = new ItemPicker<>(CircuitComponent.this, list);
                vp = picker.select();
            }
            return vp;
        }

        @Override
        void clicked(MouseEvent e) {
            Vector pos = getPosVector(e);

            if (e.getButton() == MouseEvent.BUTTON3) {
                VisualElement vp = getVisualElement(pos, true);
                if (vp != null)
                    editAttributes(vp, e);
            } else if (e.getButton() == MouseEvent.BUTTON1) {
                VisualElement vp = getVisualElement(pos, false);
                if (vp != null) {
                    if (circuit.isPinPos(raster(pos), vp) && !e.isControlDown())
                        mouseWire.activate(pos);
                    else
                        mouseMoveElement.activate(vp, pos);
                } else {
                    if (e.isControlDown()) {
                        Wire wire = circuit.getWireAt(pos, SIZE2);
                        if (wire != null)
                            mouseMoveWire.activate(wire, pos);
                    } else if (!focusWasLost)
                        mouseWire.activate(pos);
                }
            }
            focusWasLost = false;
        }

        @Override
        void pressed(MouseEvent e) {
            downButton = e.getButton();
            pos = getPosVector(e);
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (downButton == MouseEvent.BUTTON1) {
                mouseSelect.activate(pos, getPosVector(e));
                return true;
            }
            return false;
        }

    }

    private final class MouseControllerInsertElement extends MouseController {
        private VisualElement element;
        private Vector delta;

        private MouseControllerInsertElement(Cursor cursor) {
            super(cursor);
        }

        private void activate(VisualElement element) {
            super.activate();
            this.element = element;
            delta = null;
            deleteAction.setActive(true);
            rotateAction.setEnabled(true);
        }

        @Override
        void moved(MouseEvent e) {
            if (delta == null) {
                GraphicMinMax minMax = element.getMinMax(false);
                delta = element.getPos().sub(minMax.getMax());
            }
            element.setPos(raster(getPosVector(e).add(delta)));
            repaint();
        }

        @Override
        public void delete() {
            mouseNormal.activate();
        }

        @Override
        public void drawTo(Graphic gr) {
            if (delta != null)
                element.drawTo(gr, true);
        }

        @Override
        void clicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                circuit.add(element);
                hasChanged();
            }
            mouseNormal.activate();
            focusWasLost = false;
        }

        @Override
        public void rotate() {
            element.rotate();
            repaint();
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }
    }

    private final class MouseControllerMoveElement extends MouseController {
        private VisualElement visualElement;
        private Vector delta;
        private Vector initialPos;
        private int initialRot;

        private MouseControllerMoveElement(Cursor cursor) {
            super(cursor);
        }

        private void activate(VisualElement visualElement, Vector pos) {
            super.activate();
            this.visualElement = visualElement;
            initialPos = visualElement.getPos();
            initialRot = visualElement.getRotate();
            delta = initialPos.sub(pos);
            deleteAction.setActive(true);
            rotateAction.setEnabled(true);
            hasChanged();
        }

        @Override
        void clicked(MouseEvent e) {
            visualElement.setPos(raster(visualElement.getPos()));
            mouseNormal.activate();
        }

        @Override
        void moved(MouseEvent e) {
            Vector pos = getPosVector(e);
            visualElement.setPos(raster(pos.add(delta)));
            circuit.modified();
            hasChanged();
        }

        @Override
        public void drawTo(Graphic gr) {
            visualElement.drawTo(gr, true);
        }

        @Override
        public void delete() {
            circuit.delete(visualElement);
            mouseNormal.activate();
            isManualScale = true;
        }

        @Override
        public void rotate() {
            visualElement.rotate();
            circuit.modified();
            hasChanged();
        }

        @Override
        public void escapePressed() {
            visualElement.setPos(raster(initialPos));
            visualElement.setRotation(initialRot);
            mouseNormal.activate();
        }
    }

    private final class MouseControllerMoveWire extends MouseController {
        private Wire wire;
        private Vector pos;
        private Vector initialPos;

        private MouseControllerMoveWire(Cursor cursor) {
            super(cursor);
        }

        private void activate(Wire wire, Vector pos) {
            super.activate();
            this.wire = wire;
            this.pos = raster(pos);
            this.initialPos = this.pos;
            deleteAction.setActive(true);
            removeHighLighted();
            hasChanged();
        }

        @Override
        void clicked(MouseEvent e) {
            removeHighLighted();
            circuit.elementsMoved();
            mouseNormal.activate();
        }

        @Override
        void moved(MouseEvent e) {
            Vector pos = raster(getPosVector(e));
            final Vector delta = pos.sub(this.pos);
            if (!delta.isZero()) {
                wire.move(delta);
                wire.noDot();
                isManualScale = true;
                circuit.modified();
                hasChanged();
            }
            this.pos = pos;
        }

        @Override
        public void delete() {
            circuit.delete(wire);
            mouseNormal.activate();
            isManualScale = true;
        }

        @Override
        public void drawTo(Graphic gr) {
            // ensure that highlighted wire is visible by drawing it on top of other drawings.
            wire.drawTo(gr, true);
        }

        @Override
        public void escapePressed() {
            wire.move(initialPos.sub(pos));
            removeHighLighted();
            circuit.elementsMoved();
            mouseNormal.activate();
        }
    }


    private final class MouseControllerWire extends MouseController {
        private Wire wire;

        private MouseControllerWire(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector startPos) {
            super.activate();
            Vector pos = raster(startPos);
            wire = new Wire(pos, pos);
        }

        @Override
        void moved(MouseEvent e) {
            wire.setP2(raster(getPosVector(e)));
            repaint();
        }

        @Override
        void clicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3)
                mouseNormal.activate();
            else {
                circuit.add(wire);
                if (circuit.isPinPos(wire.p2))
                    mouseNormal.activate();
                else
                    wire = new Wire(wire.p2, wire.p2);
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            wire.drawTo(gr, false);
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }
    }

    private final class MouseControllerSelect extends MouseController {
        private static final int MIN_SIZE = 8;
        private Vector corner1;
        private Vector corner2;
        private boolean wasReleased;

        private MouseControllerSelect(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector corner1, Vector corner2) {
            super.activate();
            this.corner1 = corner1;
            this.corner2 = corner2;
            deleteAction.setActive(true);
            copyAction.setEnabled(true);
            rotateAction.setEnabled(true);
            wasReleased = false;
        }

        @Override
        void clicked(MouseEvent e) {
            mouseNormal.activate();
            removeHighLighted();
        }

        @Override
        void released(MouseEvent e) {
            wasReleased = true;
            Vector dif = corner1.sub(corner2);
            if (Math.abs(dif.x) > MIN_SIZE && Math.abs(dif.y) > MIN_SIZE)
                setCursor(moveCursor);
            else {
                removeHighLighted();
                mouseNormal.activate();
            }
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (wasReleased) {
                mouseMoveSelected.activate(corner1, corner2, getPosVector(e));
            } else {
                corner2 = getPosVector(e);
                if ((e.getModifiersEx() & CTRL_DOWN_MASK) != 0) {
                    Vector dif = corner2.sub(corner1);
                    int dx = dif.x;
                    int dy = dif.y;
                    int absDx = Math.abs(dx);
                    int absDy = Math.abs(dy);
                    if (absDx != absDy) {
                        if (absDx > absDy) {
                            if (dx > absDy) dx = absDy;
                            else dx = -absDy;
                        } else {
                            if (dy > absDx) dy = absDx;
                            else dy = -absDx;
                        }
                    }
                    corner2 = corner1.add(dx, dy);
                }

                ArrayList<Drawable> elements = circuit.getElementsToHighlight(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
                removeHighLighted();
                if (elements != null)
                    addHighLighted(elements);

                repaint();
            }
            return true;
        }

        @Override
        public void drawTo(Graphic gr) {
            Vector p1 = new Vector(corner1.x, corner2.y);
            Vector p2 = new Vector(corner2.x, corner1.y);
            gr.drawLine(corner1, p1, Style.DASH);
            gr.drawLine(p1, corner2, Style.DASH);
            gr.drawLine(p2, corner2, Style.DASH);
            gr.drawLine(corner1, p2, Style.DASH);
        }

        @Override
        public void delete() {
            circuit.delete(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
            mouseNormal.activate();
            isManualScale = true;
        }

        public void rotate() {
            mouseMoveSelected.activate(corner1, corner2, lastMousePos);
            mouseMoveSelected.rotate();
        }

        @Override
        public void escapePressed() {
            removeHighLighted();
            mouseNormal.activate();
        }
    }

    private void rotateElements(ArrayList<Movable> elements, Vector pos) {
        Vector p1 = raster(pos);

        Transform transform = new TransformRotate(p1, 1) {
            @Override
            public Vector transform(Vector v) {
                return super.transform(v.sub(p1));
            }
        };

        for (Movable m : elements) {

            if (m instanceof VisualElement) {
                VisualElement ve = (VisualElement) m;
                ve.rotate();
                ve.setPos(transform.transform(ve.getPos()));
            } else if (m instanceof Wire) {
                Wire w = (Wire) m;
                w.p1 = transform.transform(w.p1);
                w.p2 = transform.transform(w.p2);
            } else {
                Vector p = m.getPos();
                Vector t = transform.transform(p);
                m.move(t.sub(p));
            }

        }

        circuit.modified();
        hasChanged();
    }


    private final class MouseControllerMoveSelected extends MouseController {
        private ArrayList<Movable> elements;
        private Vector lastPos;
        private Vector center;
        private boolean wasMoved;

        private MouseControllerMoveSelected(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector corner1, Vector corner2, Vector pos) {
            super.activate();
            rotateAction.setEnabled(true);
            lastPos = pos;
            center = corner1.add(corner2).div(2);
            wasMoved = false;
            elements = circuit.getElementsToMove(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
        }

        @Override
        void moved(MouseEvent e) {
            lastPos = getPosVector(e);
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (elements != null) {
                Vector pos = getPosVector(e);
                Vector delta = raster(pos.sub(lastPos));

                if (delta.x != 0 || delta.y != 0) {
                    for (Movable m : elements)
                        m.move(delta);
                    wasMoved = true;

                    hasChanged();
                    lastPos = lastPos.add(delta);
                    center = center.add(delta);
                }
            }
            return true;
        }

        @Override
        void released(MouseEvent e) {
            if (wasMoved)
                circuit.elementsMoved();
            removeHighLighted();
            mouseNormal.activate();
        }

        @Override
        public void rotate() {
            rotateElements(elements, center);
        }
    }

    private final class MouseControllerInsertCopied extends MouseController {
        private ArrayList<Movable> elements;
        private Vector lastPos;

        private MouseControllerInsertCopied(Cursor cursor) {
            super(cursor);
        }

        private void activate(ArrayList<Movable> elements, Vector pos) {
            super.activate();
            this.elements = elements;
            lastPos = pos;
            deleteAction.setActive(true);
            rotateAction.setEnabled(true);
        }

        @Override
        void moved(MouseEvent e) {
            if (elements != null) {
                Vector pos = getPosVector(e);
                Vector delta = raster(pos.sub(lastPos));

                if (delta.x != 0 || delta.y != 0) {
                    for (Movable m : elements)
                        m.move(delta);

                    repaint();
                    lastPos = lastPos.add(delta);
                }
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            if (elements != null)
                for (Movable m : elements)
                    if (m instanceof Drawable)
                        ((Drawable) m).drawTo(gr, true);
        }

        @Override
        public void delete() {
            mouseNormal.activate();
        }

        @Override
        void clicked(MouseEvent e) {
            if (elements != null && e.getButton() == 1) {
                for (Movable m : elements) {
                    if (m instanceof Wire)
                        circuit.add((Wire) m);
                    if (m instanceof VisualElement)
                        circuit.add((VisualElement) m);
                }
            }
            mouseNormal.activate();
            focusWasLost = false;
        }

        @Override
        public void rotate() {
            rotateElements(elements, lastPos);
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }
    }


    private interface Actor {
        boolean interact(CircuitComponent cc, Point p, Vector posInComponent, Sync modelSync);
    }

    private final class MouseControllerRun extends MouseController {

        private boolean dragHandled;

        private MouseControllerRun(Cursor cursor) {
            super(cursor);
        }

        @Override
        void pressed(MouseEvent e) {
            VisualElement ve = getInteractableElementAt(e);
            if (ve != null) {
                interact(e, ve::elementPressed);
                dragHandled = true;
            } else
                dragHandled = false;
        }

        private VisualElement getInteractableElementAt(MouseEvent e) {
            List<VisualElement> elementList = circuit.getElementListAt(getPosVector(e), false);
            for (VisualElement ve : elementList) {
                if (ve.isInteractable())
                    return ve;
            }
            return null;
        }

        @Override
        void released(MouseEvent e) {
            VisualElement ve = getInteractableElementAt(e);
            if (ve != null)
                interact(e, ve::elementReleased);
        }

        @Override
        void clicked(MouseEvent e) {
            VisualElement ve = getInteractableElementAt(e);
            if (ve != null)
                interact(e, ve::elementClicked);
        }

        @Override
        boolean dragged(MouseEvent e) {
            VisualElement ve = getInteractableElementAt(e);
            if (ve != null)
                interact(e, ve::elementDragged);
            return dragHandled;
        }

        private void interact(MouseEvent e, Actor actor) {
            Point p = new Point(e.getX(), e.getY());
            SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
            boolean modelHasChanged = actor.interact(CircuitComponent.this, p, getPosVector(e), modelSync);
            if (modelHasChanged) {
                if (manualChangeObserver != null)
                    manualChangeObserver.hasChanged();
            } else
                hasChanged();
        }
    }

}
