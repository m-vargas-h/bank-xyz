package com.duoc.ms_clientes.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClientesController {

    @Value("${ms-clientes.descripcion:Microservicio de Clientes}")
    private String descripcion;

    @GetMapping
    public List<Map<String, Object>> listarClientes() {
        return List.of(
            Map.of("id", 1, "nombre", "Juan Pérez", "rut", "12.345.678-9", "email", "juan@bankxyz.cl"),
            Map.of("id", 2, "nombre", "María González", "rut", "98.765.432-1", "email", "maria@bankxyz.cl"),
            Map.of("id", 3, "nombre", "Carlos Muñoz", "rut", "11.111.111-1", "email", "carlos@bankxyz.cl")
        );
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("servicio", descripcion, "status", "UP");
    }
}