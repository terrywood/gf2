package app;


import com.google.gson.Gson;
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
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


@Service("TraderService")
public class TraderGFService implements  InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(TraderGFService.class);
    String userAgent = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)";

    BasicCookieStore cookieStore;
    String dseSessionId = null;
    @Override
    public void afterPropertiesSet() throws Exception {
        this.cookieStore = new BasicCookieStore();

    }

   // @Scheduled(cron = "0/10 20 9,15 * * ?")
    public void balance() throws IOException {

        long c2 = System.currentTimeMillis();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(10);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(userAgent)
                .setConnectionManager(cm).build();
        try {
            String[] urisToGet = {
                    "https://etrade.gf.com.cn/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code=878002&dse_sessionId=" + dseSessionId,
                    "https://etrade.gf.com.cn/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code=878003&dse_sessionId=" +dseSessionId
            };
            GetThread[] threads = new GetThread[2];
            for (int i = 0; i < threads.length; i++) {
                HttpGet httpget = new HttpGet(urisToGet[i]);
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


        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            httpclient.close();
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
                    String ret = EntityUtils.toString(response.getEntity());
                    System.out.println(ret);
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                System.out.println(id + " - error: " + e);
            }
        }
    }
    public void login() {
        try {
            long start = System.currentTimeMillis();
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    .build();
            try {
                HttpGet httpGet = new HttpGet("https://trade.gf.com.cn/yzm.jpgx");
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
                            .setUri(new URI("https://trade.gf.com.cn/login"))
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



    public static void main(String[] args) throws ParseException, IOException {
        TraderGFService service = new TraderGFService();
        try {
            service.afterPropertiesSet();
            service.login();
            service.balance();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

 /*   @CacheEvict(value="trader",key="#id")
    public void delete(Long id){
        traderRepository.delete(id);
    }*/
}
