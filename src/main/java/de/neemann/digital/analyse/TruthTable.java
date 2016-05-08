package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.BitSetter;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTable;

import java.util.ArrayList;

/**
 * The description of a truth table.
 *
 * @author hneemann
 */
public class TruthTable {

    private final ArrayList<Variable> variables;
    private final ArrayList<Result> results;
    private BitSetter bitSetter;

    /**
     * Creates a new instance
     */
    public TruthTable() {
        this(new ArrayList<>());
    }

    /**
     * Creates a new instance with <code>vars</code> variables
     *
     * @param vars number of variables
     */
    public TruthTable(int vars) {
        this(Variable.vars(vars));
    }

    /**
     * Creates a new instance with the given variables
     *
     * @param vars the variables
     */
    public TruthTable(ArrayList<Variable> vars) {
        this.variables = vars;
        results = new ArrayList<>();
    }

    /**
     * Returns the number of rows
     *
     * @return the number of rows
     */
    public int getRows() {
        return 1 << variables.size();
    }

    /**
     * Adds a new result row
     *
     * @param name   name of the value
     * @param values the values
     */
    public void addResult(String name, BoolTable values) {
        results.add(new Result(name, values));
    }

    /**
     * Adds a variable
     *
     * @param name name of the variable
     */
    public void addVariable(String name) {
        variables.add(new Variable(name));
        bitSetter = null;
    }

    private BitSetter getBitSetter() {
        if (bitSetter == null)
            bitSetter = new DummyBitSetter(variables.size());
        return bitSetter;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Variable s : variables)
            sb.append(s.getIdentifier()).append("\t");
        for (Result s : results)
            sb.append(s.getName()).append("\t");
        sb.append('\n');

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < variables.size(); col++) {
                if (getBitSetter().getBit(row, col))
                    sb.append("1\t");
                else
                    sb.append("0\t");
            }
            for (int col = 0; col < results.size(); col++) {
                switch (results.get(col).getValues().get(row)) {
                    case one:
                        sb.append("1\t");
                        break;
                    case zero:
                        sb.append("0\t");
                        break;
                    default:
                        sb.append("x\t");
                        break;
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * @return number of collumns
     */
    public int getCols() {
        return variables.size() + results.size();
    }

    /**
     * Returns the name of the column with the given index
     *
     * @param columnIndex the col index
     * @return the name
     */
    public String getColumnName(int columnIndex) {
        if (columnIndex < variables.size())
            return variables.get(columnIndex).getIdentifier();
        else
            return results.get(columnIndex - variables.size()).getName();
    }

    /**
     * Returns the value of the table as a int value
     *
     * @param rowIndex    the row
     * @param columnIndex the col
     * @return the table value (2 means "don't care")
     */
    public int getValue(int rowIndex, int columnIndex) {
        if (columnIndex < variables.size())
            return getBitSetter().getBit(rowIndex, columnIndex) ? 1 : 0;
        else
            return results.get(columnIndex - variables.size()).getValues().get(rowIndex).asInt();
    }

    /**
     * A single result column
     */
    public static final class Result {
        private String name;
        private BoolTable values;

        /**
         * Creates a new instance
         *
         * @param name   the name of the result
         * @param values the result values
         */
        public Result(String name, BoolTable values) {
            this.name = name;
            this.values = values;
        }

        /**
         * @return the result values name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the values name
         *
         * @param name the values name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the result values
         */
        public BoolTable getValues() {
            return values;
        }
    }

    private static final class DummyBitSetter extends BitSetter {
        private DummyBitSetter(int bitCount) {
            super(bitCount);
        }

        @Override
        public void setBit(int row, int bit, boolean value) {
        }
    }
}
