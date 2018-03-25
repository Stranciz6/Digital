/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.hdl.vhdl2.boards.BoardInterface;
import de.neemann.digital.hdl.vhdl2.boards.BoardProvider;
import de.neemann.digital.lang.Lang;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Used to create the vhdl output
 */
public class VHDLGenerator implements Closeable {

    private final ElementLibrary library;
    private final CodePrinter out;
    private ArrayList<File> testBenches;
    private boolean useClockIntegration = true;

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
     */
    public VHDLGenerator(ElementLibrary library, CodePrinter out) {
        this.library = library;
        this.out = out;
    }

    /**
     * Exports the given circuit
     *
     * @param circuit the circuit to export
     * @return this for chained calls
     * @throws IOException IOException
     */
    public VHDLGenerator export(Circuit circuit) throws IOException {
        try {

            if (!circuit.getAttributes().get(Keys.ROMMANAGER).isEmpty())
                throw new HDLException(Lang.get("err_centralDefinedRomsAreNotSupported"));

            BoardInterface board = BoardProvider.getInstance().getBoard(circuit);

            HDLClockIntegrator clockIntegrator = null;
            if (board != null && useClockIntegration)
                clockIntegrator = board.getClockIntegrator();

            HDLModel model = new HDLModel(library).create(circuit, clockIntegrator);
            for (HDLCircuit hdlCircuit : model)
                hdlCircuit
                        .mergeConstants()
                        .mergeExpressions()
                        .nameNets();

            model.renameLabels(new VHDLRenaming());

            out.println("-- generated by Digital. Don't modify this file!");
            out.println("-- Any changes will be lost if this file is regenerated.");

            new VHDLCreator(out).printHDLCircuit(model.getMain());

            File outFile = out.getFile();
            if (outFile != null) {
                testBenches = new VHDLTestBenchCreator(circuit, model)
                        .write(outFile)
                        .getTestFileWritten();

                if (board != null)
                    board.writeFiles(outFile, model);
            }

            return this;
        } catch (PinException | NodeException | HDLException | HGSEvalException e) {
            throw new IOException(Lang.get("err_vhdlExporting"), e);
        }
    }

    /**
     * @return the test bench files, maybe null
     */
    public ArrayList<File> getTestBenches() {
        return testBenches;
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
     * Disables the clock integration.
     * Used only for the tests.
     *
     * @return this for chained calls
     */
    public VHDLGenerator disableClockIntegration() {
        useClockIntegration = false;
        return this;
    }
}
