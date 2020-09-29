package com.perspecta.cerms.acs.business.service.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TimeUtils {

	public static final String DATE_FORMAT = "MM-dd-yyyy";
	public static final String DATE_FORMAT_WITH_TIME = "MM-dd-yyyy hh:mm:ss";
	public static final String DATE_FORMAT_WITH_TIME_NOSPACE = "MMddyyyyhhmmss";

	public static Date getCurrentDate() {
		return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public static Date getCurrentDateWithTime() {
		return new Date();
	}

	public static String getCurrentDateString() {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		return formatter.format(getCurrentDate());
	}

	public static String getCurrentDateWithTimeString() {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_WITH_TIME_NOSPACE);
		return formatter.format(getCurrentDateWithTime());
	}

	public static int getCurrentMonth() {
		return LocalDate.now().getMonthValue();
	}

	public static int getCurrentYear() {
		return LocalDate.now().getYear();
	}


}
