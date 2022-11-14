

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HotelDate {

	public static Date parseDate(String date) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		return format.parse ( date ); 
	}
	
	public static String getDateStr(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

		return format.format( date );
	}
	
	public static String getNextDate(String dateStr) throws ParseException {
		Date date = parseDate(dateStr);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);
		return getDateStr(c.getTime());
	}
	
	public static String getPreviousDate(String dateStr) throws ParseException {
		Date date = parseDate(dateStr);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, -1);
		return getDateStr(c.getTime());
	}
	
	/**
	 * Compare two date. If dateStr1 is earlier than the other one, return -1.
	 * If dateStr1 equals dateStr2, return 0. Else return 1.
	 * @param dateStr1
	 * @param dateStr2
	 * @return
	 * @throws ParseException
	 */
	public static int compareTwoDate(String dateStr1, String dateStr2) throws ParseException{
		Date date1 = parseDate(dateStr1);
		Date date2 = parseDate(dateStr2);
		return date1.compareTo(date2);
	}
	
	public static int dayCount(String dateStart, String dateEnd) throws ParseException{
		Date date1 = parseDate(dateStart);
		Date date2 = parseDate(dateEnd);
		long diff = date2.getTime() - date1.getTime();
		return (int) (diff / 86400000);
	}
	
	public static void main(String[] args) throws ParseException {
		System.out.println(HotelDate.getNextDate("2019/04/04"));
		System.out.println(HotelDate.getPreviousDate("2019/04/04"));
		System.out.println(HotelDate.compareTwoDate("2019/04/06", "2010/06/04"));
	}
}
