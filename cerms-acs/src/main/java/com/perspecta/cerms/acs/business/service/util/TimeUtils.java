package com.perspecta.cerms.acs.business.service.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TimeUtils {

	public static final String DATE_FORMAT = "MM-dd-yyyy";

	public static Date getCurrentDate() {
		return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public static String getCurrentDateString() {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		return formatter.format(getCurrentDate());
	}


}
