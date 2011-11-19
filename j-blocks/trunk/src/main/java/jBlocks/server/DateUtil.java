package jBlocks.server;

import static java.util.Calendar.*;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Harish
 */
public class DateUtil
{
    private static GregorianCalendar _cal = new GregorianCalendar(), _cal2 = new GregorianCalendar();

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
        _cal.clear();
        _cal.set(year, month, day);

        return _cal.getTime();
    }

    public static Date addDaysTo(Date dt, int days)
    {
        _cal.clear();
        _cal.setTime(dt);
        _cal.add(DATE, days);

        return _cal.getTime();
    }

    public static boolean isSame(int field, Date dt1, Date dt2)
    {
        _cal.clear();
        _cal.setTime(dt1);

        _cal2.clear();
        _cal2.setTime(dt2);

        return _cal.get(YEAR) == _cal2.get(YEAR) && _cal.get(field) == _cal2.get(field);
    }

    public static Date lastWeekday(Date dt)
    {
        _cal.clear();
        _cal.setTime(dt);

        int dow = _cal.get(DAY_OF_WEEK);

        _cal.add(DATE, (dow == SATURDAY ? -1 : (dow == SUNDAY ? -2 : 0)));

        return _cal.getTime();
    }

    public static int get(int field, Date from)
    {
        _cal.clear();
        _cal.setTime(from);
        return _cal.get(field);
    }
}
