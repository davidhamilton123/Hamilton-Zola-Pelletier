package ast.nodes;

import lexer.TokenType;

public class RelOpNode extends SyntaxNode {
    private final SyntaxNode left;
    private final SyntaxNode right;
    private final TokenType op;

    public RelOpNode(long lineNumber, SyntaxNode left, TokenType op, SyntaxNode right) {
        super(lineNumber);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("RelOp[" + op + "](", indentAmt);
        left.displaySubtree(indentAmt + 2);
        right.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
