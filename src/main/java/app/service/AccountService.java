package app.service;

import java.io.IOException;

/**
 * Created by Riky on 2016/4/30.
 */
public interface AccountService {
    double getLastPrice(String fundCode) throws IOException;

    void order(Integer lastNet, String fundCode, double lastPrice, int amount, String bs);
}
