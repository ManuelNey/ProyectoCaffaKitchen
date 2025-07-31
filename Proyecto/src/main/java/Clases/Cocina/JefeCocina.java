package Clases.Cocina;

import Clases.Interfaz.InterfazFX;
import javafx.application.Platform;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class JefeCocina {
    private final List<Cocinero> cocineros;
    private final Queue<Pedido> pedidosPendientes = new LinkedList<>();
    private InterfazFX interfaz;

    public JefeCocina(List<Cocinero> cocineros, InterfazFX interfaz) {
        this.cocineros = cocineros;
        this.interfaz = interfaz;
    }

    public synchronized void agregarPedido(Pedido pedido) {
        pedidosPendientes.add(pedido);
        interfaz.actualizarPedidosVisibles(pedidosPendientes); // 👈 Mueve esto arriba
        asignarPedidos(); // 👈 Llama a esto después
    }


    private synchronized void asignarPedidos() {
        for (Cocinero cocinero : cocineros) {
            if (!cocinero.estaOcupado() && !pedidosPendientes.isEmpty()) {
                Pedido pedido = pedidosPendientes.poll();
                cocinero.asignarPedido(pedido);
                interfaz.quitarPedidoDeCola(pedido.getNumeroPedido()); // Para que no se muestre 2 veces
            }
        }
    }

    public synchronized void notificarCocineroLibre(Pedido pedidoTerminado) {
        Platform.runLater(() -> {
            interfaz.eliminarPedido(pedidoTerminado.getNumeroPedido());
            asignarPedidos();
            interfaz.actualizarPedidosVisibles(pedidosPendientes);
        });
    }
}
