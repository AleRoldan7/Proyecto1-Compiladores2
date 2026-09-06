package visitor.piglatin;

import ast.NodoAST;
import org.compi2.proyecto1compiladores2.GrammarPigLatinBaseVisitor;

/**
 * IMPORTANTE: antes esta clase extendía GrammarPythonBaseVisitor (la de Y?),
 * lo cual hacía que ANTLR nunca llamara a tus métodos visitX(...) reales:
 * como esta clase no era instancia de GrammarPigLatinVisitor, cada
 * ProgramContext.accept(visitor) caía en el "visitChildren" por defecto y
 * el AST de Pig Latin salía siempre null/incompleto.
 *
 * Con este cambio queda enganchada a la gramática correcta. La lógica
 * semántica de Pig Latin (imports, sección de variables, main) todavía
 * está pendiente de implementar, tal como ya la tenías planeada.
 */
public class VisitorPigLatin extends GrammarPigLatinBaseVisitor<NodoAST> {
}
