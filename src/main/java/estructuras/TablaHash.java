package estructuras;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TablaHash<T> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA = 0.75;

    private NodoHash<T>[] buckets;
    private int size;

    public TablaHash() {
        this.buckets = new NodoHash[CAPACIDAD_INICIAL];
        this.size = 0;
    }

    private int calcularIndice(String clave, int logitudBucket) {
        int codigo = clave.hashCode();
        return Math.abs(codigo) % logitudBucket;
    }

    private int calcularIndice(String clave) {
        return calcularIndice(clave, buckets.length);
    }

    private NodoHash<T> getBucket(int indice) {
        return buckets[indice];
    }

    public boolean containsKey(String clave) {
        return buscarNodo(clave) != null;
    }

    public T get(String clave) {
        NodoHash<T> nodo = buscarNodo(clave);
        return (nodo != null) ? nodo.getValor() : null;
    }

    public void put(String clave, T valor) {
        int indice = calcularIndice(clave);
        NodoHash<T> nodoActual = getBucket(indice);

        while (nodoActual != null) {

            if (nodoActual.getClave().equals(clave)) {
                nodoActual.setValor(valor);
                return;
            }

            nodoActual = nodoActual.getSiguiente();
        }

        NodoHash<T> nodoNuevo = new NodoHash<>(clave, valor);
        nodoNuevo.setSiguiente(getBucket(indice));
        buckets[indice] = nodoNuevo;
        size++;

        if ((double) size / buckets.length > FACTOR_CARGA) {
            rehash();
        }
    }

    private void rehash() {
        NodoHash<T>[] anterior = buckets;
        int capacidadNueva = anterior.length * 2;
        buckets = new NodoHash[capacidadNueva];

        for (NodoHash<T> cabeza : anterior) {
            NodoHash<T> actual = cabeza;
            while (actual != null) {
                NodoHash<T> siguiente = actual.getSiguiente(); /*SE GUARDA ANTES Y DESPUES SE HACE EL ENCADENAMIENTO*/
                int nuevoIndice = calcularIndice(actual.getClave(), capacidadNueva);
                actual.setSiguiente(buckets[nuevoIndice]);
                buckets[nuevoIndice] = actual;
                actual = siguiente;
            }
        }

    }

    private NodoHash<T> buscarNodo(String clave) {
        int indice = calcularIndice(clave);
        NodoHash<T> nodoActual = getBucket(indice);
        while (nodoActual != null) {
            if (nodoActual.getClave().equals(clave)) {
                return nodoActual;
            }

            nodoActual = nodoActual.getSiguiente();
        }

        return null;
    }


    public List<T> listaValores() {
        List<T> valores = new ArrayList<>();

        for (int i = 0; i < buckets.length; i++) {
            NodoHash<T> actual = getBucket(i);
            while (actual != null) {
                valores.add(actual.getValor());
                actual = actual.getSiguiente();
            }
        }
        return valores;
    }


}
