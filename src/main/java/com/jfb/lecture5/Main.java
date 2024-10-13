package com.jfb.lecture5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfb.lecture5.model.BusTicket;
import com.jfb.lecture5.model.enums.TicketType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class Main {
    private static Byte totalTickets = 0;
    private static Byte validTickets = 0;
    private static Byte ticketTypeViolationsCount = 0;
    private static Byte startDateViolationsCount = 0;
    private static Byte priceViolationsCount = 0;
    private static String mostPopularViolation;

    private static void readAndCheckTickets() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\resources\\ticketData.txt"))) {
            String line = reader.readLine();
            while (line != null && !line.isBlank()) {
                Optional<BusTicket> busTicket = convertToBusTicket(line);
                if (busTicket.isEmpty()) {
                    line = reader.readLine();
                    continue;
                }
                checkTicket(busTicket.get());
                line = reader.readLine();
            }
            findMostPopularViolation();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void findMostPopularViolation() {
        if (ticketTypeViolationsCount.compareTo(startDateViolationsCount) > 0 && ticketTypeViolationsCount.compareTo(priceViolationsCount) > 0) {
            mostPopularViolation = "ticket type";
        }
        if (startDateViolationsCount.compareTo(ticketTypeViolationsCount) > 0 && startDateViolationsCount.compareTo(priceViolationsCount) > 0) {
            mostPopularViolation = "start date";
        }
        if (priceViolationsCount.compareTo(ticketTypeViolationsCount) > 0 && priceViolationsCount.compareTo(startDateViolationsCount) > 0) {
            mostPopularViolation = "price";
        }
    }

    public static void main(String[] args) {
        readAndCheckTickets();
        System.out.printf("""
                Total = %s
                Valid = %s
                Most popular violation = %s""", totalTickets, validTickets, mostPopularViolation);
    }

    private static void checkTicket(BusTicket ticket) {
        boolean isTicketTypeValid = ticketTypeValidation(ticket.getTicketType(), ticket.getStartDate());
        boolean isStartDateValid = startDateValidation(ticket.getStartDate());
        boolean isPriceValid = priceValidation(ticket.getPrice());

        if (isTicketTypeValid && isStartDateValid && isPriceValid) {
            validTickets++;
        }
        totalTickets++;
    }

    private static boolean ticketTypeValidation(String ticketType, String startDate) {
        Optional<TicketType> type = parseTicketType(ticketType);
        if (type.isPresent() && type.get() != TicketType.MONTH) {
            if (startDate == null || startDate.isBlank()) {
                ticketTypeViolationsCount++;
                System.out.println("DAY, WEEK and YEAR types must have a start date");
                return false;
            }
            return true;
        }
        return false;
    }

    private static boolean startDateValidation(String startDate) {
        if (startDate == null) {
            return true;
        }
        Optional<LocalDate> start = parseStartDate(startDate);
        LocalDate now = LocalDate.now();
        if (start.isPresent()) {
            if (start.get().isAfter(now)) {
                startDateViolationsCount++;
                System.out.println("Start date can’t be in the future");
                return false;
            }
            return true;
        }
        return false;
    }

    private static boolean priceValidation(String priceStr) {
        if (priceStr == null) {
            return true;
        }
        Optional<Integer> price = parsePrice(priceStr);
        if (price.isPresent()) {
            if (price.get() == 0) {
                priceViolationsCount++;
                System.out.println("Price can’t be zero");
                return false;
            }
            if (price.get() % 2 != 0) {
                priceViolationsCount++;
                System.out.println("Price should always be even");
                return false;
            }
            return true;
        }
        return false;
    }

    private static Optional<BusTicket> convertToBusTicket(String line) {
        Optional<BusTicket> busTicket;
        try {
            busTicket = Optional.of(new ObjectMapper().readValue(line, BusTicket.class));
        } catch (Exception ex) {
            System.out.println("Unable to convert to type BusTicket");
            return Optional.empty();
        }
        return busTicket;
    }

    private static Optional<TicketType> parseTicketType(String ticketType) {
        Optional<TicketType> type;
        try {
            type = Optional.of(TicketType.valueOf(ticketType));
        } catch (Exception ex) {
            ticketTypeViolationsCount++;
            System.out.println("Ticket type valid values should be DAY, WEEK, MONTH, YEAR");
            return Optional.empty();
        }
        return type;
    }

    private static Optional<LocalDate> parseStartDate(String startDate) {
        Optional<LocalDate> start;
        try {
            start = Optional.of(LocalDate.parse(startDate));
        } catch (Exception e) {
            startDateViolationsCount++;
            System.out.println("Ticket startDate values should be format [yyyy-MM-dd]");
            return Optional.empty();
        }
        return start;
    }

    private static Optional<Integer> parsePrice(String priceStr) {
        Optional<Integer> price;
        try {
            price = Optional.of(Integer.parseInt(priceStr));
        } catch (Exception ex) {
            priceViolationsCount++;
            System.out.println("Price should be a number");
            return Optional.empty();
        }
        return price;
    }
}