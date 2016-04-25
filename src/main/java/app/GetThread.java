package app;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.util.List;
import java.util.Map;

/**
 * Created by Riky on 2016/4/24.
 */
public class GetThread  extends Thread{
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
               // System.out.println(result);
                Map map = gson.fromJson(result, Map.class);
                Map data = (Map) ((List) map.get("data")).get(0);
                //System.out.println(data.get("stock_name"));
                this.data = data;
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(id + " - error: " + e);
        }
    }
}
