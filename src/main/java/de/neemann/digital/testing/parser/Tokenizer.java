package de.neemann.digital.testing.parser;

import java.io.IOException;
import java.io.Reader;

/**
 * Simple tokenizer to tokenize boolean expressions.
 *
 * @author hneemann
 */
public class Tokenizer {

    enum Token {UNKNOWN, IDENT, AND, OR, NOT, OPEN, CLOSE, NUMBER, EOL, EOF, SHIFTLEFT, SHIFTRIGHT, COMMA, EQUAL, ADD, SUB, MUL, GREATER, SMALER, DIV}

    private final Reader in;
    private Token token;
    private boolean isToken;
    private StringBuilder builder;
    private boolean isUnreadChar = false;
    private int unreadChar;
    private int line = 1;

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
        Token token = peek();
        consume();
        return token;
    }

    /**
     * Consumes the token after a peek call
     */
    public void consume() {
        isToken = false;
    }

    /**
     * Peeks the next token.
     * The token is kept in the stream, so next() or peek() will return this token again!
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
            case '\n':
            case '\r':
                line++;
                return Token.EOL;
            case '(':
                token = Token.OPEN;
                break;
            case ')':
                token = Token.CLOSE;
                break;
            case '&':
                token = Token.AND;
                break;
            case '|':
                token = Token.OR;
                break;
            case '+':
                token = Token.ADD;
                break;
            case '-':
                token = Token.SUB;
                break;
            case '*':
                token = Token.MUL;
                break;
            case '/':
                token = Token.DIV;
                break;
            case '<':
                if (isNextChar('<')) {
                    token = Token.SHIFTLEFT;
                } else {
                    token = Token.SMALER;
                }
                break;
            case '>':
                if (isNextChar('>')) {
                    token = Token.SHIFTRIGHT;
                } else {
                    token = Token.GREATER;
                }
                break;
            case '~':
                token = Token.NOT;
                break;
            case ',':
                token = Token.COMMA;
                break;
            case '=':
                token = Token.EQUAL;
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
                } else if (isNumberChar(c)) {
                    token = Token.NUMBER;
                    builder.setLength(0);
                    builder.append((char) c);
                    boolean wasChar = true;
                    do {
                        c = readChar();
                        if (isNumberChar(c)) {
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

    private boolean isNextChar(char should) throws IOException {
        int c = readChar();
        if (c == should)
            return true;
        unreadChar(c);
        return false;
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
                || (c == '_');
    }

    private boolean isNumberChar(int c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isWhiteSpace(int c) {
        return c == ' ' || c == '\t';
    }

    @Override
    public String toString() {
        if (token == Token.IDENT || token == Token.UNKNOWN)
            return getIdent();
        else
            return token.name();
    }

    /**
     * @return the parsed test vectors
     */
    public int getLine() {
        return line;
    }

}
