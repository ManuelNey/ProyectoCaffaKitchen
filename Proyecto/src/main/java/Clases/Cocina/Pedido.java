package Clases.Cocina;
import Clases.Herramientas.Herramienta;

import java.util.ArrayList;

public class Pedido {
    public String nombre;
    public Herramienta herramienta;
    public int tiempo;
    private int numPedido;
    public Pedido(String nombre, Herramienta herramienta, int tiempo, int numPedido) {
        this.nombre = nombre;
        this.herramienta = herramienta;
        this.tiempo=tiempo;
        this.numPedido=numPedido;
    }
    public String getNombre() { return nombre; }
    public Herramienta getHerramienta() { return herramienta; }

    public int getNumeroPedido() {
        return numPedido;
    }

    public int getTiempo(){
        return this.tiempo;
    }
}