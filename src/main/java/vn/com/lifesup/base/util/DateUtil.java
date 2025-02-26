package vn.com.lifesup.base.util;


import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import vn.com.lifesup.base.config.ConfigConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Log4j2
public class DateUtil {
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    private DateUtil() {
    }

    public static Instant convertToInstant(String strDate, boolean isFullDateTime) {
        Date date = convertStringToDate(strDate, isFullDateTime);
        return date == null ? null : date.toInstant();
    }

    public static String convertToString(Instant instant, boolean isFullDateTime) {
        if (instant == null) {
            return StringUtils.EMPTY;
        }
        Date date = new Date(instant.getEpochSecond() * 1000);
        return dateToString(date, isFullDateTime ? ConfigConstants.DATETIME_FORMAT : ConfigConstants.DATE_FORMAT);
    }

    public static String dateToString(Date fromDate, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(fromDate);
    }

    public static boolean validateFromDateAndToDate(String fromDateStr, String toDateStr, boolean isFullDate, long numberBetween) {
        Date fromDate = convertStringToDate(fromDateStr, isFullDate);
        Date toDate = convertStringToDate(toDateStr, isFullDate);
        boolean validated = true;
        if (fromDate != null && toDate != null) {
            if (fromDate.getTime() > toDate.getTime()) {
                validated = false;
            }
            long dayBetweenInMilliseconds = toDate.getTime() - fromDate.getTime();
            long dayBetween = TimeUnit.DAYS.convert(dayBetweenInMilliseconds, TimeUnit.MILLISECONDS);
            if (numberBetween > 0 && dayBetween > numberBetween) {
                validated = false;
            }
        }
        return validated;
    }

    /**
     * Convert string date to date date
     *
     * @param strDate String
     * @param isFullDateTime:true: full date time, false: date sort
     * @return Date
     */
    public static Date convertStringToDate(String strDate, boolean isFullDateTime) {
        if (strDate == null || StringUtils.EMPTY.equals(strDate)) {
            return null;
        }
        if (isFullDateTime) {
            if (strDate.length() != DATETIME_FORMAT.length()) {
                return null;
            }
        } else {
            if (strDate.length() != DATE_FORMAT.length()) {
                return null;
            }
        }
        try {
            Date date;
            if (isFullDateTime) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATETIME_FORMAT);
                simpleDateFormat.setLenient(false);
                date = simpleDateFormat.parse(strDate);
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
                simpleDateFormat.setLenient(false);
                date = simpleDateFormat.parse(strDate);
            }
            return date;
        } catch (ParseException e) {
            log.error("Loi! convertStringToDate: {}", e.getMessage());
        }
        return null;
    }
}
