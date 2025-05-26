package proyecto;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.Text;

import proyecto.Variable;

import proyecto.antlr.ExprBaseVisitor;
import proyecto.antlr.ExprParser.AsignacionContext;
import proyecto.antlr.ExprParser.BloqueContext;
import proyecto.antlr.ExprParser.BooleanoContext;
import proyecto.antlr.ExprParser.ContenidoContext;
import proyecto.antlr.ExprParser.DeclaracionContext;
import proyecto.antlr.ExprParser.Entrada_salidaContext;
import proyecto.antlr.ExprParser.Estructura_controlContext;
import proyecto.antlr.ExprParser.ExpresionContext;
import proyecto.antlr.ExprParser.Expresion_multiplicacionContext;
import proyecto.antlr.ExprParser.Expresion_parentesisContext;
import proyecto.antlr.ExprParser.Expresion_sumaContext;
import proyecto.antlr.ExprParser.FuncionContext;
import proyecto.antlr.ExprParser.Lista_declaracionesContext;
import proyecto.antlr.ExprParser.Lista_funcionesContext;
import proyecto.antlr.ExprParser.Lista_sentenciasContext;
import proyecto.antlr.ExprParser.Llamada_funcionContext;
import proyecto.antlr.ExprParser.ParametrosContext;
import proyecto.antlr.ExprParser.ProgramaContext;
import proyecto.antlr.ExprParser.SentenciaContext;


public class Evaluador extends ExprBaseVisitor<Integer> {

    private Map<String, Variable> variables = new HashMap<>(); //Guarda las variables
    private Set<String> funciones = new HashSet<>();
    private PrintWriter writer;             // Archivo de salida .py donde se escribirá el código traducido
    private String ExpresionFinal = "";     //texto utilizado para guardar la expresion
    private int indentLevel = 0;            // Controla el nivel de sangría para bloques
    private String paramfinales = "";       //Guarda los parametros de las funciones
    
    public Evaluador(String Rutasalida){
        try {
            // Sobrescribe directamente el archivo indicado por la ruta
            writer = new PrintWriter(new FileWriter(Rutasalida, false)); // false = sobrescribe
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Metodo para cerrar el archivo 
    public void cerrarArchivo() {
    if (writer != null) {
        writer.close();
    }
    }


    //Metodo para escribir en el archivo
    private void writeln(String line) {
        for (int i = 0; i < indentLevel; i++) {
            writer.print("    "); // 4 espacios para sangría
        }
        writer.println(line);
    }

    @Override
    public Integer visitAsignacion(AsignacionContext ctx) {
        
        // Extrae el nombre de la variable
        String varName = ctx.IDENT().getText();
        String value;
        String Texto;

        if (!variables.containsKey(varName)) {
        System.err.println("Error: La variable '" + varName + "' no ha sido declarada.");
        return null;
        }

        if (ctx.expresion() != null) {
            visit(ctx.expresion()); // Actualiza ExpresionFinal
            value = ExpresionFinal;
            Texto = ctx.expresion().getText();
        } else if (ctx.STRING() != null) {
            value = ctx.STRING().getText(); // mantener comillas
            Texto = value;
        } else if (ctx.booleano() != null) {
            visit(ctx.booleano()); // actualiza ExpresionFinal si se visitan booleanos
            value = ExpresionFinal;
            Texto = value;
        } else {
            value = "none"; // por si acaso
            Texto = "none";
        }

        // Actualizar el valor en el mapa
        Variable variable = variables.get(varName);
        variable.SetValor(value);

        writeln(varName + " = " + Texto);        

        System.out.println("Visit asignacion");
        System.out.println("Valor: " + ctx.getText());
        return 0;
    }

    @Override
    public Integer visitBloque(BloqueContext ctx) {
        System.out.println("Visit: bloque");
        Integer result = visitChildren(ctx); // Visita sentencias internas
        indentLevel--;                      // Restaura sangría
        return result;
    }

    @Override
    public Integer visitBooleano(BooleanoContext ctx) {

        System.out.println("Visit: Booleano");
        // Caso 'True' o 'False'
        if (ctx.getText().equals("True") || ctx.getText().equals("False")) {
            ExpresionFinal = ctx.getText();  // Python usa True/False tal cual
            return 0;
        }

        // Caso IDENT operador IDENT
        if (ctx.IDENT().size() == 2) {
            String izq = ctx.IDENT(0).getText();
            String der = ctx.IDENT(1).getText();
            String operador = ctx.getChild(1).getText();    //Obtener el operador que se esta utilizando

            if (!variables.containsKey(izq)){
                System.err.println("Error: Variable '" + izq + "' no esta declarada");
                return null;
            }
            if (!variables.containsKey(der)){
                System.err.println("Error: Variable '" + izq + "' no esta declarada");
                return null;
            }

            ExpresionFinal = izq + " " + operador + " " + der;
            return 0;
        }

        // Caso NUMERO operador NUMERO
        if (ctx.NUMERO().size() == 2) {
            String izq = ctx.NUMERO(0).getText();
            String der = ctx.NUMERO(1).getText();
            String operador = ctx.getChild(1).getText();

            ExpresionFinal = izq + " " + operador + " " + der;
            return 0;
        }        
        
        return 0;
    }

    @Override
    public Integer visitContenido(ContenidoContext ctx) {
        System.out.println("Visit: Contenido");

        if (ctx.IDENT() != null) {
            String nombre = ctx.IDENT().getText();
            if (!variables.containsKey(nombre)){ //Validar que la variable haya sido declarada antes
                System.err.println("Error: Variable '" + nombre + "' no esta declarada");
                return null;
            }
            writeln(ctx.IDENT().getText());
        } else if (ctx.STRING() != null) {
            writeln(ctx.STRING().getText());
        }
        return visitChildren(ctx);
    }

    @Override
    public Integer visitDeclaracion(DeclaracionContext ctx) {
        // postorden: primero visita hijos luego al nodo padre
        if (ctx.expresion() != null) {
            visit(ctx.expresion());
        }
        
        String nombre = ctx.IDENT().getText();              //Extraer el nombre de la variable

        if (variables.containsKey(nombre)){
            System.err.println("Error: La variable '" + nombre + "' ya fue declarada.");
            return null;
        }

        String tipo = ctx.getStart().getText();             //Obtiene el tipo de variable 
        String valor;
        String Texto = "";

        if (ctx.expresion() != null){
            if (!tipo.equals("int") && !tipo.equals("float")) {
                System.err.println("El tipo de variable no coincide");
                return null;
            }
            visit(ctx.expresion());         //El resultado queda en ExpresionFinal
            valor = ExpresionFinal;
            Texto = ctx.expresion().getText();
        }
        else if (ctx.STRING() != null){
            if (!tipo.equals("string")){
                System.err.println("El tipo de variable no coincide");
                return null;
            }
            valor = ctx.STRING().getText();             //Importante, esto si reserva las comillas
            Texto = valor;
        }
        else if (ctx.booleano() != null){
            if (!tipo.equals("bool")){
                System.err.println("El tipo de variable no coincide");
                return null;
            }
            visit(ctx.booleano());
            valor = ExpresionFinal;         //El resultado queda en ExpresionFinal
            Texto = valor;
        }
        else {
            valor = "none";
            Texto = "none";
        }

        Variable Nueva = new Variable(tipo, valor);
        variables.put(nombre, Nueva);

        //Escribir la Salida en salida.py
        writeln(nombre + " = " + Texto);


        System.out.println("Visit declaracion");
        System.out.println("Valor: " + ctx.getText());
        return 0;        
    }

    @Override
    public Integer visitEntrada_salida(Entrada_salidaContext ctx) {
        System.out.println("Visit: entrada_salida");

        // Distingue si es print, write o read por el primer hijo del árbol
        if (ctx.getChild(0).getText().equals("print")) {
            String contenido = ctx.contenido().getText();
            if (ctx.contenido().IDENT() != null && !variables.containsKey(ctx.contenido().IDENT().getText())){
                System.err.println("Error: La variable '" + ctx.contenido().IDENT().getText() + "' no ha sido declarada.");
                return null;
            }
            writeln("print(" + contenido + ")");
        } else if (ctx.getChild(0).getText().equals("write")) {
            if (ctx.contenido().IDENT() != null && !variables.containsKey(ctx.contenido().IDENT().getText())){
                System.err.println("Error: La variable '" + ctx.contenido().IDENT().getText() + "' no ha sido declarada.");
                return null;
            }
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

        return 0;
    }

    @Override
    public Integer visitEstructura_control(Estructura_controlContext ctx) {
        String keyword = ctx.getStart().getText(); // 'if' o 'while'
        String condicion = ctx.booleano().getText(); //extrae la condicion de la estructura (booleano)

        if(keyword.equals("if")){
            System.out.println("Visit: estructura_control - if");

            //escribir la estructura if en python
            writeln("if "+ condicion +":");
            indentLevel++;
            visit(ctx.bloque(0));

            if(ctx.bloque().size() > 1){
                writeln("else:");
                indentLevel++;
                visit(ctx.bloque(1));
            }
        } else {
            System.out.println("Visit: estructura_control - while");       //es un while
            writeln("while "+ condicion+ ":");
            indentLevel++;
            visit(ctx.bloque(0));
        }

        return 0;       
    }

    @Override
    public Integer visitExpresion(ExpresionContext ctx) {
        System.out.println("Visit: Expresion");

        ExpresionFinal = String.valueOf(visit(ctx.expresion_suma()));

        return visit(ctx.expresion_suma());

        // TODO Auto-generated method stub
        //return super.visitExpresion(ctx);
    }

    @Override
    public Integer visitExpresion_multiplicacion(Expresion_multiplicacionContext ctx) {
        //System.out.println("Visit: Expresion_multiplicacion");

        // Visitamos el primer término
        Integer resultado = visit(ctx.expresion_parentesis(0));

        for (int i = 1; i < ctx.expresion_parentesis().size(); i++) {
            String operador = ctx.getChild(2 * i - 1).getText(); // accede al operador entre los operandos
            Integer siguiente = visit(ctx.expresion_parentesis(i));

            if (operador.equals("*")) {
             resultado *= siguiente;
            } else if (operador.equals("/")) {
             resultado /= siguiente;
            }
        }

        System.out.println("Visit expresion_multiplicacion");
        System.out.println("Valor: " + resultado);
        return resultado;
        // TODO Auto-generated method stub
        //return super.visitExpresion_multiplicacion(ctx);
    }

    @Override
    public Integer visitExpresion_parentesis(Expresion_parentesisContext ctx) {
        //System.out.println("Visit: Expresion_parentesis");

        Integer valor = null;

        if (ctx.NUMERO() != null) {
            valor = Integer.parseInt(ctx.NUMERO().getText());
            System.out.println("Visit expresion_parentesis");
            System.out.println("Valor: " + valor);
        } else if (ctx.IDENT() != null) {
            String nombre = ctx.IDENT().getText();
            if (!variables.containsKey(nombre)) {
                System.err.println("Error: Variable '" + nombre + "' no esta declarada.");
                return null;
            }
            System.out.println("Visit expresion_parentesis");
            System.out.println("Valor: " + nombre);

            Variable var = variables.get(nombre);
            valor = Integer.parseInt(var.getValor());
            
        } else if (ctx.expresion() != null) {
            valor = visit(ctx.expresion());
            System.out.println("Visit expresion_parentesis");
            System.out.println("Valor: " + valor);
        }        
        return valor;

        // TODO Auto-generated method stub
        //return super.visitExpresion_parentesis(ctx);
    }

    @Override
    public Integer visitExpresion_suma(Expresion_sumaContext ctx) {
        //System.out.println("Visit: Expresion_suma");
        Integer resultado = visit(ctx.expresion_multiplicacion(0));

        for (int i = 1; i < ctx.expresion_multiplicacion().size(); i++) {
            String operador = ctx.getChild(2 * i - 1).getText(); // accede al operador entre operandos
            Integer siguiente = visit(ctx.expresion_multiplicacion(i));

            if (operador.equals("+")) {
                resultado += siguiente;
            } else if (operador.equals("-")) {
                resultado -= siguiente;
            }
        }

        System.out.println("Visit expresion_suma");
        System.out.println("Valor: " + resultado);
        
        return resultado;

        // TODO Auto-generated method stub
        //return super.visitExpresion_suma(ctx);
    }

    @Override
    public Integer visitFuncion(FuncionContext ctx) {
        System.out.println("Visit: Funcion");

        String nombreFuncion = ctx.IDENT().getText();

        if (funciones.contains(nombreFuncion)){
            System.err.println("Error: Ya existe una funcion llamada '" + nombreFuncion + "'");
            return null;
        }
        funciones.add(nombreFuncion);

        
        if (!ctx.parametros().isEmpty()) {
            visit(ctx.parametros(0)); // Solo uno porque es opcional y se agrupa con *
        }

        writeln("def " + nombreFuncion + "(" + paramfinales + "):");
        
        indentLevel++;

        // Procesar el bloque de la función
        visit(ctx.bloque());        

        System.out.println("Visit funcion: " + nombreFuncion);
        return 0;
    }

    @Override
    public Integer visitLista_declaraciones(Lista_declaracionesContext ctx) {
        
        visitChildren(ctx);
        System.out.println("Visit lista_declaraciones");
        return 0;

        // TODO Auto-generated method stub
        //return super.visitLista_declaraciones(ctx);
    }

    @Override
    public Integer visitLista_funciones(Lista_funcionesContext ctx) {
        visitChildren(ctx);
        System.out.println("Visit lista_funciones");
        return 0;

        // TODO Auto-generated method stub
        //return super.visitLista_funciones(ctx);
    }

    @Override
    public Integer visitLista_sentencias(Lista_sentenciasContext ctx) {
        visitChildren(ctx); // recorre postorden
        System.out.println("Visit lista_sentencias");
        return 0;

        // TODO Auto-generated method stub
        //return super.visitLista_sentencias(ctx);
    }

    @Override
    public Integer visitLlamada_funcion(Llamada_funcionContext ctx) {
        // Extrae el nombre de la función
        String nombreFuncion = ctx.IDENT(0).getText();
        if (!funciones.contains(nombreFuncion)){
            System.err.println("La funcion que se desea llamar no existe");
            return null;
        }

        // Extrae argumentos (desde el segundo IDENT en adelante, si existen)
        List<String> argumentos = new ArrayList<>();
        for (int i = 1; i < ctx.IDENT().size(); i++) {
            argumentos.add(ctx.IDENT(i).getText());
        }

        // Arma la línea en formato Python
        String llamada = nombreFuncion + "(" + String.join(", ", argumentos) + ")";

        // Escribe en salida.py
        writeln(llamada);

        // Mensaje de depuración
        System.out.println("Visit: llamada_funcion -> " + llamada);

        return 0;
    }

    @Override
    public Integer visitParametros(ParametrosContext ctx) {
        List<String> parametros = new ArrayList<>();

        List<TerminalNode> tipos = ctx.children.stream()
            .filter(c -> c instanceof TerminalNode && (
                c.getText().equals("int") || 
                c.getText().equals("float") || 
                c.getText().equals("string") || 
                c.getText().equals("bool")))
            .map(c -> (TerminalNode) c)
            .toList();

        List<TerminalNode> nombres = ctx.IDENT();

        for (int i = 0; i < nombres.size(); i++) {
            String tipo = tipos.get(i).getText();
            String nombre = nombres.get(i).getText();

            // Guarda en el mapa de variables con valor ""
            Variable paramVar = new Variable(tipo, "0");
            variables.put(nombre, paramVar);

            parametros.add(nombre);
        }

        paramfinales = String.join(", ", parametros);
        return 0;     
    }

    @Override
    public Integer visitPrograma(ProgramaContext ctx) {

        Variable v = new Variable("string", "0");
        variables.put("contenido", v);

        if (visitChildren(ctx) == null){
            //System.err.println("El programa ha presentado un error");
        }
        System.out.println("Visit programa");
        return 0;

        // TODO Auto-generated method stub
        //return super.visitPrograma(ctx);
    }

    @Override
    public Integer visitSentencia(SentenciaContext ctx) {
        visitChildren(ctx); // primero todo hijo
        System.out.println("Visit sentencia");
        return 0;

        // TODO Auto-generated method stub
        //return super.visitSentencia(ctx);
    }

        
}
