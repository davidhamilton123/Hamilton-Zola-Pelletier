import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ast.SyntaxTree;
import ast.nodes.SyntaxNode;
import lexer.Token;
import lexer.TokenType;

/** Minimal evaluator that walks the AST and prints values of expression statements. */
public class Evaluator {
    private final Map<String, Object> env = new HashMap<>();

    public void run(SyntaxTree tree) {
        Object root = tree.getRootNode();
        if (root == null) return;
        evalProg((SyntaxNode) root);
    }

    // ---------------- internals ----------------

    private void evalProg(SyntaxNode prog) {
        LinkedList<?> stmts = (LinkedList<?>) get(prog, "statements");
        if (stmts == null) return;
        for (Object s : stmts) {
            Object val = eval((SyntaxNode) s);
            // For non-val statements (i.e., expressions), print the result
            if (!s.getClass().getSimpleName().equals("ValNode")) {
                System.out.println(stringify(val));
            }
        }
    }

    private Object eval(SyntaxNode n) {
        String kind = n.getClass().getSimpleName();
        switch (kind) {
            case "ValNode": {
                Token name = (Token) get(n, "name");
                String id = name.getValue();
                Object rhs = eval((SyntaxNode) get(n, "rhs"));
                env.put(id, rhs);
                return rhs;
            }
            case "BinOpNode": {
                Object l = eval((SyntaxNode) get(n, "left"));
                Object r = eval((SyntaxNode) get(n, "right"));
                TokenType op = (TokenType) get(n, "op");
                return binOp(l, r, op);
            }
            case "UnaryOpNode": {
                Object child = eval((SyntaxNode) get(n, "child"));
                TokenType op = (TokenType) get(n, "op");
                return unaryOp(child, op);
            }
            case "TokenNode": {
                Token tok = (Token) get(n, "token");
                return literal(tok);
            }
            default:
                throw new RuntimeException("Unknown node: " + kind);
        }
    }

    private Object binOp(Object l, Object r, TokenType op) {
        switch (op) {
            case ADD:  return num(l) + num(r);
            case SUB:  return num(l) - num(r);
            case MULT: return num(l) * num(r);
            case DIV:  return num(l) / num(r);
            case MOD:  return (long) num(l) % (long) num(r);
            case AND:  return bool(l) && bool(r);
            case OR:   return bool(l) || bool(r);
            default:   throw new RuntimeException("Unsupported bin op: " + op);
        }
    }

    private Object unaryOp(Object v, TokenType op) {
        switch (op) {
            case NOT: return !bool(v);
            default:  throw new RuntimeException("Unsupported unary op: " + op);
        }
    }

    private Object literal(Token tok) {
        switch (tok.getType()) {
            case INT:   return Long.parseLong(tok.getValue());
            case REAL:  return Double.parseDouble(tok.getValue());
            case TRUE:  return Boolean.TRUE;
            case FALSE: return Boolean.FALSE;
            case ID: {
                Object val = env.get(tok.getValue());
                if (val == null) throw new RuntimeException("Undefined variable: " + tok.getValue());
                return val;
            }
            default:
                throw new RuntimeException("Unsupported literal token: " + tok);
        }
    }

    // numeric promotion: if either is real, return double; else long
    private double num(Object v) {
        if (v instanceof Double) return (Double) v;
        if (v instanceof Float)  return ((Float) v).doubleValue();
        if (v instanceof Long)   return ((Long) v).doubleValue();
        if (v instanceof Integer) return ((Integer) v).doubleValue();
        throw new RuntimeException("Expected number, got: " + v);
    }

    private boolean bool(Object v) {
        if (v instanceof Boolean) return (Boolean) v;
        throw new RuntimeException("Expected boolean, got: " + v);
    }

    private Object get(Object o, String field) {
        try {
            Field f = o.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(o);
        } catch (Exception e) {
            throw new RuntimeException("Reflection failed for " + o.getClass().getSimpleName() + "." + field, e);
        }
    }

    private String stringify(Object v) {
        if (v instanceof Double) {
            double d = (Double) v;
            if (Math.floor(d) == d) return String.valueOf((long) d);
            return String.valueOf(d);
        }
        return String.valueOf(v);
    }
}
