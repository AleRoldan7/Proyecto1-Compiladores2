package ui.view;

import analisis.CompiladorProyecto;
import analisis.ResultadoProyecto;
import enums.TipoArchivo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import tablas.FilaTabla;
import ui.button_option.ConsolaErrores;
import ui.button_option.TablaSimbolosView;
import utils.arbol_de_trabajo.ArbolDeTrabajo;
import utils.coloreado.SintaxColoreado;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainView extends BorderPane {

    private HBox menu;
    private TabPane editorTabs;
    private SplitPane root;
    private StackPane contenido;
    private Label insigniaEstado;
    private Label indicadorPosicion;

    private final ArbolDeTrabajo arbolDeTrabajo = new ArbolDeTrabajo();

    private final CompiladorProyecto compiladorProyecto = new CompiladorProyecto();
    private ResultadoProyecto resultadoProyecto;
    private final ConsolaErrores consolaErrores = new ConsolaErrores();

    /** Una pestaña por archivo abierto, para poder tener los 3 lenguajes a la vista al mismo tiempo. */
    private final Map<File, Tab> pestanasPorArchivo = new HashMap<>();
    /** Último mapa de archivos del proyecto usado para compilar (nombre -> File), para poder navegar desde la consola. */
    private final Map<String, File> archivosProyectoPorNombre = new HashMap<>();

    // ---- Paleta (misma que Codex Latinus) ----
    private static final String FONDO = "#121212";
    private static final String SUPERFICIE = "#1A1A1A";
    private static final String PANEL = "#1E1E1E";
    private static final String PRIMARIO = "#D9A15C";
    private static final String SECUNDARIO = "#4FD1C5";
    private static final String TEXTO = "#ECECEC";
    private static final String TEXTO_SECUNDARIO = "#9A9A9A";
    private static final String BORDE = "#2A2A2A";
    private static final String EXITO = "#4CAF7D";
    private static final String ERROR = "#E05260";

    public MainView() {
        menuCreado();
        contenidoCentral();

        arbolDeTrabajo.setOnArchivoAbierto((archivo, contenidoTexto) -> abrirOFocalizarPestana(archivo, contenidoTexto));
        estilizarArbol();

        consolaErrores.setOnNavegar(this::navegarAError);

        setTop(menu);
        setLeft(arbolDeTrabajo);
        setCenter(root);

        setStyle("-fx-background-color: " + FONDO + ";");
    }


    private void menuCreado() {
        menu = new HBox(6);
        menu.setPadding(new Insets(9, 16, 9, 16));
        menu.setAlignment(Pos.CENTER_LEFT);
        menu.setStyle(
                "-fx-background-color: #151515;" +
                        "-fx-border-color: " + BORDE + ";" +
                        "-fx-border-width: 0 0 2 0;"
        );

        VBox tituloBox = new VBox(0);
        tituloBox.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("Codex Latinus");
        titulo.setStyle(
                "-fx-text-fill: " + TEXTO + ";" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-family: 'Georgia';" +
                        "-fx-font-weight: bold;"
        );

        Label subtitulo = new Label("Compilador");
        subtitulo.setStyle(
                "-fx-text-fill: " + TEXTO_SECUNDARIO + ";" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-family: 'Arial';"
        );

        tituloBox.getChildren().addAll(titulo, subtitulo);

        Separator separador = new Separator();
        separador.setOrientation(javafx.geometry.Orientation.VERTICAL);
        separador.setPadding(new Insets(0, 8, 0, 8));
        separador.setStyle("-fx-background-color: " + BORDE + ";");

        // ---- Archivo ----
        MenuButton buttonArchivo = new MenuButton("Archivo");
        MenuItem nuevo = new MenuItem("Nuevo");
        MenuItem abrir = new MenuItem("Abrir");
        MenuItem carpeta = new MenuItem("Carpeta");
        MenuItem guardar = new MenuItem("Guardar");
        MenuItem guardarComo = new MenuItem("Guardar Como");
        MenuItem descargar = new MenuItem("Descargar");
        MenuItem cerrarPestana = new MenuItem("Cerrar pestaña");

        buttonArchivo.getItems().addAll(
                nuevo, new SeparatorMenuItem(),
                abrir, carpeta, new SeparatorMenuItem(),
                guardar, guardarComo, new SeparatorMenuItem(),
                descargar, new SeparatorMenuItem(),
                cerrarPestana
        );

        nuevo.setOnAction(e -> crearPestanaSinTitulo());

        abrir.setOnAction(e -> {
            try {
                arbolDeTrabajo.abrirArchivoSuelto();
            } catch (Exception ex) {
                mostrarError("No se pudo abrir el archivo");
                ex.printStackTrace();
            }
        });

        carpeta.setOnAction(e -> {
            try {
                arbolDeTrabajo.abrirCarpeta();
            } catch (Exception ex) {
                mostrarError("No se pudo abrir la carpeta");
                ex.printStackTrace();
            }
        });

        guardar.setOnAction(e -> guardarPestanaActiva());

        guardarComo.setOnAction(e -> {
            try {
                arbolDeTrabajo.guardarComo();
                mostrarEstado("Guardado", EXITO);
            } catch (Exception ex) {
                mostrarError("No se pudo guardar como");
                ex.printStackTrace();
            }
        });

        descargar.setOnAction(e -> {
            try {
                arbolDeTrabajo.descargarProyecto();
                mostrarEstado("Proyecto descargado", EXITO);
            } catch (Exception ex) {
                mostrarError("No se pudo descargar el proyecto");
                ex.printStackTrace();
            }
        });

        cerrarPestana.setOnAction(e -> {
            Tab activa = editorTabs.getSelectionModel().getSelectedItem();
            if (activa != null) {
                editorTabs.getTabs().remove(activa);
            }
        });

        Button buttonCompilarProyecto = new Button("Compilar Proyecto");
        Button buttonSimbolos = new Button("Tabla Símbolos");
        Button buttonC3d = new Button("Generar C3D");

        estilizarBoton(buttonArchivo);
        estilizarBotonPrincipal(buttonCompilarProyecto);
        estilizarBoton(buttonSimbolos);
        estilizarBoton(buttonC3d);

        buttonCompilarProyecto.setOnAction(e -> onCompilarProyecto());
        buttonSimbolos.setOnAction(e -> onMostrarSimbolos());
        buttonC3d.setOnAction(e -> onGenerarC3D());

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        insigniaEstado = new Label("Sin compilar");
        estilizarInsignia(insigniaEstado, TEXTO_SECUNDARIO, "#EEE7DC");

        menu.getChildren().addAll(
                tituloBox, separador,
                buttonArchivo, buttonCompilarProyecto, buttonSimbolos, buttonC3d,
                espaciador, insigniaEstado
        );
    }

    // ============================================================
    //  EDITOR (MULTI-PESTAÑA) + PANEL DE RESULTADOS
    // ============================================================
    private void contenidoCentral() {
        editorTabs = new TabPane();
        editorTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        editorTabs.setStyle("-fx-background-color: " + SUPERFICIE + ";");

        // ---- Barra inferior: posicion + ir a linea (aplica a la pestaña activa) ----
        indicadorPosicion = new Label("Línea 1, Columna 1");
        indicadorPosicion.setPadding(new Insets(5, 12, 5, 12));
        indicadorPosicion.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        "-fx-text-fill: " + TEXTO_SECUNDARIO + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                        "-fx-border-color: " + BORDE + ";" +
                        "-fx-border-width: 1 0 0 0;"
        );
        indicadorPosicion.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(indicadorPosicion, Priority.ALWAYS);

        Label textoIrLinea = new Label("Ir a línea:");
        textoIrLinea.setStyle(
                "-fx-text-fill: " + TEXTO_SECUNDARIO + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-family: 'Arial';"
        );

        TextField campoLinea = new TextField();
        campoLinea.setPromptText("Línea");
        campoLinea.setPrefWidth(70);
        campoLinea.setMaxWidth(70);
        estilizarCampoLinea(campoLinea, BORDE);

        Button botonIrLinea = new Button("Ir");
        botonIrLinea.setStyle(
                "-fx-background-color: " + PRIMARIO + ";" +
                        "-fx-text-fill: #121212;" +
                        "-fx-background-radius: 4;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        campoLinea.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) irALinea(campoLinea);
        });
        botonIrLinea.setOnAction(e -> irALinea(campoLinea));

        HBox barraInferior = new HBox(8, indicadorPosicion, textoIrLinea, campoLinea, botonIrLinea);
        barraInferior.setAlignment(Pos.CENTER_LEFT);
        barraInferior.setPadding(new Insets(5, 8, 5, 8));
        barraInferior.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        "-fx-border-color: " + BORDE + ";" +
                        "-fx-border-width: 1 0 0 0;"
        );

        VBox columnaEditor = new VBox(editorTabs, barraInferior);
        VBox.setVgrow(editorTabs, Priority.ALWAYS);
        columnaEditor.setFillWidth(true);
        columnaEditor.setStyle(
                "-fx-background-color: " + SUPERFICIE + ";" +
                        "-fx-border-color: " + BORDE + ";" +
                        "-fx-border-width: 0 1 0 0;"
        );

        // ---- Panel de resultados (tabla simbolos, cuartetas, consola de errores, etc.) ----
        contenido = new StackPane();
        Label placeholder = new Label("Codex Latinus");
        placeholder.setStyle(
                "-fx-text-fill: " + TEXTO_SECUNDARIO + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-family: 'Georgia';"
        );
        contenido.getChildren().add(placeholder);
        contenido.setPadding(new Insets(16));
        contenido.setStyle(
                "-fx-background-color: " + SUPERFICIE + ";" +
                        "-fx-border-color: " + BORDE + ";" +
                        "-fx-border-width: 1;"
        );

        StackPane contenidoConMargen = new StackPane(contenido);
        contenidoConMargen.setPadding(new Insets(12));
        contenidoConMargen.setStyle("-fx-background-color: " + FONDO + ";");

        root = new SplitPane();
        root.getItems().addAll(columnaEditor, contenidoConMargen);
        root.setDividerPositions(0.55);
        root.setStyle("-fx-background-color: " + FONDO + "; -fx-box-border: transparent;");
        SplitPane.setResizableWithParent(columnaEditor, true);
        SplitPane.setResizableWithParent(contenidoConMargen, true);

        // Cuando cambia la pestaña activa, el indicador de línea/columna sigue a esa pestaña
        editorTabs.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                PestanaEditor datos = (PestanaEditor) actual.getUserData();
                actualizarIndicadorPosicion(indicadorPosicion, datos.area.getCaretPosition());
            }
        });
    }

    private void estilizarArbol() {
        arbolDeTrabajo.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        "-fx-control-inner-background: " + PANEL + ";" +
                        "-fx-border-color: " + BORDE + ";" +
                        "-fx-border-width: 0 1 0 0;" +
                        "-fx-text-fill: " + TEXTO + ";"
        );
        arbolDeTrabajo.setPrefWidth(220);
    }

    // ============================================================
    //  PESTAÑAS DEL EDITOR
    // ============================================================

    /** Guarda la referencia al archivo (si tiene) junto con su CodeArea. */
    private static class PestanaEditor {
        File archivo; // null si es una pestaña "Sin título"
        final CodeArea area;

        PestanaEditor(File archivo, CodeArea area) {
            this.archivo = archivo;
            this.area = area;
        }
    }

    private void abrirOFocalizarPestana(File archivo, String contenidoTexto) {
        Tab existente = pestanasPorArchivo.get(archivo);
        if (existente != null) {
            editorTabs.getSelectionModel().select(existente);
            return;
        }

        CodeArea area = crearCodeArea();
        area.replaceText(contenidoTexto);

        Tab tab = new Tab(archivo.getName());
        tab.setUserData(new PestanaEditor(archivo, area));
        tab.setContent(area);
        tab.setOnCloseRequest(e -> pestanasPorArchivo.remove(archivo));

        pestanasPorArchivo.put(archivo, tab);
        editorTabs.getTabs().add(tab);
        editorTabs.getSelectionModel().select(tab);
    }

    private void crearPestanaSinTitulo() {
        CodeArea area = crearCodeArea();
        Tab tab = new Tab("Sin título");
        tab.setUserData(new PestanaEditor(null, area));
        tab.setContent(area);
        editorTabs.getTabs().add(tab);
        editorTabs.getSelectionModel().select(tab);
    }

    private CodeArea crearCodeArea() {
        CodeArea area = new CodeArea();
        area.setWrapText(false);
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        new SintaxColoreado(area); // coloreado en tiempo real, uno por pestaña

        area.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                        "-fx-font-size: 14px;" +
                        "-fx-control-inner-background: #141414;" +
                        "-fx-background-color: #141414;" +
                        "-fx-text-fill: " + TEXTO + ";" +
                        "-fx-highlight-fill: #3A2F1D;" +
                        "-fx-highlight-text-fill: " + PRIMARIO + ";" +
                        "-fx-caret-color: " + PRIMARIO + ";"
        );

        area.caretPositionProperty().addListener((obs, anterior, nuevaPos) -> {
            Tab activa = editorTabs.getSelectionModel().getSelectedItem();
            if (activa != null && ((PestanaEditor) activa.getUserData()).area == area) {
                actualizarIndicadorPosicion(indicadorPosicion, nuevaPos.intValue());
            }
        });

        return area;
    }

    private PestanaEditor pestanaActiva() {
        Tab tab = editorTabs.getSelectionModel().getSelectedItem();
        return tab == null ? null : (PestanaEditor) tab.getUserData();
    }

    private void guardarPestanaActiva() {
        PestanaEditor activa = pestanaActiva();
        if (activa == null) {
            mostrarError("No hay ninguna pestaña abierta");
            return;
        }
        if (activa.archivo == null) {
            try {
                arbolDeTrabajo.guardarComo();
                mostrarEstado("Usa 'Guardar Como' para archivos sin título todavía", TEXTO_SECUNDARIO);
            } catch (Exception ex) {
                mostrarError("No se pudo guardar");
            }
            return;
        }
        try {
            arbolDeTrabajo.guardarArchivo(activa.archivo, activa.area.getText());
            mostrarEstado("Guardado", EXITO);
        } catch (IOException ex) {
            mostrarError("No se pudo guardar");
            ex.printStackTrace();
        }
    }

    // ============================================================
    //  ACCIONES DE COMPILACION (LOS 3 ARCHIVOS A LA VEZ)
    // ============================================================
    private void onCompilarProyecto() {

        Map<TipoArchivo, List<File>> archivosPorTipo = arbolDeTrabajo.localizarArchivosDelProyecto();

        boolean hayAlgunArchivo = archivosPorTipo.values().stream().anyMatch(l -> !l.isEmpty());
        if (!hayAlgunArchivo) {
            mostrarError("Abre una carpeta (o al menos un archivo .y/.z/.pig) primero");
            return;
        }

        // Recordamos el mapeo nombre -> File para poder navegar desde la consola de errores
        archivosProyectoPorNombre.clear();
        archivosPorTipo.values().forEach(lista ->
                lista.forEach(f -> archivosProyectoPorNombre.put(f.getName(), f)));

        mostrarEstado("Analizando los 3 archivos...", TEXTO_SECUNDARIO);

        try {
            resultadoProyecto = compiladorProyecto.compilar(archivosPorTipo, this::obtenerContenidoDe);

            consolaErrores.mostrarErrores(resultadoProyecto.getErrores());
            mostrarVista(consolaErrores);

            if (resultadoProyecto.isCorrecto()) {
                mostrarEstado("✓ Los 3 archivos compilan correctamente", EXITO);
            } else {
                mostrarEstado("✗ Se encontraron errores", ERROR);
            }

        } catch (Exception ex) {
            mostrarError("Error interno durante el análisis del proyecto");
            ex.printStackTrace();
        }
    }

    /** Si el archivo está abierto en una pestaña, usa ese texto (aunque no esté guardado); si no, lo lee de disco. */
    private String obtenerContenidoDe(File archivo) {
        Tab tab = pestanasPorArchivo.get(archivo);
        if (tab != null) {
            return ((PestanaEditor) tab.getUserData()).area.getText();
        }
        try {
            return Files.readString(archivo.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + archivo.getName(), e);
        }
    }

    /** Doble clic en una fila de la consola: abre (o enfoca) el archivo y salta a la línea. */
    private void navegarAError(String nombreArchivo, int linea) {
        File archivo = archivosProyectoPorNombre.get(nombreArchivo);
        if (archivo == null) return;

        if (!pestanasPorArchivo.containsKey(archivo)) {
            try {
                String contenido = Files.readString(archivo.toPath(), StandardCharsets.UTF_8);
                abrirOFocalizarPestana(archivo, contenido);
            } catch (IOException e) {
                mostrarError("No se pudo abrir " + nombreArchivo);
                return;
            }
        } else {
            editorTabs.getSelectionModel().select(pestanasPorArchivo.get(archivo));
        }

        PestanaEditor activa = pestanaActiva();
        if (activa != null && linea > 0) {
            moverCursorALinea(activa.area, linea);
        }
    }

    private void moverCursorALinea(CodeArea area, int linea) {
        String texto = area.getText();

        String[] lineas = texto.split("\\R", -1);

        int lineaObjetivo = Math.max(
                1,
                Math.min(linea, lineas.length)
        );

        int posicion = area.position(lineaObjetivo - 1, 0).toOffset();

        area.moveTo(posicion);
        area.showParagraphAtTop(Math.max(0, lineaObjetivo - 2));
        area.requestFocus();
    }

    private void onMostrarSimbolos() {
        if (resultadoProyecto == null) {
            mostrarError("Primero debes compilar el proyecto");
            return;
        }

        TablaSimbolosView tabla = new TablaSimbolosView();
        tabla.actualizar((List<FilaTabla>) resultadoProyecto.getContexto().getTablaSimbolos().getHistorialTabla());

        mostrarVista(tabla);
    }

    private void onGenerarC3D() {
        // TODO: generar cuartetas y mostrar la tabla correspondiente en 'contenido'
        mostrarEstado("Generando C3D...", TEXTO_SECUNDARIO);
    }

    private void mostrarVista(Node node) {
        contenido.getChildren().setAll(node);
    }

    private void mostrarEstado(String texto, String colorFondo) {
        insigniaEstado.setText(texto);
        estilizarInsignia(insigniaEstado, "#FFFFFF", colorFondo);
    }

    private void mostrarError(String texto) {
        mostrarEstado(texto, ERROR);
    }

    // ============================================================
    //  ESTILOS
    // ============================================================
    private void estilizarBoton(ButtonBase boton) {
        String base =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXTO + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 8 13 8 13;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;";

        String hover =
                "-fx-background-color: #2A2A2A;" +
                        "-fx-text-fill: " + SECUNDARIO + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 8 13 8 13;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;";

        boton.setStyle(base);
        boton.setOnMouseEntered(e -> boton.setStyle(hover));
        boton.setOnMouseExited(e -> boton.setStyle(base));
    }

    private void estilizarBotonPrincipal(Button boton) {
        String base =
                "-fx-background-color: " + PRIMARIO + ";" +
                        "-fx-text-fill: #121212;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;";

        String hover =
                "-fx-background-color: " + SECUNDARIO + ";" +
                        "-fx-text-fill: #121212;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;";

        boton.setStyle(base);
        boton.setOnMouseEntered(e -> boton.setStyle(hover));
        boton.setOnMouseExited(e -> boton.setStyle(base));
    }

    private void estilizarInsignia(Label label, String colorTexto, String colorFondo) {
        label.setStyle(
                "-fx-text-fill: " + colorTexto + ";" +
                        "-fx-background-color: " + colorFondo + ";" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 5 10 5 10;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-font-weight: bold;"
        );
    }

    private void estilizarCampoLinea(TextField campo, String colorBorde) {
        campo.setStyle(
                "-fx-background-color: #141414;" +
                        "-fx-text-fill: " + TEXTO + ";" +
                        "-fx-prompt-text-fill: " + TEXTO_SECUNDARIO + ";" +
                        "-fx-border-color: " + colorBorde + ";" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-font-family: 'Consolas', monospace;" +
                        "-fx-font-size: 11px;"
        );
    }

    // ============================================================
    //  UTILIDADES DEL EDITOR
    // ============================================================
    private void actualizarIndicadorPosicion(Label indicador, int posicionCursor) {
        PestanaEditor activa = pestanaActiva();
        if (activa == null) return;

        String texto = activa.area.getText();
        int limite = Math.min(posicionCursor, texto.length());
        int linea = 1;
        int inicioLineaActual = 0;

        for (int i = 0; i < limite; i++) {
            if (texto.charAt(i) == '\n') {
                linea++;
                inicioLineaActual = i + 1;
            }
        }
        int columna = limite - inicioLineaActual + 1;
        indicador.setText("Línea " + linea + ", Columna " + columna);
    }

    private void irALinea(TextField campoLinea) {
        PestanaEditor activa = pestanaActiva();
        if (activa == null) return;

        try {
            int linea = Integer.parseInt(campoLinea.getText().trim());
            moverCursorALinea(activa.area, linea);
            estilizarCampoLinea(campoLinea, BORDE);
        } catch (NumberFormatException ex) {
            estilizarCampoLinea(campoLinea, ERROR);
        }
    }

    public String getCodigo() {
        PestanaEditor activa = pestanaActiva();
        return activa == null ? "" : activa.area.getText();
    }

    public void setCodigoEnEditor(String texto) {
        PestanaEditor activa = pestanaActiva();
        if (activa == null) {
            crearPestanaSinTitulo();
            activa = pestanaActiva();
        }
        activa.area.replaceText(texto);
    }
}
