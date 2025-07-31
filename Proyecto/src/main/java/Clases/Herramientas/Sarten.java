package Clases.Herramientas;

import javafx.scene.control.ProgressBar;

public class Sarten extends Herramienta {

    public Sarten(ProgressBar barra) {
        super("Sarten", 1, barra);
    }

    @Override
    public void dibujarProceso(int duracionMs) throws InterruptedException {
        super.dibujarProceso(duracionMs); // Usa la versión bloqueante
    }

}
