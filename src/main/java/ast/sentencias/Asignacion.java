package ast.sentencias;

import ast.expresiones.AccesoArreglo;
import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Asignacion extends Expresion {

    private Expresion destino;
    private Expresion valor;

    public Asignacion(int linea, int columna, Expresion destino, Expresion valor) {
        super(linea, columna);
        this.destino = destino;
        this.valor = valor;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        // Caso especial: asignación a arreglo → index_set
        if (destino instanceof AccesoArreglo acceso) {

            String valorStr = valor.generarC3D(contexto);
            String arrStr = acceso.getArreglo().generarC3D(contexto);
            String indiceStr = generarIndiceAplanado(acceso, contexto);

            contexto.agregar("index_set", arrStr, indiceStr, valorStr);
            return valorStr;
        }

        // Caso normal: variable simple
        String valorStr = valor.generarC3D(contexto);
        String destinoStr = destino.generarC3D(contexto);

        contexto.asignar(destinoStr, valorStr);
        return destinoStr;
    }

    private String generarIndiceAplanado(AccesoArreglo acceso, ContextoC3D contexto) {

        var indices = acceso.getIndicesArreglo();

        if (indices.isEmpty()) return "0";
        if (indices.size() == 1) return indices.get(0).generarC3D(contexto);

        String acumulado = indices.get(0).generarC3D(contexto);

        for (int i = 1; i < indices.size(); i++) {
            String idx = indices.get(i).generarC3D(contexto);
            acumulado = contexto.binaria("+", acumulado, idx, enums.TipoDato.ENTERO);
        }

        return acumulado;
    }
}
