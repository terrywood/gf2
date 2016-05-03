import app.entity.APIResult;
import com.google.gson.Gson;

import java.io.IOException;

/**
 * Created by terry.wu on 2016/4/26 0026.
 */
public class PythonTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        String str = "{\"total\":1,\"error_no\":\"0\",\"data\":[{\"zssale_price2\":\"0.000\",\"stock_interest\":\"-1.00000000\",\"zssale_price3\":\"0.000\",\"zssale_price4\":\"0.000\",\"zssale_price5\":\"0.000\",\"last_price\":\"0.932\",\"open_price\":\"0.879\",\"zssale_price1\":\"0.000\",\"buy_amount5\":\"3000.00\",\"time\":\"114706\",\"buy_amount2\":\"10000.00\",\"buy_amount1\":\"6000.00\",\"buy_amount4\":\"5000.00\",\"buy_amount3\":\"20000.00\",\"svs_low_price\":\"0.8892\",\"business_amount\":\"3894000.00\",\"high_price\":\"0.937\",\"buy_price3\":\"0.929\",\"sale_price4\":\"0.937\",\"buy_price2\":\"0.930\",\"sale_price3\":\"0.936\",\"svs_last_price\":\"0.9460\",\"buy_price1\":\"0.931\",\"sale_price2\":\"0.935\",\"sale_price1\":\"0.934\",\"svs_open_price\":\"0.8970\",\"buy_price5\":\"0.927\",\"sale_price5\":\"0.938\",\"buy_price4\":\"0.928\",\"low_price\":\"0.865\",\"svs_high_price\":\"0.9494\",\"exchange_index\":\"0\",\"close_exchange_index\":\"0\",\"date\":\"20160503\",\"svs_close_price\":\"0.8941\",\"close_price\":\"0.877\",\"sale_amount4\":\"3000.00\",\"sale_amount5\":\"5000.00\",\"stock_name\":\"看涨份额\",\"sale_amount2\":\"2000.00\",\"sale_amount3\":\"1000.00\",\"sale_amount1\":\"6000.00\",\"zsbuy_amount3\":\"0.00\",\"fund_code\":\"878002\",\"zsbuy_amount2\":\"0.00\",\"zsbuy_amount5\":\"0.00\",\"zsbuy_amount4\":\"0.00\",\"zsbuy_amount1\":\"0.00\",\"zssale_amount5\":\"0.00\",\"zsbuy_price1\":\"0.000\",\"zssale_amount3\":\"0.00\",\"zssale_amount4\":\"0.00\",\"zsbuy_price2\":\"0.000\",\"zssale_amount1\":\"0.00\",\"zsbuy_price3\":\"0.000\",\"business_balance\":\"3537481.000\",\"zssale_amount2\":\"0.00\",\"zsbuy_price4\":\"0.000\",\"zsbuy_price5\":\"0.000\",\"success\":true}],\"error_info\":\"\",\"success\":true}";
        {
            long begin = System.currentTimeMillis();
            Gson gson = new Gson();
            APIResult obj = gson.fromJson(str, APIResult.class);
            System.out.println(obj);

     /*      Map map = gson.fromJson(str, Map.class);
            Map data = (Map) ((List) map.get("data")).get(0);
            System.out.println(MapUtils.getDouble(data, "last_price"))*/
            ;
            System.out.println((System.currentTimeMillis() - begin));
        }



    }


}
