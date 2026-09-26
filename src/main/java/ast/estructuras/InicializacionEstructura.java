package ast.estructuras;

import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InicializacionEstructura extends Expresion {

    private String nombreTipo;
    private List<Expresion> valores;

    public InicializacionEstructura(int linea, int columna, String nombreTipo, List<Expresion> valores) {
        super(linea, columna);
        this.nombreTipo = nombreTipo;
        this.valores = valores;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        int ancho = contexto.tamanioEstructura(nombreTipo);
        String destino = contexto.nuevoTemporal();
        contexto.agregar("new", nombreTipo, String.valueOf(ancho), destino);

        List<String> ordenCampos = contexto.camposDeEstructura(nombreTipo);
        List<Expresion> vals = (valores == null) ? List.of() : valores;

        for (int i = 0; i < vals.size() && i < ordenCampos.size(); i++) {

            String nombreCampo = ordenCampos.get(i);
            ContextoC3D.CampoLayout layout = contexto.layoutEstructura(nombreTipo, nombreCampo);
            String origen = vals.get(i).generarC3D(contexto);

            if (layout.esEmbebido()) {
                for (int k = 0; k < layout.ancho(); k++) {
                    String celda = contexto.nuevoTemporal();
                    TipoDato tipoCelda = contexto.tipoDeCelda(layout.tipoAnidado(), k);
                    contexto.agregar("attr_get", origen, String.valueOf(k), celda, tipoCelda);
                    contexto.agregar("field_set", String.valueOf(layout.offset() + k), celda, destino);
                }
            } else {
                contexto.agregar("field_set", String.valueOf(layout.offset()), origen, destino);
            }
        }

        return destino;
    }
}