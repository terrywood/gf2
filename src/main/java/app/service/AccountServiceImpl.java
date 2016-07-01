package app.service;

import app.CommandUtils;
import app.entity.APIData;
import app.entity.APIResult;
import app.entity.GridTrading;
import app.repository.GridTradingRepository;
import com.google.gson.Gson;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by terry.wu on 2016/4/29 0029.
 */
@Service("AccountService")
public class AccountServiceImpl implements AccountService, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
    public String userAgent = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)";
    public BasicCookieStore cookieStore = new BasicCookieStore();
    public String dseSessionId = null;
    public String domain = "https://trade.gf.com.cn";
    @Autowired
    private GridTradingRepository gridTradingRepository;

    public boolean connected = false;

    private Gson gson = new Gson();


    static class GetThread extends Thread {
        private final CloseableHttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;
        private final int id;
        private APIData data;

        public APIData getData() {
            return data;
        }

        private Gson gson = new Gson();

        public GetThread(CloseableHttpClient httpClient, HttpGet httpget, int id) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpget = httpget;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                CloseableHttpResponse response = httpClient.execute(httpget, context);
                String result = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
                try {
                    log.info(result);
                    APIResult obj = gson.fromJson(result, APIResult.class);
                    APIData apiData = obj.getData().get(0);
                    this.data = apiData;
                } catch (Exception e) {
                    log.info("parse data exception : " + result);

                }
            } catch (IOException e) {
                System.out.println(id + " - error: " + e.getMessage());
            }
        }

    }

    @Override
    public Map<String, APIData> getLastPrice(String[] fundCodes) throws IOException {
        Map<String, APIData> ret = new HashMap<>();
        if (connected) {
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(10);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    .setConnectionManager(cm).build();
            try {
                GetThread[] threads = new GetThread[fundCodes.length];
                for (int i = 0; i < threads.length; i++) {
                    String url = domain + "/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code=" + fundCodes[i] + "&dse_sessionId=" + dseSessionId;
                    HttpGet httpget = new HttpGet(url);
                    threads[i] = new GetThread(httpclient, httpget, i + 1);
                }
                for (int j = 0; j < threads.length; j++) {
                    threads[j].start();
                }
                for (int j = 0; j < threads.length; j++) {
                    threads[j].join();
                }
                for (int j = 0; j < threads.length; j++) {
                    APIData data = threads[j].getData();
                    if (data == null) {
                        this.connected = false;
                        break;
                    }
                    ret.put(fundCodes[j], data);
                }
                // System.out.println("fund["+fundCodes[0]+"]"+upData);
                // System.out.println("fund["+fundCodes[1]+"]"+downData);
            } catch (Exception e) {

            } finally {

            }

        } else {
            System.out.println("gf account is log out");
            login();

        }
        //System.out.println(ret);
        return ret;
    }


    public double getLastPrice(String fundCode) throws IOException {
        if (connected) {
            String httpUrl = domain + "/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code=" + fundCode + "&dse_sessionId=" + dseSessionId;
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    //  .setDefaultConnectionConfig(connectionConfig)
                    .build();
            HttpGet httpGet = new HttpGet(httpUrl);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            String result = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
            //  System.out.println("gf account result:" + result);
            try {
                APIResult obj = gson.fromJson(result, APIResult.class);
                return obj.getData().get(0).getLast_price();
            } catch (Exception e) {
                log.info("parse data exception : " + result);
                //e.printStackTrace();
                connected = false;
                return 0d;
            } finally {
                httpGet.releaseConnection();
                httpclient.close();
            }

        } else {
            System.out.println("gf account is log out");
            login();
            return 0d;
        }

    }

    @Override
    public void order(Integer lastNet, String fundCode, double lastPrice, int amount, String bs) {
        //System.out.println("initPrice[" + intPrice + "] grid[" + grid + "]position[" + position + "]");
        String httpUrl = domain + "/entry?entrust_bs=" + bs + "&auto_deal=true&classname=com.gf.etrade.control.NXBUF2Control&method=nxbentrust&fund_code=" + fundCode + "&dse_sessionId=" + dseSessionId + "&entrust_price=" + lastPrice + "&entrust_amount=" + amount;
        log.info(httpUrl);
        try {

            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    .build();
            HttpGet httpGet = new HttpGet(httpUrl);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());
            log.info(result);
            GridTrading model = new GridTrading();
            model.setFund(fundCode);
            model.setPrice(lastPrice);
            model.setLogTime(new Date());
            model.setType(bs);
            model.setAmount(amount);
            model.setLastNet(lastNet);
            model.setResult(result);
            gridTradingRepository.save(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void order(String upCode, String downCode, double upPrice, double downPrice, String bs) {
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .build();

        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(domain + "/entry");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("classname", "com.gf.etrade.control.NXBUF2Control"));
            formparams.add(new BasicNameValuePair("method", "nxbdoubleentrust"));
            formparams.add(new BasicNameValuePair("dse_sessionId", dseSessionId));
            formparams.add(new BasicNameValuePair("fund_code", upCode));
            formparams.add(new BasicNameValuePair("entrust_amount", "1000"));
            formparams.add(new BasicNameValuePair("entrust_price", String.valueOf(upPrice)));
            formparams.add(new BasicNameValuePair("fund_code_1", downCode));
            formparams.add(new BasicNameValuePair("entrust_amount_1", "1000"));
            formparams.add(new BasicNameValuePair("entrust_price_1", String.valueOf(downPrice)));
            formparams.add(new BasicNameValuePair("entrust_bs", bs));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            String responseBody = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
            log.info("upCode["+downCode+"][" + upPrice + "] downCode["+downCode+"][" + downPrice + "]");
            log.info(responseBody);
            EntityUtils.consume(entity);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    public synchronized void login() {
        if (connected) {
            return;
        }
        Gson gson = new Gson();
        try {
            long start = System.currentTimeMillis();
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    .build();
            try {
                HttpGet httpGet = new HttpGet(domain + "/yzm.jpgx");
                CloseableHttpResponse response3 = httpclient.execute(httpGet);
                HttpEntity entity3 = response3.getEntity();
                File file = new File("d:/gf/gf.jpg");
                FileUtils.copyInputStreamToFile(entity3.getContent(), file);
                // BufferedImage image = ImageIO.read(entity3.getContent());
                EntityUtils.consume(entity3);
                String capthca = null;
                {
                 /*   InputStream inputStream = System.in;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    System.out.println("请输入验证码:");
                    capthca = bufferedReader.readLine();*/
                    CommandUtils.executeCommand("python d:\\gf\\verify.py");
                    CommandUtils.executeCommand("tesseract.exe D:\\gf\\gf_bw.jpg d:\\gf\\code");
                    capthca = StringUtils.trimAllWhitespace(FileUtils.readFileToString(new File("d:\\gf\\code.txt"), Charset.forName("UTF-8")));
                    if (capthca.length() != 5) {
                        System.out.println("error capthca[" + capthca + "] re login");
                        return;
                    }
                    HttpUriRequest login = RequestBuilder.post()
                            .setUri(new URI(domain + "/login"))
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
                        //System.out.println("result:" + EntityUtils.toString(entity));
                        Map map = gson.fromJson(EntityUtils.toString(entity), Map.class);
                        System.out.println(map);
                        EntityUtils.consume(entity);
                        if (MapUtils.getBoolean(map, "success")) {
                            System.out.println("Post logon cookies:");
                            /*DailyEntity userSession = new DailyEntity();
                            userSession.setCookieStore(cookieStore);*/
                            List<Cookie> cookies = cookieStore.getCookies();
                            if (cookies.isEmpty()) {
                                System.out.println("None");
                            } else {
                                for (int i = 0; i < cookies.size(); i++) {
                                    String name = cookies.get(i).getName();
                                    if (name.equals("dse_sessionId")) {
                                        dseSessionId = cookies.get(i).getValue();
                                        // userSession.setDseSessionId(dseSessionId);
                                    }
                                    //cookieStore.addCookie(cookies.get(i));
                                    System.out.println("- " + cookies.get(i).toString());
                                }
                                this.connected = true;
                                // this.setUserSession(userSession);
                            }


                        } else {
                            System.out.println("verify code error re login");

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
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (connected == false) {
            login();
        }
    }
}
