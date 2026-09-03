package tablas;

import enums.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FilaTabla {

    private final String nombre;
    private final Categoria categoria;
    private final String tipo;
    private final String detalle;
    private final String ambito;
    private final int linea;

}
