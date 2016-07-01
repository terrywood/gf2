package app.service;

import app.entity.APIData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by Riky on 2016/4/30.
 */
public interface AccountService {
    Map<String,APIData> getLastPrice(String[] fundCodes) throws IOException;

    double getLastPrice(String fundCode) throws IOException;

    void order(Integer lastNet, String fundCode, double lastPrice, int amount, String bs);

    void order(String upCode, String downCode, double upPrice, double downPrice, String bs);
}
