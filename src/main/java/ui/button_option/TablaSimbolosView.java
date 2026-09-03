package ui.button_option;

import enums.Categoria;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tablas.FilaTabla;
import tablas.TablaSimbolos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TablaSimbolosView extends BorderPane {

    private static final String SUPERFICIE = "#1A1A1A";
    private static final String PANEL = "#1E1E1E";
    private static final String TEXTO = "#ECECEC";
    private static final String TEXTO_SECUNDARIO = "#9A9A9A";
    private static final String BORDE = "#2A2A2A";

    private final ObservableList<FilaTabla> datos = FXCollections.observableArrayList();
    private final TableView<FilaTabla> tableView = new TableView<>();
    private final ComboBox<String> filtroCategoria = new ComboBox<>();
    private final Label contadorLabel = new Label();

    public TablaSimbolosView() {
        setPadding(new Insets(16));
        setStyle("-fx-background-color: transparent;");
        construirColumnas();
        estilizarTabla();
        setCenter(crearPlaceholder("Compila un programa para ver la tabla de símbolos"));
    }

    /** Punto unico de entrada: recibe la lista ya combinada  */
    public void actualizar(List<FilaTabla> filas) {
        if (filas == null || filas.isEmpty()) {
            setCenter(crearPlaceholder("No se registraron símbolos"));
            return;
        }

        datos.setAll(filas);

        FilteredList<FilaTabla> filtradas = new FilteredList<>(datos, f -> true);
        filtroCategoria.setItems(FXCollections.observableArrayList(
                "Todo", "Variables", "Parámetros", "Funciones", "Estructuras", "Campos de estructura"));
        filtroCategoria.setValue("Todo");
        filtroCategoria.valueProperty().addListener((obs, viejo, nuevo) ->
                filtradas.setPredicate(f -> coincideCategoria(f.getCategoria(), nuevo)));

        tableView.setItems(filtradas);
        contadorLabel.textProperty().bind(
                Bindings.size(filtradas).asString("%d símbolo(s) mostrados"));

        setCenter(envolver());
    }

    private boolean coincideCategoria(Categoria c, String filtro) {
        return switch (filtro) {
            case "Variables" -> c == Categoria.VARIABLE;
            case "Parámetros" -> c == Categoria.PARAMETRO;
            case "Funciones" -> c == Categoria.FUNCION;
            case "Estructuras" -> c == Categoria.ESTRUCTURA;
            case "Campos de estructura" -> c == Categoria.CAMPO_ESTRUCTURA;
            default -> true; // "Todo"
        };
    }

    private void construirColumnas() {
        TableColumn<FilaTabla, String> colNombre = columna("Nombre", FilaTabla::getNombre);
        TableColumn<FilaTabla, String> colCategoria = columna("Categoría",
                f -> formatearCategoria(f.getCategoria()));
        TableColumn<FilaTabla, String> colTipo = columna("Tipo", f ->
                f.getTipo() == null || f.getTipo().isBlank() ? "—" : f.getTipo());
        TableColumn<FilaTabla, String> colDetalle = columna("Detalle", f ->
                f.getDetalle() == null || f.getDetalle().isBlank() ? "—" : f.getDetalle());
        TableColumn<FilaTabla, String> colAmbito = columna("Ámbito", FilaTabla::getAmbito);
        TableColumn<FilaTabla, String> colLinea = columna("Línea", f -> String.valueOf(f.getLinea()));
        colLinea.setPrefWidth(60);

        tableView.getColumns().addAll(colNombre, colCategoria, colTipo, colDetalle, colAmbito, colLinea);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(420);
        tableView.setPlaceholder(crearPlaceholder("Sin resultados para este filtro"));
    }

    private String formatearCategoria(Categoria c) {
        return switch (c) {
            case VARIABLE -> "Variable";
            case PARAMETRO -> "Parámetro";
            case FUNCION -> "Función";
            case ESTRUCTURA -> "Estructura";
            case CAMPO_ESTRUCTURA -> "Campo de estructura";
            case ATRIBUTO -> "Atributo";
            case CAMPO ->  "Campo";
            case CLASE ->  "Clase";
            case CONSTRUCTOR ->  "Constructor";
            case METODO -> "Metodo";
        };
    }

    private TableColumn<FilaTabla, String> columna(
            String titulo, Function<FilaTabla, String> extractor) {
        TableColumn<FilaTabla, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleStringProperty(extractor.apply(d.getValue())));
        return col;
    }

    private void estilizarTabla() {
        tableView.setStyle(
                "-fx-control-inner-background: " + PANEL + ";" +
                        "-fx-background-color: " + PANEL + ";" +
                        "-fx-table-cell-border-color: " + BORDE + ";" +
                        "-fx-text-fill: " + TEXTO + ";"
        );
    }

    private VBox envolver() {
        HBox filtroBar = new HBox(8, new Label("Filtrar:"), filtroCategoria, contadorLabel);
        filtroBar.setAlignment(Pos.CENTER_LEFT);
        filtroBar.getChildren().get(0).setStyle("-fx-text-fill: " + TEXTO_SECUNDARIO + ";");
        contadorLabel.setStyle("-fx-text-fill: " + TEXTO_SECUNDARIO + "; -fx-font-size: 11px;");
        HBox.setMargin(contadorLabel, new Insets(0, 0, 0, 12));

        VBox contenedor = new VBox(8, filtroBar, tableView);
        contenedor.setPadding(new Insets(10));
        contenedor.setStyle("-fx-background-color: " + SUPERFICIE + ";");
        return contenedor;
    }

    private Label crearPlaceholder(String texto) {
        Label label = new Label(texto);
        label.setStyle(
                "-fx-text-fill: " + TEXTO_SECUNDARIO + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-family: 'Arial';"
        );
        return label;
    }
}
