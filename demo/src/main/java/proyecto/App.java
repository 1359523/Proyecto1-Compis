package proyecto;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import proyecto.antlr.ExprLexer;
import proyecto.antlr.ExprParser;

public class App 
{
    public static void main( String[] args )
    {
        String input = "print (" + '"' + "Hello World" + '"' + ");";

        //Crear lexer y parser
        ExprLexer lexer = new ExprLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExprParser parser = new ExprParser(tokens);

        //obtener el arbol de sintaxis
        ParseTree tree = parser.programa();

        //Usar el visitor
        Evaluador visitor = new Evaluador();
        
        int resultado = visitor.visit(tree);

        System.out.println("Resultado: " + resultado);
    }
}
