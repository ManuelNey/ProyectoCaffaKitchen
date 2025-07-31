package Clases.Herramientas;

public interface IHerramienta {

    public void pedir() throws InterruptedException;

    public void liberar();

    public String getNombre();

    void dibujarProceso(int duracionMs) throws InterruptedException;

}