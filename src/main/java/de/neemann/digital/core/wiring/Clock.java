package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class Clock implements Element {

    /**
     * the clocks description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("Clock", Clock.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.RUN_AT_REAL_TIME)
            .addAttribute(Keys.FREQUENCY);

    private final ObservableValue output;
    private final int frequency;
    private final String label;

    /**
     * Creates a new instance
     *
     * @param attributes the clocks attributes
     */
    public Clock(ElementAttributes attributes) {
        output = new ObservableValue("C", 1);
        if (attributes.get(Keys.RUN_AT_REAL_TIME)) {
            int f = attributes.get(Keys.FREQUENCY);
            if (f < 1) f = 1;
            frequency = f;
        } else
            frequency = 0;
        label = attributes.get(Keys.LABEL);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"));
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
        model.addClock(this);
        model.addSignal(new Signal(label, output));
    }

    /**
     * @return the clock output value
     */
    public ObservableValue getClockOutput() {
        return output;
    }

    /**
     * @return the clocks frequency
     */
    public int getFrequency() {
        return frequency;
    }

}
