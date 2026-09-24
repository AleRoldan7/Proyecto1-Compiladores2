package semantico.coordinadorsemantico;

import ast.expresiones.Expresion;
import ast.expresiones.LlamadaFuncion;
import ast.tipos.Tipo;
import enums.Categoria;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;
import tablas.FilaTabla;
import tablas.InformeTipo;
import tablas.MetodoRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class InferidorLlamadaFuncion implements InferirTipo<LlamadaFuncion> {

    private static final Set<String> LECTURAS = Set.of("readln", "leer");
    private static final Set<String> IMPRESIONES = Set.of("print", "println", "imprimir");

    private final InferirTipoCoordinador inferirTipoCoordinador;

    public static boolean esLectura(String nombre) {
        return LECTURAS.contains(nombre);
    }

    @Override
    public Tipo inferir(LlamadaFuncion nodoLlamada, AnalisisContexto analisisContexto) {

        String nombre = nodoLlamada.getNombre();
        int linea = nodoLlamada.getLinea();
        int columna = nodoLlamada.getColumna();

        // 1. Tipos de los argumentos (lista nula = sin argumentos)
        List<Expresion> argumentos = nodoLlamada.getArgumentos() == null
                ? List.of() : nodoLlamada.getArgumentos();

        List<Tipo> tiposArgumentos = new ArrayList<>();
        boolean argumentosValidos = true;

        for (Expresion argumento : argumentos) {
            Tipo tipo = inferirTipoCoordinador.inferir(argumento, analisisContexto);
            if (tipo == null) {
                argumentosValidos = false;   // el error ya se reportó, no lo repetimos
            }
            tiposArgumentos.add(tipo);
        }

        // 2. Funciones predefinidas
        if (esLectura(nombre)) {
            String nombreTexto = analisisContexto.getDialecto().nombrarTipo(TipoDato.TEXTO);
            return Tipos.simple(linea, columna, nombreTexto);
        }

        if (IMPRESIONES.contains(nombre)) {
            return Tipos.simple(linea, columna, Tipos.VOID);
        }

        if (!argumentosValidos) {
            return null;
        }

        // 3. Método de la clase actual (.z)
        InformeTipo claseActual = analisisContexto.getClaseActual();

        if (claseActual != null && claseActual.tieneMetodo(nombre)) {
            nodoLlamada.setMetodoDeClase(true);
            MetodoRecord firma = Tipos.resolverSobrecarga(
                    claseActual.firmasDe(nombre), tiposArgumentos, analisisContexto);

            if (firma == null) {
                analisisContexto.reportarError(linea, columna,
                        "No existe una versión de '" + nombre + "' que reciba esos argumentos");
                return null;
            }

            Tipo retorno = firma.tipoRetorno();
            return retorno != null ? retorno : Tipos.simple(linea, columna, Tipos.VOID);
        }

        // 4. Función global: de este archivo o importada de un .y
        FilaTabla fila = analisisContexto.getTablaSimbolos().buscar(nombre);

        if (fila == null || fila.getCategoria() != Categoria.FUNCION) {
            analisisContexto.reportarError(linea, columna,
                    "Función '" + nombre + "' no declarada (ni en la clase actual ni en los archivos importados)");
            return null;
        }

        String textoRetorno = fila.getTipo();   // ASUNCIÓN: FilaTabla tiene getTipo()

        if (textoRetorno == null || textoRetorno.isBlank()) {
            return Tipos.simple(linea, columna, Tipos.VOID);
        }

        return Tipos.desdeTexto(linea, columna, textoRetorno);
    }
}