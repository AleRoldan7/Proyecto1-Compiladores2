package semantico.analizadores;

import ast.NodoAST;
import ast.sentencias.Bloque;
import ast.sentencias.Sentencia;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorBloque implements AnalizadorSemantico<Bloque> {

    private final AnalizadorSemanticoCoordinador analizadorSemanticoCoordinador;

    @Override
    public void analizar(Bloque nodoBloque, AnalisisContexto  analisisContexto) {

        analisisContexto.getTablaSimbolos().entrarAmbito("bloque");

        for (Sentencia sentencia : nodoBloque.getSentencias()) {
            analizadorSemanticoCoordinador.analizar((NodoAST) sentencia, analisisContexto);
        }

        analisisContexto.getTablaSimbolos().salirAmbito();
    }

}
