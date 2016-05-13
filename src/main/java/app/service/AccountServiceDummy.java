package app.service;

import app.entity.GridTrading;
import app.repository.GridTradingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Riky on 2016/4/30.
 */
//@Service("AccountService")
public class AccountServiceDummy implements AccountService {
    @Autowired
    GridTradingRepository gridTradingRepository;

    @Override
    public double getLastPrice(String fundCode) throws IOException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Random random = new Random();
        Double d = random.nextDouble()/50 +1;
        NumberFormat format =   java.text.NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);
        double ret = Double.valueOf( format.format(d));
        System.out.println("get last price:"+ret);
        return ret;
    }

    @Override
    public void order(Integer lastNet, String fundCode, double lastPrice, int amount, String bs) {
        GridTrading model = new GridTrading();
        model.setFund(fundCode);
        model.setPrice(lastPrice);
        model.setType(bs);
        model.setAmount(amount);
        model.setLastNet(lastNet);
        gridTradingRepository.save(model);
        System.out.println(model);
    }

    @Override
    public String getDaily(String code) throws URISyntaxException, IOException {
        return null;
    }
}
