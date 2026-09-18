package ast.sentencias;

import c3d.ByteCode;

/**
 * Marca todo lo que puede aparecer como una sentencia dentro de un Bloque.
 *
 * FIX IMPORTANTE: antes esta interfaz estaba VACIA, y eso rompia la
 * generacion de C3D en cadena. Bloque guarda List<Sentencia>, asi que al
 * recorrerlo para emitir codigo hacia:
 *
 *     for (Sentencia s : sentencias) s.generarC3D(contexto);  // no compila
 *
 * porque el compilador solo ve el tipo Sentencia, que no declaraba ningun
 * metodo. Lo mismo pasaba con CicloFor.inicializacion.
 *
 * Al extender ByteCode (que declara generarC3D), cualquier cosa tipada
 * como Sentencia ya expone el metodo. Todas las implementaciones actuales
 * son subclases de NodoAST, que ya lo define, asi que no hay que tocar
 * ninguna otra clase.
 */
public interface Sentencia extends ByteCode {
}