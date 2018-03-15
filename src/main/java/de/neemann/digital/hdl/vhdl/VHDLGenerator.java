/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.External;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.Ground;
import de.neemann.digital.core.io.VDD;
import de.neemann.digital.core.pld.PullDown;
import de.neemann.digital.core.pld.PullUp;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model.*;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.hdl.vhdl.boards.BoardInterface;
import de.neemann.digital.hdl.vhdl.boards.BoardProvider;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.LineBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Exports the given circuit to vhdl
 */
public class VHDLGenerator implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(VHDLGenerator.class);
    private final CodePrinter out;
    private final ElementLibrary library;
    private final VHDLLibrary vhdlLibrary;
    private VHDLTestBenchCreator testBenches;
    private int nodesWritten;
    private boolean omitClockDividers = false;

    /**
     * Creates a new exporter
     *
     * @param library the library
     * @throws IOException IOException
     */
    public VHDLGenerator(ElementLibrary library) throws IOException {
        this(library, new CodePrinterStr());
    }

    /**
     * Creates a new exporter
     *
     * @param library the library
     * @param out     the output stream
     * @throws IOException IOException
     */
    public VHDLGenerator(ElementLibrary library, CodePrinter out) throws IOException {
        this.library = library;
        this.out = out;
        vhdlLibrary = new VHDLLibrary();
    }

    /**
     * If called the integration of clock dividers and so on is omitted.
     * Mainly used for tests.
     *
     * @return this for chained calls
     */
    public VHDLGenerator omitClockDividers() {
        this.omitClockDividers = true;
        return this;
    }

    /**
     * Writes the file to the given stream
     *
     * @param circuit the circuit to export
     * @return this for chained calls
     * @throws IOException IOException
     */
    public VHDLGenerator export(Circuit circuit) throws IOException {
        out.println("-- generated by Digital. Don't modify this file!");
        out.println("-- Any changes will be lost if this file is regenerated.\n");

        try {

            if (!circuit.getAttributes().get(Keys.ROMMANAGER).isEmpty())
                throw new HDLException(Lang.get("err_centralDefinedRomsAreNotSupported"));

            BoardInterface board = BoardProvider.getInstance().getBoard(circuit);

            ModelList modelList = new ModelList(library);
            HDLModel model = new HDLModel(circuit, library, modelList).setName("main");

            if (!omitClockDividers
                    && model.getClocks() != null
                    && model.getClocks().size() > 0
                    && board != null)
                board.getClockIntegrator().integrateClocks(model);

            export(model);

            for (HDLModel m : modelList) {
                out.println();
                out.println("-- " + m.getName());
                out.println();
                if (m.getClocks() != null)
                    throw new HDLException(Lang.get("err_vhdlClockOnlyAllowedInRoot"));

                export(m);
                nodesWritten++;
            }

            nodesWritten += vhdlLibrary.finish(out);

            File outFile = out.getFile();
            if (board != null && outFile != null)
                board.writeFiles(outFile, model);

            if (outFile != null) {
                testBenches = new VHDLTestBenchCreator(circuit, model);
                testBenches.write(out.getFile());
            }

            if (outFile != null)
                LOGGER.info("exported " + outFile + " (" + nodesWritten + " nodes)");
        } catch (HDLException | PinException | NodeException e) {
            e.setOrigin(circuit.getOrigin());
            throw new IOException(Lang.get("err_vhdlExporting"), e);
        } catch (ElementNotFoundException e) {
            throw new IOException(Lang.get("err_vhdlExporting"), e);
        }
        return this;
    }

    private void export(HDLModel model) throws HDLException, NodeException, IOException {
        try {
            SplitterHandler splitterHandler = new SplitterHandler(model, out);

            writeComment(out, model.getDescription(), model);

            out.println("LIBRARY ieee;");
            out.println("USE ieee.std_logic_1164.all;");
            out.println("USE ieee.numeric_std.all;\n");
            out.print("entity ").print(model.getName()).println(" is").inc();
            writePort(out, model.getPorts());
            out.dec().println("end " + model.getName() + ";");

            out.println("\narchitecture " + model.getName() + "_arch of " + model.getName() + " is");

            HashSet<String> componentsWritten = new HashSet<>();
            for (HDLNode node : model)
                if (node.is(Splitter.DESCRIPTION))
                    splitterHandler.register(node);
                else if (!isConstant(node)) {
                    String nodeName = getVhdlEntityName(node);
                    if (!componentsWritten.contains(nodeName)) {
                        writeComponent(node);
                        componentsWritten.add(nodeName);
                    }
                }

            out.println().inc();
            for (Signal sig : model.getSignals()) {
                if (!sig.isPort()) {
                    out.print("signal ");
                    out.print(sig.getName());
                    out.print(": ");
                    out.print(getType(sig.getBits()));
                    out.println(";");
                }
            }

            out.dec().println("begin").inc();

            for (Signal s : model.getSignals()) {
                if (s.isConstant()) {
                    s.setIsWritten();
                    out.print(s.getName());
                    out.print(" <= ");
                    out.print(s.getConstant().vhdlValue());
                    out.println(";");
                }
            }

            splitterHandler.write();

            int g = 0;
            for (HDLNode node : model)
                if (!node.is(Splitter.DESCRIPTION) && !isConstant(node)) {
                    out.print("gate").print(g++).print(" : ").println(getVhdlEntityName(node)).inc();
                    vhdlLibrary.writeGenericMap(out, node);
                    writePortMap(node);
                    out.dec();
                }

            // map signals to output ports
            for (Port p : model.getPorts().getOutputs())
                out.print(p.getName()).print(" <= ").print(p.getSignal().getName()).println(";");

            out.dec().print("end ").print(model.getName()).println("_arch;");
        } catch (HDLException | NodeException e) {
            e.setOrigin(model.getOrigin());
            throw e;
        }
    }

    private static void writeComment(CodePrinter out, String descr, HDLInterface entity) throws IOException {
        if (descr != null && descr.length() > 0)
            out.print("-- ").print(new LineBreaker().setLineBreak("\n-- ").breakLines(descr)).eol();


        Ports ports = entity.getPorts();
        boolean isPortComment = false;
        for (Port p : ports)
            if (p.getDescription() != null && p.getDescription().length() > 0) {
                isPortComment = true;
                break;
            }

        if (isPortComment) {
            out.println("--");
            boolean isInput = false;
            for (Port p : ports) {
                if (p.getDirection() == Port.Direction.in) {
                    if (!isInput) {
                        out.print("-- Inputs:").eol();
                        isInput = true;
                    }
                    writePortComment(out, p);
                }
            }
            boolean isOutput = false;
            for (Port p : ports) {
                if (p.getDirection() == Port.Direction.out) {
                    if (!isOutput) {
                        out.print("-- Outputs:").eol();
                        isOutput = true;
                    }
                    writePortComment(out, p);
                }
            }
        }
    }

    private static void writePortComment(CodePrinter out, Port p) throws IOException {
        out.print("--   ");
        out.println(new LineBreaker(p.getName(), 15, 70).setLineBreak("\n--   ").breakLines(p.getDescription()));
    }

    private boolean isConstant(HDLNode node) {
        return node.is(Ground.DESCRIPTION)
                || node.is(VDD.DESCRIPTION)
                || node.is(PullUp.DESCRIPTION)
                || node.is(PullDown.DESCRIPTION)
                || node.is(Const.DESCRIPTION);
    }

    private void writePortMap(HDLNode node) throws HDLException, IOException {
        boolean useOrigNames = false;
        final VisualElement visualElement = node.getVisualElement();
        if (visualElement != null)
            useOrigNames = visualElement.equalsDescription(External.DESCRIPTION);

        out.println("port map (").inc();
        Separator comma = new Separator(",\n");
        for (Port p : node.getPorts()) {
            if (p.getSignal() != null) {
                comma.check(out);
                if (useOrigNames)
                    out.print(p.getOrigName());
                else
                    out.print(p.getName());
                out.print(" => " + p.getSignal().getName());
                if (p.getDirection() == Port.Direction.out)
                    p.getSignal().setIsWritten();
            }
        }
        out.println(" );").dec();
    }

    private String getVhdlEntityName(HDLNode node) throws HDLException {
        if (node.isCustom())
            return node.getHDLName();
        else
            return vhdlLibrary.getName(node);
    }

    private void writeComponent(HDLNode node) throws HDLException, IOException {
        out.println().inc();
        out.print("component ").println(getVhdlEntityName(node)).inc();
        vhdlLibrary.writeDeclaration(out, node);
        out.dec().println("end component;").dec();
    }

    /**
     * Writes the given ports to the output
     *
     * @param out   the output stream
     * @param ports the ports
     * @throws HDLException HDLException
     * @throws IOException  IOException
     */
    public static void writePort(CodePrinter out, Ports ports) throws HDLException, IOException {
        out.println("port (");
        Separator semic = new Separator(";\n");
        for (Port p : ports) {
            semic.check(out);
            out.print("  ");
            out.print(p.getName());
            out.print(": ");
            out.print(getDirection(p));
            out.print(" ");
            out.print(getType(p.getBits()));
        }
        out.println(" );");
    }

    /**
     * returns the vhdl type
     *
     * @param bits the number of bits
     * @return the type
     * @throws HDLException HDLException
     */
    public static String getType(int bits) throws HDLException {
        if (bits == 0)
            throw new HDLException(Lang.get("err_vhdlBitNumberNotAvailable"));
        if (bits == 1)
            return "std_logic";
        else
            return "std_logic_vector (" + (bits - 1) + " downto 0)";
    }

    /**
     * Returns the VHDL direction qualifier
     *
     * @param p the port
     * @return the direction
     * @throws HDLException HDLException
     */
    public static String getDirection(Port p) throws HDLException {
        switch (p.getDirection()) {
            case in:
                return "in";
            case out:
                return "out";
            default:
                throw new HDLException(Lang.get("err_vhdlUnknownPortType_N", p.getDirection().toString()));
        }
    }

    @Override
    public String toString() {
        return out.toString();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    /**
     * @return the test bench creator
     */
    public VHDLTestBenchCreator getTestBenches() {
        return testBenches;
    }

}
