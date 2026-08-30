module org.compi2.proyecto1compiladores2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens org.compi2.proyecto1compiladores2 to javafx.fxml;
    exports org.compi2.proyecto1compiladores2;
}