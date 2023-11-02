package com.lcm.core.utilities;

import java.time.LocalDate;
import java.util.Random;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.sql.Timestamp;
import java.util.UUID;
import java.text.SimpleDateFormat;

public class CommonUtil
{
    private static final SimpleDateFormat sdf;
    
    public static String generateUUID() {
        final UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    
    public static int generateRandomNumber() {
        return (int)(Math.random() * 100.0 + 1.0);
    }

    public static String currentTimeStamp() {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return CommonUtil.sdf.format(timestamp);
    }
    
    public static String readXMLFile(final String path) {
        final StringBuilder sb = new StringBuilder();
        try (final BufferedReader br = new BufferedReader(new FileReader(path))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                sb.append(sCurrentLine);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("IO Exception : " + e.getMessage());
        }
        return sb.toString();
    }
    
    public static Date dateNow(final String dateReq, final String format) {
        Date date = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String dateNow;
        if (dateReq == null) {
            dateNow = dateFormat.format(date.getTime());
        }
        else {
            dateNow = dateReq;
        }
        try {
            date = dateFormat.parse(dateNow);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Parser Exception : " + e.getMessage());
        }
        return date;
    }
    
    public static String getUTCCurrentDateTime(final String format) {
        final ZonedDateTime zonedDateTimeNow = ZonedDateTime.now(ZoneId.of("UTC"));
        return zonedDateTimeNow.toLocalDateTime().format(DateTimeFormatter.ofPattern(format));
    }
    
    public static Long getStartTimeOfDay(final String format) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        final Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.HOUR_OF_DAY, 0);
        Date date = null;
        try {
            date = sdf.parse(sdf.format(now.getTime()));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        return date.getTime();
    }
    
    public static boolean isUUID(final String key) {
        return key.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
    
    public static String generateDate(final int day, final String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, day);
        return sdf.format(c.getTime());
    }

    public static String lastDate(final String date, final String pattern) {
        LocalDate convertedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
        convertedDate = convertedDate.withDayOfMonth(
                convertedDate.getMonth().length(convertedDate.isLeapYear()));

        DateTimeFormatter formatters = DateTimeFormatter.ofPattern(pattern);
        return convertedDate.format(formatters);
    }

    public static String addDaysFromDate(final String date, final int days, final String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(date));

        cal.add(Calendar.DAY_OF_MONTH, days);

        return sdf.format(cal.getTime());
    }

    public static String generateString(final Random random, final String characters, final int length) {
        final char[] text = new char[length];
        for (int i = 0; i < length; ++i) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }
    
    public static int diffFromNowAndSuppliedDate(final String dateBeforeString) {
        float daysBetween = 0.0f;
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        final String dateAfterString = getUTCCurrentDateTime("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            final Date dateBefore = df.parse(dateBeforeString);
            final Date dateAfter = df.parse(dateAfterString);
            final long difference = dateAfter.getTime() - dateBefore.getTime();
            daysBetween = (float)(difference / 86400000L);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Math.round(daysBetween);
    }
    
    static {
        sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    }
}
