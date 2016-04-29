package app.service;

import app.CommandUtils;
import app.entity.GridTrading;
import app.entity.UserSession;
import app.repository.GridTradingRepository;
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
import org.hibernate.annotations.Synchronize;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by terry.wu on 2016/4/29 0029.
 */
@Service
public class AccountService  implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    public   String userAgent = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)";
    public   BasicCookieStore cookieStore = new BasicCookieStore();
    public   String dseSessionId = null;
    public   String domain = "https://etrade.gf.com.cn";
    @Autowired
    private GridTradingRepository gridTradingRepository;
    //private  UserSession userSession;
    public  boolean isLogin = false;

/*    public UserSession getUserSession() {
        return userSession;
    }
    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
    }*/


    public double getLastPrice(String fundCode) throws IOException {
        if(isLogin){
            Gson gson = new Gson();
            String httpUrl = domain + "/entry?classname=com.gf.etrade.control.NXBUF2Control&method=nxbQueryPrice&fund_code=" + fundCode + "&dse_sessionId=" + dseSessionId;
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    .build();
            HttpGet httpGet = new HttpGet(httpUrl);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            Map map = gson.fromJson(IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8), Map.class);
            Map data = (Map) ((List) map.get("data")).get(0);
            return MapUtils.getDouble(data, "last_price");
        }else{
            System.out.println("gf account is log out");
            login();
            return 0d;
        }

    }

    public void order(Integer lastNet,String fundCode,double lastPrice, int amount, String bs) {
        //System.out.println("initPrice[" + intPrice + "] grid[" + grid + "]lastNet[" + lastNet + "]");
        String httpUrl = domain + "/entry?entrust_bs=" + bs + "&auto_deal=true&classname=com.gf.etrade.control.NXBUF2Control&method=nxbentrust&fund_code=" + fundCode + "&dse_sessionId=" + dseSessionId + "&entrust_price=" + lastPrice + "&entrust_amount=" + amount;
        log.info(httpUrl);
        try {

           /* CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setUserAgent(userAgent)
                    .build();
            HttpGet httpGet = new HttpGet(httpUrl);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());
            System.out.println(result);*/

            GridTrading model = new GridTrading();
            model.setFund(fundCode);
            model.setPrice(lastPrice);
            model.setLogTime(new Date());
            model.setType(bs);
            model.setAmount(amount);
            model.setLastNet(lastNet);
            gridTradingRepository.save(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean login() {
        boolean isOk = false;
        Gson gson = new Gson();
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
                File file = new File("d:/gf/gf.jpg");
                FileUtils.copyInputStreamToFile(entity3.getContent(),file);
                // BufferedImage image = ImageIO.read(entity3.getContent());
                EntityUtils.consume(entity3);
                String capthca = null;
                {
                 /*   InputStream inputStream = System.in;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    System.out.println("请输入验证码:");
                    capthca = bufferedReader.readLine();*/
                    CommandUtils.executeCommand("python d:\\gf\\verify.py");
                    CommandUtils.executeCommand("D:\\gf\\tesseract.exe D:\\gf\\gf_bw.jpg d:\\gf\\code");
                    capthca = StringUtils.trimAllWhitespace(FileUtils.readFileToString(new File("d:\\gf\\code.txt"), Charset.forName("UTF-8")));

                    if(capthca.length()!=5){
                        System.out.println("error capthca["+capthca+"] re login");
                        return  false;
                    }
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
                        //System.out.println("result:" + EntityUtils.toString(entity));
                        Map map  = gson.fromJson( EntityUtils.toString(entity), Map.class);
                        System.out.println(map);
                        EntityUtils.consume(entity);
                        if(MapUtils.getBoolean(map,"success")){
                            System.out.println("Post logon cookies:");
                            /*UserSession userSession = new UserSession();
                            userSession.setCookieStore(cookieStore);*/
                             List<Cookie> cookies = cookieStore.getCookies();
                            if (cookies.isEmpty()) {
                                System.out.println("None");
                            } else {
                                for (int i = 0; i < cookies.size(); i++) {
                                    String name = cookies.get(i).getName();
                                    if(name.equals("dse_sessionId")){
                                        dseSessionId =  cookies.get(i).getValue();
                                       // userSession.setDseSessionId(dseSessionId);
                                    }
                                    //cookieStore.addCookie(cookies.get(i));
                                    System.out.println("- " + cookies.get(i).toString());
                                }
                                isOk = true;
                                this.isLogin = true;
                               // this.setUserSession(userSession);
                            }


                        }else{
                            System.out.println("verify code error re login");
                            return  false;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        if(isLogin==false){
            login();
        }
    }
}