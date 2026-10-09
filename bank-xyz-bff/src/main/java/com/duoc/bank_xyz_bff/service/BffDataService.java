package com.duoc.bank_xyz_bff.service;

import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualDto;
import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualResumenDto;
import com.duoc.bank_xyz_bff.dto.interes.InteresDto;
import com.duoc.bank_xyz_bff.dto.transaccion.TransaccionDto;
import com.duoc.bank_xyz_bff.dto.transaccion.TransaccionResumenDto;
import com.duoc.bank_xyz_bff.exception.ResourceNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@Service
public class BffDataService {

    private static final String CUENTAS = "http://ms-cuentas/api/cuentas";
    private static final String TRANSACCIONES = "http://ms-transacciones/api/transacciones";

    private static final ParameterizedTypeReference<List<TransaccionDto>> TRANSACCIONES_T = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<CuentaAnualDto>> CUENTAS_T = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<CuentaAnualResumenDto>> CUENTAS_RESUMEN_T = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<InteresDto>> INTERESES_T = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_T = new ParameterizedTypeReference<>() {};

    private final MsClient ms;

    public BffDataService(MsClient ms) {
        this.ms = ms;
    }

    // --- Transacciones ---
    public List<TransaccionDto> getTransacciones() {
        return ms.get(TRANSACCIONES, TRANSACCIONES_T);
    }

    public List<TransaccionDto> getTransaccionesByTipo(String tipo) {
        return getTransacciones().stream().filter(t -> tipo.equalsIgnoreCase(t.getTipo())).toList();
    }

    public TransaccionResumenDto getResumenTransacciones() {
        return ms.get(TRANSACCIONES + "/resumen", ParameterizedTypeReference.forType(TransaccionResumenDto.class));
    }

    // --- Cuentas anuales ---
    public List<CuentaAnualDto> getCuentasAnuales() {
        return ms.get(CUENTAS, CUENTAS_T);
    }

    public List<CuentaAnualDto> getCuentaAnualById(int cuentaId) {
        return getCuentasAnuales().stream()
                .filter(c -> c.getCuentaId() != null && c.getCuentaId() == cuentaId).toList();
    }

    public List<CuentaAnualResumenDto> getResumenCuentas() {
        return ms.get(CUENTAS + "/resumen", CUENTAS_RESUMEN_T);
    }

    // --- Intereses ---
    public List<InteresDto> getIntereses() {
        return ms.get(CUENTAS + "/intereses", INTERESES_T);
    }

    public List<InteresDto> getInteresesByTipo(String tipo) {
        return getIntereses().stream().filter(i -> tipo.equalsIgnoreCase(i.getTipo())).toList();
    }

    public List<InteresDto> getInteresByCuenta(int cuentaId) {
        return getIntereses().stream()
                .filter(i -> i.getCuentaId() != null && i.getCuentaId() == cuentaId).toList();
    }

    public List<CuentaAnualDto> getMovimientosByCuenta(int cuentaId) {
        return getCuentaAnualById(cuentaId);
    }

    // --- Retiro ATM (inicia la Saga en ms-transacciones) ---
    public Map<String, Object> registrarRetiro(int cuentaId, int monto) {
        return ms.post(TRANSACCIONES,
                Map.of("cuentaId", cuentaId, "monto", monto, "tipo", "retiro"), MAP_T);
    }

    public Map<String, Object> getEstadoRetiro(int id) {
        try {
            return ms.get(TRANSACCIONES + "/estado/" + id, MAP_T);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Transacción no encontrada: " + id);
        }
    }
}