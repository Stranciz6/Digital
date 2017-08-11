package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Reads a file with the vhdl code to create the entity
 */
public class VHDLFile implements VHDLEntity {
    private final static String ENTITY_PREFIX = "DIG_";

    private final String entityName;
    private final ArrayList<String> vhdl;
    private final boolean hasData;
    private final Interval port;
    private final Interval arch;
    private boolean written = false;
    private boolean writtenBus = false;
    private ArrayList<String> generics = new ArrayList<>();

    /**
     * Creates a new instance
     *
     * @param elementName the filename
     * @throws IOException IOException
     */
    public VHDLFile(String elementName) throws IOException {
        this.entityName = ENTITY_PREFIX + elementName;
        vhdl = readFile(entityName);
        hasData = hasdata();
        port = extract("entity " + entityName + " is", "end " + entityName + ";");
        extractGenerics(port);

        arch = extract("architecture " + entityName + "_arch of " + entityName + " is", "end " + entityName + "_arch;");
    }

    private void extractGenerics(Interval port) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = port.start + 1; i < port.end; i++)
            sb.append(vhdl.get(i)).append(' ');
        StringTokenizer st = new StringTokenizer(sb.toString(), "(), :;\t");
        if (st.hasMoreTokens() && st.nextToken().equalsIgnoreCase("generic")) {
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                if (t.equalsIgnoreCase("port")) break;

                generics.add(t);
                if (!st.hasMoreTokens()) break;
                t = st.nextToken();
                if (!t.equalsIgnoreCase("integer"))
                    throw new IOException("only generic integers are supported, not '" + t + "'!");
            }
        }
    }

    private boolean hasdata() {
        for (String s : vhdl)
            if (s.contains("{{data}}"))
                return true;
        return false;
    }

    private Interval extract(String start, String end) throws IOException {
        return new Interval(indexOf(start), indexOf(end));
    }

    private int indexOf(String text) throws IOException {
        for (int i = 0; i < vhdl.size(); i++)
            if (vhdl.get(i).equalsIgnoreCase(text))
                return i;
        throw new IOException("string " + text + " not found!");
    }

    private ArrayList<String> readFile(String name) throws IOException {
        ArrayList<String> vhdl = new ArrayList<>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(createFileName(name));
        if (inputStream == null)
            throw new IOException("file not present: " + createFileName(name));
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = in.readLine()) != null)
                vhdl.add(line);
        }
        return vhdl;
    }

    private static String createFileName(String name) {
        return "vhdl/" + name + ".vhdl";
    }

    /**
     * Creates the name of the file used to load the vhdl file for the given element
     *
     * @param elementName the element name
     * @return the filename
     */
    public static String neededFileName(String elementName) {
        return createFileName(ENTITY_PREFIX + elementName);
    }


    @Override
    public void writeHeader(CodePrinter out, HDLNode node) throws IOException {
        for (int i = 0; i < port.start - 1; i++)
            out.println(vhdl.get(i));
    }

    @Override
    public String getName(HDLNode node) {
        if (hasData) {
            if (node.get(Keys.BITS) > 1)
                return entityName + "_BUS";
            else
                return entityName;
        } else
            return entityName;
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        if (hasData) {
            if (node.get(Keys.BITS) > 1)
                return !writtenBus;
            else
                return !written;
        } else
            return !written;
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        if (hasData && node.get(Keys.BITS) > 1)
            out.println("generic ( bitCount : integer );");
        for (int i = port.start + 1; i < port.end; i++)
            out.println(transform(vhdl.get(i), node));
    }

    private String transform(String s, HDLNode node) throws HDLException {
        String type;
        if (hasData && node.get(Keys.BITS) > 1) {
            type = "std_logic_vector((bitCount-1) downto 0)";
        } else
            type = "std_logic";
        return s.replace("{{data}}", type);
    }


    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException, HDLException {
        for (int i = arch.start + 1; i < arch.end; i++)
            out.println(transform(vhdl.get(i), node));

        if (hasData) {
            if (node.get(Keys.BITS) > 1)
                writtenBus = true;
            else
                written = true;
        } else
            written = true;
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException, HDLException {
        GenericWriter gw = new GenericWriter(out);
        if (hasData && node.get(Keys.BITS) > 1)
            gw.print("bitCount => " + node.get(Keys.BITS));
        for (String g : generics)
            gw.print(g + " => " + node.get(new Key<>(g, 0)).toString());
        gw.close();
    }

    @Override
    public boolean createsSignals() {
        return true;
    }

    private static final class Interval {
        private final int start;
        private final int end;

        private Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static final class Dummy extends VHDLEntitySimple {

        @Override
        public String getName(HDLNode node) {
            return node.getVisualElement().getElementName();
        }

        @Override
        public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException, HDLException {
        }
    }

    /**
     * Creates the needed chdl interface for the given node
     *
     * @param node the node
     * @return the interface
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    public static String getVHDLTemplate(HDLNode node) throws IOException, HDLException {
        Dummy d = new Dummy();
        CodePrinterStr out = new CodePrinterStr();
        d.writeHeader(out, node);
        out.println();
        String name = ENTITY_PREFIX + node.getName();
        out.println("entity " + name + " is").inc();
        d.writeDeclaration(out, node);
        out.dec().println("end " + name + ";");
        out.println();
        out.println("architecture " + name + "_arch of " + name + " is");
        out.println("begin");
        out.println();
        out.println("end " + name + "_arch;");
        return out.toString();
    }

    private static final class GenericWriter {
        private final CodePrinter out;
        private boolean open;

        private GenericWriter(CodePrinter out) {
            this.out = out;
            open = false;
        }

        public void print(String gen) throws IOException {
            if (!open) {
                out.println("generic map (").inc();
                open = true;
            } else
                out.println(",");
            out.print(gen);
        }

        public void close() throws IOException {
            if (open)
                out.println(" )").dec();
        }
    }
}
