package app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by terry.wu on 2016/4/12 0012.
 */
@Service
@CacheConfig(cacheNames = "holidayCache")
public class HolidayService {
    private static final Logger log = LoggerFactory.getLogger(HolidayService.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static String[] holidays = new String[]{
            "2016-06-09","2016-06-10","2016-09-15","2016-09-16"
            ,"2016-10-03","2016-10-04","2016-10-05","2016-10-06","2016-10-07"
    };

    @Cacheable
    public boolean isTradeDayTimeByMarket() {
        boolean ret = true;
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int week = cal.get(Calendar.DAY_OF_WEEK);
        if (week == 1 || week == 7) {
            ret = false;
        }else if (hour < 9 || hour >= 15) {
            ret = false;
        }else  if (hour == 9 && minute < 10) {
            ret = false;
        }else{
            String date = sdf.format(cal.getTime());
            for(String str : holidays){
                if(date.equals(str)){
                    ret = false;
                }
            }
        }


        log.info("check isTradeDayTimeByMarket ["+ret+"]");
        return ret;
    }
}
