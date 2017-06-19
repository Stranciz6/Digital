package de.neemann.digital.core.io;

import de.neemann.digital.core.ObservableValue;

/**
 * A simple value.
 * <p>
 * Created by hneemann on 19.06.17.
 */
public class InValue {

    private final long value;
    private final boolean highZ;

    /**
     * Creates a new value
     *
     * @param value the value
     */
    public InValue(long value) {
        this.value = value;
        this.highZ = false;
    }

    /**
     * Creates a new value
     *
     * @param highZ ht ehigh z state
     */
    public InValue(boolean highZ) {
        this.value = 0;
        this.highZ = true;
    }

    /**
     * Creates a new value
     *
     * @param value the value
     */
    public InValue(ObservableValue value) {
        if (value.isHighZ()) {
            this.highZ = true;
            this.value = 0;
        } else {
            this.highZ = false;
            this.value = value.getValueIgnoreHighZ();
        }
    }

    /**
     * Creates a new instance
     *
     * @param value the value a "Z" means "high z"
     * @throws NumberFormatException NumberFormatException
     */
    public InValue(String value) {
        if (value.toLowerCase().trim().equalsIgnoreCase("z")) {
            this.highZ = true;
            this.value = 0;
        } else {
            this.highZ = false;
            this.value = Long.decode(value.trim());
        }

    }

    /**
     * @return the value
     */
    public long getValue() {
        return value;
    }

    /**
     * @return High Z State
     */
    public boolean isHighZ() {
        return highZ;
    }

    @Override
    public String toString() {
        if (highZ)
            return "Z";
        else
            return Long.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InValue inValue = (InValue) o;

        if (value != inValue.value) return false;
        return highZ == inValue.highZ;
    }

    @Override
    public int hashCode() {
        int result = (int) (value ^ (value >>> 32));
        result = 31 * result + (highZ ? 1 : 0);
        return result;
    }
}
