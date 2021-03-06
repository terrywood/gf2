package app.service;


import app.entity.APIData;
import app.entity.GridEntity;
import app.repository.GridEntityRepository;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
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
    @Autowired
    private HolidayService holidayService;

    //ExecutorService service ;
    @Override
    public void afterPropertiesSet() throws Exception {
        //  service = Executors.newFixedThreadPool(10);
    }


    @Scheduled(fixedDelay = 1)
    private void job() throws IOException {
        if (holidayService.isTradeDayTimeByMarket()) {

            String codes[] = new String[]{"878002", "878003"};

            carry(codes);
            carry(new String[]{"878004", "878005"});

            grid();

        }else {
            log.info("sleep to 10 min");
            try {
                Thread.sleep(1000 * 60 * 10); //sleep 10 min
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    private void carry(String[] codes) {
        try {
            //String codes[] = new String[]{"878002", "878003"};
            Map<String, APIData> map = accountService.getLastPrice(codes);
            APIData upData = map.get(codes[0]);
            APIData downData = map.get(codes[1]);
            if (upData != null && downData != null) {
                double upSalePrice = upData.getSale_price1();
                double downSalePrice = downData.getSale_price1();
                double upBuyPrice = upData.getBuy_price1();
                double downBuyPrice = downData.getBuy_price1();
              //  double upLastPrice = upData.getLast_price();
              //  double downloadLastPrice = downData.getLast_price();
                double saleTotal = upSalePrice + downSalePrice;
                double buyTotal = upBuyPrice + downBuyPrice;
                //double lastTotal = upLastPrice + downloadLastPrice;
                if (saleTotal < 1.998d) {
                    this.accountService.order(codes[0], codes[1], upSalePrice, downSalePrice, "1");
                } else if (buyTotal > 2.003d) {
                    this.accountService.order(codes[0], codes[1], upBuyPrice, downBuyPrice, "2");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void grid() {
            long start = System.currentTimeMillis();
            List<GridEntity> list = gridService.findAll();
            for (GridEntity entity : list) {
                double intPrice = entity.getIntPrice();
                String fundCode = entity.getFundCode();
                int position = entity.getPosition();
                double grid = entity.getGrid();
                int minNet = entity.getMinNet();
                int maxNet = entity.getMaxNet();
                int volume = entity.getVolume();
                double lastPrice = 0;
                try {
                    lastPrice = accountService.getLastPrice(entity.getFundCode());
                    //log.info("lastPrice["+lastPrice+"]");
                    if (lastPrice > 0d) {
                        double grindPrice = grid * (position) + intPrice;
                        int step = new Double((lastPrice - grindPrice) / grid).intValue();
                        if (step > 0) {
                            position += step;
                            if (position > minNet && position < maxNet) {
                                accountService.order(position, fundCode, lastPrice, Math.abs(volume * step), "2");//sell
                            }
                            entity.setPosition(position);
                            gridService.save(entity);
                        } else if (step < 0) {
                            position += step;
                            if (position >= minNet && position <= maxNet) {
                                accountService.order(position, fundCode, lastPrice, Math.abs(volume * step), "1"); //buy
                            }
                            entity.setPosition(position);
                            gridService.save(entity);
                        }
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                    log.info("sleep 5 sec on error: " + e.getMessage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } finally {

                }
            }
    }

}
