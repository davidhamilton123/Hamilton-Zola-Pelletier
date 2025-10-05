// File: syntax/nodes/SyntaxNode.java
package syntax.nodes;

/**
 * Base class for all nodes in the Abstract Syntax Tree (AST).
 * Each node tracks its line number and must implement displaySubtree().
 */
public abstract class SyntaxNode {
    private final long lineNumber;

    public SyntaxNode(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    /** Returns the line number of this node. */
    public long getLineNumber() {
        return lineNumber;
    }

    /** Each subclass must print its structure recursively. */
    public abstract void displaySubtree(int indentAmt);

    /** Helper method to print indented lines. */
    protected void printIndented(String text, int indentAmt) {
        for (int i = 0; i < indentAmt; i++) {
            System.out.print(" ");
        }
        System.out.println(text);
    }
}
