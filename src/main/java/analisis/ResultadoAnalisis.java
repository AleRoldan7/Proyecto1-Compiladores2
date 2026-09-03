package analisis;

import ast.NodoAST;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tablas.TablaSimbolos;
import tablas.TablaTipos;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResultadoAnalisis {

    private final boolean correcto;
    private final NodoAST ast;
    private final TablaSimbolos tablaSimbolos;
    private final TablaTipos tablaTipos;
    private final List<String> errores;
}
