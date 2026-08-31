module org.compi2.proyecto1compiladores2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.antlr.antlr4.runtime;
    requires org.fxmisc.richtext;

    opens ui.view to javafx.graphics, javafx.fxml;
    exports ui.view;
}