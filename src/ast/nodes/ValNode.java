package ast.nodes;

import lexer.Token;

public class ValNode extends SyntaxNode {
    private final Token name;
    private final SyntaxNode rhs;

    public ValNode(long lineNumber, Token name, SyntaxNode rhs) {
        super(lineNumber);
        this.name = name;
        this.rhs = rhs;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Val[" + name.getValue() + "](", indentAmt);
        rhs.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
