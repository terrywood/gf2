package app.service;

import app.entity.GridTrading;
import app.repository.GridTradingRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Riky on 2016/4/30.
 */
//@Service("AccountService")
public class AccountServiceDummy implements AccountService,InitializingBean {
    @Autowired
    GridTradingRepository gridTradingRepository;

    int i = 0;
    List list878002  ;
    List list878004  ;
    private Gson gson = new Gson();
    @Override
    public void afterPropertiesSet() throws Exception {
        Reader reader = new FileReader("D:\\Documents\\gf2\\src\\main\\resources\\templates\\js\\878002.json");
        Reader reader3 = new FileReader("D:\\Documents\\gf2\\src\\main\\resources\\templates\\js\\878004.json");
        list878002 = gson.fromJson(reader, List.class);
        list878004 = gson.fromJson(reader3, List.class);
        System.out.println("list878004.size()-------------------------");
        System.out.println(list878004.size());
        System.out.println("list878004.size()-------------------------");
    }

    @Override
    public double getLastPrice(String fundCode) throws IOException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double ret  = 0;

        if(fundCode.equals("878004")){
            if(i<list878004.size()){
                List<Double> list  = (List<Double>) list878004.get(i);
                System.out.println("list878004.list-------------------------");
                System.out.println(list);
                ret  = list.get(1);
                i++;
            }
        }

      /*  Random random = new Random();
        Double d = random.nextDouble()/50 +1;
        NumberFormat format =   java.text.NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);
        double ret = Double.valueOf( format.format(d));
        System.out.println("get last price:"+ret);*/
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



}
