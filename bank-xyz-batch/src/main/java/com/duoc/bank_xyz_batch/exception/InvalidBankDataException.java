package com.duoc.bank_xyz_batch.exception;

public class InvalidBankDataException extends RuntimeException {
    public InvalidBankDataException(String message) {
        super(message);
    }
}