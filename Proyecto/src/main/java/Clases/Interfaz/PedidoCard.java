package Clases.Interfaz;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class PedidoCard extends VBox {
    private final Label estadoLabel;
    private final int numeroPedido;

    public PedidoCard(int numeroPedido, String nombrePlato, String estadoInicial, Image imagenPlato) {
        this.numeroPedido = numeroPedido;

        // Imagen
        ImageView imagen = new ImageView(imagenPlato);
        imagen.setFitWidth(60);
        imagen.setFitHeight(60);

        // Info
        Label numeroLabel = new Label("Pedido #" + numeroPedido);
        Label nombreLabel = new Label(nombrePlato);
        estadoLabel = new Label("Estado: " + estadoInicial);

        // Estilo visual tipo tarjeta
        setPadding(new Insets(10));
        setSpacing(5);
        setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");
        setPrefWidth(120);
        setMaxWidth(120);

        getChildren().addAll(imagen, numeroLabel, nombreLabel, estadoLabel);
    }

    public void actualizarEstado(String nuevoEstado) {
        estadoLabel.setText("Estado: " + nuevoEstado);
    }

    public int getNumeroPedido() {
        return numeroPedido;
    }
}

