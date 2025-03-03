#include <iostream>
#include <fstream>
#include <string>

using namespace std;

enum TokenType {
    IDENTIFICADOR, NUMERO_ENTERO, NUMERO_FLOTANTE, CADENA_TEXTO,
    OPERADOR_ARITMETICO, OPERADOR_RELACIONAL, PALABRA_CLAVE,
    SIMBOLO_ESPECIAL, ERROR, FIN_DE_LINEA, FIN_DE_ARCHIVO
};

struct Token {
    TokenType tipo;
    string valor;   
};

const string palabras_clave[] = { "int", "float", "string", "bool", "if", "else", "while", "return", "read", "write", "def" };
const char operadores_aritmeticos[] = { '+', '-', '*', '/' };
const string operadores_relacionales[] = { "==", "!=", "> ", "< " };
const char simbolos_especiales[] = { '{', '}', '(', ')', ';', '=', ','};

bool esDigito(char c) { return c >= '0' && c <= '9'; }
bool esLetra(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'; }
bool esEspacio(char c) { return c == ' ' || c == '\t' || c == '\n'; }
bool esOperadorAritmetico(char c) {
    for (char op : operadores_aritmeticos) if (c == op) return true;
    return false;
}
bool esSimboloEspecial(char c) {
    for (char s : simbolos_especiales) if (c == s) return true;
    return false;
}
bool esPalabraClave(string str) {
    for (string palabra : palabras_clave) if (str == palabra) return true;
    return false;
}
bool esOperadorRelacional(string str) {
    for (string op : operadores_relacionales) if (str == op) return true;
    return false;
}

class AnalizadorLexico {
private:
    string codigo;
    int posicion;
public:
    AnalizadorLexico(string entrada) : codigo(entrada), posicion(0) {}

    Token obtenerSiguienteToken() {
        while (posicion < codigo.size() && esEspacio(codigo[posicion])) {
            posicion++; // Ignorar espacios en blanco
        }
        if (posicion >= codigo.size()) return { FIN_DE_LINEA, "" };

        char c = codigo[posicion];

        // Identificadores o palabras clave
        if (esLetra(c)) {
            string lexema;
            while (posicion < codigo.size() && (esLetra(codigo[posicion]) || esDigito(codigo[posicion]))) {
                lexema += codigo[posicion++];
            }
            return esPalabraClave(lexema) ? Token{ PALABRA_CLAVE, lexema } : Token{ IDENTIFICADOR, lexema };
        }

        // Números enteros y flotantes
        if (esDigito(c)) {
            string numero;
            while (posicion < codigo.size() && esDigito(codigo[posicion])) {
                numero += codigo[posicion++];
            }
            if (codigo[posicion] == '.' && esDigito(codigo[posicion + 1])) {
                numero += codigo[posicion++];
                while (posicion < codigo.size() && esDigito(codigo[posicion])) {
                    numero += codigo[posicion++];
                }
                return { NUMERO_FLOTANTE, numero };
            }
            return { NUMERO_ENTERO, numero };
        }

        // Cadenas de texto
        if (c == '"') {
            string cadena;
            cadena += codigo[posicion++];
            while (posicion < codigo.size() && codigo[posicion] != '"') {
                cadena += codigo[posicion++];
            }
            if (posicion < codigo.size()) cadena += codigo[posicion++]; // Agregar comilla de cierre
            return { CADENA_TEXTO, cadena };
        }

        // Operadores relacionales
        string dosCaracteres = codigo.substr(posicion, 2);
        if (esOperadorRelacional(dosCaracteres)) {
            posicion += 2;
            return { OPERADOR_RELACIONAL, dosCaracteres };
        }

        // Operadores aritméticos
        if (esOperadorAritmetico(c)) {
            posicion++;
            return { OPERADOR_ARITMETICO, string(1, c) };
        }

        // Símbolos especiales
        if (esSimboloEspecial(c)) {
            posicion++;
            return { SIMBOLO_ESPECIAL, string(1, c) };
        }

        // Si no coincide con ningún token válido
        posicion++;
        return { ERROR, string(1, c) };
    }
};

void imprimirToken(Token token) {
    string nombresTipos[] = {
        "IDENTIFICADOR", "NUMERO_ENTERO", "NUMERO_FLOTANTE", "CADENA_TEXTO",
        "OPERADOR_ARITMETICO", "OPERADOR_RELACIONAL", "PALABRA_CLAVE",
        "SIMBOLO_ESPECIAL", "ERROR", "FIN_DE_LINEA", "FIN_DE_ARCHIVO"
    };    
    cout << "Token: " << nombresTipos[token.tipo] << " -> " << token.valor << endl;
}

int main() {
    ifstream archivo(R"(C:\Users\adria\OneDrive\Documents\GitHub\Proyecto1-Compis\Proyecto_Compiladores_Fase1\HolaMundo.txt)");
    if (!archivo) {
        cout << "Error al abrir el archivo" << endl;
        return 1;
    }

    string linea;
    while (getline(archivo, linea)) {
        AnalizadorLexico analizador(linea);
        Token token;

        do {
            token = analizador.obtenerSiguienteToken();
            if (token.tipo != FIN_DE_LINEA) imprimirToken(token);            
        } while (token.tipo != FIN_DE_LINEA);
    }
    Token Final;
    Final.tipo = FIN_DE_ARCHIVO;
    imprimirToken(Final);

    archivo.close();

    return 0;
}
