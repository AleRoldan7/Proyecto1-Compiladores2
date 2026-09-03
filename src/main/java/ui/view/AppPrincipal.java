package ui.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppPrincipal extends Application {

    @Override
    public void start(Stage stage) {

        MainView mainView = new MainView();
        Scene scene = new Scene(mainView,1200,700);

        scene.getStylesheets().add(getClass().getResource("/pintar/colores.css").toExternalForm());


        stage.setTitle("Código 3 direcciones");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
