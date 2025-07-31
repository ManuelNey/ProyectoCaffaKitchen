package Clases.Cocina;
import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javafx.scene.media.AudioClip;


public class Cocinero extends Thread {
    private final String nombre;
    private final java.util.function.BiConsumer<String, Runnable> moverCallback;
    private final java.util.function.Consumer<String> actualizarEtiqueta;
    private final JefeCocina jefeCocina;
    private static final AudioClip timbre = new AudioClip(
            Cocinero.class.getResource("/timbre.mp3").toExternalForm()
    );

    private final Semaphore pedidoSem = new Semaphore(0, true);

    private volatile boolean ocupado = false;
    private Pedido pedidoActual;

    private boolean cancelado = false;

    public Cocinero(String nombre,
                    java.util.function.BiConsumer<String, Runnable> moverCallback,
                    java.util.function.Consumer<String> actualizarEtiqueta,
                    JefeCocina jefeCocina) {
        this.nombre = nombre;
        this.moverCallback = moverCallback;
        this.actualizarEtiqueta = actualizarEtiqueta;
        this.jefeCocina = jefeCocina;
    }

    public synchronized boolean estaOcupado() {
        return ocupado;
    }

    public void asignarPedido(Pedido pedido) {
        this.pedidoActual = pedido;
        this.ocupado = true;
        pedidoSem.release();
    }

    @Override
    public void run() {
        while (true) {
            try {
                pedidoSem.acquire();  // Aca espera hasta realese() en asignarPedido.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            Pedido p;
            synchronized(this) { p = pedidoActual; }
            procesarPedido(p);
            synchronized(this) {
                pedidoActual = null;
                ocupado = false;
                if (!cancelado) {
                    actualizarEtiqueta.accept("Libre");
                }
                cancelado = false;
            }
            jefeCocina.notificarCocineroLibre(p);
        }
    }

    private void procesarPedido(Pedido pedido) {
        try {
            // 1) mover al jefe→herramienta
            CountDownLatch l1 = new CountDownLatch(1);
            moverCallback.accept(
                    nombre + "-Jefe->" + nombre + "-" + pedido.getHerramienta().getNombre(),
                    () -> { actualizarEtiqueta.accept("Preparando " + pedido.getNombre()); l1.countDown(); }
            );
            l1.await();

            // pedir con TIMEOUT
            boolean ok = pedido.getHerramienta().pedir(3, TimeUnit.SECONDS);
            if (!ok) {
                actualizarEtiqueta.accept("Cliente impaciente: cancelado " + pedido.getNombre());
                this.cancelado = true;
                return;
            }
            try {
                pedido.getHerramienta().dibujarProceso(pedido.getTiempo());
            } finally {
                pedido.getHerramienta().liberar();
            }

            Platform.runLater(() -> {timbre.play();});

            // muevo: herramienta → entrega
            CountDownLatch l2 = new CountDownLatch(1);
            moverCallback.accept(
                    nombre + "-" + pedido.getHerramienta().getNombre() + "->" + nombre + "-Entrega",
                    () -> { actualizarEtiqueta.accept("Entregado " + pedido.getNombre()); l2.countDown(); }
            );
            l2.await();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
