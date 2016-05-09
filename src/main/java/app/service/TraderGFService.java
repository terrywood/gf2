package app.service;


import app.entity.GridEntity;
import app.repository.GridEntityRepository;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service("TraderService")
public class TraderGFService implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(TraderGFService.class);
    @Autowired
    private AccountService accountService;
    @Autowired
    private GridService gridService;

    private boolean isHoliday = false;
    @Override
    public void afterPropertiesSet() throws Exception {

    }

  /*
    class Work implements Runnable{
        GridEntity entity;
        public Work(GridEntity entity) {
            this.entity = entity;
        }
        @Override
        public void run() {
            log.info("Runnable->"+entity.getFundCode());
            double intPrice = entity.getIntPrice();
            String fundCode = entity.getFundCode();
            int position = entity.getPosition();
            double grid = entity.getGrid();
            int minNet = entity.getMinNet();
            int volume = entity.getVolume();
            try {
                double lastPrice = accountService.getLastPrice(entity.getFundCode());
                if (lastPrice > 0d) {
                    double grindPrice = grid * (position) + intPrice;
                    int step = new Double((lastPrice - grindPrice) / grid).intValue();
                    // System.out.println("lastPrice[" + lastPrice + "] gridPrice[" + grindPrice + "] step[" + step + "]");
                    if (step > 0) {
                        position += step;
                        if (position > minNet) {
                            accountService.order(position, fundCode, lastPrice, Math.abs(volume * step), "2");//sell
                            entity.setPosition(position);
                            gridService.save(entity);
                        }
                    } else if (step < 0) {
                        position += step;
                        if (position >= minNet) {
                            accountService.order(position, fundCode, lastPrice, Math.abs(volume * step), "1"); //buy
                            entity.setPosition(position);
                            gridService.save(entity);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                System.out.println("sleep to 5 sec");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } finally {
            }
        }
    }
    @Scheduled(fixedDelay = 1)
    private void check() throws InterruptedException {
        long start = System.currentTimeMillis();
        if(isTradeDayTimeByMarket()){
            List<GridEntity> list=  gridService.findAll();
            ExecutorService service = Executors.newFixedThreadPool(list.size());
            for (GridEntity entity : list) {
                service.execute(new Work(entity));
            }
            service.shutdown();
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        }else{
            try {
                Thread.sleep(1000*60*10); //sleep 10 min
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("use ms:" + (System.currentTimeMillis() - start));
    }
*/


    @Scheduled(fixedDelay = 1)
    private void check() {
        long start = System.currentTimeMillis();
        if (isTradeDayTimeByMarket()) {
            List<GridEntity> list = gridService.findAll();
            for (GridEntity entity : list) {
                double intPrice = entity.getIntPrice();
                String fundCode = entity.getFundCode();
                int position = entity.getPosition();
                double grid = entity.getGrid();
                int minNet = entity.getMinNet();
                int volume = entity.getVolume();
                try {
                    double lastPrice = accountService.getLastPrice(entity.getFundCode());
                    if (lastPrice > 0d) {
                        double grindPrice = grid * (position) + intPrice;
                        int step = new Double((lastPrice - grindPrice) / grid).intValue();
                        if (step > 0) {
                            position += step;
                            if (position > minNet) {
                                accountService.order(position, fundCode, lastPrice, Math.abs(volume * step), "2");//sell
                                entity.setPosition(position);
                                gridService.save(entity);
                            }
                        } else if (step < 0) {
                            position += step;
                            if (position >= minNet) {
                                accountService.order(position, fundCode, lastPrice, Math.abs(volume * step), "1"); //buy
                                entity.setPosition(position);
                                gridService.save(entity);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log.info(e.getMessage());
                    log.info("sleep to 5 sec");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } finally {
                }

                long speed = (System.currentTimeMillis() - start);
                if (speed > 1000) {
                    log.info("use ms:" + (System.currentTimeMillis() - start));
                }
            }
        } else {
            try {
                Thread.sleep(1000 * 60 * 10); //sleep 10 min
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


/*    @Scheduled(cron = "0 1 0 ? * MON-FRI")
    private void checkIsHoliday(){
        String today = DateUtils.formatDate(new Date(),"yyyy-MM-dd");
        String[] holidays ={"2016-06-09","2016-06-10","2016-19-15","2016-09-16","2016-10-03","2016-10-04","2016-10-05","2016-10-06","2016-10-07"};
        for(String str : holidays){
            if(str.equals(today)){
                this.isHoliday = true;
                break;
            }
        }
        this.isHoliday=false;
    }*/

    public boolean isTradeDayTimeByMarket() {
     /*  if (1 == 1) {
            return true;
        }*/
       // if(isHoliday)return false;

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int week = cal.get(Calendar.DAY_OF_WEEK);
        if (week == 1 || week == 7) {
            return false;
        }
        if (hour < 9 || hour >= 15) {
            return false;
        }
        if (hour == 9 && minute < 15) {
            return false;
        }
        return true;
    }

}
