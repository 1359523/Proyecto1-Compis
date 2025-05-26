package proyecto;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import proyecto.antlr.ExprLexer;
import proyecto.antlr.ExprParser;


public class App {
    public static void main(String[] args) {

        String ruta = "C:\\antlr\\Proyecto_Fase2\\Proyecto_Fase2\\pruebacompleta.txt";
        String contenido = "";
        try{
            contenido = Files.readString(Path.of(ruta));            
        } catch (IOException e){
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }

        String rutasalida = "C:\\antlr\\Proyecto_Fase2\\demo\\src\\main\\java\\proyecto\\salida.py";

        ExprLexer lexer = new ExprLexer(CharStreams.fromString(contenido));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExprParser parser = new ExprParser(tokens);
        ParseTree tree = parser.programa();
        //PythonGeneratorVisitor visitor = new PythonGeneratorVisitor(rutasalida);

        Evaluador visitor = new Evaluador(rutasalida);
        
        visitor.visit(tree);
        visitor.cerrarArchivo();
        //visitor.close();

        //System.out.println("Resultado: " + resultado);        
    }
}

