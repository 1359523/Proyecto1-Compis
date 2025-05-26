package proyecto;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
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

    private Map<String, Variable> variables = new HashMap<>();
    private PrintWriter writer;             // Archivo de salida .py donde se escribirá el código traducido
    private String ExpresionFinal = "";     //texto utilizado para guardar la expresion
    private int indentLevel = 0;            // Controla el nivel de sangría para bloques


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
        visit(ctx.expresion()); // postorden

        System.out.println("Visit asignacion");
        System.out.println("Valor: " + ctx.getText());
        return 0;
    }

    @Override
    public Integer visitBloque(BloqueContext ctx) {
        System.out.println("Visit: Bloque");
        
        // TODO Auto-generated method stub
        return super.visitBloque(ctx);
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

        /*if (ctx.IDENT() != null){ //Es un identificador
            System.out.println("value: " + ctx.IDENT());

        } else {

        }*/
        // TODO Auto-generated method stub
        return super.visitContenido(ctx);
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
        System.out.println("Visit: Entrada_Salida");

        // TODO Auto-generated method stub
        return super.visitEntrada_salida(ctx);
    }

    @Override
    public Integer visitEstructura_control(Estructura_controlContext ctx) {
        System.out.println("Visit: Estructura_control");

        // TODO Auto-generated method stub
        return super.visitEstructura_control(ctx);
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

        // TODO Auto-generated method stub
        return super.visitFuncion(ctx);
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
        System.out.println("Visit: Llamada_funcion");

        // TODO Auto-generated method stub
        return super.visitLlamada_funcion(ctx);
    }

    @Override
    public Integer visitParametros(ParametrosContext ctx) {
        System.out.println("Visit: Parametros");

        // TODO Auto-generated method stub
        return super.visitParametros(ctx);
    }

    @Override
    public Integer visitPrograma(ProgramaContext ctx) {

        if (visitChildren(ctx) == null){
            System.err.println("El programa ha presentado un error");
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
