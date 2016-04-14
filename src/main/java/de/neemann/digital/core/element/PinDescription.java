package de.neemann.digital.core.element;

/**
 * Description of a pin
 *
 * @author hneemann
 */
public interface PinDescription {

    enum Direction {input, output, both}

    /**
     * @return the pins name
     */
    String getName();

    /**
     * @return the pins description
     */
    String getDescription();

    /**
     * @return the Pins direction
     */
    Direction getDirection();

}
