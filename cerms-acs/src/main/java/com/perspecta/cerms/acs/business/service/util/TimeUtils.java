package com.perspecta.cerms.acs.business.service.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TimeUtils {

	public static Date getCurrentDate() {
		return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
}
