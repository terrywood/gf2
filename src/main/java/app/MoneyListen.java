package app;

import org.apache.http.HttpEntity;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Map;


public class MoneyListen implements Runnable {
    private String userAgent = TraderGFService.userAgent;
    private String cow;
    private String beer;
    public  BasicCookieStore cookieStore;
    public  String dseSessionId = null;
    public  double buy;
    public  double sale;
    PoolingHttpClientConnectionManager cm ;
    CloseableHttpClient httpclient ;
    public MoneyListen(String cow,
                       String beer
            ,BasicCookieStore cookieStore,
                       String dseSessionId,
                       double buy,
                       double sale
    ) {
        this.cow = cow;
        this.beer = beer;
        this.cookieStore = cookieStore;
        this.dseSessionId = dseSessionId;
        this.buy= buy;
        this.sale= sale;

        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(10);
        httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .setConnectionManager(cm).build();
    }

    @Override
    public void run() {
        try {
            long c2 = System.currentTimeMillis();

        /*    CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    .setConnectionManager(cm).build();
*/
            String[] urisToGet = {
                    "https://etrade.gf.com.cn/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code="+cow+"&dse_sessionId="+dseSessionId,
                    "https://etrade.gf.com.cn/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code="+beer+"&dse_sessionId="+dseSessionId
            };
            GetThread[] threads = new GetThread[2];
            HttpGet httpget = new HttpGet(urisToGet[0]);
            HttpGet httpget2 = new HttpGet(urisToGet[1]);
            threads[0] = new GetThread(httpclient, httpget,  1);
            threads[1] = new GetThread(httpclient, httpget2,  2);
            threads[0].start();
            threads[1].start();
            threads[0].join();
            threads[1].join();

            Map upData = threads[0].getData();
            Map downData = threads[1].getData();
            double upSalePrice = Double.parseDouble(upData.get("sale_price1").toString());
            double downSalePrice = Double.parseDouble(downData.get("sale_price1").toString());
            double upBuyPrice = Double.parseDouble(upData.get("buy_price1").toString());
            double downBuyPrice = Double.parseDouble(downData.get("buy_price1").toString());
            double buyTotal = upBuyPrice + downBuyPrice;
            double saleTotal = upSalePrice + downSalePrice;
            if (saleTotal < buy) {
                trading("1",upSalePrice, downSalePrice);
                System.out.println("Buy：S+[" + String.valueOf(saleTotal) + "]　  code[878002,878003]");
                System.out.println("upData->" + upData);
                System.out.println("downData->" + downData);
                System.out.println("-------------------------------------------------");
                Thread.sleep(1000);
            } else if (buyTotal > sale) {
                trading("2",upBuyPrice, downBuyPrice);
                System.out.println("Sale：S+[" + String.valueOf(saleTotal) + "]　B+[" + String.valueOf(buyTotal) + "] code[878002,878003]");
                // System.out.println("upData->"+upData);
                //System.out.println("downData->"+downData);
                // System.out.println("-------------------------------------------------");
                Thread.sleep(1000);
            }


           // trading("1",upSalePrice, downSalePrice);
           // trading("2",upBuyPrice, downBuyPrice);

            System.out.println(cow+" and "+beer+" times:" +(System.currentTimeMillis()-c2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        run();
    }



   // bs 1, buy. 2, sale
   private synchronized void trading(String bs,double upPrice, double downPrice){
      //  String dseSessionId = TraderGFService.dseSessionId;
       // BasicCookieStore cookieStore = TraderGFService.cookieStore;
      /*  CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build();*/
        try {
            HttpUriRequest trader = RequestBuilder.post()
                    .setUri(new URI("https://etrade.gf.com.cn/entry"))
                    .addParameter("classname", "com.gf.etrade.control.NXBUF2Control")
                    .addParameter("method", "nxbdoubleentrust")
                    .addParameter("dse_sessionId",dseSessionId)
                    .addParameter("entrust_price", String.valueOf(upPrice))
                    .addParameter("entrust_amount", "1000")
                    .addParameter("fund_code", cow)
                    .addParameter("fund_code_1", beer)
                    .addParameter("entrust_amount_1", "1000")
                    .addParameter("entrust_price_1", String.valueOf(downPrice))
                    .addParameter("entrust_bs", bs)
                    .build();
            CloseableHttpResponse response = httpclient.execute(trader);
            HttpEntity entity = response.getEntity();
            System.out.println("----------------------------------");
            System.out.println(EntityUtils.toString(entity));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {


        }

    }

}
