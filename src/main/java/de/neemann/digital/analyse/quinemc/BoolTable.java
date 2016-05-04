package de.neemann.digital.analyse.quinemc;

import de.neemann.digital.analyse.expression.ExpressionException;

/**
 * A simple bool table
 *
 * @author hneemann
 */
public interface BoolTable {
    /**
     * @return the table row count
     */
    int size();

    /**
     * returns the value at the given row
     *
     * @param i the index
     * @return the value
     * @throws ExpressionException ExpressionException
     */
    ThreeStateValue get(int i) throws ExpressionException;
}
