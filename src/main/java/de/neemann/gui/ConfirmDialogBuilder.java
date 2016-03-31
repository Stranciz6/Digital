package de.neemann.gui;

import javax.swing.*;
import java.awt.*;

/**
 * A simple confirmation dialog builder
 *
 * @author hneemann
 */
public class ConfirmDialogBuilder {

    private final String message;
    private String title = "Question";
    private String yes = "Yes";
    private String no = "No";
    private int def = 0;
    private String cancle;

    /**
     * Creates anew dialog builder
     *
     * @param message the dialog message
     */
    public ConfirmDialogBuilder(String message) {
        this.message = message;
    }

    /**
     * Sets the dialogs title
     *
     * @param title the dialogs title
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the text for the yes-option
     *
     * @param yes the yes option text
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setYesOption(String yes) {
        this.yes = yes;
        return this;
    }

    /**
     * Sets the text for the no-option
     *
     * @param no the no option text
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setNoOption(String no) {
        this.no = no;
        return this;
    }

    /**
     * Sets the text for the cancle-option
     *
     * @param cancle the cancle option text
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setCancleOption(String cancle) {
        this.cancle = cancle;
        return this;
    }

    /**
     * Sets yes as the default option
     *
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setYesAsDefault() {
        def = 0;
        return this;
    }

    /**
     * Sets no as the default option
     *
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setNoAsDefault() {
        def = 1;
        return this;
    }

    /**
     * Shows the dialog
     *
     * @param parent the parent component
     * @return the result of showOptionDialog
     */
    public int show(Component parent) {
        Object[] options;
        if (cancle == null)
            options = new Object[]{yes, no};
        else
            options = new Object[]{yes, no, cancle};

        return JOptionPane.showOptionDialog(parent,
                message,
                title,
                cancle == null ? JOptionPane.YES_NO_OPTION : JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[def]);
    }

}
