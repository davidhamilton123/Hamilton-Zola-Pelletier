package ast.nodes;

import lexer.TokenType;

public class UnaryOpNode extends SyntaxNode {
    private final SyntaxNode child;
    private final TokenType op;

    public UnaryOpNode(long lineNumber, TokenType op, SyntaxNode child) {
        super(lineNumber);
        this.op = op;
        this.child = child;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("UnaryOp[" + op + "](", indentAmt);
        child.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
