package com.duoc.bank_xyz.processor;

import com.duoc.bank_xyz.model.Interes;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class InteresProcessor implements ItemProcessor<Interes, Interes> {

    @Override
    public Interes process(Interes interes) throws Exception {
        if (interes.getSaldo() <= 0) {
            System.out.println("Cuenta sin saldo, omitiendo cuenta_id: " + interes.getCuentaId());
            return null;
        }

        double tasa = switch (interes.getTipo().toLowerCase()) {
            case "ahorro"   -> 0.03;
            case "prestamo" -> 0.05;
            case "hipoteca" -> 0.04;
            default -> {
                System.out.println("Tipo de cuenta desconocido, omitiendo cuenta_id: " + interes.getCuentaId());
                yield -1;
            }
        };

        if (tasa < 0) return null;

        double interesCalculado = interes.getSaldo() * tasa;
        interes.setSaldoFinal(interes.getSaldo() + interesCalculado);
        interes.setInteres(interesCalculado);

        return interes;
    }
}