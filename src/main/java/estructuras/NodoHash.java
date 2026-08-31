package estructuras;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodoHash<T> {

    private String clave;
    private T valor;
    private NodoHash<T> siguiente;

    public NodoHash(String clave, T valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public NodoHash(T valor, NodoHash<T> siguiente) {
        this.valor = valor;
        this.siguiente = siguiente;
    }
}
