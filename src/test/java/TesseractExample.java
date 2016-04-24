import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import java.io.File;

/**
 * Created by terry.wu on 2016/4/18 0018.
 */
public class TesseractExample {
    public final Log log = LogFactory.getLog(this.getClass());;
    public static void main(String[] args) {
       //

        File imageFile = new File("G:\\dev\\yzm1.jpg");
        TesseractExample example = new TesseractExample();
        example.test();
    }

    public void test(){
        log.info(" ----------------------info");
        log.debug(" ----------------------debug");
        log.warn(" ----------------------warn");
        log.error(" ----------------------error");
    }
}
