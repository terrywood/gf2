package app.service;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Riky on 2016/4/30.
 */
public interface AccountService {
    double getLastPrice(String fundCode) throws IOException;

    void order(Integer lastNet, String fundCode, double lastPrice, int amount, String bs);
     String getDaily(String code) throws URISyntaxException, IOException;
}
