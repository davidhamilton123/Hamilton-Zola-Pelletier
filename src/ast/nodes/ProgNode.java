package ast.nodes;

import java.util.LinkedList;

public class ProgNode extends SyntaxNode {
    private final LinkedList<SyntaxNode> statements;

    public ProgNode(long lineNumber, LinkedList<SyntaxNode> statements) {
        super(lineNumber);
        this.statements = statements;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Prog(", indentAmt);
        for (SyntaxNode s : statements) {
            s.displaySubtree(indentAmt + 2);
        }
        printIndented(")", indentAmt);
    }
}
