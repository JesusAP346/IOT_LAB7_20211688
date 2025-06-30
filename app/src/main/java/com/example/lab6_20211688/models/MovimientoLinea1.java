package com.example.lab6_20211688.models;

import com.google.firebase.Timestamp;

public class MovimientoLinea1 {
    private String id; // ID del documento en Firestore
    private String idTarjeta;
    private Timestamp fecha;         // NUEVO: fecha como Timestamp (para filtrar)
    private String fechaTexto;       // NUEVO: fecha en texto (para mostrar)
    private String estacionEntrada;
    private String estacionSalida;
    private int tiempoViaje;

    public MovimientoLinea1() {} // Constructor vacío requerido por Firestore

    public MovimientoLinea1(String idTarjeta, Timestamp fecha, String fechaTexto, String estacionEntrada, String estacionSalida, int tiempoViaje) {
        this.idTarjeta = idTarjeta;
        this.fecha = fecha;
        this.fechaTexto = fechaTexto;
        this.estacionEntrada = estacionEntrada;
        this.estacionSalida = estacionSalida;
        this.tiempoViaje = tiempoViaje;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdTarjeta() { return idTarjeta; }
    public void setIdTarjeta(String idTarjeta) { this.idTarjeta = idTarjeta; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getFechaTexto() { return fechaTexto; }
    public void setFechaTexto(String fechaTexto) { this.fechaTexto = fechaTexto; }

    public String getEstacionEntrada() { return estacionEntrada; }
    public void setEstacionEntrada(String estacionEntrada) { this.estacionEntrada = estacionEntrada; }

    public String getEstacionSalida() { return estacionSalida; }
    public void setEstacionSalida(String estacionSalida) { this.estacionSalida = estacionSalida; }

    public int getTiempoViaje() { return tiempoViaje; }
    public void setTiempoViaje(int tiempoViaje) { this.tiempoViaje = tiempoViaje; }
}
