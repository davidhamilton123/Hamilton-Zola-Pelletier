package lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CharacterStream {
    private final String src;
    private int idx = 0;
    private boolean skipAdvance = false;
    private long lineNumber = 1;

    public CharacterStream(File file) throws FileNotFoundException {
        try {
            this.src = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new FileNotFoundException(file.getPath());
        }
    }

    public CharacterStream(String input) {
        this.src = (input == null) ? "" : input;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public char getCurrentChar() {
        if (idx >= src.length()) return '\0';
        return src.charAt(idx);
    }

    public CharacterClass getCurrentClass() {
        if (idx >= src.length()) return CharacterClass.END;
        char c = src.charAt(idx);
        if (Character.isWhitespace(c)) return CharacterClass.WHITE_SPACE;
        if (Character.isLetter(c))     return CharacterClass.LETTER;
        if (Character.isDigit(c))      return CharacterClass.DIGIT;
        return CharacterClass.OTHER;
    }

    /** Advance one character (unless a previous tokenizer set skipNextAdvance). */
    public void advance() {
        if (skipAdvance) {
            // Consume the deferred-advance flag without moving the index.
            skipAdvance = false;
            return;
        }
        if (idx >= src.length()) return;
        if (src.charAt(idx) == '\n') lineNumber++;
        idx++;
    }

    /** Tell the stream: “the char we just looked at belongs to next token; don't advance once.” */
    public void skipNextAdvance() {
        skipAdvance = true;
    }

    /** Clear any deferred-advance flag without moving the index. */
    private void consumeDeferredAdvance() {
        if (skipAdvance) skipAdvance = false;
    }

    /** Move to the next non-blank (whitespace) character. */
    public void advanceToNonBlank() {
        // Clear any deferred-advance from the previous token so the first advance
        // inside lookup() will actually move the index.
        consumeDeferredAdvance();
        // Then skip whitespace.
        while (getCurrentClass() == CharacterClass.WHITE_SPACE) {
            advance();
        }
    }
}

