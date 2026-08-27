package com.duoc.bank_xyz.processor;

import com.duoc.bank_xyz.model.Transaccion;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TransaccionProcessor implements ItemProcessor<Transaccion, Transaccion> {

    @Override
    public Transaccion process(Transaccion transaccion) throws Exception {
        if (transaccion.getMonto() == null || transaccion.getMonto() <= 0) {
            System.out.println("Transaccion anomala detectada, id: " + transaccion.getId()
                    + " monto: " + transaccion.getMonto());
            return null;
        }
        return transaccion;
    }
    
}