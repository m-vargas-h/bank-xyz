package com.duoc.bank_xyz_bff.controller;

import com.duoc.bank_xyz_bff.config.SecurityConfig;
import com.duoc.bank_xyz_bff.service.BffDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MobileBffController.class)
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
class MobileBffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BffDataService dataService;

    @Test
    @WithMockUser(roles = "MOBILE")
    void getIntereses_conRolMobile_retornaOk() throws Exception {
        when(dataService.getIntereses()).thenReturn(List.of(
                Map.of("cuenta_id", 1, "saldo", 5000, "tipo", "ahorro", "extra", "ignorado")
        ));

        mockMvc.perform(get("/mobile/intereses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canal").value("mobile"))
                .andExpect(jsonPath("$.estado").value("ok"))
                .andExpect(jsonPath("$.datos[0].cuenta_id").value(1))
                .andExpect(jsonPath("$.datos[0].saldo").value(5000));
    }

    @Test
    @WithMockUser(roles = "MOBILE")
    void getCuentaById_noExiste_retorna404() throws Exception {
        when(dataService.getCuentaAnualById(9999)).thenReturn(List.of());

        mockMvc.perform(get("/mobile/cuentas/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value("error"))
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    @WithMockUser(roles = "ATM")
    void getIntereses_conRolIncorrecto_retorna403() throws Exception {
        mockMvc.perform(get("/mobile/intereses"))
                .andExpect(status().isForbidden());
    }
}