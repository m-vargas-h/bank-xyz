package com.duoc.bank_xyz_bff.controller;

import com.duoc.bank_xyz_bff.config.SecurityConfig;
import com.duoc.bank_xyz_bff.dto.interes.InteresDto;
import com.duoc.bank_xyz_bff.exception.ResourceNotFoundException;
import com.duoc.bank_xyz_bff.service.BffDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtmBffController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "server.ssl.enabled=false",
        "bff.security.web.user=web_user",
        "bff.security.web.password=web_pass_2024",
        "bff.security.mobile.user=mobile_user",
        "bff.security.mobile.password=mobile_pass_2024",
        "bff.security.atm.user=atm_user",
        "bff.security.atm.password=atm_pass_2024"
})
class AtmBffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BffDataService dataService;

    @Test
    @WithMockUser(roles = "ATM")
    void getSaldo_conRolAtm_retornaOk() throws Exception {
        InteresDto i = new InteresDto();
        i.setCuentaId(1L);
        i.setSaldo(new BigDecimal("8000"));
        i.setTipo("corriente");

        when(dataService.getInteresByCuenta(1)).thenReturn(List.of(i));

        mockMvc.perform(get("/atm/saldo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canal").value("atm"))
                .andExpect(jsonPath("$.estado").value("ok"))
                .andExpect(jsonPath("$.datos[0].saldo").value(8000));
    }

    @Test
    @WithMockUser(roles = "ATM")
    void getSaldo_cuentaNoExiste_retorna404() throws Exception {
        when(dataService.getInteresByCuenta(9999)).thenReturn(List.of());

        mockMvc.perform(get("/atm/saldo/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value("error"))
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    @WithMockUser(roles = "WEB")
    void getSaldo_conRolIncorrecto_retorna403() throws Exception {
        mockMvc.perform(get("/atm/saldo/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ATM")
    void retiro_valido_retorna202() throws Exception {
        when(dataService.registrarRetiro(101, 5000))
                .thenReturn(Map.of("id", 42, "monto", 5000, "estado", "PENDIENTE"));

        mockMvc.perform(post("/atm/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cuentaId\":101,\"monto\":5000}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.canal").value("atm"))
                .andExpect(jsonPath("$.datos.id").value(42))
                .andExpect(jsonPath("$.datos.estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser(roles = "ATM")
    void retiro_montoInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/atm/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cuentaId\":101,\"monto\":-10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    @WithMockUser(roles = "ATM")
    void retiro_sinCuenta_retorna400() throws Exception {
        mockMvc.perform(post("/atm/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":5000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    @WithMockUser(roles = "WEB")
    void retiro_conRolIncorrecto_retorna403() throws Exception {
        mockMvc.perform(post("/atm/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cuentaId\":101,\"monto\":5000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ATM")
    void estadoRetiro_existente_retornaOk() throws Exception {
        when(dataService.getEstadoRetiro(42)).thenReturn(Map.of(
                "id", 42, "cuenta_id", 101, "monto", 5000,
                "estado", "FALLIDA", "motivo", "Saldo insuficiente"));

        mockMvc.perform(get("/atm/retiro/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estado").value("FALLIDA"))
                .andExpect(jsonPath("$.datos.motivo").value("Saldo insuficiente"));
    }

    @Test
    @WithMockUser(roles = "ATM")
    void estadoRetiro_noExiste_retorna404() throws Exception {
        when(dataService.getEstadoRetiro(999))
                .thenThrow(new ResourceNotFoundException("Transacción no encontrada: 999"));

        mockMvc.perform(get("/atm/retiro/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }
}