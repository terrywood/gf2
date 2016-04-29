package app.entity;

import lombok.Data;
import org.apache.http.impl.client.BasicCookieStore;

/**
 * Created by terry.wu on 2016/4/29 0029.
 */

@Data
public class UserSession {
    String dseSessionId;
    BasicCookieStore cookieStore;
    boolean isLogin = false;
}
