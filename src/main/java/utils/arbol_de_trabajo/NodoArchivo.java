package utils.arbol_de_trabajo;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NodoArchivo extends TreeItem<File> {

    private boolean existe = false;

    public NodoArchivo(File file) {
        super(file);
    }

    @Override
    public boolean isLeaf() {
        return getValue() == null || getValue().isFile();
    }

    @Override
    public ObservableList<TreeItem<File>> getChildren() {
        if (!existe) {
            existe = true;
            super.getChildren().setAll(cargarHijos());
        }
        return super.getChildren();
    }

    private List<TreeItem<File>> cargarHijos() {
        File archivo = getValue();
        if (archivo == null || !archivo.isDirectory()) {
            return Collections.emptyList();
        }
        File[] hijos = archivo.listFiles();
        if (hijos == null) {
            return Collections.emptyList();
        }
        // carpetas primero, luego archivos, ambos en orden alfabético
        return Arrays.stream(hijos)
                .sorted(Comparator.comparing(File::isFile)
                        .thenComparing(f -> f.getName().toLowerCase()))
                .map(NodoArchivo::new)
                .collect(Collectors.toList());
    }
}
