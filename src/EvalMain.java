import java.io.File;
import parser.MFLParser;
import parser.ParseException;
import ast.SyntaxTree;

public class EvalMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: java EvalMain <file.mfl>");
            return;
        }
        MFLParser p = new MFLParser(new File(args[0]));
        SyntaxTree ast = p.parse();
        new Evaluator().run(ast);
    }
}
