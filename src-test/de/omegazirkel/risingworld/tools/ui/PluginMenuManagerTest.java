package de.omegazirkel.risingworld.tools.ui;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class PluginMenuManagerTest {
    @Test
    public void dispatchesOnlyTheFirstCallbackForOneMenuDisplay() {
        AtomicInteger calls = new AtomicInteger();

        var once = PluginMenuManager.once(ignored -> calls.incrementAndGet());
        once.onCall(0);
        once.onCall(0);

        assertEquals(1, calls.get());
    }
}
