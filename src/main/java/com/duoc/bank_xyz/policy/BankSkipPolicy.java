package com.duoc.bank_xyz.policy;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.stereotype.Component;

@Component
public class BankSkipPolicy implements SkipPolicy {

    private static final int SKIP_LIMIT = 10;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if (skipCount >= SKIP_LIMIT) {
            throw new SkipLimitExceededException(SKIP_LIMIT, t);
        }
        if (t instanceof IllegalArgumentException) {
            System.out.println("[SkipPolicy] Registro omitido por dato inválido: " + t.getMessage()
                    + " | omisiones acumuladas: " + (skipCount + 1));
            return true;
        }
        if (t instanceof org.springframework.dao.DataIntegrityViolationException) {
            System.out.println("[SkipPolicy] Registro omitido por violación de integridad: " + t.getMessage()
                    + " | omisiones acumuladas: " + (skipCount + 1));
            return true;
        }
        return false;
    }
}