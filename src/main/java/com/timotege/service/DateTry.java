package com.timotege.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

public class DateTry {

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        //LocalDateTime.parse(now, DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL));
    }

}
