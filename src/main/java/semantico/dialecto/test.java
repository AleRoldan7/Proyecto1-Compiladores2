package semantico.dialecto;
import analisis.CompiladorArchivo;
import ast.NodoAST;
import c3d.ContextoC3D;
import enums.TipoArchivo;
import semantico.AnalisisContexto;
import semantico.ErrorSemantico;
import tablas.TablaSimbolos;
import tablas.TablaTipos;

/**
 * Banco de pruebas de las TRES etapas de analisis, para los TRES lenguajes.
 *
 * Como leer los resultados:
 *
 *   [OK]    la prueba se comporto como se esperaba
 *   [FALLA] el compilador NO detecto un error que deberia haber detectado,
 *           o rechazo codigo que era valido
 *
 * Cada prueba declara si el codigo deberia compilar limpio (esperaErrores
 * = false) o si deberia reportar al menos un error (true). Asi las pruebas
 * negativas -- que son las que de verdad valen -- fallan ruidosamente
 * cuando el analizador se te queda corto.
 */
public class test {

    private static int ok = 0;
    private static int fallas = 0;


    public static void main(String[] args) {

        System.out.println("=".repeat(70));
        System.out.println("  ANALISIS LEXICO / SINTACTICO / SEMANTICO - 3 LENGUAJES");
        System.out.println("=".repeat(70));

        //pruebasZetariano();
        pruebasY();
       // pruebasPigLatin();

        System.out.println();
        System.out.println("=".repeat(70));
        System.out.printf("  RESULTADO: %d OK, %d FALLAS%n", ok, fallas);
        System.out.println("=".repeat(70));
    }

    /* =====================================================================
       ========================== ZETARIANO (.z) ===========================
       ===================================================================== */


    private static void pruebasZetariano() {

        seccion("ZETARIANO (.z)");

        probar("Clase valida", TipoArchivo.ZETARIANO, false, """
            public class Persona {
                String nombre;
                int edad;
                int[] numeros = {10, 20, 30, 40, 50};
                
                public Persona(String n, int e) {
                    nombre = n;
                    edad = e;
                }

                public int calcularAnio(int anioActual) {
                    return anioActual - edad;
                }
            
            }
            """);

        probar("LEXICO: caracter invalido (#)", TipoArchivo.ZETARIANO, true, """
            public class Persona {
                int # edad;
            }
            """);

        probar("SINTACTICO: falta punto y coma", TipoArchivo.ZETARIANO, true, """
            public class Persona {
                int edad
                String nombre;
            }
            """);

        probar("SINTACTICO: llave sin cerrar", TipoArchivo.ZETARIANO, true, """
            public class Persona {
                int edad;
            """);

        probar("SEMANTICO: atributo duplicado", TipoArchivo.ZETARIANO, true, """
            public class Persona {
                int edad;
                int edad;
            }
            """);

        probar("SEMANTICO: variable no declarada", TipoArchivo.ZETARIANO, true, """
            public class Persona {
                public void probar() {
                    x = 10;
                }
            }
            """);

        probar("SEMANTICO: tipos incompatibles (int = String)", TipoArchivo.ZETARIANO, true, """
            public class Persona {
                public void probar() {
                    int edad;
                    edad = "hola";
                }
            }
            """);

        probar("SEMANTICO: constructor con otro nombre", TipoArchivo.ZETARIANO, true, """
            public class Persona {
                public Animal() {
                }
            }
            """);

        probar("SEMANTICO: break fuera de ciclo", TipoArchivo.ZETARIANO, true, """
            public class Persona {
                public void probar() {
                    break;
                }
            }
            """);

        probar("SEMANTICO: metodo inexistente", TipoArchivo.ZETARIANO, true, """
            public class Persona {
                public void probar() {
                    Persona p;
                    p = new Persona();
                    p.metodoQueNoExiste();
                }
            }
            """);
        int[] numero = {10,5,1};

        probar("SEMANTICO: recursividad valida", TipoArchivo.ZETARIANO, false, """
            public class Matematica {
                public int factorial(int n) {
                    if (n <= 1) {
                        return 1;
                    }
                    return n * factorial(n - 1);
                }
            }
            """);
    }

    /* =====================================================================
       ============================= Y? (.y) ===============================
       ===================================================================== */

    private static void pruebasY() {

        seccion("Y? (.y)");

        probar("Estructuras y funciones validas", TipoArchivo.Y_INTERROGACION, false, """
            
            %estructuras
            estructura Persona:
                entero edad = "hola"
                cadena nombre

            %funciones
            definir saludar(cadena nombre):
                imprimir(nombre)

            definir sumar(entero a) -> entero :
                retornar a + 10
            """);

        probar("SINTACTICO: indentacion incorrecta", TipoArchivo.Y_INTERROGACION, true, """
            %funciones
            definir probar():
            imprimir("sin indentar")
            """);

        probar("SEMANTICO: funcion duplicada", TipoArchivo.Y_INTERROGACION, true, """
            %funciones
            definir probar():
                imprimir("a")

            definir probar():
                imprimir("b")
            """);

        probar("SEMANTICO: campo de estructura repetido", TipoArchivo.Y_INTERROGACION, true, """
            %estructuras
            estructura Persona:
                entero edad
                entero edad

            %funciones
            definir probar():
                imprimir("x")
            """);

        probar("SEMANTICO: tipo desconocido en parametro", TipoArchivo.Y_INTERROGACION, true, """
            %estructuras
            estructura Persona:
                entero edad
            """);

        probar("SEMANTICO: declaracion", TipoArchivo.Y_INTERROGACION, true, """
            %funciones
            definir probar(Inexistente x):
                imprimir("x")
            """);

        probar("SEMANTICO: parametro duplicado", TipoArchivo.Y_INTERROGACION, true, """
            %funciones
            definir probar(entero a, entero a):
                imprimir("x")
            """);
    }

    /* =====================================================================
       =========================== PIG LATIN (.pig) ========================
       ===================================================================== */

    private static void pruebasPigLatin() {

        seccion("PIG LATIN (.pig)");

        probar("Programa valido", TipoArchivo.PIG_LATIN, false, """
            import carpeta.Funciones.y

            VARIABILES>
            esto edad : numerus 20;
            esto nombre : textum "Comandante";

            MAIOR>
            >> "Hola comandante!" ;
            si (edad >= 18) {
                edad = edad + 1;
            } finis ;
            FINIS;
            """);

        probar("LEXICO: caracter invalido (@)", TipoArchivo.PIG_LATIN, true, """
            import carpeta.Funciones.y

            MAIOR>
            esto x : numerus @ 5;
            FINIS;
            """);

        probar("SINTACTICO: falta FINIS del main", TipoArchivo.PIG_LATIN, true, """
            import carpeta.Funciones.y

            MAIOR>
            >> "hola" ;
            """);

        probar("SINTACTICO: si sin finis", TipoArchivo.PIG_LATIN, true, """
            import carpeta.Funciones.y

            MAIOR>
            si (1 < 2) {
                >> "a" ;
            }
            FINIS;
            """);

        probar("SEMANTICO: variable global duplicada", TipoArchivo.PIG_LATIN, true, """
            import carpeta.Funciones.y

            VARIABILES>
            esto edad : numerus 20;
            esto edad : numerus 30;

            MAIOR>
            >> edad ;
            FINIS;
            """);

        probar("SEMANTICO: variable no declarada", TipoArchivo.PIG_LATIN, true, """
            import carpeta.Funciones.y

            MAIOR>
            noExiste = 5;
            FINIS;
            """);

        probar("SEMANTICO: ciclos con perge/interrumpe", TipoArchivo.PIG_LATIN, false, """
            import carpeta.Funciones.y

            VARIABILES>
            esto i : numerus 0;

            MAIOR>
            dum (i < 10) {
                i++;
                si (i == 3) {
                    perge;
                } finis ;
                si (i == 8) {
                    interrumpe;
                } finis ;
            } finis ;
            FINIS;
            """);

        probar("SEMANTICO: interrumpe fuera de ciclo", TipoArchivo.PIG_LATIN, true, """
            import carpeta.Funciones.y

            MAIOR>
            interrumpe;
            FINIS;
            """);
    }

    /* =====================================================================
       ============================== MOTOR ================================
       ===================================================================== */

    /**
     * Compila un fragmento aislado y compara contra lo esperado.
     *
     * Ojo: cada prueba usa su PROPIO AnalisisContexto (tabla de simbolos
     * limpia). Eso es a proposito: si compartieran tabla, una prueba
     * ensuciaria a la siguiente y los duplicados saldrian donde no toca.
     * El flujo real del proyecto (CompiladorProyecto) si comparte tabla
     * entre los tres archivos, que es justamente lo que permite que Pig
     * Latin vea lo que definieron Y? y Zetariano.
     */
    private static void probar(String descripcion, TipoArchivo tipo,
                               boolean esperaErrores, String codigo) {

        AnalisisContexto contexto = new AnalisisContexto(new TablaSimbolos(), new TablaTipos());

        contexto.setArchivoActual("prueba" + tipo.getExtension());

        NodoAST ast = null;
        String excepcion = null;

        try {
            ast = new CompiladorArchivo().analizar(codigo, tipo, contexto);
        } catch (Exception e) {
            excepcion = e.getClass().getSimpleName() + ": " + e.getMessage();
        }

        boolean tuvoErrores = contexto.tieneErrores() || excepcion != null;
        boolean paso = tuvoErrores == esperaErrores;

        if (paso) {
            ok++;
        } else {
            fallas++;
        }

        System.out.printf("  %-8s %-45s %s%n",
                paso ? "[OK]" : "[FALLA]",
                descripcion,
                tuvoErrores ? "(" + contexto.getErrores().size() + " error/es)" : "(limpio)");

        /*
         * Cuando la prueba falla, mostrar el detalle: es la unica forma de
         * saber si el analizador se quedo corto o si el mensaje esta mal.
         */
        if (!paso) {

            if (excepcion != null) {
                System.out.println("           EXCEPCION: " + excepcion);
            }

            for (ErrorSemantico error : contexto.getErrores()) {
                System.out.println("           -> " + error);
            }

            if (!esperaErrores) {
                System.out.println("           (se esperaba que compilara limpio)");
            } else {
                System.out.println("           (se esperaba AL MENOS un error y no hubo ninguno)");
            }
        }

        // Para los casos validos, mostrar que el C3D se genera sin reventar.
        if (paso && !esperaErrores && ast != null) {

            try {
                ContextoC3D c3d = new ContextoC3D();
                ast.generarC3D(c3d);

                System.out.printf("           C3D: %d cuartetas%n",
                        c3d.getCuartetas().size());

            } catch (Exception e) {
                System.out.println("           C3D REVENTO: "
                        + e.getClass().getSimpleName() + ": " + e.getMessage());
                fallas++;
                ok--;
            }
        }
    }

    private static void seccion(String titulo) {
        System.out.println();
        System.out.println("-".repeat(70));
        System.out.println("  " + titulo);
        System.out.println("-".repeat(70));
    }
}