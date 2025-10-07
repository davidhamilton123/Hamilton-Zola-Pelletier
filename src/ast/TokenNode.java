package ast;


import ast.nodes.SyntaxNode;
// This is TokenNode.java
import lexer.Token;

/** Leaf node wrapping a token (ID, INT, REAL, TRUE, FALSE). */
public class TokenNode extends SyntaxNode {
    private final Token token;

    public TokenNode(long lineNumber, Token token) {
        super(lineNumber);
        this.token = token;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        // Token.toString() already prints like INT(3), ID(x), TRUE, etc.
        printIndented("Token(" + token.toString() + ")", indentAmt);
    }
}
