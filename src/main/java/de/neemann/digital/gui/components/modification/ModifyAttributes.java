package de.neemann.digital.gui.components.modification;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;

/**
 * Created by hneemann on 25.05.17.
 */
public class ModifyAttributes extends ModificationOfVisualElement {

    private final ElementAttributes attributes;

    public ModifyAttributes(VisualElement ve) {
        super(ve);
        attributes = new ElementAttributes(ve.getElementAttributes());
    }

    @Override
    public void modify(Circuit circuit) {
        VisualElement ve = getVisualElement(circuit);
        ve.getElementAttributes().getValuesFrom(attributes);
    }
}
