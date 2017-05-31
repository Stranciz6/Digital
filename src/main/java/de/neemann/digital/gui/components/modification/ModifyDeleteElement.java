package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;

/**
 * Modifier which deletes an element
 * Created by hneemann on 26.05.17.
 */
public class ModifyDeleteElement extends ModificationOfVisualElement {

    /**
     * The element to delete
     *
     * @param ve         the visual element
     * @param initialPos its initial position
     */
    public ModifyDeleteElement(VisualElement ve, Vector initialPos) {
        super(ve, initialPos, Lang.get("mod_deletedElement_N", getTranslatedName(ve)));
    }

    @Override
    public void modify(Circuit circuit) {
        circuit.delete(getVisualElement(circuit));
    }
}
