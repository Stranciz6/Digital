package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.DetermineJKStateMachine;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;

/**
 * Expression listener which sends all result tu its parent.
 * If the expression is a stata expression it creates the J and K control equations
 * and alos sends the calculated control expressions to its parent.
 *
 * @author hneemann
 */
public class ExpressionListenerJK implements ExpressionListener {
    private final ExpressionListener parent;

    /**
     * Creates a new instance
     *
     * @param parent the parent ExpressionListener
     */
    public ExpressionListenerJK(ExpressionListener parent) {
        this.parent = parent;
    }

    @Override
    public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
        parent.resultFound(name, expression);

        if (name.endsWith("n+1")) {
            String detName = name.substring(0, name.length() - 2);
            DetermineJKStateMachine jk = new DetermineJKStateMachine(detName, expression);
            Expression j = jk.getJ();
            parent.resultFound("J_" + detName, j);
            Expression s = QuineMcCluskey.simplify(j);
            if (s != j) {
                parent.resultFound("", s);
            }
            Expression k = jk.getK();
            parent.resultFound("K_" + detName, k);
            s = QuineMcCluskey.simplify(k);
            if (s != k) {
                parent.resultFound("", s);
            }
        }

    }
}
