
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleCronJobUtil {
	
	private static Log LOG = LogFactory.getLog(SimpleCronJobUtil.class);
	
	private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(32);
	
	private SimpleCronJobUtil() {}
	
	public static void putCronJob(Runnable job, long period, TimeUnit timeUnit) {
	    SERVICE.scheduleAtFixedRate(job, period, period, timeUnit);
        LOG.info("SimpleCronJobUtil putCronJob [job:" + job.getClass().getName() 
                + ", initialDelay:" + period + ", period:" + period + ", timeUnit:" + timeUnit + "]");
	}
	
	public static void putCronJob(Runnable job, 
			int hourOfDay, int minuteOfHour, int secondOfMinite) {
		long period = 24 * 60 * 60 * 1000;
		long initialDelay = getDelay(hourOfDay, minuteOfHour, secondOfMinite);
		SERVICE.scheduleAtFixedRate(job, initialDelay, period, TimeUnit.MILLISECONDS);
		LOG.info("SimpleCronJobUtil putCronJob [job:" + job.getClass().getName() 
				+ ", initialDelay:" + initialDelay + ", period:" + period + ", timeUnit:" + TimeUnit.MILLISECONDS + "]");
		
	}
	
	private static long getDelay(int hourOfDay, int minuteOfHour, int secondOfMinite) {
		
		Calendar date = Calendar.getInstance();
		
		int currentDayOfYear = date.get(Calendar.DAY_OF_YEAR);
		
		int currentHour = date.get(Calendar.HOUR_OF_DAY);
		int currentMinute = date.get(Calendar.MINUTE);
		int currentSecond = date.get(Calendar.SECOND);

		boolean later = false;
		if (hourOfDay < currentHour) {
			later = true;
		} else if (hourOfDay == currentHour) {
			if (minuteOfHour < currentMinute) {
				later = true;
			} else if (minuteOfHour == currentSecond) {
				if (secondOfMinite < currentSecond) {
					later = true;
				}
			}
		}
		if (later) {
			date.set(Calendar.DAY_OF_YEAR, currentDayOfYear + 1);
		}

		date.set(Calendar.HOUR_OF_DAY, hourOfDay);
		date.set(Calendar.MINUTE, minuteOfHour);
		date.set(Calendar.SECOND, secondOfMinite);
		
		return date.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

	}
	
}
