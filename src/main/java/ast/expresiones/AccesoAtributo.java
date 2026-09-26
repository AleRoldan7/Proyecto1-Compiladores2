package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccesoAtributo extends Expresion {

    private Expresion base;
    private String atributo;

    private String tipoContenedor;
    private boolean contenedorEsEstructura;

    private TipoDato tipoResultado;

    public AccesoAtributo(
            int linea,
            int columna,
            Expresion base,
            String atributo) {

        super(linea, columna);

        this.base = base;
        this.atributo = atributo;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String basePlace =
                base.generarC3D(contexto);

        if (contenedorEsEstructura) {

            ContextoC3D.CampoLayout layout =
                    contexto.layoutEstructura(
                            tipoContenedor,
                            atributo
                    );

            if (layout.esEmbebido()) {

                return contexto.binaria(
                        "+",
                        basePlace,
                        String.valueOf(layout.offset()),
                        TipoDato.ESTRUCTURA
                );
            }

            String temporal =
                    contexto.nuevoTemporal();

            contexto.agregar(
                    "attr_get",
                    basePlace,
                    String.valueOf(layout.offset()),
                    temporal,
                    tipoResultado
            );

            return temporal;
        }

        String temporal =
                contexto.nuevoTemporal();

        contexto.agregar(
                "attr_get",
                basePlace,
                String.valueOf(
                        contexto.desplazamiento(
                                tipoContenedor,
                                atributo
                        )
                ),
                temporal,
                tipoResultado
        );

        return temporal;
    }
}