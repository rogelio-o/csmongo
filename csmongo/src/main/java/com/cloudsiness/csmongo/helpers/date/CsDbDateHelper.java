package com.cloudsiness.csmongo.helpers.date;

import java.util.Calendar;
import java.util.Date;

import io.vertx.core.MultiMap;

public class CsDbDateHelper {

	
	public CsDbDateHelper() {
		
	}
	
	
	public Date getDateFromRequestParams(String attribute, MultiMap params, Calendar cal) {
		String day = params.get(attribute + "_day");
		String month = params.get(attribute + "_month");
		String year = params.get(attribute + "_year");
		
		if(day == null || day.isEmpty() || month == null || month.isEmpty() || year == null || year.isEmpty()) {
			return null;
		} else {
			cal.set(Calendar.YEAR, Integer.valueOf(year));
			cal.set(Calendar.MONTH, Integer.valueOf(month));
			cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
			
			return cal.getTime();
		}
	}
	
	
	public static CsDbDateHelper instance() {
		return new CsDbDateHelper();
	}
}
