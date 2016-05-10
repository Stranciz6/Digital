package de.neemann.digital.analyse.expression;

import de.neemann.digital.analyse.expression.modify.ExpressionModifier;

/**
 * An expression which can be evaluated to a boolean value
 *
 * @author hneemann
 */
public interface Expression {

    /**
     * Evaluates the expression and returns the bool value
     *
     * @param context the expressions context
     * @return the bool value
     * @throws ExpressionException ExpressionException
     */
    boolean calculate(Context context) throws ExpressionException;

    /**
     * Traverses the expression
     *
     * @param visitor the visitor
     * @param <V>     the visitors type
     * @return the visitor
     */
    <V extends ExpressionVisitor> V traverse(V visitor);

    /**
     * Used to modify the ast
     *
     * @param modifier the modifier
     */
    default void modify(ExpressionModifier modifier) {
    }

    /**
     * String used to order expressions
     *
     * @return the ordering string
     */
    String getOrderString();
}
