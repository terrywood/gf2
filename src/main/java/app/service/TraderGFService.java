package app.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service("TraderService")
public class TraderGFService implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(TraderGFService.class);
    private double intPrice;
    private double grid = intPrice * 0.007;
    private int lastNet = 0;
    private int minNet = -10;
    private int volume = 1000;
    private String fundCode = "878002";

    @Autowired
    private AccountService accountService;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Scheduled(fixedDelay = 1)
    public void check() {
        long start = System.currentTimeMillis();
        if(intPrice ==0){
            try {
                intPrice = accountService.getLastPrice(fundCode);
                grid = intPrice * 0.01;
                System.out.println("initPrice[" + intPrice + "] grid[" + grid + "] lastNet[" + lastNet + "]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                double price = accountService.getLastPrice(fundCode);
                if (price > 0d) checkPrice(price);
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
                System.out.println("use ms:" + (System.currentTimeMillis() - start));
            }
        }
    }
    public void checkPrice(double lastPrice) {
        double grindPrice = grid * (lastNet) + intPrice;
        int step = new Double((lastPrice - grindPrice) / grid).intValue();
        // System.out.println("lastPrice["+lastPrice+"] gridPrice["+grindPrice+"] step["+step+"]");
        if (step > 0) {
            lastNet += step;
            if (lastNet > minNet) {
                accountService.order(lastNet,fundCode,lastPrice, Math.abs(volume * step), "2");//sell
            }
        } else if (step < 0) {
            lastNet += step;
            if (lastNet >= minNet) {
                accountService.order(lastNet,fundCode,lastPrice, Math.abs(volume * step), "1"); //buy
            }
        }
    }


}
