// File: syntax/SyntaxTree.java
package syntax;

import syntax.Nodes.SyntaxNode;

/**
 * Represents the root of an Abstract Syntax Tree (AST).
 * Stores a reference to the root node and provides utilities to display it.
 */
public class SyntaxTree {
    private final SyntaxNode root;

    public SyntaxTree(SyntaxNode root) {
        this.root = root;
    }

    /** Returns the root node of this syntax tree. */
    public SyntaxNode getRoot() {
        return root;
    }

    /** Prints a structured view of the tree starting from the root. */
    public void display() {
        if (root != null) {
            root.displaySubtree(0);
        } else {
            System.out.println("<empty tree>");
        }
    }
}
