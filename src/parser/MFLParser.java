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
import java.util.LinkedList;

import ast.SyntaxTree;
import ast.nodes.SyntaxNode;
import ast.nodes.ProgNode;
import ast.nodes.ValNode;
import ast.nodes.BinOpNode;
import ast.nodes.UnaryOpNode;
import ast.nodes.TokenNode;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import parser.ParseException;

/**
 * Parser for the MFL language.
 * Builds an AST using recursive-descent methods for a small expression grammar.
 */
public class MFLParser extends Parser {

    /**
     * Constructs a new parser for the file {@code src}.
     *
     * @param src the source code file to parse.
     * @throws FileNotFoundException if the file cannot be found.
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
     * Parses according to the grammar and returns the AST.
     */
    @Override
    public SyntaxTree parse() throws ParseException {
        // PRIME the token stream (loads first non-comment token into nextTok)
        nextToken();

        SyntaxNode root = parseProg();
        return new SyntaxTree(root);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    // Token doesn't carry line numbers in this codebase; use a placeholder.
    private long currentLine() { return 1L; }

    // program := { statement ';' }
    private SyntaxNode parseProg() throws ParseException {
        LinkedList<SyntaxNode> stmts = new LinkedList<>();
        while (!tokenIs(TokenType.EOF)) {
            SyntaxNode s = parseStmt();
            match(TokenType.SEMI, ";");
            stmts.add(s);
        }
        return new ProgNode(currentLine(), stmts);
    }

    // statement := 'val' ID ':=' expr | expr
    private SyntaxNode parseStmt() throws ParseException {
        if (tokenIs(TokenType.VAL)) {
            match(TokenType.VAL, "val");
            Token name = getCurrToken();
            match(TokenType.ID, "identifier");
            match(TokenType.ASSIGN, ":=");
            SyntaxNode rhs = parseExpr();
            return new ValNode(currentLine(), name, rhs);
        } else {
            return parseExpr();
        }
    }

    // expr := orExpr
    private SyntaxNode parseExpr() throws ParseException { return parseOr(); }

    // orExpr := andExpr { OR andExpr }
    private SyntaxNode parseOr() throws ParseException {
        SyntaxNode left = parseAnd();
        while (tokenIs(TokenType.OR)) {
            Token op = getCurrToken(); match(TokenType.OR, "or");
            SyntaxNode right = parseAnd();
            left = new BinOpNode(currentLine(), left, op.getType(), right);
        }
        return left;
    }

    // andExpr := addExpr { AND addExpr }
    private SyntaxNode parseAnd() throws ParseException {
        SyntaxNode left = parseAdd();
        while (tokenIs(TokenType.AND)) {
            Token op = getCurrToken(); match(TokenType.AND, "and");
            SyntaxNode right = parseAdd();
            left = new BinOpNode(currentLine(), left, op.getType(), right);
        }
        return left;
    }

    // addExpr := mulExpr { (ADD|SUB) mulExpr }
    private SyntaxNode parseAdd() throws ParseException {
        SyntaxNode left = parseMul();
        while (tokenIs(TokenType.ADD) || tokenIs(TokenType.SUB)) {
            Token op = getCurrToken();
            if (tokenIs(TokenType.ADD)) match(TokenType.ADD, "+");
            else match(TokenType.SUB, "-");
            SyntaxNode right = parseMul();
            left = new BinOpNode(currentLine(), left, op.getType(), right);
        }
        return left;
    }

    // mulExpr := unary { (MULT|DIV|MOD) unary }
    private SyntaxNode parseMul() throws ParseException {
        SyntaxNode left = parseUnary();
        while (tokenIs(TokenType.MULT) || tokenIs(TokenType.DIV) || tokenIs(TokenType.MOD)) {
            Token op = getCurrToken();
            if (tokenIs(TokenType.MULT)) match(TokenType.MULT, "*");
            else if (tokenIs(TokenType.DIV)) match(TokenType.DIV, "/");
            else match(TokenType.MOD, "mod");
            SyntaxNode right = parseUnary();
            left = new BinOpNode(currentLine(), left, op.getType(), right);
        }
        return left;
    }

    // unary := NOT unary | primary
    private SyntaxNode parseUnary() throws ParseException {
        if (tokenIs(TokenType.NOT)) {
            Token op = getCurrToken(); match(TokenType.NOT, "not");
            SyntaxNode child = parseUnary();
            return new UnaryOpNode(currentLine(), op.getType(), child);
        }
        return parsePrimary();
    }

    // primary := INT | REAL | TRUE | FALSE | ID | '(' expr ')'
    private SyntaxNode parsePrimary() throws ParseException {
        Token tok = getCurrToken();
        if (tokenIs(TokenType.INT) || tokenIs(TokenType.REAL) ||
            tokenIs(TokenType.TRUE) || tokenIs(TokenType.FALSE)) {
            match(tok.getType(), tok.getValue());
            return new TokenNode(currentLine(), tok);
        } else if (tokenIs(TokenType.ID)) {
            match(TokenType.ID, "identifier");
            return new TokenNode(currentLine(), tok);
        } else if (tokenIs(TokenType.LPAREN)) {
            match(TokenType.LPAREN, "(");
            SyntaxNode e = parseExpr();
            match(TokenType.RPAREN, ")");
            return e;
        }
        throw new ParseException();
    }
}



