package de.neemann.digital.analyse.parser;

import java.io.IOException;
import java.io.Reader;

/**
 * Simple tokenizer to tokenize boolean expressions.
 *
 * @author hneemann
 */
public class Tokenizer {


    enum Token {UNKNOWN, IDENT, AND, OR, NOT, OPEN, CLOSE, ONE, ZERO, EOF}

    private final Reader in;
    private Token token;
    private boolean isToken;
    private StringBuilder builder;
    private boolean isUnreadChar = false;
    private int unreadChar;

    /**
     * Creates a new instance
     *
     * @param in the reader
     */
    public Tokenizer(Reader in) {
        this.in = in;
        token = Token.UNKNOWN;
        isToken = false;
        builder = new StringBuilder();
    }

    /**
     * Reads the next token
     *
     * @return the token
     * @throws IOException IOException
     */
    public Token next() throws IOException {
        peek();
        isToken = false;
        return token;
    }

    /**
     * peeks the next token.
     * The token is kept in the stream, so next will return this token again!
     *
     * @return the token
     * @throws IOException IOException
     */
    public Token peek() throws IOException {
        if (isToken)
            return token;

        int c;
        do {
            c = readChar();
        } while (isWhiteSpace(c));

        switch (c) {
            case -1:
                token = Token.EOF;
                break;
            case '0':
                token = Token.ZERO;
                break;
            case '1':
                token = Token.ONE;
                break;
            case '(':
                token = Token.OPEN;
                break;
            case ')':
                token = Token.CLOSE;
                break;
            case '&':
                c = readChar();
                if (c != '&') unreadChar(c);
            case '*':
            case '∧':
                token = Token.AND;
                break;
            case '|':
                c = readChar();
                if (c != '|') unreadChar(c);
            case '+':
            case '∨':
                token = Token.OR;
                break;
            case '¬':
            case '!':
                token = Token.NOT;
                break;
            default:
                if (isIdentChar(c)) {
                    token = Token.IDENT;
                    builder.setLength(0);
                    builder.append((char) c);
                    boolean wasChar = true;
                    do {
                        c = readChar();
                        if (isIdentChar(c) || isNumberChar(c)) {
                            builder.append((char) c);
                        } else {
                            unreadChar(c);
                            wasChar = false;
                        }
                    } while (wasChar);
                } else {
                    token = Token.UNKNOWN;
                    builder.setLength(0);
                    builder.append((char) c);
                }
        }

        isToken = true;
        return token;
    }

    /**
     * @return the identifier
     */
    public String getIdent() {
        return builder.toString();
    }

    private int readChar() throws IOException {
        if (isUnreadChar) {
            isUnreadChar = false;
            return unreadChar;
        } else
            return in.read();
    }

    private void unreadChar(int c) {
        unreadChar = c;
        isUnreadChar = true;
    }

    private boolean isIdentChar(int c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c == '_')
                || (c == '^');
    }

    private boolean isNumberChar(int c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isWhiteSpace(int c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }

    @Override
    public String toString() {
        if (token == Token.IDENT || token == Token.UNKNOWN)
            return getIdent();
        else
            return token.name();
    }
}
