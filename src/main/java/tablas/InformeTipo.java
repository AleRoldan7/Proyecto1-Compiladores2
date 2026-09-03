package tablas;

import ast.tipos.Tipo;
import enums.TipoDato;
import estructuras.TablaHash;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InformeTipo {

    private final String nombre;
    private final TipoDato categoria;
    private final TablaHash<Tipo> atributos = new TablaHash<>();
    private final TablaHash<Metodo> metodos = new TablaHash<>();

    public InformeTipo(String nombre, TipoDato categoria) {
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public void agregarAtributo(String nombre, Tipo tipo) {
        atributos.put(nombre, tipo);
    }

    public void agregarMetodo(Metodo metodo) {
        metodos.put(metodo.nombre(), metodo);
    }

    public boolean tieneAtributo(String nombre) {
        return atributos.containsKey(nombre);
    }

    public Tipo tipoDeAtributo(String nombre) {
        return atributos.get(nombre);
    }

    public boolean tieneMetodo(String nombre) {
        return metodos.containsKey(nombre);
    }

}
