package utils.coloreado;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PintarCodigo {

    public static StyleSpans<Collection<String>> pintar(
            String codigo,
            List<TokenColor> tokens) {

        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();

        int ultimo = 0;

        for (TokenColor token : tokens) {

            if (token.getInicioToken() > ultimo) {
                builder.add(
                        Collections.singleton("default"),
                        token.getInicioToken() - ultimo
                );
            }

            builder.add(
                    Collections.singleton(token.getColor()),
                    token.getLongitudToken()
            );

            ultimo = token.getInicioToken() + token.getLongitudToken();
        }

        if (ultimo < codigo.length()) {
            builder.add(
                    Collections.singleton("default"),
                    codigo.length() - ultimo
            );
        }

        return builder.create();
    }
}
