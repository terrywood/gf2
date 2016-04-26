package app;


import com.google.gson.Gson;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.Map;


@Service("TraderService")
public class TraderGFService implements  InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(TraderGFService.class);
    public   String userAgent = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)";
    public   BasicCookieStore cookieStore;
    public   String dseSessionId = null;
    public   String domain = "https://etrade.gf.com.cn";
    // ExecutorService pool = Executors.newFixedThreadPool(4);


    private double intPrice;
    private double grid = intPrice*0.007;
    private int lastNet =0;
    private int minNet = -10;
    private int volume =1000;
    private  String fundCode ="878002";

    @Override
    public void afterPropertiesSet() throws Exception {
        this.cookieStore = new BasicCookieStore();
        if(login()){
            intPrice = getLastPrice();
            grid = intPrice*0.01;
            System.out.println("initPrice["+intPrice+"] grid["+grid+"]");
            //check();
         /*   pool.execute(new MoneyListen("878002","878003",cookieStore,dseSessionId,1.997d,2.003d));
            pool.execute(new MoneyListen("878004","878005",cookieStore,dseSessionId,1.997d,2.003d));
            balance();*/
        }
    }

    @Scheduled(fixedDelay = 10)
    public void check() {
        long start = System.currentTimeMillis();
        try {
            double price = getLastPrice();
            if(price>0d) checkPrice(price);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("sleep to 5 sec");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }finally {
            System.out.println("use ms:"+ (System.currentTimeMillis()-start));

        }


    }

    public void  checkPrice(double lastPrice){

        double curPrice = grid*(lastNet) +intPrice;
        int step = new Double((lastPrice - curPrice)/grid).intValue();
        // System.out.println("lastPrice["+lastPrice+"] gridPrice["+curPrice+"] step["+step+"]");
        if(step>0){
            lastNet+=step;
            if(lastNet>minNet){
                order(lastPrice,Math.abs(volume*step),"2");//sell
            }
        }else if(step<0){
            lastNet+=step;
            if(lastNet>=minNet){
                order(lastPrice,Math.abs(volume*step),"1"); //buy
            }
        }


    }

    public  double getLastPrice() throws IOException {
        Gson gson = new Gson();
        String httpUrl =domain+"/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code="+fundCode+"&dse_sessionId="+dseSessionId;

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build();
        HttpGet httpGet = new HttpGet(httpUrl);
        CloseableHttpResponse response =  httpclient.execute(httpGet);
        Map map = gson.fromJson(IOUtils.toString( response.getEntity().getContent(), Consts.UTF_8), Map.class);
        Map data = (Map)((List) map.get("data")).get(0);
        return MapUtils.getDouble(data,"last_price");

    }

    public void  order(double lastPrice, int amount, String bs){
        String httpUrl =domain+"/entry?entrust_bs="+bs+"&auto_deal=true&classname=com.gf.etrade.control.NXBUF2Control&method=nxbentrust&fund_code="+fundCode+"&dse_sessionId="+dseSessionId+"&entrust_price="+lastPrice+"&entrust_amount="+amount;
        try {
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    .build();
            HttpGet httpGet = new HttpGet(httpUrl);
            CloseableHttpResponse response =  httpclient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());

            System.out.println(result);

          /*  GridTrading model = new GridTrading();
            model.setFund(fundCode);
            model.setPrice(lastPrice);
            model.setLogTime(new Date());
            model.setType(bs);
            model.setAmount(amount);
            model.setLastNet(lastNet);
            gridTradingDao.save(model);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
    public void balance(){
       String url ="https://etrade.gf.com.cn/entry?classname=com.gf.etrade.control.StockUF2Control&method=queryFund&dse_sessionId="+dseSessionId+"&_dc=1461512091606";
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response3 = null;
        try {
            response3 = httpclient.execute(httpGet);
            HttpEntity entity3 = response3.getEntity();
            System.out.println(EntityUtils.toString(entity3));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        balance();
    }*/

    public boolean login() {
        boolean isOk = false;
        try {
            long start = System.currentTimeMillis();
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    .build();
            try {
                HttpGet httpGet = new HttpGet(domain+"/yzm.jpgx");
                CloseableHttpResponse response3 = httpclient.execute(httpGet);
                HttpEntity entity3 = response3.getEntity();
                File file = new File("d:/gf.jpg");
                FileUtils.copyInputStreamToFile(entity3.getContent(),file);
                // BufferedImage image = ImageIO.read(entity3.getContent());
                EntityUtils.consume(entity3);
                String capthca = null;
                {
                    InputStream inputStream = System.in;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    System.out.println("请输入验证码:");
                    capthca = bufferedReader.readLine();

                    HttpUriRequest login = RequestBuilder.post()
                            .setUri(new URI(domain+"/login"))
                            .addParameter("username", "*1B*8DJo*0FJd*D9*28rq*5E*FF*8Fj*9EG*97*883*91G*16bw*22*A05*A8*CCL8G*97*883*91G*16bw*22*A05*A8*CCL8G*97*883*91G*16bw*22*A05*A8*CCL8*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00")
                            .addParameter("password", "*E1*B58F*23*B7*C6*2E*05*3F*E6*5D*09*C2*122G*97*883*91G*16bw*22*A05*A8*CCL8G*97*883*91G*16bw*22*A05*A8*CCL8G*97*883*91G*16bw*22*A05*A8*CCL8*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00*00")
                            .addParameter("tmp_yzm", capthca)
                            .addParameter("authtype", "2")
                            .addParameter("mac", "B4-6D-83-6C-A8-09,192.168.2.2")
                            .addParameter("disknum", "S30YJ9JG914606")
                            .addParameter("loginType", "2")
                            .addParameter("origin", "web")
                            .build();
                    CloseableHttpResponse response2 = httpclient.execute(login);
                    try {
                        HttpEntity entity = response2.getEntity();
                        //  System.out.println("Login form get: " + response2.getStatusLine());
                        //   String result = IOUtils.toString(entity.getContent(), "UTF-8");
                        System.out.println("result:" + EntityUtils.toString(entity));
                        EntityUtils.consume(entity);
                        System.out.println("Post logon cookies:");
                        List<Cookie> cookies = cookieStore.getCookies();
                        if (cookies.isEmpty()) {
                            System.out.println("None");
                        } else {
                            for (int i = 0; i < cookies.size(); i++) {
                                String name = cookies.get(i).getName();
                                if(name.equals("dse_sessionId")){
                                    dseSessionId =  cookies.get(i).getValue();
                                }
                                //cookieStore.addCookie(cookies.get(i));
                                System.out.println("- " + cookies.get(i).toString());
                            }
                            isOk = true;
                        }
                    } finally {
                        response2.close();
                    }
                }
            } finally {
                httpclient.close();
            }
            long end = System.currentTimeMillis() - start;
            log.info("use times :" + end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOk;
    }


/*
    public static void main(String[] args) throws ParseException, IOException {
        //BasicConfigurator.configure();
        String log4jConfPath = "D:\\dev\\workspace\\gf2\\src\\main\\resources\\log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);

        TraderGFService service = new TraderGFService();
        try {
            service.afterPropertiesSet();
         *//*   service.login();
            service.balance();*//*
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/

 /*   @CacheEvict(value="trader",key="#id")
    public void delete(Long id){
        traderRepository.delete(id);
    }*/
}
