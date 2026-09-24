package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccesoAtributo extends Expresion {

    private Expresion objeto;
    private String atributo;

    public AccesoAtributo(int linea, int columna, Expresion objeto, String atributo) {
        super(linea, columna);
        this.objeto = objeto;
        this.atributo = atributo;
    }

    public int desplazamiento(ContextoC3D contexto) {
        boolean esThis = objeto instanceof Identificador id && "this".equals(id.getNombreIdentificador());
        return contexto.desplazamientoDe(esThis ? contexto.getClaseActual() : null, atributo);
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        String objeto = getObjeto().generarC3D(contexto);
        String temporal = contexto.nuevoTemporal();
        contexto.agregar("attr_get", objeto, String.valueOf(desplazamiento(contexto)), temporal);
        return temporal;
    }
}
