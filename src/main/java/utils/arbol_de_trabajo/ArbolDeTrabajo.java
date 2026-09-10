package utils.arbol_de_trabajo;

import enums.TipoArchivo;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
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
        configurarArbol();
        configurarMenuContextual();
        setCenter(arbol);
    }

    public void setOnArchivoAbierto(OnArchivoAbierto listener) {
        this.listener = listener;
    }

    public File getCarpetaRaiz() {
        return carpetaRaiz;
    }

    public File getArchivoActual() {
        return archivoActual;
    }

    /**
     * Le pasas el contenido actual del editor para poder guardarlo.
     */
    public void guardarArchivoActual(String contenido) throws IOException {
        if (archivoActual == null) {
            throw new IllegalStateException("No hay un archivo activo. Usa 'Guardar como'.");
        }
        guardarArchivo(archivoActual, contenido);
    }

    /** Guarda un archivo puntual (usado cuando hay varias pestañas abiertas). */
    public void guardarArchivo(File archivo, String contenido) throws IOException {
        Files.writeString(archivo.toPath(), contenido, StandardCharsets.UTF_8);
    }

    /**
     * Busca dentro de la carpeta del proyecto (o junto al archivo suelto
     * abierto, si no hay carpeta) los archivos .y, .z y .pig, agrupados por
     * tipo. Necesario para poder compilar los 3 lenguajes a la vez.
     */
    public Map<TipoArchivo, List<File>> localizarArchivosDelProyecto() {
        Map<TipoArchivo, List<File>> resultado = new EnumMap<>(TipoArchivo.class);
        resultado.put(TipoArchivo.Y_INTERROGACION, new ArrayList<>());
        resultado.put(TipoArchivo.ZETARIANO, new ArrayList<>());
        resultado.put(TipoArchivo.PIG_LATIN, new ArrayList<>());

        File base = carpetaRaiz != null ? carpetaRaiz
                : (archivoActual != null ? archivoActual.getParentFile() : null);

        if (base != null && base.isDirectory()) {
            recorrerYClasificar(base, resultado);
        } else if (archivoActual != null) {
            TipoArchivo tipo = TipoArchivo.porArchivo(archivoActual);
            if (tipo != TipoArchivo.DESCONOCIDO) {
                resultado.get(tipo).add(archivoActual);
            }
        }

        return resultado;
    }

    private void recorrerYClasificar(File carpeta, Map<TipoArchivo, List<File>> resultado) {
        File[] hijos = carpeta.listFiles();
        if (hijos == null) return;

        for (File hijo : hijos) {
            if (hijo.isDirectory()) {
                recorrerYClasificar(hijo, resultado);
            } else {
                TipoArchivo tipo = TipoArchivo.porArchivo(hijo);
                if (tipo != TipoArchivo.DESCONOCIDO) {
                    resultado.get(tipo).add(hijo);
                }
            }
        }
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

    private void configurarMenuContextual() {
        arbol.setOnContextMenuRequested(evento -> {

            TreeItem<File> seleccionado =
                    arbol.getSelectionModel().getSelectedItem();

            ContextMenu menu = new ContextMenu();

            // Si no hay nada seleccionado
            if (seleccionado == null) {
                MenuItem nuevaCarpeta = new MenuItem("📁 Nueva carpeta");

                nuevaCarpeta.setOnAction(e -> crearCarpeta(carpetaRaiz));

                menu.getItems().add(nuevaCarpeta);

            } else {

                File archivoSeleccionado = seleccionado.getValue();

                // ------------------------------------------------
                // Si seleccionamos una CARPETA
                // ------------------------------------------------
                if (archivoSeleccionado.isDirectory()) {

                    MenuItem nuevoArchivo =
                            new MenuItem("📄 Nuevo archivo");

                    MenuItem nuevaCarpeta =
                            new MenuItem("📁 Nueva carpeta");

                    MenuItem actualizar =
                            new MenuItem("🔄 Actualizar");

                    nuevoArchivo.setOnAction(e ->
                            crearArchivo(archivoSeleccionado, seleccionado)
                    );

                    nuevaCarpeta.setOnAction(e ->
                            crearCarpeta(archivoSeleccionado)
                    );

                    actualizar.setOnAction(e ->
                            refrescarNodo(seleccionado)
                    );

                    menu.getItems().addAll(
                            nuevoArchivo,
                            nuevaCarpeta,
                            new SeparatorMenuItem(),
                            actualizar
                    );

                    // ------------------------------------------------
                    // Si seleccionamos un ARCHIVO
                    // ------------------------------------------------
                } else {

                    MenuItem abrir =
                            new MenuItem("📄 Abrir");

                    MenuItem eliminar =
                            new MenuItem("🗑 Eliminar");

                    abrir.setOnAction(e ->
                            cargarEnEditor(archivoSeleccionado)
                    );

                    eliminar.setOnAction(e ->
                            eliminarArchivo(archivoSeleccionado, seleccionado)
                    );

                    menu.getItems().addAll(
                            abrir,
                            eliminar
                    );
                }
            }

            menu.show(
                    arbol,
                    evento.getScreenX(),
                    evento.getScreenY()
            );
        });
    }

    private void crearCarpeta(File carpetaPadre) {

        if (carpetaPadre == null || !carpetaPadre.isDirectory()) {
            mostrarAlerta("Selecciona una carpeta primero.");
            return;
        }

        TextInputDialog dialogo =
                new TextInputDialog();

        dialogo.setTitle("Nueva carpeta");
        dialogo.setHeaderText("Crear una nueva carpeta");
        dialogo.setContentText("Nombre:");

        Optional<String> resultado =
                dialogo.showAndWait();

        if (resultado.isEmpty()) {
            return;
        }

        String nombre = resultado.get().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("El nombre no puede estar vacío.");
            return;
        }

        File nuevaCarpeta =
                new File(carpetaPadre, nombre);

        if (nuevaCarpeta.exists()) {
            mostrarAlerta(
                    "Ya existe una carpeta o archivo con ese nombre."
            );
            return;
        }

        if (nuevaCarpeta.mkdir()) {

            TreeItem<File> nodoPadre =
                    buscarNodo(carpetaPadre);

            if (nodoPadre != null) {
                agregarHijo(nodoPadre, nuevaCarpeta);
                nodoPadre.setExpanded(true);
            }

        } else {
            mostrarAlerta("No se pudo crear la carpeta.");
        }
    }

    private void crearArchivo(
            File carpetaPadre,
            TreeItem<File> nodoPadre
    ) {

        ChoiceDialog<String> tipoDialogo =
                new ChoiceDialog<>(
                        "Y?",
                        "Y?",
                        "Zetariano",
                        "Pig Latin"
                );

        tipoDialogo.setTitle("Nuevo archivo");
        tipoDialogo.setHeaderText("Selecciona el tipo de archivo");
        tipoDialogo.setContentText("Lenguaje:");

        Optional<String> tipoResultado =
                tipoDialogo.showAndWait();

        if (tipoResultado.isEmpty()) {
            return;
        }

        TextInputDialog nombreDialogo =
                new TextInputDialog();

        nombreDialogo.setTitle("Nuevo archivo");
        nombreDialogo.setHeaderText("Nombre del archivo");
        nombreDialogo.setContentText("Nombre:");

        Optional<String> nombreResultado =
                nombreDialogo.showAndWait();

        if (nombreResultado.isEmpty()) {
            return;
        }

        String nombre =
                nombreResultado.get().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("El nombre no puede estar vacío.");
            return;
        }

        String extension;

        switch (tipoResultado.get()) {

            case "Y?" -> extension = ".y";

            case "Zetariano" -> extension = ".z";

            case "Pig Latin" -> extension = ".pig";

            default -> {
                mostrarAlerta("Tipo de archivo inválido.");
                return;
            }
        }

        // Si el usuario escribió la extensión, no la duplicamos
        if (!nombre.toLowerCase().endsWith(extension)) {
            nombre += extension;
        }

        File nuevoArchivo =
                new File(carpetaPadre, nombre);

        if (nuevoArchivo.exists()) {
            mostrarAlerta(
                    "Ya existe un archivo con ese nombre."
            );
            return;
        }

        try {

            Files.writeString(
                    nuevoArchivo.toPath(),
                    "",
                    StandardCharsets.UTF_8
            );

            TreeItem<File> nuevoNodo =
                    new NodoArchivo(nuevoArchivo);

            nodoPadre.getChildren().add(nuevoNodo);
            nodoPadre.setExpanded(true);

            // Abrir automáticamente el archivo nuevo
            cargarEnEditor(nuevoArchivo);

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo crear el archivo",
                    ex
            );
        }
    }

    private void agregarHijo(
            TreeItem<File> nodoPadre,
            File archivo
    ) {

        TreeItem<File> nuevoNodo =
                new NodoArchivo(archivo);

        nodoPadre.getChildren().add(nuevoNodo);
    }

    private TreeItem<File> buscarNodo(File archivo) {

        if (arbol.getRoot() == null) {
            return null;
        }

        return buscarNodoRecursivo(
                arbol.getRoot(),
                archivo
        );
    }

    private TreeItem<File> buscarNodoRecursivo(
            TreeItem<File> nodo,
            File archivo
    ) {

        if (nodo.getValue().equals(archivo)) {
            return nodo;
        }

        for (TreeItem<File> hijo : nodo.getChildren()) {

            TreeItem<File> encontrado =
                    buscarNodoRecursivo(hijo, archivo);

            if (encontrado != null) {
                return encontrado;
            }
        }

        return null;
    }

    private void refrescarNodo(TreeItem<File> nodo) {

        File carpeta = nodo.getValue();

        if (!carpeta.isDirectory()) {
            return;
        }

        nodo.getChildren().clear();

        File[] archivos =
                carpeta.listFiles();

        if (archivos == null) {
            return;
        }

        Arrays.sort(
                archivos,
                Comparator.comparing(
                        File::isFile
                ).thenComparing(
                        File::getName,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        for (File archivo : archivos) {
            nodo.getChildren().add(
                    new NodoArchivo(archivo)
            );
        }

        nodo.setExpanded(true);
    }

    private void eliminarArchivo(
            File archivo,
            TreeItem<File> nodo
    ) {

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmacion.setTitle("Eliminar");
        confirmacion.setHeaderText(
                "¿Eliminar " + archivo.getName() + "?"
        );
        confirmacion.setContentText(
                "Esta acción no se puede deshacer."
        );

        Optional<ButtonType> resultado =
                confirmacion.showAndWait();

        if (resultado.isEmpty() ||
                resultado.get() != ButtonType.OK) {
            return;
        }

        try {

            Files.delete(archivo.toPath());

            if (archivo.equals(archivoActual)) {
                archivoActual = null;
            }

            TreeItem<File> padre =
                    nodo.getParent();

            if (padre != null) {
                padre.getChildren().remove(nodo);
            }

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo eliminar el archivo",
                    ex
            );
        }
    }

    public void crearNuevaCarpeta() {

        if (carpetaRaiz == null) {
            mostrarAlerta("Primero debes abrir una carpeta de proyecto.");
            return;
        }

        TreeItem<File> seleccionado =
                arbol.getSelectionModel().getSelectedItem();

        File carpetaPadre;

        if (seleccionado != null &&
                seleccionado.getValue().isDirectory()) {

            carpetaPadre = seleccionado.getValue();

        } else {
            carpetaPadre = carpetaRaiz;
        }

        crearCarpeta(carpetaPadre);
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
