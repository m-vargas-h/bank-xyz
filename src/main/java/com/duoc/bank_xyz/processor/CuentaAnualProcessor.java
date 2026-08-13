package com.duoc.bank_xyz.processor;

import com.duoc.bank_xyz.model.CuentaAnual;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CuentaAnualProcessor implements ItemProcessor<CuentaAnual, CuentaAnual> {

    @Override
    public CuentaAnual process(CuentaAnual cuentaAnual) throws Exception {
        if (cuentaAnual.getMonto() <= 0) {
            System.out.println("Movimiento sin monto, omitiendo cuenta_id: " + cuentaAnual.getCuentaId()
                    + " transaccion: " + cuentaAnual.getTransaccion());
            return null;
        }
        return cuentaAnual;
    }
}