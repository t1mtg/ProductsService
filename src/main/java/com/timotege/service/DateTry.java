package com.timotege.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTry {

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse("2022-05-28T21:12:01.000Z"));
        System.out.println(now);
    }

}
