package proyecto;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import proyecto.antlr.ExprBaseVisitor;
import proyecto.antlr.ExprParser;

// Esta clase extiende el visitor base generado por ANTLR para recorrer el árbol sintáctico
public class PythonGeneratorVisitor extends ExprBaseVisitor<Integer> {

    // Archivo de salida .py donde se escribirá el código traducido
    private PrintWriter writer;

    // Controla el nivel de sangría para bloques
    private int indentLevel = 0;

    // Constructor: abre el archivo salida.py para escritura
    public PythonGeneratorVisitor() {
        try {
            writer = new PrintWriter(new FileWriter("salida.py"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar que escribe una línea en el archivo con la sangría correspondiente
    private void writeln(String line) {
        for (int i = 0; i < indentLevel; i++) {
            writer.print("    "); // 4 espacios para sangría
        }
        writer.println(line);
    }

    // Traduce una asignación en el código fuente
    @Override
    public Integer visitAsignacion(ExprParser.AsignacionContext ctx) {
        System.out.println("Visit: asignacion");

        // Extrae el nombre de la variable
        String varName = ctx.IDENT().getText();

        // Extrae el valor asignado, puede ser una expresión, string o booleano
        String value = ctx.expresion() != null ? ctx.expresion().getText() :
                       ctx.STRING() != null ? ctx.STRING().getText() :
                       ctx.booleano() != null ? ctx.booleano().getText() : "";

        // Escribe la asignación en Python
        writeln(varName + " = " + value);
        return visitChildren(ctx);
    }

    // Traduce una declaración (aunque en Python no se necesita declarar tipo)
    @Override
    public Integer visitDeclaracion(ExprParser.DeclaracionContext ctx) {
        System.out.println("Visit: declaracion");
        return visitChildren(ctx);
    }

    // Traduce un bloque { ... } como sangría en Python
    @Override
    public Integer visitBloque(ExprParser.BloqueContext ctx) {
        System.out.println("Visit: bloque");
        indentLevel++;                      // Aumenta sangría
        Integer result = visitChildren(ctx); // Visita sentencias internas
        indentLevel--;                      // Restaura sangría
        return result;
    }

    // Visita cualquier tipo de sentencia (asignación, control, print, etc.)
    @Override
    public Integer visitSentencia(ExprParser.SentenciaContext ctx) {
        System.out.println("Visit: sentencia");
        return visitChildren(ctx);
    }

    // Visita la lista de sentencias dentro de un bloque
    @Override
    public Integer visitLista_sentencias(ExprParser.Lista_sentenciasContext ctx) {
        System.out.println("Visit: lista_sentencias");
        return visitChildren(ctx);
    }

    // Traduce una expresión booleana
    @Override
    public Integer visitBooleano(ExprParser.BooleanoContext ctx) {
        System.out.println("Visit: booleano");
        return visitChildren(ctx);
    }

    // Traduce el contenido de print, write y otras expresiones
    @Override
    public Integer visitContenido(ExprParser.ContenidoContext ctx) {
        System.out.println("Visit: contenido");
        if (ctx.IDENT() != null) {
            writeln(ctx.IDENT().getText());
        } else if (ctx.STRING() != null) {
            writeln(ctx.STRING().getText());
        }
        return visitChildren(ctx);
    }

    // Traduce sentencias de entrada y salida (read, write, print)
    @Override
    public Integer visitEntrada_salida(ExprParser.Entrada_salidaContext ctx) {
        System.out.println("Visit: entrada_salida");

        // Distingue si es print, write o read por el primer hijo del árbol
        if (ctx.getChild(0).getText().equals("print")) {
            String contenido = ctx.contenido().getText();
            writeln("print(" + contenido + ")");
        } else if (ctx.getChild(0).getText().equals("write")) {
            String dato = ctx.contenido().getText();
            String archivo = ctx.STRING().getText();
            writeln("with open(" + archivo + ", 'w') as f:");
            indentLevel++;
            writeln("f.write(str(" + dato + "))");
            indentLevel--;
        } else if (ctx.getChild(0).getText().equals("read")) {
            String archivo = ctx.STRING().getText();
            writeln("with open(" + archivo + ", 'r') as f:");
            indentLevel++;
            writeln("contenido = f.read()");
            indentLevel--;
        }

        return null;
    }

    @Override
    public Integer visitParametros(ExprParser.ParametrosContext ctx) {
        System.out.println("Visit: parametros");
        StringBuilder params = new StringBuilder();

        // Primera variable
        params.append(ctx.getChild(1).getText()); // IDENT después del tipo

        // Parametros siguientes (si hay)
        for (int i = 2; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals(",")) {
                String siguienteIdent = ctx.getChild(i + 2).getText(); // después de coma viene tipo, luego IDENT
                params.append(", ").append(siguienteIdent);
                i += 2; // saltar tipo y nombre
            }
        }
        writeln("# def con parámetros: " + params.toString()); // Comentario informativo
        return visitChildren(ctx);
    }
    
    // Cierra el archivo de salida
    public void close() {
        writer.close();
    }
}
