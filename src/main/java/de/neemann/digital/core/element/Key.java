package de.neemann.digital.core.element;

/**
 * Class is used to define the keys used to access the models attributes
 *
 * @author hneemann
 */
public class Key<VALUE> {
    private final String key;
    private final VALUE def;
    private final String name;

    Key(String key, String name, VALUE def) {
        this.key = key;
        this.name = name;
        if (def == null)
            throw new NullPointerException();
        this.def = def;
    }

    /**
     * Returns the attributes key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the attributes display name
     *
     * @return thr name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the default value of this key
     */
    public VALUE getDefault() {
        return def;
    }

    /**
     * @return The values class
     */
    public Class getValueClass() {
        return def.getClass();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * A integer attribute.
     * Stores additional combo box values
     */
    public static final class KeyInteger extends Key<Integer> {
        private Integer[] values;

        KeyInteger(String key, String name, Integer def) {
            super(key, name, def);
        }

        KeyInteger setComboBoxValues(Integer[] values) {
            this.values = values;
            return this;
        }

        /**
         * @return the values to show in the combo box
         */
        public Integer[] getComboBoxValues() {
            return values;
        }
    }


}
