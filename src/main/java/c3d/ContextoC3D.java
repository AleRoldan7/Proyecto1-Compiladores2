package c3d;

import enums.TipoDato;

import java.util.*;

public class ContextoC3D {

    private final List<Cuarteta> cuartetas = new ArrayList<>();
    private final Set<String> funciones = new LinkedHashSet<>();
    private final List<String> cadenas = new ArrayList<>();
    private final Map<String, Map<String, Integer>> disposiciones = new HashMap<>();   // clase -> atributo -> desplazamiento
    private final Deque<String[]> destinos = new ArrayDeque<>();                        // {continue, break}; continue = null en un switch
    private final Map<String, List<CampoDef>> definicionesEstructura = new LinkedHashMap<>();
    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;
    private String claseActual;
    private final Map<String, Map<String, CampoLayout>> layoutsEstructura = new HashMap<>();
    private final Map<String, Integer> tamaniosEstructura = new HashMap<>();
    private final Set<String> nombresEstructuras = new HashSet<>();
    private final Map<String, Map<Integer, TipoDato>> tiposCelda = new HashMap<>();
    private final Map<String, Map<String, TipoDato>> tiposAtributo = new HashMap<>();
    private final Map<String, List<Integer>> tamaniosArreglo = new HashMap<>();
    // ---------- Temporales y etiquetas ----------

    public String nuevoTemporal() {
        return "t" + contadorTemporales++;
    }

    public String nuevaEtiqueta() {
        return "L" + contadorEtiquetas++;
    }

    // ---------- Emisión de cuartetas ----------

    public void agregar(String operador, String arg1, String arg2, String resultado) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, null));
    }

    public void agregar(String operador, String arg1, String arg2,
                        String resultado, TipoDato tipo) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, tipo));
    }

    public void emitir(String operador, String arg1, String arg2, String resultado) {
        agregar(operador, arg1, arg2, resultado);
    }

    public void agregarEtiqueta(String etiqueta) {
        agregar("label", null, null, etiqueta);
    }

    public void salto(String etiqueta) {
        agregar("goto", null, null, etiqueta);
    }

    public void saltoSiVerdadero(String condicion, String etiqueta) {
        agregar("if_true", condicion, null, etiqueta);
    }

    public void saltoSiFalso(String condicion, String etiqueta) {
        agregar("if_false", condicion, null, etiqueta);
    }

    public void emitirComentario(String texto) {
        agregar("comment", null, null, texto);
    }

    /** t = operador operando */
    public String unaria(String operador, String operando, TipoDato tipo) {
        String temporal = nuevoTemporal();
        cuartetas.add(new Cuarteta(operador, operando, null, temporal, tipo));
        return temporal;
    }

    /** t = izquierda operador derecha */
    public String binaria(String operador, String izquierda, String derecha, TipoDato tipo) {
        String temporal = nuevoTemporal();
        cuartetas.add(new Cuarteta(operador, izquierda, derecha, temporal, tipo));
        return temporal;
    }

    /** destino = valor */
    /** destino = valor */
    public void asignar(String destino, String valor) {
        if (esAtributo(destino)) {
            int offset = desplazamiento(claseActual, destino);
            // self.<offset> = valor   →  GenerarCodigoC lo traduce a heap[self+offset] = valor
            cuartetas.add(new Cuarteta("field_set", String.valueOf(offset), valor, "self", null));
        } else {
            cuartetas.add(new Cuarteta("=", valor, null, destino, null));
        }
    }

    public List<Cuarteta> getCuartetas() {
        return cuartetas;
    }

    // ---------- Funciones ----------

    /** Funciones globales (.y) y main: se llaman por su nombre. Métodos (.z): Clase_metodo. */
    public static String nombreFuncion(String clase, String metodo) {
        return clase == null ? metodo : clase + "_" + metodo;
    }

    /** Igual que en tu nodo Constructor: init_Clase */
    public static String nombreConstructor(String clase) {
        return "init_" + clase;
    }

    public void registrarFuncion(String nombre) {
        funciones.add(nombre);
    }

    public boolean existeFuncion(String nombre) {
        return funciones.contains(nombre);
    }

    public Set<String> getFunciones() {
        return funciones;
    }

    public void abrirFuncion(String nombre, List<String> parametros) {
        registrarFuncion(nombre);
        agregarEtiqueta("func_" + nombre);
        for (String parametro : parametros) {
            agregar("param_decl", "int", parametro, null);
        }
    }

    public void cerrarFuncion(String nombre) {
        agregarEtiqueta("end_" + nombre);
    }

    // ---------- Cadenas ----------

    public List<String> getCadenas() {
        return cadenas;
    }

    // ---------- Objetos en el heap ----------

    public void registrarClase(String clase, List<String> atributos) {
        registrarClase(clase, atributos, null);
    }

    /** tipos puede ser null (compatibilidad); si no viene, cada atributo queda DESCONOCIDO. */
    public void registrarClase(String clase, List<String> atributos, List<TipoDato> tipos) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        Map<String, TipoDato> tiposMapa = new LinkedHashMap<>();
        int desplazamiento = 0;
        for (int i = 0; i < atributos.size(); i++) {
            String atributo = atributos.get(i);
            mapa.put(atributo, desplazamiento++);
            TipoDato tipo = (tipos != null && i < tipos.size()) ? tipos.get(i) : TipoDato.DESCONOCIDO;
            tiposMapa.put(atributo, tipo);
        }
        disposiciones.put(clase, mapa);
        tiposAtributo.put(clase, tiposMapa);
    }

    /** Tipo declarado del atributo 'atributo' en 'clase' (para tipar attr_get de acceso implícito). */
    public TipoDato tipoDeAtributo(String clase, String atributo) {
        Map<String, TipoDato> tiposMapa = tiposAtributo.get(clase);
        if (tiposMapa == null) {
            return TipoDato.DESCONOCIDO;
        }
        return tiposMapa.getOrDefault(atributo, TipoDato.DESCONOCIDO);
    }

    private Map<String, Integer> disposicion(String clase) {
        Map<String, Integer> mapa = disposiciones.get(clase);
        if (mapa == null) {
            throw new IllegalStateException("La clase '" + clase + "' no tiene disposición en el heap (¿se generó antes su archivo?)");
        }
        return mapa;
    }



    public int tamanio(String clase) {
        return disposicion(clase).size();
    }

    public String getClaseActual() {
        return claseActual;
    }

    public void setClaseActual(String claseActual) {
        this.claseActual = claseActual;
    }

    public boolean esAtributo(String nombre) {
        return claseActual != null && disposicion(claseActual).containsKey(nombre);
    }

    // ---------- break / continue ----------

    public void entrarCiclo(String etiquetaContinue, String etiquetaBreak) {
        destinos.push(new String[]{etiquetaContinue, etiquetaBreak});
    }

    public void salirCiclo() {
        destinos.pop();
    }

    /** Un switch admite break pero no continue: el continue sigue apuntando al ciclo de afuera. */
    public void entrarSwitch(String etiquetaBreak) {
        destinos.push(new String[]{null, etiquetaBreak});
    }

    public void salirSwitch() {
        destinos.pop();
    }

    public String etiquetaBreak() {
        return destinos.isEmpty() ? null : destinos.peek()[1];
    }

    public String etiquetaContinue() {
        for (String[] destino : destinos) {          // recorre desde el más interno
            if (destino[0] != null) {
                return destino[0];
            }
        }
        return null;
    }

    private final Set<String> locales = new HashSet<>();


    public void declararLocal(String nombre) {
        locales.add(nombre);
    }

    public boolean esLocal(String nombre) {
        return locales.contains(nombre);
    }

    public static boolean esImpresion(String nombre) {
        return Set.of("print", "println", "imprimir").contains(nombre);
    }

    public static boolean esLectura(String nombre) {
        return Set.of("readln", "leer").contains(nombre);
    }

    /** El texto del lexer ya trae sus comillas: no se duplican. */
    public String registrarString(String texto) {
        cadenas.add(texto);
        String contenido = texto.replace("\r", "").replace("\n", "\\n").replace("\t", "\\t");
        boolean yaTieneComillas = contenido.length() >= 2 && contenido.startsWith("\"") && contenido.endsWith("\"");
        return yaTieneComillas ? contenido : "\"" + contenido + "\"";
    }

    public int desplazamiento(String clase, String atributo) {
        Integer d = disposicion(clase).get(atributo);
        if (d == null) {
            throw new IllegalStateException("La clase '" + clase + "' no tiene el atributo '" + atributo + "'");
        }
        return d;
    }

    /** Si no se conoce la clase del objeto, se busca en todas; falla si el nombre es ambiguo. */
    public int desplazamientoDe(String clase, String atributo) {

        if (clase != null) {
            return desplazamiento(clase, atributo);
        }

        Integer encontrado = null;

        for (Map<String, Integer> mapa : disposiciones.values()) {
            Integer d = mapa.get(atributo);
            if (d == null) continue;
            if (encontrado != null && !encontrado.equals(d)) {
                throw new IllegalStateException("El atributo '" + atributo
                        + "' está en varias clases con distinta posición: falta conocer la clase del objeto");
            }
            encontrado = d;
        }

        if (encontrado == null) {
            throw new IllegalStateException("Ninguna clase tiene el atributo '" + atributo + "'");
        }

        return encontrado;
    }

    /**
     * Registra los tamaños fijos por dimensión de un arreglo declarado (ej.
     * "entero matriz[3][4]" -> [3, 4]), para que AccesoArreglo pueda calcular
     * la dirección correcta de un acceso PARCIAL (ej. matriz[i], que debe
     * devolver la dirección de una fila de 4 celdas, no dereferenciar).
     * Se llama desde DeclaracionArreglo.generarC3D() cuando los tamaños son
     * literales conocidos en tiempo de compilación (siempre lo son en esta
     * gramática: dimension exige NUMERO_ENTERO/ENTERO literal).
     */
    public void registrarTamaniosArreglo(String nombreVariable, List<Integer> tamanios) {
        tamaniosArreglo.put(nombreVariable, tamanios);
    }

    /** Tamaños por dimensión del arreglo 'nombreVariable', o null si no se conocen. */
    public List<Integer> tamaniosDeArreglo(String nombreVariable) {
        return tamaniosArreglo.get(nombreVariable);
    }

    public record CampoLayout(int offset, int ancho, boolean esEmbebido, String tipoAnidado) {}

    /** esEmbebido=true cuando el tipo del campo es otra 'estructura' (se aplana); false para primitivos o clases (referencia). */
    public record CampoDef(String nombre, boolean esEmbebido, String tipoAnidado, TipoDato tipo, int anchoDeclarado) {
        public CampoDef(String nombre, boolean esEmbebido, String tipoAnidado, TipoDato tipo) {
            this(nombre, esEmbebido, tipoAnidado, tipo, 1);
        }
    }
    public void registrarEstructura(String nombre, List<CampoDef> campos) {
        nombresEstructuras.add(nombre);
        definicionesEstructura.put(nombre, campos);
    }

    public boolean esEstructura(String tipo) {
        return nombresEstructuras.contains(tipo);
    }

    public int tamanioEstructura(String tipo) {
        Integer t = tamaniosEstructura.get(tipo);
        if (t == null) {
            throw new IllegalStateException("La estructura '" + tipo + "' no tiene tamaño registrado "
                    + "(¿se registró antes de usarla? el orden del .y importa)");
        }
        return t;
    }

    public CampoLayout layoutEstructura(String tipo, String campo) {
        Map<String, CampoLayout> layout = layoutsEstructura.get(tipo);
        if (layout == null) {
            throw new IllegalStateException("La estructura '" + tipo + "' no tiene layout registrado");
        }
        CampoLayout c = layout.get(campo);
        if (c == null) {
            throw new IllegalStateException("La estructura '" + tipo + "' no tiene el campo '" + campo + "'");
        }
        return c;
    }

    public List<String> camposDeEstructura(String tipo) {
        Map<String, CampoLayout> layout = layoutsEstructura.get(tipo);
        if (layout == null) {
            throw new IllegalStateException("La estructura '" + tipo + "' no tiene layout registrado");
        }
        return new ArrayList<>(layout.keySet());   // LinkedHashMap: orden de declaración
    }

    public void resolverEstructuras() {

        for (String nombre : definicionesEstructura.keySet()) {
            calcularLayoutEstructura(nombre, new HashSet<>());
        }
    }

    private void calcularLayoutEstructura(String nombre, Set<String> visitando) {

        if (tamaniosEstructura.containsKey(nombre)) {
            return;
        }

        if (!visitando.add(nombre)) {
            throw new IllegalStateException(
                    "Referencia circular entre estructuras: " + nombre
            );
        }

        List<CampoDef> campos = definicionesEstructura.get(nombre);

        if (campos == null) {
            throw new IllegalStateException(
                    "No existe la definición de la estructura '" + nombre + "'"
            );
        }

        Map<String, CampoLayout> layout = new LinkedHashMap<>();   // <-- esto faltaba
        Map<Integer, TipoDato> celdas = new HashMap<>();
        int offset = 0;

        for (CampoDef campo : campos) {

            int ancho = 1;
            boolean embebidoFinal = campo.esEmbebido();
            String tipoAnidadoFinal = campo.tipoAnidado();

            if (campo.esEmbebido() && campo.tipoAnidado() != null) {

                // ---- struct anidada (caso ya existente, sin cambios) ----
                String tipoAnidado = campo.tipoAnidado();

                if (!definicionesEstructura.containsKey(tipoAnidado)) {
                    throw new IllegalStateException(
                            "La estructura '" + nombre + "' utiliza la estructura '"
                                    + tipoAnidado + "', pero no existe"
                    );
                }

                calcularLayoutEstructura(tipoAnidado, visitando);
                ancho = tamanioEstructura(tipoAnidado);

                Map<Integer, TipoDato> celdasAnidadas = tiposCelda.get(tipoAnidado);
                for (int k = 0; k < ancho; k++) {
                    celdas.put(offset + k, celdasAnidadas.get(k));
                }

            } else if (campo.anchoDeclarado() > 1) {
                // ---- campo arreglo de primitivos, ej. entero notas[3] ----
                ancho = campo.anchoDeclarado();
                embebidoFinal = true;
                tipoAnidadoFinal = null;

                for (int k = 0; k < ancho; k++) {
                    celdas.put(offset + k, campo.tipo());
                }


            } else {
                // ---- primitivo simple o referencia a clase (sin cambios) ----
                celdas.put(offset, campo.tipo());
            }

            layout.put(
                    campo.nombre(),
                    new CampoLayout(offset, ancho, embebidoFinal, tipoAnidadoFinal)
            );

            offset = offset + ancho;
        }
        tiposCelda.put(nombre, celdas);
        layoutsEstructura.put(nombre, layout);
        tamaniosEstructura.put(nombre, offset);

        visitando.remove(nombre);
    }

    /** Tipo real de la celda aplanada en offset local dentro de 'tipoEstructura' (para castear al copiar campos embebidos). */
    public TipoDato tipoDeCelda(String tipoEstructura, int offsetLocal) {
        return tiposCelda.getOrDefault(tipoEstructura, Map.of())
                .getOrDefault(offsetLocal, TipoDato.DESCONOCIDO);
    }

    private final Map<String, String> tipoBaseArreglo = new HashMap<>();

    public void registrarTipoBaseArreglo(String nombreVariable, String tipoBase) {
        tipoBaseArreglo.put(nombreVariable, tipoBase);
    }

    public String tipoBaseDeArreglo(String nombreVariable) {
        return tipoBaseArreglo.get(nombreVariable);
    }
}