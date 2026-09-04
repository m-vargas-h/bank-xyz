package com.duoc.bank_xyz.util;

import com.duoc.bank_xyz.exception.InvalidBankDataException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DateParser {

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String normalize(String fecha, String contexto) {
        if (fecha == null || fecha.isBlank()) {
            throw new InvalidBankDataException("Fecha nula o vacía en " + contexto);
        }
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(fecha.trim(), formatter);
                return date.format(OUTPUT);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new InvalidBankDataException("Formato de fecha no reconocido en " + contexto + ": " + fecha);
    }
}