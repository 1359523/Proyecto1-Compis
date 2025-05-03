package proyecto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

//Imports de antlr
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import proyecto.antlr.ExprLexer;
import proyecto.antlr.ExprParser;

public class App 
{
    public static void main( String[] args )
    {
        String ruta = "C:\\antlr\\Proyecto_Fase2\\Proyecto_Fase2\\prueba2.txt";
        String contenido = "";
        try{
            contenido = Files.readString(Path.of(ruta));            
        } catch (IOException e){
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }

        System.out.println(contenido);

        //Crear lexer y parser
        ExprLexer lexer = new ExprLexer(CharStreams.fromString(contenido));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExprParser parser = new ExprParser(tokens);

        //obtener el arbol de sintaxis
        ParseTree tree = parser.programa();

        //Usar el visitor
        Evaluador visitor = new Evaluador();
        
        visitor.visit(tree);

        //System.out.println("Resultado: " + resultado);
    }
}
