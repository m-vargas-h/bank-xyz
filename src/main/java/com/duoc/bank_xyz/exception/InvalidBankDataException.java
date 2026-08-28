package com.duoc.bank_xyz.exception;

public class InvalidBankDataException extends RuntimeException {
    public InvalidBankDataException(String message) {
        super(message);
    }
}