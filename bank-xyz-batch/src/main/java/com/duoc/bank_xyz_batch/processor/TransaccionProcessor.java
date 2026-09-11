package com.duoc.bank_xyz_batch.processor;

import com.duoc.bank_xyz_batch.exception.InvalidBankDataException;
import com.duoc.bank_xyz_batch.model.Transaccion;
import com.duoc.bank_xyz_batch.util.DateParser;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TransaccionProcessor implements ItemProcessor<Transaccion, Transaccion> {

    @Override
    public Transaccion process(Transaccion transaccion) throws Exception {
        if (transaccion.getMonto() == null || transaccion.getMonto() <= 0) {
            throw new InvalidBankDataException("Transaccion anomala detectada, id: "
                    + transaccion.getId() + " monto: " + transaccion.getMonto());
        }
        if (!transaccion.getTipo().equals("credito") && !transaccion.getTipo().equals("debito")) {
            throw new InvalidBankDataException("Tipo de transaccion invalido, id: "
                    + transaccion.getId() + " tipo: " + transaccion.getTipo());
        }
        String fechaNormalizada = DateParser.normalize(transaccion.getFecha(),
                "transaccion id=" + transaccion.getId());
        transaccion.setFecha(fechaNormalizada);
        return transaccion;
    }
}