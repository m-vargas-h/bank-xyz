package com.duoc.bank_xyz.policy;

import com.duoc.bank_xyz.exception.InvalidBankDataException;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class BankSkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if (t instanceof FlatFileParseException) {
            System.out.println("[SkipPolicy] Error de lectura CSV, omitiendo registro: " + t.getMessage());
            return true;
        }
        if (t instanceof InvalidBankDataException) {
            System.out.println("[SkipPolicy] Dato invalido, omitiendo registro: " + t.getMessage());
            return true;
        }
        if (t instanceof IllegalArgumentException) {
            System.out.println("[SkipPolicy] Argumento invalido, omitiendo registro: " + t.getMessage());
            return true;
        }
        if (t instanceof DataIntegrityViolationException) {
            System.out.println("[SkipPolicy] Error de integridad en BD, omitiendo registro: " + t.getMessage());
            return true;
        }
        return false;
    }
}