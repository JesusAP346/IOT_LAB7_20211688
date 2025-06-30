package com.example.lab6_20211688.models;

public class MovimientoLimaPass {
    private String id;
    private String idTarjeta;
    private String fecha;
    private String paraderoEntrada;
    private String paraderoSalida;
    private int tiempoViaje;
    // y su getter/setter:
    public int getTiempoViaje() { return tiempoViaje; }
    public void setTiempoViaje(int tiempoViaje) { this.tiempoViaje = tiempoViaje; }


    public MovimientoLimaPass() {} // Necesario para Firebase

    public MovimientoLimaPass(String idTarjeta, String fecha, String paraderoEntrada, String paraderoSalida) {
        this.idTarjeta = idTarjeta;
        this.fecha = fecha;
        this.paraderoEntrada = paraderoEntrada;
        this.paraderoSalida = paraderoSalida;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdTarjeta() { return idTarjeta; }
    public void setIdTarjeta(String idTarjeta) { this.idTarjeta = idTarjeta; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getParaderoEntrada() { return paraderoEntrada; }
    public void setParaderoEntrada(String paraderoEntrada) { this.paraderoEntrada = paraderoEntrada; }

    public String getParaderoSalida() { return paraderoSalida; }
    public void setParaderoSalida(String paraderoSalida) { this.paraderoSalida = paraderoSalida; }
}
