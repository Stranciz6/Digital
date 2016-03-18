package de.neemann.digital.core.part;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Describes one concrete Part.
 *
 * @author hneemann
 */
public class PartAttributes {
    private HashMap<AttributeKey, Object> attributes;
    private transient ArrayList<AttributeListener> listeners;

    public <VALUE> VALUE get(AttributeKey<VALUE> key) {
        if (attributes == null)
            return key.getDefault();
        else {
            VALUE value = (VALUE) attributes.get(key);
            if (value == null)
                return key.getDefault();
            return value;
        }
    }

    public <VALUE> void set(AttributeKey<VALUE> key, VALUE value) {
        if (value != get(key)) {
            if (value.equals(key.getDefault())) {
                if (attributes != null)
                    attributes.remove(key);
            } else {
                if (attributes == null)
                    attributes = new HashMap<>();
                attributes.put(key, value);
            }
            fireValueChanged(key);
        }
    }

    private void fireValueChanged(AttributeKey key) {
        if (listeners != null)
            for (AttributeListener l : listeners)
                l.attributeChanged(key);
    }

    public void addListener(AttributeListener listener) {
        if (listeners == null)
            listeners = new ArrayList<>();
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(AttributeListener listener) {
        if (listeners != null)
            listeners.remove(listener);
    }

    public PartAttributes bits(int bits) {
        set(AttributeKey.Bits, bits);
        return this;
    }
}
