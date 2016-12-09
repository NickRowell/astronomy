package time;

/**
 * My own implementation of a date class. This class represents an instance
 * in time.
 * 
 * Internally this is represented as the number of seconds that have passed
 * since 00:00:00 on Nov 17 1858. This is the zero point for the Modified
 * Julian Date.
 * 
 * We wish to parse epochs in the format "2007.10.17" or 1998.613
 * 
 * We wish to express epochs in MJD, and in year/month/day format.
 * 
 * @author nickrowell
 */
public class Epoch {
	
	 /** Difference between Julian Day & Modified Julian Day. */
    public static double MJD_CORRECTION = 2400000.5;
    
    public static double JULIAN_DAYS_PER_YEAR = 365.25;
    
    /** Modified Julian Date (units of days). */
    public double mjd;
    
    /** Conventional Gregorian date. */
    public int year;
    public int month;
    public int day;
    
    /** UTC. */
    public int hour;
    public int minute;
    public int second;
    
    /**
     * Constructor taking year in 1998.613 format.
     * @param pyear 
     */
    public Epoch(double pyear)
    {
        if(pyear < 1859.0)
            throw new RuntimeException("Negative MJD not supported");
        
        // Round to nearest year
        year = (int)Math.floor(pyear);
        
        // Get fraction of year
        double fyear = pyear - Math.floor(pyear);
        // Scale to number of days
        double fdays = isLeapYear(year) ? fyear*366 : fyear*365;
        
        // Get total number of days passed since MJD zeropoint
        mjd = getDaysPassedToYearStart(year) + fdays;
        
        // Round to nearest day
        int idays = (int)Math.floor(fdays);
        
        // Convert this to month/day. Conversion depends on leap year status.
        month = 1;
        
        while(idays > 0)
        {
            if(idays > getDaysInTheMonth(year, month))
            {
                month ++;
            }
            else
            {
                day = idays;
            }
            idays -= getDaysInTheMonth(year, month);
        }
        
        // Get UT from fraction of a day
        fdays -= Math.floor(fdays);      // residual day
        double fhour = fdays * 24;
        hour = (int)Math.floor(fhour);
        fhour -= Math.floor(fhour);      // residual hour
        double fmin = fhour * 60;
        minute = (int)Math.floor(fmin);
        fmin -= Math.floor(fmin);        // residual minute
        double fsec = fmin * 60;
        second = (int)Math.floor(fsec);        
        
    }
    
    /**
     * Constructor taking multiple fields.
     * @param pyear     Year (Gregorian)
     * @param pmonth    Month (Gregorian)
     * @param pday      Day (Gregorian)
     * @param phour      Hour (UT)
     * @param pmin       Minute (UT)
     * @param psec       Second (UT)
     */
    public Epoch(int pyear, int pmonth, int pday, int phour, int pmin, int psec)
    {
        if(pyear < 1859)
            throw new RuntimeException("Negative MJD not supported");
        
        year  = pyear;
        month = pmonth;
        day   = pday;
        
        hour = phour;
        minute = pmin;
        second = psec;
        
        // Whole number MJD. Day 1 corresponds to zero days past the start of 
        // the month, so we need to subtract 1 here.
        mjd = getDaysPassedToYearStart(year) + getDaysPassedToMonthStart(year, month) + day - 1;
        
        // Fractional part of date (UT)
        mjd += (hour + minute/60.0 + second/3600.0)/24.0;
        
    }
    
    /**
     * Get the Julian Day number for this epoch.
     * @return 
     */
    public double jd()
    {
        return mjd + MJD_CORRECTION;
    }
    
    
    /**
     * Get the total number of days passed between Nov 17 1858 and the start
     * of the year specified by the argument.
     * @param year 
     * @return  
     */
    public static int getDaysPassedToYearStart(int year)
    {
        if(year<1859) throw new RuntimeException("Year "+year+" not supported");
        
        int days = 0;
        
        // Add on days for each consecutive year
        for(int y=1859; y<year; y++) days += isLeapYear(y) ? 366 : 365;
        
        // Add on the final days of 1858: 14 from 17-30 Nov inclusive, 31 Dec.
        days += 45;
        
        return days;
    }
    
    public static int getDaysInTheMonth(int year, int month)
    {
        switch(month)
        {
            case 1:  return 31;
            case 2:  return isLeapYear(year) ? 29 : 28;
            case 3:  return 31;
            case 4:  return 30;
            case 5:  return 31;
            case 6:  return 30;
            case 7:  return 31;
            case 8:  return 31;
            case 9:  return 30;
            case 10: return 31;
            case 11: return 30;
            case 12: return 31;
            default: throw new RuntimeException("Month "+month+" unrecognized"); 
        }
    }
    
    public static int getDaysPassedToMonthStart(int year, int month)
    {
        
        int days = 0;
        
        for(int i=1; i<month; i++) days += getDaysInTheMonth(year, i);
        
        return days;
    }
    
    /**
     * Identifies leap years in the range 1801-2399.
     * @param year
     * @return 
     */
    public static boolean isLeapYear(int year)
    {
        if(year<=1800 || year>=2400)
            throw new RuntimeException("isLeapYear: Year "+year+" outside range");
        
        // Check for 100-year cases that are NOT leap years
        if(year%100 == 0)
        {
            switch(year)
            {
                case 1900: return false;
                case 2000: return true;
                case 2100: return false;
                case 2200: return false;
                case 2300: return false;
            }
        }
        
        // Remaining cases
        return (year%4 == 0);
    }
    
    /**
     * Fraction of the year past March 20th. Useful for producing parallax 
     * figures showing location of Earth in it's orbit about the sun at a
     * particular epoch. Not used in operational paralalx code.
     * @return 
     */
    public double getFractionOfYearAfterSpringEquinox()
    {
        int days_passed=0, total_days=0;
        
        // Epoch falls before this year's equinox: total number of days to next
        // equinox depends on whether THIS year is a leap year.
        if(month==3 && day <= 20)
            total_days = isLeapYear(year) ? 366 : 365;
        // Epoch falls after this year's equinox: total number of days to next
        // equinox depends on whether NEXT year is a leap year.
        else
            total_days = isLeapYear(year+1) ? 366 : 365;
        
        switch(month)
        {
            case 4:  days_passed = 11  + day; break;
            case 5:  days_passed = 41  + day; break;
            case 6:  days_passed = 72  + day; break;
            case 7:  days_passed = 102 + day; break;
            case 8:  days_passed = 133 + day; break;
            case 9:  days_passed = 164 + day; break;
            case 10: days_passed = 194 + day; break;
            case 11: days_passed = 225 + day; break;
            case 12: days_passed = 255 + day; break;
            case 1:  days_passed = 286 + day; break;
            case 2:  days_passed = 317 + day; break;
            case 3:  
                if(day<=20)
                {
                    if(isLeapYear(year))
                        days_passed = 346 + day;
                    else
                        days_passed = 345 + day;        
                }
                else
                {
                    days_passed = day-20;
                }
        }
        
        return (double)days_passed / (double)total_days;
    }
    
    
    public static String monthToString(int month)
    {
        switch(month)
        {
            case 1:  return "Jan";
            case 2:  return "Feb";
            case 3:  return "Mar";
            case 4:  return "Apr";
            case 5:  return "May";
            case 6:  return "Jun";
            case 7:  return "Jul";
            case 8:  return "Aug";
            case 9:  return "Sep";
            case 10: return "Oct";
            case 11: return "Nov";
            case 12: return "Dec";
            default: return "unrecognized month: "+month;
        }
    }
    
    @Override
    public String toString()
    {
        return year+"."+(month<10 ? "0" : "")+month+"."+(day<10 ? "0" : "")+day 
             +(hour<10 ? " 0" : " ")+hour+(minute<10 ? ":0" : ":")+minute+
              (second<10 ? ":0" : ":")+second;
    }
    
}
