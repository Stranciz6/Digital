package de.neemann.digital.analyse;

import de.neemann.digital.core.*;
import de.neemann.digital.core.Observer;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import java.util.*;

import static de.neemann.digital.core.Model.MAX_LOOP_COUNTER;

/**
 * Used to analyse on which inputs a given output depends.
 * So you only have to take into account the inputs, a given outputs
 * depends on.
 * Created by hneemann on 11.06.17.
 */
public class DependencyAnalyser {

    private final HashMap<Signal, Set<ObservableValue>> dependencyMap;

    /**
     * Creates a new instance
     *
     * @param modelAnalyser the model analyser
     * @throws BacktrackException BacktrackException
     * @throws PinException       PinException
     */
    public DependencyAnalyser(ModelAnalyser modelAnalyser) throws BacktrackException, PinException {
        dependencyMap = new HashMap<>();
        for (Signal s : modelAnalyser.getInputs()) {
            Set<ObservableValue> effected = new HashSet<>();
            backtracking(s.getValue(), effected, MAX_LOOP_COUNTER);
            dependencyMap.put(s, effected);
        }
    }

    /**
     * Returns all inputs the given output depends on
     *
     * @param output the output to analyse
     * @return the list of inputs which effect the given output
     */
    public ArrayList<Signal> getInputs(Signal output) {
        ArrayList<Signal> list = new ArrayList<>();
        for (Map.Entry<Signal, Set<ObservableValue>> e : dependencyMap.entrySet()) {
            if (e.getValue().contains(output.getValue()))
                list.add(e.getKey());
        }
        return list;
    }

    private void backtracking(ObservableValue value, Set<ObservableValue> effected, int depth) throws PinException, BacktrackException {
        effected.add(value);

        if (depth < 0)
            throw new BacktrackException(Lang.get("err_backtrackLoopFound"));

        for (Observer o : value) {
            if ((o instanceof NodeInterface)) {
                ObservableValues outputs = ((NodeInterface) o).getOutputs();
                for (ObservableValue co : outputs)
                    backtracking(co, effected, depth - 1);
            } else
                throw new BacktrackException(Lang.get("err_backtrackOf_N_isImpossible", o.getClass().getSimpleName()));
        }
    }

}
