package com.duoc.bank_xyz.processor;

import com.duoc.bank_xyz.exception.InvalidBankDataException;
import com.duoc.bank_xyz.model.Interes;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class InteresProcessor implements ItemProcessor<Interes, Interes> {

    private static final double TASA_AHORRO = 0.03;
    private static final double TASA_PRESTAMO = 0.07;
    private static final double TASA_HIPOTECA = 0.05;

    @Override
    public Interes process(Interes interes) throws Exception {
        if (interes.getSaldo() == null || interes.getSaldo() <= 0) {
            throw new InvalidBankDataException("Cuenta sin saldo valido, cuenta_id: "
                    + interes.getCuentaId() + " saldo: " + interes.getSaldo());
        }
        if (interes.getTipo() == null || interes.getTipo().equals("-1")
                || interes.getTipo().equals("unknown")) {
            throw new InvalidBankDataException("Tipo de cuenta invalido, cuenta_id: "
                    + interes.getCuentaId() + " tipo: " + interes.getTipo());
        }

        double tasa = switch (interes.getTipo()) {
            case "ahorro"   -> TASA_AHORRO;
            case "prestamo" -> TASA_PRESTAMO;
            case "hipoteca" -> TASA_HIPOTECA;
            default -> throw new InvalidBankDataException("Tipo de cuenta no reconocido, cuenta_id: "
                    + interes.getCuentaId() + " tipo: " + interes.getTipo());
        };

        double interesCalculado = interes.getSaldo() * tasa;
        interes.setInteres(interesCalculado);
        interes.setSaldoFinal(interes.getSaldo() + interesCalculado);
        return interes;
    }
}