package utils.coloreado;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.util.Duration;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

public class SintaxColoreado {

    private CodeArea codeArea;

    private PauseTransition pauseTransition = new PauseTransition(Duration.millis(200));

    public SintaxColoreado(CodeArea codeArea) {
        this.codeArea = codeArea;
        iniciar();
    }

    private void iniciar() {

        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            pauseTransition.setOnFinished(event -> {
                pintar();
            });

            pauseTransition.playFromStart();
        });
    }


    private void pintar(){

        String codigo = codeArea.getText();

        Task<StyleSpans<Collection<String>>> tarea =

                new Task<>() {

                    @Override
                    protected StyleSpans<Collection<String>> call() {

                        Coloreado c = new Coloreado();

                        return PintarCodigo.pintar(

                                codigo,

                                c.colorear(codigo)

                        );

                    }

                };

        tarea.setOnSucceeded(e->{

            codeArea.setStyleSpans(

                    0,

                    tarea.getValue()

            );

        });

        Thread hilo = new Thread(tarea);

        hilo.setDaemon(true);

        hilo.start();

    }
}
