package ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.fxmisc.richtext.CodeArea;
import utils.arbol_de_trabajo.ArbolDeTrabajo;

public class MainView extends BorderPane {

    private HBox menu;
    private CodeArea codeArea;
    private ArbolDeTrabajo arbolDeTrabajo = new ArbolDeTrabajo();

    public MainView() {

        menuCreado();
        editor();

        arbolDeTrabajo.setOnArchivoAbierto((archivo, contenido) -> codeArea.replaceText(contenido));

        setTop(menu);
        setLeft(arbolDeTrabajo);
        setCenter(codeArea);

    }

    private void menuCreado() {

        menu = new HBox(6);

        menu.setPadding(new Insets(9,16,9,16));
        menu.setAlignment(Pos.CENTER_LEFT);

        MenuButton buttonArchivo = new MenuButton("Archivo");
        MenuItem nuevo = new MenuItem("Nuevo");
        MenuItem abrir = new MenuItem("Abrir");
        MenuItem carpeta = new MenuItem("Carpera");
        MenuItem guardar = new MenuItem("Guardar");
        MenuItem guradarComo = new MenuItem("Guardar Como");
        MenuItem descargar = new  MenuItem("Descargar");

        buttonArchivo.getItems().addAll(nuevo, new SeparatorMenuItem(), abrir, carpeta, new SeparatorMenuItem(), guardar, guradarComo,
                new SeparatorMenuItem(), descargar);

        nuevo.setOnAction((evento) -> {
            codeArea.clear();
        });

        abrir.setOnAction((evento) -> {
            try {
                arbolDeTrabajo.abrirArchivoSuelto();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        carpeta.setOnAction((evento) -> {
            try {
                arbolDeTrabajo.abrirCarpeta();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        guardar.setOnAction((evento) -> {
            try {
                arbolDeTrabajo.guardarArchivoActual(codeArea.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        guradarComo.setOnAction((evento) -> {
            try {
                arbolDeTrabajo.guardarComo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        descargar.setOnAction((evento) -> {
            try {
                arbolDeTrabajo.descargarProyecto();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button buttonCompilar = new Button("Compilar");
        Button buttonSimbolos = new Button("Tabla Simbolos");
        Button buttonC3d = new Button("Generar C3D");


        menu.getChildren().addAll(buttonArchivo,buttonCompilar,buttonSimbolos,buttonC3d);
    }

    private void editor() {

        codeArea = new CodeArea();

        codeArea.setWrapText(false);
        codeArea.setParagraphGraphicFactory(line -> new Label(String.valueOf(line + 1)));
    }

}
