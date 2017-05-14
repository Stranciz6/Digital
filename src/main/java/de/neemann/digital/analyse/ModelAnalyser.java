package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.BitSetter;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import de.neemann.digital.core.*;
import de.neemann.digital.core.basic.And;
import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.basic.Or;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.flipflops.FlipflopD;
import de.neemann.digital.core.flipflops.FlipflopJK;
import de.neemann.digital.core.flipflops.FlipflopT;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyses a given model.
 * Calculates the truth table which is generated by the given model
 *
 * @author hneemann
 */
public class ModelAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelAnalyser.class);
    private static final int MAX_INPUTS_ALLOWED = 24;

    private final Model model;
    private final ArrayList<Signal> inputs;
    private final ArrayList<Signal> outputs;
    private final int rows;

    /**
     * Creates a new instance
     *
     * @param model the model
     * @throws AnalyseException AnalyseException
     */
    public ModelAnalyser(Model model) throws AnalyseException {
        this.model = model;

        try {
            replaceJKFF();
            replaceTFF();
        } catch (NodeException e) {
            throw new AnalyseException(e);
        }

        inputs = checkBinaryInputs(model.getInputs());
        checkUnique(inputs);
        outputs = checkBinaryOutputs(model.getOutputs());

        for (Node n : model)
            if (n.hasState() && !(n instanceof FlipflopD))
                throw new AnalyseException(Lang.get("err_cannotAnalyse_N", n.getClass().getSimpleName()));

        int i = 0;
        List<FlipflopD> flipflops = model.findNode(FlipflopD.class);
        flipflops = replaceMultiBitFlipflops(flipflops);
        for (FlipflopD ff : flipflops) {
            checkClock(ff);
            if (ff.getBits() != 1)
                throw new AnalyseException(Lang.get("err_MultiBitFlipFlopFound"));

            ff.getDInput().removeObserver(ff); // turn off flipflop
            String label = ff.getLabel();
            if (label.length() == 0)
                throw new AnalyseException(Lang.get("err_DFlipflopWithoutALabel"));

            if (!label.endsWith("n"))
                label += "n";

            outputs.add(i++, new Signal(label + "+1", ff.getDInput()));

            ObservableValue q = ff.getOutputs().get(0);
            final Signal sig = new Signal(label, q);
            if (inputs.contains(sig))
                throw new AnalyseException(Lang.get("err_varName_N_UsedTwice", sig.getName()));
            inputs.add(sig);

            ObservableValue notQ = ff.getOutputs().get(1);
            q.addObserver(() -> notQ.setValue(~q.getValue()));
        }

        if (inputs.size() == 0)
            throw new AnalyseException(Lang.get("err_analyseNoInputs"));
        if (inputs.size() > MAX_INPUTS_ALLOWED)
            throw new AnalyseException(Lang.get("err_toManyInputs_max_N0_is_N1", MAX_INPUTS_ALLOWED, inputs.size()));
        if (outputs.size() == 0)
            throw new AnalyseException(Lang.get("err_analyseNoOutputs"));
        rows = 1 << inputs.size();
    }

    private void checkUnique(ArrayList<Signal> signals) throws AnalyseException {
        for (int i = 0; i < signals.size() - 1; i++)
            for (int j = i + 1; j < signals.size(); j++)
                if (signals.get(i).equals(signals.get(j)))
                    throw new AnalyseException(Lang.get("err_varName_N_UsedTwice", signals.get(i).getName()));
    }

    private void checkClock(Node node) throws AnalyseException {
        if (!getClock().hasObserver(node))
            throw new AnalyseException(Lang.get("err_ffNeedsToBeConnectedToClock"));
    }

    private ObservableValue getClock() throws AnalyseException {
        ArrayList<Clock> clocks = model.getClocks();
        if (clocks.size() != 1)
            throw new AnalyseException(Lang.get("err_aSingleClockNecessary"));
        return clocks.get(0).getClockOutput();
    }

    private List<FlipflopD> replaceMultiBitFlipflops(List<FlipflopD> flipflops) throws AnalyseException {
        ArrayList<FlipflopD> out = new ArrayList<>();
        for (FlipflopD ff : flipflops) {
            if (ff.getBits() == 1)
                out.add(ff);
            else {
                try {
                    model.removeNode(ff);
                    ff.getDInput().removeObserver(ff);
                    ff.getClock().removeObserver(ff);

                    Splitter insp = Splitter.createOneToN(ff.getBits());
                    insp.setInputs(new ObservableValues(ff.getDInput()));

                    Splitter outsp = Splitter.createNToOne(ff.getBits());
                    Splitter noutsp = Splitter.createNToOne(ff.getBits());


                    ObservableValues.Builder spinput = new ObservableValues.Builder();
                    ObservableValues.Builder spninput = new ObservableValues.Builder();
                    for (int i = 0; i < ff.getBits(); i++) {
                        ObservableValue qn = new ObservableValue("", 1);
                        ObservableValue nqn = new ObservableValue("", 1);
                        FlipflopD newff = new FlipflopD(ff.getLabel() + i, qn, nqn);
                        spinput.add(qn);
                        spninput.add(nqn);
                        newff.setInputs(new ObservableValues(insp.getOutputs().get(i), getClock()));
                        model.add(newff);
                        out.add(newff);
                    }
                    outsp.setInputs(spinput.build());
                    noutsp.setInputs(spninput.build());

                    final FlipflopD oldff = ff;
                    ObservableValue spq = outsp.getOutputs().get(0);
                    spq.addObserver(() -> oldff.getOutputs().get(0).setValue(spq.getValue()));
                    ObservableValue spnq = noutsp.getOutputs().get(0);
                    spnq.addObserver(() -> oldff.getOutputs().get(1).setValue(spnq.getValue()));

                } catch (NodeException e) {
                    throw new AnalyseException(e);
                }
            }
        }
        return out;
    }


    private ArrayList<Signal> checkBinaryOutputs(ArrayList<Signal> list) throws AnalyseException {
        ArrayList<Signal> outputs = new ArrayList<>();
        for (Signal s : list) {
            final int bits = s.getValue().getBits();
            if (bits == 1)
                outputs.add(s);
            else {
                try {
                    Splitter sp = Splitter.createOneToN(bits);
                    sp.setInputs(s.getValue().asList());
                    int i = 0;
                    for (ObservableValue out : sp.getOutputs()) {
                        outputs.add(new Signal(s.getName() + i, out));
                        i++;
                    }
                } catch (NodeException e) {
                    throw new AnalyseException(e);
                }
            }
        }
        return outputs;
    }

    private ArrayList<Signal> checkBinaryInputs(ArrayList<Signal> list) throws AnalyseException {
        ArrayList<Signal> inputs = new ArrayList<>();
        for (Signal s : list) {
            final int bits = s.getValue().getBits();
            if (bits == 1)
                inputs.add(s);
            else {
                try {
                    Splitter sp = Splitter.createNToOne(bits);
                    final ObservableValue out = sp.getOutputs().get(0);
                    out.addObserver(() -> s.getValue().setValue(out.getValue()));

                    ObservableValues.Builder builder = new ObservableValues.Builder();
                    for (int i = 0; i < bits; i++) {
                        ObservableValue o = new ObservableValue(s.getName() + i, 1);
                        builder.add(o);
                        inputs.add(new Signal(s.getName() + i, o));
                    }
                    sp.setInputs(builder.build());
                } catch (NodeException e) {
                    throw new AnalyseException(e);
                }
            }
        }
        return inputs;
    }


    /**
     * Analyses the circuit
     *
     * @return the generated truth table
     * @throws NodeException NodeException
     */
    public TruthTable analyse() throws NodeException {
        LOGGER.debug("start to analyse the model...");
        long time = System.currentTimeMillis();
        BitSetter bitsetter = new BitSetter(inputs.size()) {
            @Override
            public void setBit(int row, int bit, boolean value) {
                inputs.get(bit).getValue().setBool(value);
            }
        };

        TruthTable tt = new TruthTable();
        for (Signal s : inputs)
            tt.addVariable(s.getName());

        for (Signal s : inputs)
            tt.addPinNumber(s);
        for (Signal s : outputs)
            tt.addPinNumber(s);

        ArrayList<BoolTableByteArray> data = new ArrayList<>();
        for (Signal s : outputs) {
            BoolTableByteArray e = new BoolTableByteArray(rows);
            data.add(e);
            tt.addResult(s.getName(), e);
        }

        model.init();
        for (int row = 0; row < rows; row++) {
            bitsetter.fill(row);
            model.doStep();
            for (int i = 0; i < outputs.size(); i++) {
                data.get(i).set(row, outputs.get(i).getValue().getBool());
            }
        }
        time = System.currentTimeMillis() - time;
        LOGGER.debug("model analysis: " + time / 1000.0 + " sec");
        return tt;
    }

    private void replaceJKFF() throws NodeException, AnalyseException {
        List<FlipflopJK> jkList = model.findNode(FlipflopJK.class);

        for (FlipflopJK jk : jkList) {
            checkClock(jk);

            // remove JK-ff from model
            model.removeNode(jk);
            jk.getClockVal().removeObserver(jk);
            jk.getjVal().removeObserver(jk);
            jk.getkVal().removeObserver(jk);

            // create d ff
            ObservableValue q = jk.getOutputs().get(0);
            ObservableValue qn = jk.getOutputs().get(1);
            FlipflopD d = new FlipflopD(jk.getLabel(), q, qn);

            And a1 = new And(new ElementAttributes());
            a1.setInputs(new ObservableValues(jk.getjVal(), qn));
            And a2 = new And(new ElementAttributes());
            Not nk = new Not(new ElementAttributes());
            nk.setInputs(jk.getkVal().asList());
            a2.setInputs(new ObservableValues(nk.getOutput(), q));

            Or or = new Or(new ElementAttributes());
            or.setInputs(new ObservableValues(a1.getOutput(), a2.getOutput()));

            d.setInputs(new ObservableValues(or.getOutputs().get(0), jk.getClockVal()));

            model.add(a1);
            model.add(a2);
            model.add(nk);
            model.add(or);
            model.add(d);
        }
    }

    private void replaceTFF() throws NodeException, AnalyseException {
        List<FlipflopT> jkList = model.findNode(FlipflopT.class);

        for (FlipflopT tff : jkList) {
            checkClock(tff);

            // remove T-ff from model
            model.removeNode(tff);
            tff.getClockVal().removeObserver(tff);

            // create d ff
            ObservableValue q = tff.getOutputs().get(0);
            ObservableValue qn = tff.getOutputs().get(1);
            FlipflopD d = new FlipflopD(tff.getLabel(), q, qn);

            d.setInputs(new ObservableValues(qn, getClock()));

            model.add(d);
        }
    }


}
