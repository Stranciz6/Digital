package de.neemann.gui.language;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author hneemann
 */
public class ResourcesTest extends TestCase {
    private static final String example
            = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<resources>\n" +
            "  <string name=\"menu_save\">Speichern</string>\n" +
            "  <string name=\"menu_open\">Öffnen</string>\n" +
            "</resources>";

    public void testWrite() throws Exception {
        Resources res = new Resources();
        res.put("menu_open", "Öffnen");
        res.put("menu_save", "Speichern");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        res.save(baos);
        assertEquals(example, baos.toString());
    }

    public void testRead() throws Exception {
        Resources res = new Resources(new ByteArrayInputStream(example.getBytes()));

        assertEquals("Öffnen", res.get("menu_open"));
        assertEquals("Speichern", res.get("menu_save"));
    }
}