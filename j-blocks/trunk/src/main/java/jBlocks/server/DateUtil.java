package jBlocks.server;

import static java.util.Calendar.*;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Harish
 */
public class DateUtil
{
    private DateUtil()
    {
        // Static class
    }

    /**
     * @param year
     *            Year number
     * @param month
     *            Month (0 based); 0 is January
     * @param day
     *            Day of the month
     * @return {@link Date}
     */
    public static Date create(int year, int month, int day)
    {
        GregorianCalendar cal = new GregorianCalendar();

        cal.clear();
        cal.set(year, month, day);

        return cal.getTime();
    }

    public static Date addDaysTo(Date dt, int days)
    {
        GregorianCalendar cal = new GregorianCalendar();

        cal.clear();
        cal.setTime(dt);
        cal.add(DATE, days);

        return cal.getTime();
    }

    public static boolean isSame(int field, Date dt1, Date dt2)
    {
        GregorianCalendar cal = new GregorianCalendar(), cal2 = new GregorianCalendar();

        cal.clear();
        cal.setTime(dt1);

        cal2.clear();
        cal2.setTime(dt2);

        return cal.get(YEAR) == cal2.get(YEAR) && cal.get(field) == cal2.get(field);
    }

    public static Date lastWeekday(Date dt)
    {
        GregorianCalendar cal = new GregorianCalendar();

        cal.clear();
        cal.setTime(dt);

        int dow = cal.get(DAY_OF_WEEK);

        cal.add(DATE, (dow == SATURDAY ? -1 : (dow == SUNDAY ? -2 : 0)));

        return cal.getTime();
    }

    public static int get(int field, Date from)
    {
        GregorianCalendar cal = new GregorianCalendar();

        cal.clear();
        cal.setTime(from);
        return cal.get(field);
    }
}
