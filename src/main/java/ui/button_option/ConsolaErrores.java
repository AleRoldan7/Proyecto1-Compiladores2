package ui.button_option;

import enums.TipoErrorSemantico;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import semantico.ErrorSemantico;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Panel tipo "Problems"/consola de un IDE: una fila por error/advertencia,
 * ya etiquetada con el archivo exacto donde ocurrió. Doble clic en una fila
 * navega a ese archivo (abriéndolo si hace falta) y posiciona el cursor en
 * la línea/columna reportadas.
 */
public class ConsolaErrores extends BorderPane {

    private static final String PANEL = "#1E1E1E";
    private static final String TEXTO = "#ECECEC";
    private static final String TEXTO_SECUNDARIO = "#9A9A9A";
    private static final String BORDE = "#2A2A2A";
    private static final String ERROR = "#E05260";
    private static final String ADVERTENCIA = "#D9A15C";

    private final TableView<ErrorSemantico> tabla = new TableView<>();
    private final Label resumen = new Label();

    /** (archivo, linea) -> el llamador decide cómo abrir/mover el cursor */
    private BiConsumer<String, Integer> onNavegar;

    public ConsolaErrores() {
        construirColumnas();
        estilizar();

        tabla.setRowFactory(tv -> {
            var fila = new javafx.scene.control.TableRow<ErrorSemantico>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty() && onNavegar != null) {
                    ErrorSemantico e = fila.getItem();
                    onNavegar.accept(e.archivo(), e.linea());
                }
            });
            return fila;
        });

        resumen.setStyle("-fx-text-fill: " + TEXTO_SECUNDARIO + "; -fx-font-size: 11px;");
        resumen.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));

        setCenter(tabla);
        setBottom(resumen);
    }

    public void setOnNavegar(BiConsumer<String, Integer> onNavegar) {
        this.onNavegar = onNavegar;
    }

    public void mostrarErrores(List<ErrorSemantico> errores) {
        tabla.getItems().setAll(errores);

        long totalErrores = errores.stream().filter(e -> e.tipoError() == TipoErrorSemantico.ERROR).count();
        long totalAdvertencias = errores.size() - totalErrores;

        if (errores.isEmpty()) {
            resumen.setText("Sin errores ni advertencias.");
        } else {
            resumen.setText(totalErrores + " error(es), " + totalAdvertencias + " advertencia(s).");
        }
    }

    public void limpiar() {
        tabla.getItems().clear();
        resumen.setText("");
    }

    @SuppressWarnings("unchecked")
    private void construirColumnas() {
        TableColumn<ErrorSemantico, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().tipoError() == TipoErrorSemantico.ERROR ? "Error" : "Advertencia"));
        colTipo.setPrefWidth(90);
        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean vacio) {
                super.updateItem(valor, vacio);
                if (vacio || valor == null) {
                    setText(null);
                    setTextFill(Color.web(TEXTO));
                } else {
                    setText(valor);
                    setTextFill(Color.web(valor.equals("Error") ? ERROR : ADVERTENCIA));
                }
            }
        });

        TableColumn<ErrorSemantico, String> colArchivo = new TableColumn<>("Archivo");
        colArchivo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().archivo()));
        colArchivo.setPrefWidth(160);

        TableColumn<ErrorSemantico, String> colPosicion = new TableColumn<>("Posición");
        colPosicion.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().linea() + ":" + d.getValue().columna()));
        colPosicion.setPrefWidth(80);

        TableColumn<ErrorSemantico, String> colMensaje = new TableColumn<>("Mensaje");
        colMensaje.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().mensaje()));
        colMensaje.setPrefWidth(420);

        tabla.getColumns().setAll(List.of(colTipo, colArchivo, colPosicion, colMensaje));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("Sin errores todavía. Compila el proyecto para ver resultados aquí."));
    }

    private void estilizar() {
        tabla.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        "-fx-control-inner-background: " + PANEL + ";" +
                        "-fx-text-fill: " + TEXTO + ";"
        );
        setStyle("-fx-border-color: " + BORDE + "; -fx-border-width: 1;");
    }
}
