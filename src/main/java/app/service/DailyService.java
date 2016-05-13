package app.service;

import app.entity.DailyEntity;
import app.repository.DailyEntityRepository;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

@Service
public class DailyService {
    private static final Logger log = LoggerFactory.getLogger(DailyService.class);
    @Autowired
    private DailyEntityRepository dailyEntityRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private HolidayService holidayService;

    private static String[] codes = new String[]{"878002","878003","878004","878005"};

    @Scheduled(cron = "0/30 * 9-16 * * MON-FRI")
    public void fetchDaily() {
        if(holidayService.isTradeDayTimeByMarket()){
            String day =DateUtils.formatDate(new Date(),"yyyyMMdd");
            for(String code :codes){
                //log.info("fetch code["+code+"] data");
                try {
                    String daily = accountService.getDaily(code);
                    if(!StringUtils.isEmpty(daily)){
                        String id = code+"-"+day;
                        DailyEntity obj = dailyEntityRepository.findOne(id);
                        if(obj ==null){
                            DailyEntity entity = new DailyEntity();
                            entity.setContent(daily);
                            entity.setId(id);
                            entity.setCode(code);
                            dailyEntityRepository.save(entity);
                        }else{
                            if(!obj.getContent().equals(daily)){
                                obj.setContent(daily);
                                dailyEntityRepository.save(obj);
                            }
                        }

                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
