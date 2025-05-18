package proyecto;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import proyecto.antlr.ExprLexer;
import proyecto.antlr.ExprParser;


public class App {
    public static void main(String[] args) {
        try {
            CharStream input = CharStreams.fromFileName("prueba1.txt");

            ExprLexer lexer = new ExprLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ExprParser parser = new ExprParser(tokens);

            ParseTree tree = parser.programa(); // la regla inicial se llama programa

            PythonGeneratorVisitor visitor = new PythonGeneratorVisitor();
            visitor.visit(tree);
            visitor.close();


            System.out.println("Traducción a Python generada en el archivo salida.py");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

