package de.neemann.digital.draw.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.VariableVisitor;
import de.neemann.digital.draw.builder.BuilderException;
import de.neemann.digital.draw.builder.BuilderInterface;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Builder implementation which only collects the expressions to build.
 *
 * @author hneemann
 */
public class BuilderCollector implements BuilderInterface<BuilderCollector> {
    private final VariableVisitor vars;
    private ArrayList<String> outputs;
    private HashMap<String, Expression> combinatorial;
    private HashMap<String, Expression> registered;

    /**
     * Creates a new instance
     */
    public BuilderCollector() {
        vars = new VariableVisitor();
        outputs = new ArrayList<>();
        combinatorial = new HashMap<>();
        registered = new HashMap<>();
    }

    @Override
    public BuilderCollector addExpression(String name, Expression expression) throws BuilderException {
        expression.traverse(vars);
        outputs.add(name);
        combinatorial.put(name, expression);
        return this;
    }

    @Override
    public BuilderCollector addState(String name, Expression expression) throws BuilderException {
        expression.traverse(vars);
        outputs.add(name);
        registered.put(name, expression);
        return this;
    }

    /**
     * @return the output names
     */
    public ArrayList<String> getOutputs() {
        return outputs;
    }

    /**
     * @return the input names
     */
    public ArrayList<String> getInputs() {
        ArrayList<String> inputs = new ArrayList<>();
        for (Variable v : vars.getVariables())
            if (!outputs.contains(v.getIdentifier()))
                inputs.add(v.getIdentifier());
        return inputs;
    }

    /**
     * @return the combinatorial expressions
     */
    public HashMap<String, Expression> getCombinatorial() {
        return combinatorial;
    }

    /**
     * @return the registered expressions
     */
    public HashMap<String, Expression> getRegistered() {
        return registered;
    }

}
