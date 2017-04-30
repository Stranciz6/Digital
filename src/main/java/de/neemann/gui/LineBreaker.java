package de.neemann.gui;

/**
 * Used to break lines.
 * Created by hneemann on 29.04.17.
 */
public class LineBreaker {
    private static final int DEF_COLS = 70;

    private final String label;
    private final int indent;
    private final int cols;
    private final StringBuilder outText;
    private boolean isFirst;
    private int pos;
    private boolean preserveLineBreaks = false;
    private boolean toHTML = false;
    private boolean containsLineBreak = false;

    /**
     * Creates a new instance
     */
    public LineBreaker() {
        this(DEF_COLS);
    }

    /**
     * Creates a new instance
     *
     * @param cols number of columns to use
     */
    public LineBreaker(int cols) {
        this("", 0, cols);
    }

    /**
     * Creates a new instance
     *
     * @param label  the lable to use in the first line
     * @param indent columns to indent
     * @param cols   number of columns to use
     */
    public LineBreaker(String label, int indent, int cols) {
        this.label = label;
        this.indent = indent;
        this.cols = cols;
        outText = new StringBuilder(label);
        isFirst = true;
    }

    /**
     * Breaks the line
     *
     * @param text the text to handle
     * @return the text containing the line breaks
     */
    public String breakLines(String text) {
        if (text == null)
            return null;

        for (int i = 0; i < indent - label.length(); i++)
            outText.append(" ");

        StringBuilder word = new StringBuilder();
        pos = indent;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\n':
                    if (preserveLineBreaks) {
                        addWord(word);
                        lineBreak();
                        break;
                    }
                case '\r':
                case ' ':
                    addWord(word);
                    break;
                default:
                    word.append(c);
            }
        }
        addWord(word);

        String ret = outText.toString();
        if (containsLineBreak && toHTML)
            ret = "<html>" + ret.replace("\n", "<br>") + "</html>";

        return ret;
    }

    private void addWord(StringBuilder word) {
        if (word.length() > 0) {
            if (pos + (isFirst ? word.length() : word.length() + 1) > cols) {
                lineBreak();
            } else {
                if (!isFirst) {
                    outText.append(" ");
                    pos++;
                }
            }
            outText.append(word);
            pos += word.length();
            word.setLength(0);
            isFirst = false;
        }
    }

    private void lineBreak() {
        if (!isFirst) {
            outText.append('\n');
            for (int j = 0; j < indent; j++)
                outText.append(" ");
            pos = indent;
            containsLineBreak = true;
            isFirst = true;
        }
    }

    /**
     * Preserves the contained line breaks
     *
     * @return this for chained calls
     */
    public LineBreaker preserveContainedLineBreaks() {
        this.preserveLineBreaks = true;
        return this;
    }

    /**
     * Returns an HTML string
     *
     * @return this for chained calls
     */
    public LineBreaker toHTML() {
        this.toHTML = true;
        return this;
    }
}
