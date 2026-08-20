package com.duoc.bank_xyz.listener;

import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class BankSkipListener<T, S> implements SkipListener<T, S> {

    @Override
    public void onSkipInRead(Throwable t) {
        System.out.println("[SkipListener] Registro omitido en LECTURA: " + t.getMessage());
    }

    @Override
    public void onSkipInProcess(T item, Throwable t) {
        System.out.println("[SkipListener] Registro omitido en PROCESO: " + item
                + " | causa: " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(S item, Throwable t) {
        System.out.println("[SkipListener] Registro omitido en ESCRITURA: " + item
                + " | causa: " + t.getMessage());
    }
}