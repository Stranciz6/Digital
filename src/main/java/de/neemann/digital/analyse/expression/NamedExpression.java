package de.neemann.digital.analyse.expression;

/**
 * @author hneemann
 */
public class NamedExpression implements Expression {

    private final Expression exp;
    private final String name;

    /**
     * Creates a new instance
     *
     * @param name the name of the expression
     * @param exp  the expression
     */
    public NamedExpression(String name, Expression exp) {
        this.exp = exp;
        this.name = name;
    }

    /**
     * @return the name of the expression
     */
    public String getName() {
        return name;
    }

    /**
     * @return the named expression
     */
    public Expression getExpression() {
        return exp;
    }


    @Override
    public boolean calculate(Context context) throws ExpressionException {
        return exp.calculate(context);
    }

    @Override
    public <V extends ExpressionVisitor> V traverse(V visitor) {
        return exp.traverse(visitor);
    }

    @Override
    public String getOrderString() {
        return exp.getOrderString();
    }

    @Override
    public String toString() {
        return name+"="+exp.toString();
    }
}
