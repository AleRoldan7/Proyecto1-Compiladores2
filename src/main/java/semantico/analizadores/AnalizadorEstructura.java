package semantico.analizadores;

import ast.estructuras.Campo;
import ast.estructuras.Estructura;
import enums.TipoDato;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.AnalizadorSemantico;
import tablas.InformeTipo;

import java.util.HashSet;
import java.util.Set;

/**
 * Registra una estructura de Y? en la TablaTipos compartida, para que
 * después Pig Latin pueda importarla y usarla.
 */
public class AnalizadorEstructura implements AnalizadorSemantico<Estructura> {

    @Override
    public void analizar(Estructura estructura, AnalisisContexto contexto) {

        if (contexto.getTablaTipos().existeTipo(estructura.getNombre())) {

            contexto.reportarError(estructura.getLinea(), estructura.getColumna(),
                    "El tipo '" + estructura.getNombre() + "' ya está definido");
            return;
        }

        InformeTipo informe = new InformeTipo(estructura.getNombre(), TipoDato.ESTRUCTURA);

        /*
         * Se registra ANTES de validar los campos para permitir que una
         * estructura se referencie a sí misma por referencia.
         */
        contexto.getTablaTipos().registrar(informe);

        Set<String> vistos = new HashSet<>();

        if (estructura.getCampos() == null) {
            return;
        }

        for (Campo campo : estructura.getCampos()) {

            if (!vistos.add(campo.getNombre())) {

                contexto.reportarError(campo.getLinea(), campo.getColumna(),
                        "El campo '" + campo.getNombre() + "' está repetido en la estructura '"
                                + estructura.getNombre() + "'");
                continue;
            }

            // El tipo del campo debe ser primitivo del dialecto, o un tipo ya definido.
            if (Tipos.canonico(campo.getTipo(), contexto) == null) {

                contexto.reportarError(campo.getLinea(), campo.getColumna(),
                        "Tipo desconocido '" + Tipos.base(campo.getTipo())
                                + "' en el campo '" + campo.getNombre() + "'");
                continue;
            }

            informe.agregarAtributo(campo.getNombre(), campo.getTipo());
        }
    }
}