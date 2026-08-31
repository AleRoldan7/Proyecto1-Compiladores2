package utils.arbol_de_trabajo;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArbolDeTrabajo extends BorderPane {

    public interface OnArchivoAbierto {
        void abrir(File file, String contenido);
    }

    private final TreeView<File> arbol = new TreeView<>();
    private File carpetaRaiz;
    private File archivoActual;
    private OnArchivoAbierto listener;

    public ArbolDeTrabajo() {
        //setTop(construirBarraHerramientas());
        configurarArbol();
        setCenter(arbol);
    }

    public void setOnArchivoAbierto(OnArchivoAbierto listener) {
        this.listener = listener;
    }

    /**
     * Le pasas el contenido actual del editor para poder guardarlo.
     */
    public void guardarArchivoActual(String contenido) throws IOException {
        if (archivoActual == null) {
            throw new IllegalStateException("No hay un archivo activo. Usa 'Guardar como'.");
        }
        Files.writeString(archivoActual.toPath(), contenido, StandardCharsets.UTF_8);
    }


    private void configurarArbol() {
        arbol.setShowRoot(true);

        // Doble clic en un archivo -> lo abre en el editor
        arbol.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                TreeItem<File> seleccionado = arbol.getSelectionModel().getSelectedItem();
                if (seleccionado != null && seleccionado.getValue().isFile()) {
                    cargarEnEditor(seleccionado.getValue());
                }
            }
        });

        // Celdas: muestran solo el nombre, con prefijo según tipo
        arbol.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(File archivo, boolean vacio) {
                super.updateItem(archivo, vacio);
                if (vacio || archivo == null) {
                    setText(null);
                } else if (archivo.isDirectory()) {
                    setText("\uD83D\uDCC1 " + archivo.getName());
                } else {
                    setText("\uD83D\uDCC4 " + archivo.getName());
                }
            }
        });
    }

    // ---------- Acciones ----------

    public void abrirCarpeta() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecciona la carpeta del proyecto");
        File carpeta = chooser.showDialog(obtenerVentana());
        if (carpeta != null) {
            carpetaRaiz = carpeta;
            arbol.setRoot(new NodoArchivo(carpeta));
            arbol.getRoot().setExpanded(true);
        }
    }

    public void abrirArchivoSuelto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Abrir archivo");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Lenguajes de la Resistencia", "*.y", "*.z", "*.pig"), new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        File archivo = chooser.showOpenDialog(obtenerVentana());
        if (archivo != null) {
            cargarEnEditor(archivo);
        }
    }

    public void cargarEnEditor(File archivo) {
        try {
            String contenido = Files.readString(archivo.toPath(), StandardCharsets.UTF_8);
            archivoActual = archivo;
            if (listener != null) {
                listener.abrir(archivo, contenido);
            }
        } catch (IOException ex) {
            mostrarError("No se pudo abrir el archivo", ex);
        }
    }

    public void guardarComo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar como");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Y?", "*.y"), new FileChooser.ExtensionFilter("Zetariano", "*.z"), new FileChooser.ExtensionFilter("Pig Latin", "*.pig"));
        File destino = chooser.showSaveDialog(obtenerVentana());
        if (destino != null) {
            archivoActual = destino;
            // El contenido real lo escribe MainView llamando a guardarArchivoActual(...)
            if (listener != null) {
                // avisamos con contenido vacío solo para que MainView sepa
                // que hay un nuevo archivo activo; el guardado real lo hace
                // MainView al invocar guardarArchivoActual con el texto del editor.
            }
        }
    }

    /**
     * Comprime la carpeta abierta (o un solo archivo) en un .zip elegido por el usuario.
     */
    public void descargarProyecto() {
        if (carpetaRaiz == null && archivoActual == null) {
            mostrarAlerta("Primero abre una carpeta o un archivo.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Descargar como .zip");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo ZIP", "*.zip"));
        chooser.setInitialFileName((carpetaRaiz != null ? carpetaRaiz.getName() : "proyecto") + ".zip");
        File destinoZip = chooser.showSaveDialog(obtenerVentana());
        if (destinoZip == null) return;

        File origen = carpetaRaiz != null ? carpetaRaiz : archivoActual;
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destinoZip))) {
            comprimir(origen, origen.getName(), zos);
        } catch (IOException ex) {
            mostrarError("No se pudo generar el .zip", ex);
        }
    }

    private void comprimir(File archivo, String rutaEnZip, ZipOutputStream zos) throws IOException {
        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    comprimir(hijo, rutaEnZip + "/" + hijo.getName(), zos);
                }
            }
        } else {
            zos.putNextEntry(new ZipEntry(rutaEnZip));
            Files.copy(archivo.toPath(), zos);
            zos.closeEntry();
        }
    }


    private Window obtenerVentana() {
        return getScene() != null ? getScene().getWindow() : new Stage();
    }

    private void mostrarError(String titulo, Exception ex) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, titulo + ": " + ex.getMessage());
        alerta.showAndWait();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alerta.showAndWait();
    }

}
