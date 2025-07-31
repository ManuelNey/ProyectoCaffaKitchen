package Clases.Herramientas;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

public class Horno extends Herramienta {

    public Horno(ProgressBar barra) {
        super("Horno", 1, barra);
    }

    @Override
    public void dibujarProceso(int duracionMs) throws InterruptedException {
        super.dibujarProceso(duracionMs); // Usa la versión bloqueante
    }

}
