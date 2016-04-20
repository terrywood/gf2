package app;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class Scheduled300Tasks {
    @Scheduled(fixedRate = 1)
    public void checkData() throws IOException, InterruptedException {

        long c1 = System.currentTimeMillis();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(10);
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();
        try {
            String[] urisToGet = {
                    "https://etrade.gf.com.cn/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code=878002&dse_sessionId=" + Constants.gfSession,
                    "https://etrade.gf.com.cn/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code=878003&dse_sessionId=" + Constants.gfSession
            };
            GetThread[] threads = new GetThread[2];
            for (int i = 0; i < threads.length; i++) {

                HttpGet httpget = new HttpGet(urisToGet[i]);
                httpget.addHeader("Cookie", Constants.gfCookie);
                threads[i] = new GetThread(httpclient, httpget, i + 1);
            }
            // start the threads
            for (int j = 0; j < threads.length; j++) {
                threads[j].start();
            }
            // join the threads
            for (int j = 0; j < threads.length; j++) {
                threads[j].join();
            }
            Map upData = threads[0].getData();
            Map downData = threads[1].getData();
            double upSalePrice = Double.parseDouble(upData.get("sale_price1").toString());
            double downSalePrice = Double.parseDouble(downData.get("sale_price1").toString());
            double upBuyPrice = Double.parseDouble(upData.get("buy_price1").toString());
            double downBuyPrice = Double.parseDouble(downData.get("buy_price1").toString());
            //initVariance(upSalePrice,upBuyPrice,downSalePrice,downBuyPrice);
            double buyTotal = upBuyPrice + downBuyPrice;
            double saleTotal = upSalePrice + downSalePrice;
            if (saleTotal < 1.997d) {
                this.buy(upSalePrice, downSalePrice);
                System.out.println("Buy：S+[" + String.valueOf(saleTotal) + "]　  code[878002,878003]");
                System.out.println("upData->" + upData);
                System.out.println("downData->" + downData);
                System.out.println("-------------------------------------------------");
                Thread.sleep(1000);
            } else if (buyTotal > 2.003d) {
                 //this.sale(upBuyPrice, downBuyPrice);

                System.out.println("Sale：S+[" + String.valueOf(saleTotal) + "]　B+[" + String.valueOf(buyTotal) + "] code[878002,878003]");
                // System.out.println("upData->"+upData);
                //System.out.println("downData->"+downData);
                // System.out.println("-------------------------------------------------");

                Thread.sleep(1000);
            } else {
               /* double v1 = NumberUtils.getStandardDiviation(upBuy);
                System.out.println( "---->标准差["+decimalFormat.format(v1)+"]" );*/
            }
        } finally {
            httpclient.close();
        }
        //System.out.println("use time ["+(System.currentTimeMillis()-c1)+"] MS");
    }

    public void sale(double upPrice, double downPrice) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(Constants.url);
            httpPost.addHeader("Cookie", Constants.gfCookie);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("classname", "com.gf.etrade.control.NXBUF2Control"));
            formparams.add(new BasicNameValuePair("method", "nxbdoubleentrust"));
            formparams.add(new BasicNameValuePair("dse_sessionId", Constants.gfSession));
            formparams.add(new BasicNameValuePair("fund_code", "878002"));
            formparams.add(new BasicNameValuePair("entrust_amount", "1000"));
            formparams.add(new BasicNameValuePair("entrust_price", String.valueOf(upPrice)));
            formparams.add(new BasicNameValuePair("fund_code_1", "878003"));
            formparams.add(new BasicNameValuePair("entrust_amount_1", "1000"));
            formparams.add(new BasicNameValuePair("entrust_price_1", String.valueOf(downPrice)));
            formparams.add(new BasicNameValuePair("entrust_bs", "2"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            String responseBody = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
            System.out.println("878002[" + upPrice + "] 878003[" + downPrice + "]");
            System.out.println(responseBody);
            EntityUtils.consume(entity);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            // lockSaleAction = true;
        }
        //   }
    }

    public void buy(double upPrice, double downPrice) {
        // long c = System.currentTimeMillis();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(Constants.url);
            httpPost.addHeader("Cookie", Constants.gfCookie);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("classname", "com.gf.etrade.control.NXBUF2Control"));
            formparams.add(new BasicNameValuePair("method", "nxbdoubleentrust"));
            formparams.add(new BasicNameValuePair("dse_sessionId", Constants.gfSession));
            formparams.add(new BasicNameValuePair("fund_code", "878002"));
            formparams.add(new BasicNameValuePair("entrust_amount", "1000"));
            formparams.add(new BasicNameValuePair("entrust_price", String.valueOf(upPrice)));
            formparams.add(new BasicNameValuePair("fund_code_1", "878003"));
            formparams.add(new BasicNameValuePair("entrust_amount_1", "1000"));
            formparams.add(new BasicNameValuePair("entrust_price_1", String.valueOf(downPrice)));
            formparams.add(new BasicNameValuePair("entrust_bs", "1"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            String responseBody = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
            System.out.println("878002[" + upPrice + "] 878003[" + downPrice + "]");
            System.out.println(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // lockBuyAction = true;
        }
    }

    static class GetThread extends Thread {
        Gson gson = new Gson();
        private final CloseableHttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;
        private final int id;
        private Map data;
        public Map getData() {
            return data;
        }
        public GetThread(CloseableHttpClient httpClient, HttpGet httpget, int id) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpget = httpget;
            this.id = id;
        }

        public void run() {
            try {
                CloseableHttpResponse response = httpClient.execute(httpget, context);
                try {
                    String result = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
                    Map map = gson.fromJson(result, Map.class);
                    Map data = (Map) ((List) map.get("data")).get(0);
                    System.out.println(data.get("stock_name"));
                    this.data = data;
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                System.out.println(id + " - error: " + e);
            }
        }

    }

}
