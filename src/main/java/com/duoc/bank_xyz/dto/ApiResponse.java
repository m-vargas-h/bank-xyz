package com.duoc.bank_xyz.dto;

public class ApiResponse<T> {

    private final String canal;
    private final String estado;
    private final T datos;

    public ApiResponse(String canal, T datos) {
        this.canal = canal;
        this.estado = "ok";
        this.datos = datos;
    }

    public String getCanal() { return canal; }
    public String getEstado() { return estado; }
    public T getDatos() { return datos; }
}