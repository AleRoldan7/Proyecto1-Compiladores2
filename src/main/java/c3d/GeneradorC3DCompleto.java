package c3d;


import ast.NodoAST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GeneradorC3DCompleto {

    public record ArchivoFuente(String nombre, NodoAST raiz, boolean principal) {}

    public GeneradorC3DCompleto() {
    }

    public static List<Cuarteta> generar(List<ArchivoFuente> archivos) {

        ContextoC3D ctx = new ContextoC3D();

        // Pasada 1: disposición en el heap de todas las clases, de cualquier archivo
        for (ArchivoFuente archivo : archivos) {
            if (archivo.raiz() instanceof ast.Programa programa && programa.getClases() != null) {
                for (ast.clases.Clase clase : programa.getClases()) {
                    clase.registrarDisposicion(ctx);
                }
            }
        }

        // Pasada 2a: importados (.y y .z)
        for (ArchivoFuente archivo : archivos) {
            if (!archivo.principal()) {
                ctx.emitirComentario("==== " + archivo.nombre() + " ====");
                archivo.raiz().generarC3D(ctx);
            }
        }

        // Pasada 2b: el .pig, dentro de func_main
        for (ArchivoFuente archivo : archivos) {
            if (archivo.principal()) {
                ctx.emitirComentario("==== " + archivo.nombre() + " ====");
                ctx.abrirFuncion("main", List.of());
                archivo.raiz().generarC3D(ctx);
                ctx.cerrarFuncion("main");
            }
        }

        return ctx.getCuartetas();
    }

    public static String comoTexto(List<Cuarteta> cuartetas) {

        StringBuilder sb = new StringBuilder();

        for (Cuarteta q : cuartetas) {
            boolean sinSangria = "label".equals(q.getOperador()) || "comment".equals(q.getOperador());
            sb.append(sinSangria ? "" : "    ").append(q).append("\n");
        }

        return sb.toString();
    }

    public static void exportar(Path carpeta, String nombreBase, List<Cuarteta> cuartetas) throws IOException {
        Files.createDirectories(carpeta);
        Files.writeString(carpeta.resolve(nombreBase + ".c3d"), comoTexto(cuartetas));
        Files.writeString(carpeta.resolve(nombreBase + ".c"), GenerarCodigoC.traducir(cuartetas));
    }
}
