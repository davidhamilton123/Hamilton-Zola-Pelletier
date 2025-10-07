package ast.nodes;

import lexer.TokenType;

public class BinOpNode extends SyntaxNode {
    private final SyntaxNode left;
    private final SyntaxNode right;
    private final TokenType op;

    public BinOpNode(long lineNumber, SyntaxNode left, TokenType op, SyntaxNode right) {
        super(lineNumber);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("BinOp[" + op + "](", indentAmt);
        left.displaySubtree(indentAmt + 2);
        right.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
