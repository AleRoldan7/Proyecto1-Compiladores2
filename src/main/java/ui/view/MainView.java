package ui.view;

import analisis.CompiladorCodigo;
import analisis.ResultadoAnalisis;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import tablas.FilaTabla;
import ui.button_option.TablaSimbolosView;
import utils.arbol_de_trabajo.ArbolDeTrabajo;
import utils.coloreado.SintaxColoreado;

import java.util.List;

public class MainView extends BorderPane {

    private HBox menu;
    private CodeArea codeArea;
    private SplitPane root;
    private StackPane contenido;
    private Label insigniaEstado;

    private final ArbolDeTrabajo arbolDeTrabajo = new ArbolDeTrabajo();

    private final CompiladorCodigo compiladorCodigo = new CompiladorCodigo();
    private ResultadoAnalisis resultadoAnalisis;

    private SintaxColoreado sintaxColoreado;
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

        arbolDeTrabajo.setOnArchivoAbierto((archivo, contenidoTexto) -> codeArea.replaceText(contenidoTexto));
        estilizarArbol();

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

        buttonArchivo.getItems().addAll(
                nuevo, new SeparatorMenuItem(),
                abrir, carpeta, new SeparatorMenuItem(),
                guardar, guardarComo, new SeparatorMenuItem(),
                descargar
        );

        nuevo.setOnAction(e -> codeArea.clear());

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

        guardar.setOnAction(e -> {
            try {
                arbolDeTrabajo.guardarArchivoActual(codeArea.getText());
                mostrarEstado("Guardado", EXITO);
            } catch (Exception ex) {
                mostrarError("No se pudo guardar");
                ex.printStackTrace();
            }
        });

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

        Button buttonCompilar = new Button("Compilar");
        Button buttonSimbolos = new Button("Tabla Símbolos");
        Button buttonC3d = new Button("Generar C3D");

        estilizarBoton(buttonArchivo);
        estilizarBotonPrincipal(buttonCompilar);
        estilizarBoton(buttonSimbolos);
        estilizarBoton(buttonC3d);

        buttonCompilar.setOnAction(e -> onCompilar());
        buttonSimbolos.setOnAction(e -> onMostrarSimbolos());
        buttonC3d.setOnAction(e -> onGenerarC3D());

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        insigniaEstado = new Label("Sin compilar");
        estilizarInsignia(insigniaEstado, TEXTO_SECUNDARIO, "#EEE7DC");

        menu.getChildren().addAll(
                tituloBox, separador,
                buttonArchivo, buttonCompilar, buttonSimbolos, buttonC3d,
                espaciador, insigniaEstado
        );
    }

    // ============================================================
    //  EDITOR + PANEL DE RESULTADOS
    // ============================================================
    private void contenidoCentral() {
        codeArea = new CodeArea();
        codeArea.setWrapText(false);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        sintaxColoreado = new SintaxColoreado(codeArea);

        codeArea.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                        "-fx-font-size: 14px;" +
                        "-fx-control-inner-background: #141414;" +
                        "-fx-background-color: #141414;" +
                        "-fx-text-fill: " + TEXTO + ";" +
                        "-fx-highlight-fill: #3A2F1D;" +
                        "-fx-highlight-text-fill: " + PRIMARIO + ";" +
                        "-fx-caret-color: " + PRIMARIO + ";"
        );

        // ---- Barra inferior: posicion + ir a linea ----
        Label indicadorPosicion = new Label("Línea 1, Columna 1");
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

        codeArea.caretPositionProperty().addListener((obs, anterior, nuevaPos) ->
                actualizarIndicadorPosicion(indicadorPosicion, nuevaPos.intValue()));

        HBox barraInferior = new HBox(8, indicadorPosicion, textoIrLinea, campoLinea, botonIrLinea);
        barraInferior.setAlignment(Pos.CENTER_LEFT);
        barraInferior.setPadding(new Insets(5, 8, 5, 8));
        barraInferior.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        "-fx-border-color: " + BORDE + ";" +
                        "-fx-border-width: 1 0 0 0;"
        );

        VBox columnaEditor = new VBox(codeArea, barraInferior);
        VBox.setVgrow(codeArea, Priority.ALWAYS);
        columnaEditor.setFillWidth(true);
        columnaEditor.setStyle(
                "-fx-background-color: " + SUPERFICIE + ";" +
                        "-fx-border-color: " + BORDE + ";" +
                        "-fx-border-width: 0 1 0 0;"
        );

        // ---- Panel de resultados (tabla simbolos, cuartetas, etc.) ----
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
    //  ACCIONES DE COMPILACION
    // ============================================================
    private void onCompilar() {

        String codigo = codeArea.getText();
        if (codigo == null || codigo.isBlank()) {

            mostrarError("El código está vacío");
            return;
        }

        mostrarEstado("Analizando...", TEXTO_SECUNDARIO);

        try {

            resultadoAnalisis =
                    compiladorCodigo.analizar(codigo);

            if (resultadoAnalisis.isCorrecto()) {

                mostrarEstado(
                        "✓ Análisis correcto",
                        EXITO
                );

                mostrarResultadoCorrecto();

            } else {

                mostrarEstado(
                        "✗ Se encontraron errores",
                        ERROR
                );

                mostrarErrores();

            }

        } catch (Exception ex) {

            mostrarError(
                    "Error interno durante el análisis"
            );

            ex.printStackTrace();
        }
    }
    private void mostrarResultadoCorrecto() {

        VBox panel = new VBox(15);

        panel.setPadding(new Insets(20));

        Label titulo = new Label("Análisis completado");

        titulo.setStyle(
                "-fx-text-fill: " + TEXTO + ";" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );


        Label estado = new Label(
                "El código no contiene errores."
        );

        estado.setStyle(
                "-fx-text-fill: " + EXITO + ";" +
                        "-fx-font-size: 14px;"
        );


        Label ast = new Label(
                "AST generado correctamente."
        );

        ast.setStyle(
                "-fx-text-fill: " + TEXTO_SECUNDARIO + ";"
        );


        panel.getChildren().addAll(
                titulo,
                estado,
                ast
        );

        mostrarVista(panel);
    }

    private void mostrarErrores() {

        VBox panel = new VBox(10);

        panel.setPadding(new Insets(20));

        Label titulo = new Label(
                "Errores de compilación"
        );

        titulo.setStyle(
                "-fx-text-fill: " + ERROR + ";" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );


        ListView<String> listaErrores =
                new ListView<>();

        listaErrores.getItems().addAll(
                resultadoAnalisis.getErrores()
        );


        listaErrores.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        "-fx-control-inner-background: " + PANEL + ";" +
                        "-fx-text-fill: " + TEXTO + ";"
        );


        VBox.setVgrow(
                listaErrores,
                Priority.ALWAYS
        );


        panel.getChildren().addAll(
                titulo,
                listaErrores
        );

        mostrarVista(panel);
    }
    private void onMostrarSimbolos() {
        if (resultadoAnalisis == null) {

            mostrarError(
                    "Primero debes compilar el código"
            );

            return;
        }

        TablaSimbolosView tabla =
                new TablaSimbolosView();

        tabla.actualizar((List<FilaTabla>) resultadoAnalisis.getTablaSimbolos());

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
        String texto = codeArea.getText();
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
        try {
            int linea = Integer.parseInt(campoLinea.getText().trim());
            String texto = codeArea.getText();
            int totalLineas = 1;
            for (int i = 0; i < texto.length(); i++) {
                if (texto.charAt(i) == '\n') totalLineas++;
            }

            if (linea < 1 || linea > totalLineas) {
                estilizarCampoLinea(campoLinea, ERROR);
                return;
            }

            int posicion = 0;
            int lineaActual = 1;
            while (lineaActual < linea && posicion < texto.length()) {
                if (texto.charAt(posicion) == '\n') lineaActual++;
                posicion++;
            }

            codeArea.moveTo(posicion);
            codeArea.showParagraphAtTop(linea - 1);
            codeArea.requestFocus();
            estilizarCampoLinea(campoLinea, BORDE);

        } catch (NumberFormatException ex) {
            estilizarCampoLinea(campoLinea, ERROR);
        }
    }

    public String getCodigo() {
        return codeArea.getText();
    }

    public void setCodigoEnEditor(String texto) {
        codeArea.replaceText(texto);
    }
}