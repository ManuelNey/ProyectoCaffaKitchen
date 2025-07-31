package Clases.Interfaz;

import Clases.Cocina.Cocinero;
import Clases.Cocina.JefeCocina;
import Clases.Cocina.Pedido;
import Clases.Herramientas.Horno;
import Clases.Herramientas.Parrilla;
import Clases.Herramientas.Herramienta;
import Clases.Herramientas.Sarten;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class InterfazFX extends Application {

    private Pane root;
    private final Queue<Pedido> colaPendientes = new LinkedList<>();

    // Nuevo
    private FlowPane statusPane;

    /** Para representar los estados de los recursos (disponibles y procesos en cola)*/
    private Label statusLabel;


    private final Map<String, Herramienta> herramientaMap = new HashMap<>();
    private final Map<String, Label> libresLabels = new HashMap<>();
    private final Map<String, Label> colaLabels = new HashMap<>();
    private final Map<String, ProgressBar> barrasProgreso = new HashMap<>();
    private final Map<String, StackPane> cocineros = new HashMap<>();
    private final Map<String, Label> etiquetasPedidos = new HashMap<>();

    private final Map<String, Point2D> ubicaciones = Map.of(
            "Horno", new Point2D(260,420),
            "Parrilla", new Point2D(230,500),
            "Sarten", new Point2D(300,305),
            "Jefe", new Point2D(860,200),
            "Entrega", new Point2D(860,590)
    );

    private List<Cocinero> listaCocineros;
    private JefeCocina jefeCocina;
    private Horno horno;
    private Parrilla parrilla;
    private Sarten sarten;

    private VBox contenedorPedidos;

    private MediaPlayer timbre;

    @Override
    public void start(Stage primaryStage) {
        String path = getClass().getResource("/musica.mp3").toExternalForm();
        Media media = new Media(path);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setVolume(0.9); // Puedes cambiar el volumen
        mediaPlayer.play();


        root = new Pane();

        // Status pane
        statusPane = new FlowPane();
        statusPane.setHgap(20);
        statusPane.setVgap(5);
        statusPane.setLayoutX(10);
        statusPane.setLayoutY(10);

        Image fondo = new Image("overcooked3.png");
        ImageView fondoView = new ImageView(fondo);
        fondoView.setFitWidth(1500);
        fondoView.setFitHeight(900);
        root.getChildren().add(fondoView);

        root.getChildren().add(statusPane);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-background-color: rgba(0,0,0,0.6);"
                + "-fx-text-fill: white; -fx-padding:4;");
        statusLabel.setLayoutX(10);
        statusLabel.setLayoutY(10);
        root.getChildren().add(statusLabel);

        ImageView timbre= new ImageView("timbre.png");
        timbre.setFitWidth(40);
        timbre.setFitHeight(40);
        timbre.setLayoutX(900);
        timbre.setLayoutY(650);
        root.getChildren().add(timbre);

        ImageView imgJefe= new ImageView("jefe2.png");
        imgJefe.setFitWidth(100);
        imgJefe.setFitHeight(120);
        imgJefe.setLayoutX(1020);
        imgJefe.setLayoutY(550);
        root.getChildren().add(imgJefe);

        ProgressBar barraHorno    = agregarHerramienta("Horno");
        ProgressBar barraParrilla = agregarHerramienta("Parrilla");
        ProgressBar barraSarten   = agregarHerramienta("Sarten");

        // CREA UNA SOLA VEZ cada herramienta y la registra:
        horno    = new Horno(barraHorno);
        parrilla = new Parrilla(barraParrilla);
        sarten   = new Sarten(barraSarten);

        herramientaMap.put("Horno", horno);
        herramientaMap.put("Parrilla", parrilla);
        herramientaMap.put("Sarten", sarten);

        agregarCocinero("Juan");
        agregarCocinero("Ana");
        agregarCocinero("Luis");

        // Inicializar contenedorPedidos ANTES de agregar pedidos
        contenedorPedidos = new VBox(10); // espaciado horizontal de 10px
        // espaciado vertical de 10px
        contenedorPedidos.setPadding(new Insets(10));
        contenedorPedidos.setLayoutX(50);
        contenedorPedidos.setLayoutY(400);
        root.getChildren().add(contenedorPedidos);

        // Crear lista vacía y jefe
        List<Cocinero> cocinerosTemp = new java.util.ArrayList<>();
        jefeCocina = new JefeCocina(cocinerosTemp,this);

        // Crear cocineros con referencia al jefe
        Cocinero cocineroJuan = new Cocinero("Juan", this::moverCocineroPorNombreDestino, texto -> actualizarEtiquetaPedido("Juan", texto), jefeCocina);
        Cocinero cocineroAna = new Cocinero("Ana", this::moverCocineroPorNombreDestino, texto -> actualizarEtiquetaPedido("Ana", texto), jefeCocina);
        Cocinero cocineroLuis = new Cocinero("Luis", this::moverCocineroPorNombreDestino, texto -> actualizarEtiquetaPedido("Luis", texto), jefeCocina);

        // Agregar cocineros a la lista
        cocinerosTemp.add(cocineroJuan);
        cocinerosTemp.add(cocineroAna);
        cocinerosTemp.add(cocineroLuis);

        listaCocineros = cocinerosTemp;
        Herramienta[] herramientas= {horno,parrilla,sarten};
        // Agregar pedidos al jefe con herramienta aleatoria y mostrar tarjetas
        String[] platos = {"Pizza", "Hamburguesa", "Taco", "Ensalada", "Pancho"};


        Random rnd = new Random();
        for (int i = 0; i < 25; i++) {
            // Para probar: los dos primeros van al mismo Horno y tardan 12 segundos,
            // de modo que el segundo siempre muera por timeout antes de poder entrar.
            Herramienta herramienta = (i < 2)
                    ? horno
                    : herramientas[rnd.nextInt(herramientas.length)];

            long tiempo = (i < 2)
                    ? 12_000
                    : rnd.nextInt(2_000, 7_000);

            Pedido p = new Pedido(
                    platos[rnd.nextInt(platos.length)],
                    herramienta,
                    (int) tiempo,
                    i + 1
            );
            jefeCocina.agregarPedido(p);
        }

        // Iniciar threads de cocineros
        for (Cocinero c : listaCocineros) {
            c.start();
        }

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Cocina estilo Overcooked");
        primaryStage.show();

        Timeline refresco = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    StringBuilder sb = new StringBuilder();
                    herramientaMap.forEach((nombre, herr) -> {
                        sb.append(String.format("%s: Libres: %d En cola: %d   ",

                                /** Aca uso los metodos de la clase Herramienta*/
                                nombre, herr.getPermitsAvailable(), herr.getQueueLength()
                        ));
                    });
                    statusLabel.setText(sb.toString());
                }),
                new KeyFrame(Duration.millis(500))
        );
        refresco.setCycleCount(Animation.INDEFINITE);
        refresco.play();
    }

    private ProgressBar agregarHerramienta(String nombre) {
        Point2D pos = ubicaciones.get(nombre);

        // 1) Barra
        ProgressBar barra = new ProgressBar(0);
        barra.setPrefSize(80, 20);
        barra.setStyle("-fx-accent: #ff4500;");
        StackPane stack = new StackPane(barra);
        stack.setLayoutX(pos.getX());
        stack.setLayoutY(pos.getY());
        root.getChildren().add(stack);
        barrasProgreso.put(nombre, barra);


        return barra;
    }



    private void agregarCocinero(String nombre) {
        Image imagenCocinero = new Image(nombre.toLowerCase() + ".png"); // Asegúrate que estos archivos existan en resources
        ImageView cocineroView = new ImageView(imagenCocinero);
        cocineroView.setFitWidth(70); // Ajusta el tamaño según convenga
        cocineroView.setFitHeight(90);

        Label etiqueta = new Label("");
        etiqueta.setTranslateY(-30);
        etiqueta.setStyle("-fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 2 5 2 5; " +
                "-fx-background-radius: 5;");

        StackPane stack = new StackPane(cocineroView, etiqueta);
        Point2D inicio = ubicaciones.get("Jefe");
        stack.setLayoutX(inicio.getX());
        stack.setLayoutY(inicio.getY());

        root.getChildren().add(stack);
        cocineros.put(nombre, stack);
        etiquetasPedidos.put(nombre, etiqueta);
    }


    public void actualizarEtiquetaPedido(String nombreCocinero, String texto) {
        Platform.runLater(() -> {
            Label etiqueta = etiquetasPedidos.get(nombreCocinero);
            if (etiqueta != null) {
                etiqueta.setText(texto);

                if (texto.contains("cancelado")) {
                    // Haz que suene el timbre (asegúrate de haber inicializado timbre en start())
                    timbre.stop();
                    timbre.play();

                    // (opcional) quita la tarjeta de la UI
                    int num = Integer.parseInt(texto.replaceAll(".*#(\\d+).*", "$1"));
                    eliminarPedido(num);
                }
            }
        });
    }
    private void moverCocinero(String nombre, String destino, Runnable onFinish) {
        StackPane stack = cocineros.get(nombre);
        Point2D objetivo = ubicaciones.get(destino);

        if (stack != null && objetivo != null) {

            double anchoHerramienta = 64;
            double altoHerramienta = 64;

            double radioCocinero = 15; // porque los círculos tienen radio 15

// Centrado horizontal, debajo de la herramienta con un margen, y un pequeño desplazamiento hacia la izquierda
            objetivo = objetivo.add(
                    (anchoHerramienta / 2) - radioCocinero - 300,  // <- Ajuste de 20 píxeles a la izquierda
                    altoHerramienta + 5                          // Mismo margen vertical
            );




            TranslateTransition tt = new TranslateTransition(Duration.seconds(1), stack);
            double actualX = stack.getLayoutX() + stack.getTranslateX();
            double actualY = stack.getLayoutY() + stack.getTranslateY();
            double dx = objetivo.getX() - actualX;
            double dy = objetivo.getY() - actualY;

            tt.setToX(dx);
            tt.setToY(dy);

            Point2D finalObjetivo = objetivo;
            tt.setOnFinished(e -> {
                stack.setLayoutX(finalObjetivo.getX());
                stack.setLayoutY(finalObjetivo.getY());
                stack.setTranslateX(0);
                stack.setTranslateY(0);
                if (onFinish != null) onFinish.run();
            });

            if (dx == 0 && dy == 0) {
                if (onFinish != null) onFinish.run();
            } else {
                tt.play();
            }
        }
    }

    public void moverCocineroPorNombreDestino(String mensaje, Runnable onFinish) {
        Platform.runLater(() -> {
            String[] partes = mensaje.split("->");
            if (partes.length == 2) {
                moverCocineroEncadenado(partes[0], partes[1], onFinish);
            } else {
                String[] datos = mensaje.split("-");
                if (datos.length != 2) return;
                String nombre = datos[0];
                String destino = datos[1];
                moverCocinero(nombre, destino, onFinish);
            }
        });
    }

    private void moverCocineroEncadenado(String paso1, String paso2, Runnable onFinish) {
        String[] datos1 = paso1.split("-");
        String[] datos2 = paso2.split("-");
        if (datos1.length != 2 || datos2.length != 2) return;

        String nombre = datos1[0];
        String destino1 = datos1[1];
        String destino2 = datos2[1];

        StackPane stack = cocineros.get(nombre);
        Point2D objetivo1 = ubicaciones.get(destino1);
        Point2D objetivo2 = ubicaciones.get(destino2);

        if (stack == null || objetivo1 == null || objetivo2 == null) return;

        double actualX = stack.getLayoutX() + stack.getTranslateX();
        double actualY = stack.getLayoutY() + stack.getTranslateY();

        double dx1 = objetivo1.getX() - actualX;
        double dy1 = objetivo1.getY() - actualY;

        TranslateTransition tt1 = new TranslateTransition(Duration.seconds(3), stack);
        tt1.setToX(dx1);
        tt1.setToY(dy1);

        tt1.setOnFinished(e1 -> {
            stack.setLayoutX(objetivo1.getX());
            stack.setLayoutY(objetivo1.getY());
            stack.setTranslateX(0);
            stack.setTranslateY(0);

            double dx2 = objetivo2.getX() - stack.getLayoutX();
            double dy2 = objetivo2.getY() - stack.getLayoutY();

            TranslateTransition tt2 = new TranslateTransition(Duration.seconds(1), stack);
            tt2.setToX(dx2);
            tt2.setToY(dy2);

            tt2.setOnFinished(e2 -> {
                stack.setLayoutX(objetivo2.getX());
                stack.setLayoutY(objetivo2.getY());
                stack.setTranslateX(0);
                stack.setTranslateY(0);
                if (onFinish != null) onFinish.run();
            });

            if (dx2 == 0 && dy2 == 0) {
                if (onFinish != null) onFinish.run();
            } else {
                tt2.play();
            }
        });

        if (dx1 == 0 && dy1 == 0) {
            tt1.getOnFinished().handle(null);
        } else {
            tt1.play();
        }
    }

    // Recibe la cola completa de pedidos pendientes y actualiza las tarjetas visibles
    public void actualizarPedidosVisibles(Queue<Pedido> colaPedidos) {
        colaPendientes.clear();
        colaPendientes.addAll(colaPedidos);

        // Limpiar las tarjetas actuales
        contenedorPedidos.getChildren().clear();

        // Mostrar solo hasta 5 pedidos visibles
        int maxVisibles = 5;
        int count = 0;

        for (Pedido pedido : colaPendientes) {
            if (count >= maxVisibles) break;

            StackPane tarjetaPedido = crearTarjetaPedido(pedido);
            contenedorPedidos.getChildren().add(tarjetaPedido);
            count++;
        }
    }

    // Elimina la tarjeta y actualiza la lista visible para mostrar siguiente si hay
    public void eliminarPedido(int numeroPedido) {
        StackPane tarjetaAEliminar = null;

        for (javafx.scene.Node nodo : contenedorPedidos.getChildren()) {
            if (nodo instanceof StackPane) {
                StackPane tarjeta = (StackPane) nodo;

                Label etiqueta = null;
                for (javafx.scene.Node hijo : tarjeta.getChildren()) {
                    if (hijo instanceof Label) {
                        etiqueta = (Label) hijo;
                        break;
                    }
                }

                if (etiqueta != null && etiqueta.getText().contains("#" + numeroPedido)) {
                    tarjetaAEliminar = tarjeta;
                    break;
                }
            }
        }


        if (tarjetaAEliminar != null) {
            contenedorPedidos.getChildren().remove(tarjetaAEliminar);
        }

        if (!colaPendientes.isEmpty()) {
            colaPendientes.removeIf(p -> p.getNumeroPedido() == numeroPedido);
            actualizarPedidosVisibles(new LinkedList<>(colaPendientes));
        }


    }



    // Llamado desde JefeCocina cuando se asigna un pedido para que no aparezca en la lista visible
    public void quitarPedidoDeCola(int idPedido) {
        colaPendientes.removeIf(p -> p.getNumeroPedido() == idPedido);
    }

    private StackPane crearTarjetaPedido(Pedido pedido) {
        StackPane tarjeta = new StackPane();
        tarjeta.setPrefSize(200, 60);
        tarjeta.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");

        // Imagen (elige la que quieras, aquí ejemplo genérico)
        ImageView imagen = new ImageView((getImagenPorNombre(pedido.getNombre())));
        imagen.setFitWidth(40);
        imagen.setFitHeight(40);
        imagen.setTranslateX(-70);

        // Texto con nombre y número de pedido
        // Ahora incluyo la herramienta entre corchetes, ej: [pizza]
        String herramienta = pedido.getHerramienta().getNombre();
        Label texto = new Label(
                "#" + pedido.getNumeroPedido()
                        + " - " + pedido.getNombre()
                        + " [" + herramienta + "]"
        );


        texto.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        tarjeta.getChildren().addAll(imagen, texto);
        StackPane.setMargin(texto, new Insets(0, 0, 0, 30));

        return tarjeta;
    }

    private Image getImagenPorNombre(String nombre) {
        switch(nombre.toLowerCase()) {
            case "pizza": return new Image("pizzita.png");
            case "hamburguesa": return new Image("hamburguesita.png");
            case "taco": return new Image("taquito.png");
            case "ensalada": return new Image("ensaladita.png");
            case "pancho": return new Image("panchito.png");
            default: return new Image("pizzita.png");
        }
    }







    public static void main(String[] args) {
        launch(args);
    }


}
