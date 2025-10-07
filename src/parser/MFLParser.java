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

    @Override
    public SyntaxTree parse() throws ParseException {
        nextToken();
        SyntaxNode root = parseProg();

        if (!tokenIs(TokenType.EOF))
            logError("Unexpected token after end of program.");

        return new SyntaxTree(root);
    }

    // <prog> → <val> ; { <val> ; }
    private SyntaxNode parseProg() throws ParseException {
        List<SyntaxNode> stmts = new ArrayList<>();

        // handle empty files gracefully
        if (tokenIs(TokenType.EOF)) {
            return new ProgNode(getCurrLine(), new ArrayList<>());
        }

        while (!tokenIs(TokenType.EOF)) {
            SyntaxNode stmt = parseVal();
            if (stmt != null) stmts.add(stmt);

            if (tokenIs(TokenType.SEMI))
                match(TokenType.SEMI, ";");
            else
                break;
        }

        return new ProgNode(getCurrLine(), stmts);
    }

    // <val> → val <id> := <expr> | <expr>
    private SyntaxNode parseVal() throws ParseException {
        if (checkMatch(TokenType.VAL)) {
            Token idTok = getCurrToken();
            match(TokenType.ID, "identifier");
            match(TokenType.ASSIGN, ":=");
            SyntaxNode expr = parseExpr();
            return new ValNode(getCurrLine(), idTok.getValue(), expr);
        } else {
            return parseExpr();
        }
    }

    // <expr> → <rexpr> { (and | or) <rexpr> }
    private SyntaxNode parseExpr() throws ParseException {
        SyntaxNode left = parseRexpr();
        while (tokenIs(TokenType.AND) || tokenIs(TokenType.OR)) {
            Token op = getCurrToken();
            nextToken();
            SyntaxNode right = parseRexpr();
            left = new BinOpNode(getCurrLine(), op.getType().name(), left, right);
        }
        return left;
    }

    // <rexpr> → <mexpr> [ ( < | > | >= | <= | = | != ) <mexpr> ]
    private SyntaxNode parseRexpr() throws ParseException {
        SyntaxNode left = parseMexpr();
        if (tokenIs(TokenType.GT) || tokenIs(TokenType.LT) || tokenIs(TokenType.GTE)
                || tokenIs(TokenType.LTE) || tokenIs(TokenType.EQ) || tokenIs(TokenType.NEQ)) {
            Token op = getCurrToken();
            nextToken();
            SyntaxNode right = parseMexpr();
            return new RelOpNode(getCurrLine(), op.getType().name(), left, right);
        }
        return left;
    }

    // <mexpr> → <term> { ( + | - ) <term> }
    private SyntaxNode parseMexpr() throws ParseException {
        SyntaxNode left = parseTerm();
        while (tokenIs(TokenType.ADD) || tokenIs(TokenType.SUB)) {
            Token op = getCurrToken();
            nextToken();
            SyntaxNode right = parseTerm();
            left = new BinOpNode(getCurrLine(), op.getType().name(), left, right);
        }
        return left;
    }

    // <term> → not <rexpr> | <factor> { ( * | / | mod ) <factor> }
    private SyntaxNode parseTerm() throws ParseException {
        if (checkMatch(TokenType.NOT)) {
            SyntaxNode expr = parseRexpr();
            return new UnaryOpNode(getCurrLine(), "NOT", expr);
        }

        SyntaxNode left = parseFactor();
        while (tokenIs(TokenType.MULT) || tokenIs(TokenType.DIV) || tokenIs(TokenType.MOD)) {
            Token op = getCurrToken();
            nextToken();
            SyntaxNode right = parseFactor();
            left = new BinOpNode(getCurrLine(), op.getType().name(), left, right);
        }
        return left;
    }

    // <factor> → <id> | <int> | <real> | ( <expr> ) | true | false
    private SyntaxNode parseFactor() throws ParseException {
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
                SyntaxNode expr = parseExpr();
                match(TokenType.RPAREN, ")");
                return expr;
            default:
                logError("Unexpected token in factor: " + tok);
                throw new ParseException();
        }
    }

    /**********************
     * AST Node Definitions
     **********************/

    private static class ProgNode extends SyntaxNode {
        private final List<SyntaxNode> children;
        public ProgNode(long line, List<SyntaxNode> children) {
            super(line);
            this.children = children;
        }
        @Override
        public void displaySubtree(int indent) {
            printIndented("Prog(", indent);
            for (SyntaxNode child : children) {
                child.displaySubtree(indent + 2);
            }
            printIndented(")", indent);
        }
    }

    private static class ValNode extends SyntaxNode {
        private final String name;
        private final SyntaxNode expr;
        public ValNode(long line, String name, SyntaxNode expr) {
            super(line);
            this.name = name;
            this.expr = expr;
        }
        @Override
        public void displaySubtree(int indent) {
            printIndented("Val[" + name + "](", indent);
            expr.displaySubtree(indent + 2);
            printIndented(")", indent);
        }
    }

    private static class BinOpNode extends SyntaxNode {
        private final String op;
        private final SyntaxNode left, right;
        public BinOpNode(long line, String op, SyntaxNode left, SyntaxNode right) {
            super(line);
            this.op = op;
            this.left = left;
            this.right = right;
        }
        @Override
        public void displaySubtree(int indent) {
            printIndented("BinOp[" + op + "](", indent);
            left.displaySubtree(indent + 2);
            right.displaySubtree(indent + 2);
            printIndented(")", indent);
        }
    }

    private static class RelOpNode extends SyntaxNode {
        private final String op;
        private final SyntaxNode left, right;
        public RelOpNode(long line, String op, SyntaxNode left, SyntaxNode right) {
            super(line);
            this.op = op;
            this.left = left;
            this.right = right;
        }
        @Override
        public void displaySubtree(int indent) {
            printIndented("RelOp[" + op + "](", indent);
            left.displaySubtree(indent + 2);
            right.displaySubtree(indent + 2);
            printIndented(")", indent);
        }
    }

    private static class UnaryOpNode extends SyntaxNode {
        private final String op;
        private final SyntaxNode expr;
        public UnaryOpNode(long line, String op, SyntaxNode expr) {
            super(line);
            this.op = op;
            this.expr = expr;
        }
        @Override
        public void displaySubtree(int indent) {
            printIndented("UnaryOp[" + op + "](", indent);
            expr.displaySubtree(indent + 2);
            printIndented(")", indent);
        }
    }
}
