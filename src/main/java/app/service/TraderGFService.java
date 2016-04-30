package app.service;


import app.entity.GridEntity;
import app.repository.GridEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


@Service("TraderService")
public class TraderGFService implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(TraderGFService.class);
    @Autowired
    private AccountService accountService;
    @Autowired
    private  GridService gridService;
    //private List<GridEntity> list ;
    @Override
    public void afterPropertiesSet() throws Exception {
     /*
        GridEntity grid = new GridEntity();
        grid.setFundCode("878002");
        grid.setPosition(0);
        grid.setMinNet(-10);
        grid.setVolume(1000);
        grid.setIntPrice(1);
        grid.setGrid(0.01);
        list.add(grid);
        gridEntityRepository.save(list);*/
     //   list = gridEntityRepository.findAll();
      /*  ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        long initialDelay1 = 1;
        long period1 = 10;*/
        // 从现在开始1秒钟之后，每隔1秒钟执行一次job1
        //service.scheduleWithFixedDelay(new ScheduledExecutorTest("878002"), initialDelay1, period1, TimeUnit.SECONDS);
    }


    @Scheduled(fixedDelay = 1000)
    private void check() {
        long start = System.currentTimeMillis();
        if(isTradeDayTimeByMarket()){
            List<GridEntity> list=  gridService.findAll();
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
        }else{
            try {
                Thread.sleep(1000*60*10); //sleep 10 min
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("use ms:" + (System.currentTimeMillis() - start));
    }

    public boolean isTradeDayTimeByMarket() {
       if (1 == 1) {
            return true;
        }
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
