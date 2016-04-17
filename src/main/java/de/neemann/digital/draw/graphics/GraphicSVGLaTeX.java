package de.neemann.digital.draw.graphics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Subclass of {@link GraphicSVG} which creates the correct SVG representation
 * of an index if used like "x_0". But the text itself is created to be interpreted
 * by LaTeX. To include such a SVG file in LaTeX inkscape is needed. To run the transformation
 * automatically by the LaTeX compiler see InkscapePDFLaTeX.pdf.
 *
 * @see <a href="https://inkscape.org">inkscape</a>
 * @see <a href="http://mirrors.ctan.org/info/svg-inkscape/InkscapePDFLaTeX.pdf">InkscapePDFLaTeX.pdf</a>
 * @author hneemann
 */
public class GraphicSVGLaTeX extends GraphicSVG {
    /**
     * Creates new instance
     *
     * @param out the file
     * @param min  upper left corner
     * @param max  lower right corner
     * @throws IOException IOException
     */
    public GraphicSVGLaTeX(OutputStream out, Vector min, Vector max) throws IOException {
        super(out, min, max);
    }

    /**
     * Creates new instance
     *
     * @param out      the output stream to use
     * @param min      upper left corner
     * @param max      lower right corner
     * @param source   source file, only used to create a comment in the SVG file
     * @param svgScale the scaling
     * @throws IOException IOException
     */
    public GraphicSVGLaTeX(OutputStream out, Vector min, Vector max, File source, int svgScale) throws IOException {
        super(out, min, max, source, svgScale);
    }

    @Override
    public String formatText(String text, int fontSize) {
        text = formatIndex(text);
        StringBuilder sb = new StringBuilder();
        boolean inMath = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '~':
                case '\u00AC':
                    sb.append(checkMath(inMath, "\\neg{}"));
                    break;
                case '\u2265':
                    sb.append(checkMath(inMath, "\\geq\\!\\!{}"));
                    break;
                case '<':
                    if (inMath)
                        sb.append(c);
                    else
                        sb.append("\\textless{}");
                    break;
                case '>':
                    if (inMath)
                        sb.append(c);
                    else
                        sb.append("\\textgreater{}");
                    break;
                case '&':
                    sb.append("\\&");
                    break;
                case '$':
                    inMath = !inMath;
                default:
                    sb.append(c);
            }
        }
        text = sb.toString();
        if (fontSize < Style.NORMAL.getFontSize())
            text = "{\\scriptsize " + text + "}";
        return escapeXML(text);
    }

    private String checkMath(boolean inMath, String s) {
        if (inMath)
            return s;
        else
            return "$" + s + "$";
    }

    private String formatIndex(String text) {
        int p = text.lastIndexOf("_");
        if (p > 0) {
            text = "$" + text.substring(0, p) + "_{" + text.substring(p + 1) + "}$";
        }
        return text;
    }


    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        if ((style != Style.WIRE && style != Style.WIRE_OUT) || Math.abs(p1.x - p2.x) > 4)
            super.drawCircle(p1, p2, style);
    }

    @Override
    public String getColor(Style style) {
        if (style == Style.WIRE) return super.getColor(Style.NORMAL);
        if (style == Style.WIRE_OUT) return super.getColor(Style.NORMAL);
        return super.getColor(style);
    }
}
