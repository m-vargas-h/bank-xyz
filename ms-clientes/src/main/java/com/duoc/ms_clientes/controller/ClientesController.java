package com.duoc.ms_clientes.controller;

import com.duoc.ms_clientes.services.ClientesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/clientes")
public class ClientesController {

    @Autowired
    private ClientesService clientesService;

    @GetMapping
    public List<Map<String, Object>> listarClientes() {
        return clientesService.listarClientes();
    }

    @GetMapping("/intereses")
    public List<Map<String, Object>> listarIntereses() {
        return clientesService.listarInteresesCliente();
    }

    @GetMapping("/resilience")
    public CompletableFuture<List<Map<String, Object>>> getClientesResiliencia() {
        return clientesService.getClientesConResiliencia();
    }

    @GetMapping("/ratelimit")
    public List<Map<String, Object>> getClientesRateLimit() {
        return clientesService.getClientesConRateLimit();
    }
}