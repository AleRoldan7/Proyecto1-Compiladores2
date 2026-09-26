package semantico.coordinadorsemantico;

import ast.estructuras.InicializacionEstructura;
import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;
import tablas.InformeTipo;

import java.util.List;

@AllArgsConstructor
public class InferidorInicializacionEstructura implements InferirTipo<InicializacionEstructura> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(InicializacionEstructura nodo, AnalisisContexto contexto) {

        InformeTipo tipoEstructura = contexto.getTablaTipos().obtener(nodo.getNombreTipo());

        if (tipoEstructura == null) {
            contexto.reportarError(nodo.getLinea(), nodo.getColumna(),
                    "La estructura '" + nodo.getNombreTipo() + "' no está definida (¿falta el import?)");
            return null;
        }

        List<String> ordenCampos = tipoEstructura.getOrdenAtributos();
        List<Expresion> valores = nodo.getValores() == null ? List.of() : nodo.getValores();

        if (valores.size() != ordenCampos.size()) {
            contexto.reportarError(nodo.getLinea(), nodo.getColumna(),
                    "La estructura '" + nodo.getNombreTipo() + "' tiene " + ordenCampos.size()
                            + " campo(s) y se recibieron " + valores.size());
            return Tipos.simple(nodo.getLinea(), nodo.getColumna(), nodo.getNombreTipo());
        }

        for (int i = 0; i < valores.size(); i++) {

            Tipo tipoValor = inferirTipoCoordinador.inferir(valores.get(i), contexto);

            if (tipoValor == null) {
                continue;   // el error ya se reportó al inferir ese valor
            }

            String nombreCampo = ordenCampos.get(i);
            Tipo tipoCampo = tipoEstructura.tipoDeAtributo(nombreCampo);

            if (!Tipos.asignable(tipoCampo, tipoValor, contexto)) {
                contexto.reportarError(nodo.getLinea(), nodo.getColumna(),
                        "El campo '" + nombreCampo + "' de '" + nodo.getNombreTipo() + "' espera "
                                + Tipos.describir(tipoCampo, contexto) + " y se recibió "
                                + Tipos.describir(tipoValor, contexto));
            }
        }

        return Tipos.simple(nodo.getLinea(), nodo.getColumna(), nodo.getNombreTipo());
    }
}