package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.PartDescription;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class NAnd extends Function {

    public NAnd(int bits) {
        super(bits);
    }

    public static PartDescription createFactory(int bits, int inputs) {
        return new FanInDescription(inputs, () -> new NAnd(bits));
    }

    @Override
    protected int calculate(ArrayList<ObservableValue> inputs) throws NodeException {
        int f = -1;
        for (ObservableValue i : inputs) {
            f &= i.getValue();
        }
        return ~f;
    }
}
