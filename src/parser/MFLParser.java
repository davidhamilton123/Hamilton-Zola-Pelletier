/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package parser;

import java.io.File;
import java.io.FileNotFoundException;
import ast.SyntaxTree;
import lexer.Lexer;

/**
 * <p>
 * Parser for the MFL language. This is largely private methods where
 * there is one method — the "eval" method — for each non-terminal of the grammar.
 * There are also a collection of private "handle" methods that handle one
 * production associated with a non-terminal.
 * </p>
 * 
 * <p>
 * Each private method operates on the token stream. It is important to 
 * remember that all of our non-terminal processing methods maintain the invariant
 * that each method leaves the stream positioned so that the next unprocessed token
 * is at the front of the stream.
 * </p>
 * 
 * @author Zach Kissel
 */
public class MFLParser extends Parser {

    /**
     * Constructs a new parser for the file {@code source} by setting up lexer.
     * 
     * @param src the source code file to parse.
     * @throws FileNotFoundException if the file can not be found.
     */
    public MFLParser(File src) throws FileNotFoundException {
        super(new Lexer(src));
    }

    /**
     * Construct a parser that parses the string {@code str}.
     * 
     * @param str the code to evaluate.
     */
    public MFLParser(String str) {
        super(new Lexer(str));
    }

    /**
     * Parses the file according to the grammar.
     * 
     * @return the abstract syntax tree representing the parsed program.
     * @throws ParseException when parsing fails.
     */
    public SyntaxTree parse() throws ParseException {
        // TEMP STUB so --ast doesn't crash; replace with real parser soon.
        // This ensures Interpreter.java doesn't throw NullPointerException.
        return new SyntaxTree();
    }

    /************
     * Evaluation methods to construct the AST associated with the non-terminals
     ***********/
}

