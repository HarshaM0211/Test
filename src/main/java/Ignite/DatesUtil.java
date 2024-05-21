package Ignite;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class DatesUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatesUtil.class);


    // Constructors
    // ------------------------------------------------------------------------

    private DatesUtil() {
        super();
        // Private Constructor
    }


    // Methods
    // ------------------------------------------------------------------------

    public static String getyyyyMMddFrom(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        int yr = calendar.get(Calendar.YEAR);
        int mn = calendar.get(Calendar.MONTH) + 1;
        int dt = calendar.get(Calendar.DATE);

        StringBuilder sb = new StringBuilder();
        sb.append(yr);
        sb.append(mn / 10 == 0 ? "0" + mn : mn);
        sb.append(dt / 10 == 0 ? "0" + dt : dt);

        return sb.toString();
    }

    public static long toTimestamp(String dateStr, String dateFormatStr) {
        final Date date = DatesUtil.parse(dateStr, dateFormatStr);

        if (Objects.isNull(date)) {
            throw new IllegalArgumentException("Failed to parse date");
        }

        return date.getTime();
    }

    /**
     * Parses the dates string to {@link Date} as per the passed format String.
     *
     * @param dateStr actual date in string form
     * @param dateFormatStr date format
     * 
     * @return {@link Date} object
     */
    public static Date parse(String dateStr, String dateFormatStr) {
        // Sanity checks
        if (dateFormatStr == null || !(dateFormatStr.length() > 0)) {
            throw new IllegalArgumentException("Dates::parse, Date format String should not be null or empty");
        }

        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        return DatesUtil.parseDate(dateStr, dateFormatStr);
    }


    /**
     * Generates the DateString between the given Dates.(End Date is non-inclusive)
     *
     * @param fromDate - Date Object
     * @param toDate - Date Object
     * @param dateFormatStr date format
     * 
     * @return list dateStrs between the given dates.
     */
    public static List<String> generateDateStrs(Date fromDate, Date toDate, String dateFormatStr) {
        List<String> dateStrs = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
        for (Date from = fromDate; from.before(toDate);) {
            String dateStr = dateFormat.format(from);
            dateStrs.add(dateStr);
            from = new Date(DatesUtil.addDaysToTimestamp(from.getTime(), 1));
        }

        return dateStrs;
    }


    /**
     * Generates the DateString between the given Dates, in the specified format.
     *
     * @param fromDateStr - From date in {@link String} in the specified format.
     * @param toDate - To  date in {@link String} in the specified format.
     * @param dateFormatStr - Date format in {@link String}
     * 
     * @return list dateStrs between the given dates in the specified format.
     */
    public static List<String> generateDateStrs(String fromDateStr, String toDateStr, String format, boolean includeEndDate) {
        // Sanity checks
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("#getDuration :: Date format String should not be null or empty");
        }

        if (fromDateStr == null || fromDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("#getDuration :: From date String should not be null or empty");
        }

        if (toDateStr == null || toDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("#getDuration :: To date String should not be null or empty");
        }

        // Dates
        final Date fromDate = DatesUtil.parseDate(fromDateStr, format);
        final Date toDate = DatesUtil.parseDate(toDateStr, format);

        final DateFormat dateFormat = new SimpleDateFormat(format);
        
        final List<String> dateStrs = new ArrayList<>();
        for (Date from = fromDate; from.before(toDate);) {
            String dateStr = dateFormat.format(from);
            dateStrs.add(dateStr);
            from = new Date(DatesUtil.addDaysToTimestamp(from.getTime(), 1));
        }

        if (includeEndDate) {
            String dateStr = dateFormat.format(toDate);
            dateStrs.add(dateStr);
        }
        return dateStrs;
    }


    /**
     * Adds days to input timestamp
     * 
     * @param timestamp input timestamp in milliseconds
     * @param numberOfDays No. of Days to be added to the input timestamp
     * 
     * @return the updated timestamp in milliseconds
     */
    public static Long addDaysToTimestamp(Long timestamp, int numberOfDays) {
        return timestamp + TimeUnit.DAYS.toMillis(numberOfDays);
    }


    /**
     * Computes duration in days between two dates.
     * 
     * @param startDateStr - Start date as {@link String}
     * @param endDateStr - End date as {@link String}
     * @param dateFormat - Input date format in {@link String}
     * 
     * @return duration in days
     */
    public static int getDuration(String startDateStr, String endDateStr, String dateFormat, boolean includeEndDate) {
        // Sanity checks
        if (dateFormat == null || dateFormat.trim().isEmpty()) {
            throw new IllegalArgumentException("#getDuration :: Date format String should not be null or empty");
        }

        if (startDateStr == null || startDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("#getDuration :: Start date String should not be null or empty");
        }

        if (endDateStr == null || endDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("#getDuration :: End date String should not be null or empty");
        }

        final Date startDate = DatesUtil.parseDate(startDateStr, dateFormat);
        final Date endDate = DatesUtil.parseDate(endDateStr, dateFormat);

        return DatesUtil.getDuration(startDate, endDate, includeEndDate);
    }


    /**
     * Returns <code>true</code> if start time is before end time, i.e. - forward timeline. Otherwise returns
     * <code>false</code>
     * 
     * @param startDateStr - Start Date in {@link String}
     * @param endDateStr - End Date in {@link String}
     * @param dateFormat - Format in which dates are present.
     * 
     * @return boolean
     */
    public static boolean isForwardTimeline(String startDateStr, String endDateStr, String dateFormat) {
        long startTimestamp = DatesUtil.toTimestamp(startDateStr, dateFormat);
        long endTimestamp = DatesUtil.toTimestamp(endDateStr, dateFormat);

        if (startTimestamp > endTimestamp) {
            return false;
        }

        return true;
    }


    // Private Methods
    // ------------------------------------------------------------------------

    private static Date parseDate(String dateStr, String dateFormatStr) {
        try {
            return new SimpleDateFormat(dateFormatStr).parse(dateStr);
        } catch (ParseException e) {
            String errMsg = String.format("Failed to parse the date string : %s to date", dateStr);
            LOGGER.error(errMsg, e);
        }

        return null;
    }

    private static int getDuration(Date start, Date end, boolean includeEndDate) {
        long timeDiff = end.getTime() - start.getTime();
        long totalDays = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

        if (includeEndDate) {
            totalDays++;
        }

        return Math.toIntExact(totalDays);
    }

}
