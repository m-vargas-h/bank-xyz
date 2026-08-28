package com.duoc.bank_xyz.processor;

import com.duoc.bank_xyz.exception.InvalidBankDataException;
import com.duoc.bank_xyz.model.CuentaAnual;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CuentaAnualProcessor implements ItemProcessor<CuentaAnual, CuentaAnual> {

    @Override
    public CuentaAnual process(CuentaAnual cuentaAnual) throws Exception {
        if (cuentaAnual.getMonto() == null || cuentaAnual.getMonto() <= 0) {
            throw new InvalidBankDataException("Movimiento sin monto valido, cuenta_id: "
                    + cuentaAnual.getCuentaId() + " transaccion: " + cuentaAnual.getTransaccion());
        }
        if (cuentaAnual.getTransaccion() == null
                || (!cuentaAnual.getTransaccion().equals("deposito")
                &&  !cuentaAnual.getTransaccion().equals("retiro")
                &&  !cuentaAnual.getTransaccion().equals("compra")
                &&  !cuentaAnual.getTransaccion().equals("pago"))) {
            throw new InvalidBankDataException("Tipo de transaccion invalido, cuenta_id: "
                    + cuentaAnual.getCuentaId() + " transaccion: " + cuentaAnual.getTransaccion());
        }
        return cuentaAnual;
    }
}