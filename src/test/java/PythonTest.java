import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;

/**
 * Created by terry.wu on 2016/4/26 0026.
 */
public class PythonTest {
    public static void main(String[] args) throws IOException, InterruptedException {

        long start = System.currentTimeMillis();
        String domain = "https://etrade.gf.com.cn";
        CloseableHttpClient httpclient = HttpClients.custom()
                .build();
        try {
            HttpGet httpGet = new HttpGet(domain + "/yzm.jpgx");
            CloseableHttpResponse response3 = httpclient.execute(httpGet);
            HttpEntity entity3 = response3.getEntity();
            File file = new File("d:\\gf\\gf.jpg");
            FileUtils.copyInputStreamToFile(entity3.getContent(), file);


        }catch (Exception ex){

        }

        System.out.println("use times:"+(System.currentTimeMillis()-start));
    }





}
