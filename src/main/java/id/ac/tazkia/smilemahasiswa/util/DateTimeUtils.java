package id.ac.tazkia.smilemahasiswa.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for Date and DateTime operations
 */
public final class DateTimeUtils {

    private DateTimeUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Common date formats
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter INDONESIA_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    /**
     * Get current date
     * 
     * @return current LocalDate
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * Get current date time
     * 
     * @return current LocalDateTime
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * Format LocalDate to string using specified pattern
     * 
     * @param date      date to format
     * @param formatter formatter to use
     * @return formatted date string
     */
    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            return null;
        }
        return date.format(formatter);
    }

    /**
     * Format LocalDateTime to string using specified pattern
     * 
     * @param dateTime  datetime to format
     * @param formatter formatter to use
     * @return formatted datetime string
     */
    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(formatter);
    }

    /**
     * Format LocalDate to display format (dd/MM/yyyy)
     * 
     * @param date date to format
     * @return formatted date string
     */
    public static String formatDateForDisplay(LocalDate date) {
        return formatDate(date, DISPLAY_DATE);
    }

    /**
     * Format LocalDateTime to display format (dd/MM/yyyy HH:mm)
     * 
     * @param dateTime datetime to format
     * @return formatted datetime string
     */
    public static String formatDateTimeForDisplay(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DISPLAY_DATETIME);
    }

    /**
     * Parse date string using specified formatter
     * 
     * @param dateString string to parse
     * @param formatter  formatter to use
     * @return parsed LocalDate
     * @throws DateTimeParseException if parsing fails
     */
    public static LocalDate parseDate(String dateString, DateTimeFormatter formatter) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        return LocalDate.parse(dateString, formatter);
    }

    /**
     * Parse datetime string using specified formatter
     * 
     * @param dateTimeString string to parse
     * @param formatter      formatter to use
     * @return parsed LocalDateTime
     * @throws DateTimeParseException if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeString, DateTimeFormatter formatter) {
        if (StringUtils.isEmpty(dateTimeString)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    /**
     * Parse date string from display format (dd/MM/yyyy)
     * 
     * @param dateString string to parse
     * @return parsed LocalDate
     * @throws DateTimeParseException if parsing fails
     */
    public static LocalDate parseDateFromDisplay(String dateString) {
        return parseDate(dateString, DISPLAY_DATE);
    }

    /**
     * Check if a date is between two other dates (inclusive)
     * 
     * @param dateToCheck date to check
     * @param startDate   start date (inclusive)
     * @param endDate     end date (inclusive)
     * @return true if date is between start and end dates
     */
    public static boolean isBetween(LocalDate dateToCheck, LocalDate startDate, LocalDate endDate) {
        if (dateToCheck == null || startDate == null || endDate == null) {
            return false;
        }
        return !dateToCheck.isBefore(startDate) && !dateToCheck.isAfter(endDate);
    }

    /**
     * Check if a datetime is between two other datetimes (inclusive)
     * 
     * @param dateTimeToCheck datetime to check
     * @param startDateTime   start datetime (inclusive)
     * @param endDateTime     end datetime (inclusive)
     * @return true if datetime is between start and end datetimes
     */
    public static boolean isBetween(LocalDateTime dateTimeToCheck, LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        if (dateTimeToCheck == null || startDateTime == null || endDateTime == null) {
            return false;
        }
        return !dateTimeToCheck.isBefore(startDateTime) && !dateTimeToCheck.isAfter(endDateTime);
    }

    /**
     * Calculate days between two dates
     * 
     * @param startDate start date
     * @param endDate   end date
     * @return number of days between dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculate months between two dates
     * 
     * @param startDate start date
     * @param endDate   end date
     * @return number of months between dates
     */
    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    /**
     * Calculate years between two dates
     * 
     * @param startDate start date
     * @param endDate   end date
     * @return number of years between dates
     */
    public static long yearsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.YEARS.between(startDate, endDate);
    }

    /**
     * Add days to a date
     * 
     * @param date original date
     * @param days number of days to add
     * @return new date with days added
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * Add months to a date
     * 
     * @param date   original date
     * @param months number of months to add
     * @return new date with months added
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        if (date == null) {
            return null;
        }
        return date.plusMonths(months);
    }

    /**
     * Add years to a date
     * 
     * @param date  original date
     * @param years number of years to add
     * @return new date with years added
     */
    public static LocalDate addYears(LocalDate date, long years) {
        if (date == null) {
            return null;
        }
        return date.plusYears(years);
    }

    /**
     * Get the start of month for a given date
     * 
     * @param date date to get start of month for
     * @return first day of the month
     */
    public static LocalDate getStartOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1);
    }

    /**
     * Get the end of month for a given date
     * 
     * @param date date to get end of month for
     * @return last day of the month
     */
    public static LocalDate getEndOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * Get the start of year for a given date
     * 
     * @param date date to get start of year for
     * @return first day of the year
     */
    public static LocalDate getStartOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfYear(1);
    }

    /**
     * Get the end of year for a given date
     * 
     * @param date date to get end of year for
     * @return last day of the year
     */
    public static LocalDate getEndOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfYear(date.lengthOfYear());
    }

    /**
     * Check if a date is today
     * 
     * @param date date to check
     * @return true if date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(getCurrentDate());
    }

    /**
     * Check if a date is in the past
     * 
     * @param date date to check
     * @return true if date is before today
     */
    public static boolean isPast(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(getCurrentDate());
    }

    /**
     * Check if a date is in the future
     * 
     * @param date date to check
     * @return true if date is after today
     */
    public static boolean isFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(getCurrentDate());
    }
}
