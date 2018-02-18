package de.neemann.digital.draw.graphics.text.text;

/**
 * Represents a text element
 */
public interface Text {
    /**
     * @return a simplified text
     */
    Text simplify();

    /**
     * @return the text fragment in math mode if necessary
     */
    Text enforceMath();
}
