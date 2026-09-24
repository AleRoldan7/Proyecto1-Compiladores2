package ast.sentencias;

import ast.expresiones.AccesoArreglo;
import ast.expresiones.Almacenamiento;
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
        String valor = getValor().generarC3D(contexto);
        Almacenamiento.guardar(getDestino(), valor, contexto);
        return valor;
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
