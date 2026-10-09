package com.duoc.bank_xyz_bff.controller;

import com.duoc.bank_xyz_bff.dto.ApiResponse;
import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualMobileDto;
import com.duoc.bank_xyz_bff.dto.interes.InteresMobileDto;
import com.duoc.bank_xyz_bff.dto.resumen.ResumenAtmDto;
import com.duoc.bank_xyz_bff.dto.retiro.RetiroEstadoDto;
import com.duoc.bank_xyz_bff.dto.retiro.RetiroRequestDto;
import com.duoc.bank_xyz_bff.dto.retiro.RetiroResponseDto;
import com.duoc.bank_xyz_bff.dto.vista.VistaCuentaAtmDto;
import com.duoc.bank_xyz_bff.exception.ResourceNotFoundException;
import com.duoc.bank_xyz_bff.service.BffDataService;
import com.duoc.bank_xyz_bff.dto.cuenta.SaldoAtmDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/atm")
public class AtmBffController {

    private final BffDataService dataService;

    public AtmBffController(BffDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/saldo/{cuentaId}")
    public ResponseEntity<ApiResponse<SaldoAtmDto>> getSaldo(@PathVariable int cuentaId) {
        var r = dataService.getSaldoCuenta(cuentaId);
        var dto = new SaldoAtmDto(
                ((Number) r.get("cuenta_id")).intValue(),
                (String) r.get("titular"),
                ((Number) r.get("saldo")).longValue(),
                (String) r.get("estado"));
        return ResponseEntity.ok(new ApiResponse<>("atm", dto));
    }

    @GetMapping("/transacciones/{cuentaId}")
    public ResponseEntity<ApiResponse<List<CuentaAnualMobileDto>>> getTransacciones(@PathVariable int cuentaId) {
        var data = dataService.getCuentaAnualById(cuentaId);
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin transacciones para cuenta: " + cuentaId);
        List<CuentaAnualMobileDto> reducida = data.stream()
                .map(c -> new CuentaAnualMobileDto(c.getCuentaId(), c.getMonto(), c.getTransaccion()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("atm", reducida));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<ResumenAtmDto>> getResumen() {
        var full = dataService.getResumenTransacciones();
        return ResponseEntity.ok(new ApiResponse<>("atm",
                new ResumenAtmDto(full.getTotalProcesadas(), full.getTotalAnomalias())));
    }

    @GetMapping("/cuentas/{cuentaId}/detalle")
    public ResponseEntity<ApiResponse<VistaCuentaAtmDto>> getVistaCuenta(@PathVariable int cuentaId) {
        List<InteresMobileDto> intereses = dataService.getInteresByCuenta(cuentaId).stream()
                .map(i -> new InteresMobileDto(i.getCuentaId(), i.getSaldo(), i.getTipo()))
                .collect(Collectors.toList());
        if (intereses.isEmpty())
            throw new ResourceNotFoundException("Sin datos para cuenta: " + cuentaId);
        return ResponseEntity.ok(new ApiResponse<>("atm", new VistaCuentaAtmDto(intereses)));
    }

    @PostMapping("/retiro")
    public ResponseEntity<ApiResponse<RetiroResponseDto>> retirar(@RequestBody RetiroRequestDto req) {
        if (req.getCuentaId() == null || req.getCuentaId() <= 0)
            throw new IllegalArgumentException("cuentaId es obligatorio");
        if (req.getMonto() == null || req.getMonto() <= 0)
            throw new IllegalArgumentException("El monto debe ser mayor a 0");

        var r = dataService.registrarRetiro(req.getCuentaId(), req.getMonto());
        var dto = new RetiroResponseDto(
                ((Number) r.get("id")).intValue(),
                ((Number) r.get("monto")).intValue(),
                (String) r.get("estado"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse<>("atm", dto));
    }

    @GetMapping("/retiro/{id}")
    public ResponseEntity<ApiResponse<RetiroEstadoDto>> getEstadoRetiro(@PathVariable int id) {
        var r = dataService.getEstadoRetiro(id);
        var dto = new RetiroEstadoDto(
                ((Number) r.get("id")).intValue(),
                ((Number) r.get("cuenta_id")).intValue(),
                ((Number) r.get("monto")).intValue(),
                (String) r.get("estado"),
                (String) r.get("motivo"));
        return ResponseEntity.ok(new ApiResponse<>("atm", dto));
    }
}