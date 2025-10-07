package lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * This file implements a basic lexical analyzer.
 *
 * @author Zach Kissel
 */
public class Lexer {
    // The dictionary of language keywords
    private HashMap<String, TokenType> keywords;

    // Stream of characters to generate token stream from.
    private CharacterStream stream;

    /**
     * Constructs a new lexical analyzer whose source input is a file.
     *
     * @param file the file to open for lexical analysis.
     * @throws FileNotFoundException if the file can not be opened.
     */
    public Lexer(File file) throws FileNotFoundException {
        stream = new CharacterStream(file);
        loadKeywords();
    }

    /**
     * Constructs a new lexical analyzer whose source is a string.
     *
     * @param input the input to lexically analyze.
     */
    public Lexer(String input) {
        stream = new CharacterStream(input);
        loadKeywords();
    }

    /**
     * Gets the next token from the stream.
     *
     * @return the next token.
     */
    public Token nextToken() {
        String value = ""; // The value to be associated with the token.

        stream.advanceToNonBlank();
        switch (stream.getCurrentClass()) {
            // The state where we are recognizing identifiers.
            // Regex: [A-Za-Z][0-9a-zA-z]*
            case LETTER:
                value += stream.getCurrentChar();
                stream.advance(); // advance the stream.

                // Read the rest of the identifier.
                while (stream.getCurrentClass() == CharacterClass.DIGIT
                        || stream.getCurrentClass() == CharacterClass.LETTER) {
                    value += stream.getCurrentChar();
                    stream.advance();
                }
                stream.skipNextAdvance(); // The symbol just read is part of the next token.

                // Keyword or identifier?
                if (keywords.containsKey(value))
                    return new Token(keywords.get(value), value);
                return new Token(TokenType.ID, value);

            // The state where we are recognizing digits.
            // Regex: [0-9]+
            case DIGIT:
                value += stream.getCurrentChar();
                stream.advance();

                while (stream.getCurrentClass() == CharacterClass.DIGIT) {
                    value += stream.getCurrentChar();
                    stream.advance();
                }

                if (stream.getCurrentChar() == '.') { // Decimal point.
                    value += stream.getCurrentChar();
                    stream.advance();
                    while (stream.getCurrentClass() == CharacterClass.DIGIT) {
                        value += stream.getCurrentChar();
                        stream.advance();
                    }
                    stream.skipNextAdvance();
                    return new Token(TokenType.REAL, value);
                }
                stream.skipNextAdvance(); // The symbol just read is part of the next token.

                return new Token(TokenType.INT, value);

            // Handles all special character symbols.
            case OTHER:
                return lookup();

            // We reached the end of our input.
            case END:
                return new Token(TokenType.EOF, "");

            // This should never be reached.
            default:
                return new Token(TokenType.UNKNOWN, "");
        }
    }

    /** Get the current line number being processed. */
    public long getLineNumber() {
        return stream.getLineNumber();
    }

    /************
     * Private Methods
     ************/

    /** Processes the next character and return the resulting token. */
    private Token lookup() {
        String value = "";

        switch (stream.getCurrentChar()) {
        case '.': // A double with just a leading dot.
            value += ".";
            stream.advance();

            while (stream.getCurrentClass() == CharacterClass.DIGIT) {
                value += stream.getCurrentChar();
                stream.advance();
            }
            stream.skipNextAdvance();
            return new Token(TokenType.REAL, value);

        case '+':
            stream.advance();
            return new Token(TokenType.ADD, "+");

        case '-':
            stream.advance();
            return new Token(TokenType.SUB, "-");

        case '*':
            stream.advance();
            return new Token(TokenType.MULT, "*");

        case '/':
            stream.advance();
            return new Token(TokenType.DIV, "/");

        case '(':
            stream.advance();
            return new Token(TokenType.LPAREN, "(");

        case ')':
            stream.advance();
            return new Token(TokenType.RPAREN, ")");

        case ':':
            stream.advance();
            if (stream.getCurrentChar() == '=') {
                stream.advance(); // consume '=' so it doesn't become EQ
                return new Token(TokenType.ASSIGN, ":=");
            } else {
                stream.skipNextAdvance();
                return new Token(TokenType.UNKNOWN, "");
            }

        case ';':
            stream.advance();
            return new Token(TokenType.SEMI, ";");

        case '=':
            stream.advance();
            return new Token(TokenType.EQ, "=");

        case '!':
            stream.advance();
            if (stream.getCurrentChar() == '=') {
                stream.advance(); // consume '='
                return new Token(TokenType.NEQ, "!=");
            } else {
                stream.skipNextAdvance();
                return new Token(TokenType.UNKNOWN, "");
            }

        case '>':
            stream.advance();
            if (stream.getCurrentChar() == '=') {
                stream.advance(); // consume '='
                return new Token(TokenType.GTE, ">=");
            } else {
                stream.skipNextAdvance();
                return new Token(TokenType.GT, ">");
            }

        case '<':
            stream.advance();
            if (stream.getCurrentChar() == '=') {
                stream.advance(); // consume '='
                return new Token(TokenType.LTE, "<=");
            } else {
                stream.skipNextAdvance();
                return new Token(TokenType.LT, "<");
            }

        default:
            char ch = stream.getCurrentChar();
            stream.advance();
            return new Token(TokenType.UNKNOWN, String.valueOf(ch));
        }
    }

    /** Sets up the dictionary with all of the keywords. */
    private void loadKeywords() {
        keywords = new HashMap<>();

        // Reserved words
        keywords.put("val", TokenType.VAL);
        keywords.put("not", TokenType.NOT);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("mod", TokenType.MOD);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
    }
}

