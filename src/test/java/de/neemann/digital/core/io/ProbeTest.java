package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Signal;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Make sure that probes are correctly named and added to the list of signals.
 * Created by hneemann on 08.03.17.
 */
public class ProbeTest extends TestCase {

    public void testProbe() throws Exception {
        Model m = new ToBreakRunner("dig/probe/probe.dig").getModel();
        ArrayList<Signal> signals = m.getSignals();
        assertEquals(5, signals.size());

        assertTrue(signals.contains(new Signal("M-Probe1", null)));
        assertTrue(signals.contains(new Signal("M-Probe2", null)));
    }

    public void testProbeNesting() throws Exception {
        Model m = new ToBreakRunner("dig/probe/probeNest.dig").getModel();
        ArrayList<Signal> signals = m.getSignals();
        assertEquals(6, signals.size());

        assertTrue(signals.contains(new Signal("M-Probe1", null)));
        assertTrue(signals.contains(new Signal("M-Nest-Probe1", null)));
        assertTrue(signals.contains(new Signal("M-Nest-Probe2", null)));
    }
}