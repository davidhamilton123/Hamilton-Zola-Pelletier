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
import java.util.ArrayList;
import java.util.List;

import ast.SyntaxTree;
import ast.TokenNode;
import ast.nodes.SyntaxNode;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;


/**
 * Parser for the MFL language using Recursive Descent Parsing.
 * 
 * Grammar:
 * <prog>  → <val> ; { <val> ; }
 * <val>   → val <id> := <expr> | <expr>
 * <expr>  → <rexpr> { (and | or) <rexpr> }
 * <rexpr> → <mexpr> [ ( < | > | >= | <= | = | != ) <mexpr> ]
 * <mexpr> → <term> { ( + | - ) <term> }
 * <term>  → not <rexpr> | <factor> { ( * | / | mod ) <factor> }
 * <factor>→ <id> | <int> | <real> | ( <expr> ) | true | false
 */
public class MFLParser extends Parser {

    public MFLParser(File src) throws FileNotFoundException {
        super(new Lexer(src));
    }

    public MFLParser(String str) {
        super(new Lexer(str));
    }

    /**
     * Entry point for parsing — builds the full AST.
     */
    @Override
    public SyntaxTree parse() throws ParseException {
        nextToken(); // Initialize first token
        SyntaxNode root = parseProg();

        // Ensure program ends properly
        if (!tokenIs(TokenType.EOF))
            logError("Unexpected token after end of program.");

        return new SyntaxTree(root);
    }

    /**********************
     * Grammar Methods
     **********************/

    private SyntaxNode parseProg() throws ParseException {
        trace("<prog>");
        List<SyntaxNode> valNodes = new ArrayList<>();

        valNodes.add(getGoodParse(parseVal()));
        match(TokenType.SEMI, ";");

        while (!tokenIs(TokenType.EOF)) {
            valNodes.add(getGoodParse(parseVal()));
            match(TokenType.SEMI, ";");
        }

        return new ListNode(getCurrLine(), "Program", valNodes);
    }

    private SyntaxNode parseVal() throws ParseException {
        trace("<val>");
        if (checkMatch(TokenType.VAL)) {
            Token idTok = getCurrToken();
            match(TokenType.ID, "identifier");
            match(TokenType.ASSIGN, ":=");
            SyntaxNode exprNode = getGoodParse(parseExpr());
            return new BinaryOpNode(getCurrLine(), "valDecl", new TokenNode(getCurrLine(), idTok), exprNode);
        } else {
            return parseExpr();
        }
    }

    private SyntaxNode parseExpr() throws ParseException {
        trace("<expr>");
        SyntaxNode left = getGoodParse(parseRexpr());
        while (tokenIs(TokenType.AND) || tokenIs(TokenType.OR)) {
            Token op = getCurrToken();
            nextToken();
            SyntaxNode right = getGoodParse(parseRexpr());
            left = new BinaryOpNode(getCurrLine(), op.getValue(), left, right);
        }
        return left;
    }

    private SyntaxNode parseRexpr() throws ParseException {
        trace("<rexpr>");
        SyntaxNode left = getGoodParse(parseMexpr());
        if (tokenIs(TokenType.GT) || tokenIs(TokenType.LT) || tokenIs(TokenType.GTE) ||
            tokenIs(TokenType.LTE) || tokenIs(TokenType.EQ) || tokenIs(TokenType.NEQ)) {
            Token op = getCurrToken();
            nextToken();
            SyntaxNode right = getGoodParse(parseMexpr());
            return new BinaryOpNode(getCurrLine(), op.getValue(), left, right);
        }
        return left;
    }

    private SyntaxNode parseMexpr() throws ParseException {
        trace("<mexpr>");
        SyntaxNode left = getGoodParse(parseTerm());
        while (tokenIs(TokenType.ADD) || tokenIs(TokenType.SUB)) {
            Token op = getCurrToken();
            nextToken();
            SyntaxNode right = getGoodParse(parseTerm());
            left = new BinaryOpNode(getCurrLine(), op.getValue(), left, right);
        }
        return left;
    }

    private SyntaxNode parseTerm() throws ParseException {
        trace("<term>");
        if (checkMatch(TokenType.NOT)) {
            SyntaxNode expr = getGoodParse(parseRexpr());
            return new UnaryOpNode(getCurrLine(), "not", expr);
        }

        SyntaxNode left = getGoodParse(parseFactor());
        while (tokenIs(TokenType.MULT) || tokenIs(TokenType.DIV) || tokenIs(TokenType.MOD)) {
            Token op = getCurrToken();
            nextToken();
            SyntaxNode right = getGoodParse(parseFactor());
            left = new BinaryOpNode(getCurrLine(), op.getValue(), left, right);
        }
        return left;
    }

    private SyntaxNode parseFactor() throws ParseException {
        trace("<factor>");
        Token tok = getCurrToken();

        switch (tok.getType()) {
            case ID:
            case INT:
            case REAL:
            case TRUE:
            case FALSE:
                nextToken();
                return new TokenNode(getCurrLine(), tok);

            case LPAREN:
                match(TokenType.LPAREN, "(");
                SyntaxNode exprNode = getGoodParse(parseExpr());
                match(TokenType.RPAREN, ")");
                return exprNode;

            default:
                logError("Unexpected token in <factor>: " + tok);
                throw new ParseException();
        }
    }

    /**********************
     * Helper Node Classes
     **********************/

    // Represents binary operations: +, -, *, /, mod, and, or, <, >, etc.
    private static class BinaryOpNode extends SyntaxNode {
        private final String op;
        private final SyntaxNode left, right;

        public BinaryOpNode(long lineNumber, String op, SyntaxNode left, SyntaxNode right) {
            super(lineNumber);
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public void displaySubtree(int indentAmt) {
            printIndented("BinaryOp(" + op + ")", indentAmt);
            left.displaySubtree(indentAmt + 2);
            right.displaySubtree(indentAmt + 2);
        }
    }

    // Represents unary operations like "not"
    private static class UnaryOpNode extends SyntaxNode {
        private final String op;
        private final SyntaxNode expr;

        public UnaryOpNode(long lineNumber, String op, SyntaxNode expr) {
            super(lineNumber);
            this.op = op;
            this.expr = expr;
        }

        @Override
        public void displaySubtree(int indentAmt) {
            printIndented("UnaryOp(" + op + ")", indentAmt);
            expr.displaySubtree(indentAmt + 2);
        }
    }

    // Represents a list of nodes (e.g. program body)
    private static class ListNode extends SyntaxNode {
        private final String label;
        private final List<SyntaxNode> children;

        public ListNode(long lineNumber, String label, List<SyntaxNode> children) {
            super(lineNumber);
            this.label = label;
            this.children = children;
        }

        @Override
        public void displaySubtree(int indentAmt) {
            printIndented(label, indentAmt);
            for (SyntaxNode child : children) {
                child.displaySubtree(indentAmt + 2);
            }
        }
    }
}
