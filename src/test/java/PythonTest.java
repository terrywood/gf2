import org.apache.commons.io.IOUtils;
import org.python.core.PyException;
import org.springframework.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;

/**
 * Created by terry.wu on 2016/4/26 0026.
 */
public class PythonTest {
    public static void main(String[] args) throws IOException, InterruptedException {

/*
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");

        try
        {
            engine.eval(new FileReader("verify.py"));
        }
        catch(ScriptException se)
        {
        }
        catch(IOException ie)
        {
        }
*/
        //excuteCommand("ipconfig");
        //excuteCommand("python verify.py");
        Process proc = Runtime.getRuntime().exec("python verify.py");
        doWaitFor(proc);
/*      Process proc = Runtime.getRuntime().exec("cmd /c python verify.py > aa.txt");

        String code = (IOUtils.toString(proc.getInputStream()));
        if (!StringUtils.isEmpty(code))
            System.out.println("code->" + code);*/

        // proc.destroy();

    }

    public static void  excuteCommand(String command)
    {

        Runtime r = Runtime.getRuntime();
        Process p;
        try {

            p = r.exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String inline;
            while ((inline = br.readLine()) != null) {
                System.out.println(inline);

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int doWaitFor(Process process) {
        InputStream in = null;
        InputStream err = null;
        int exitValue = -1; // returned to caller when p is finished
        try {
            in = process.getInputStream();
            err = process.getErrorStream();
            boolean finished = false; // Set to true when p is finished
            while (!finished) {
                try {
                    while (in.available() > 0) {
                        // Print the output of our system call
                        Character c = new Character((char) in.read());
                        System.out.print(c);
                    }
                    while (err.available() > 0) {
                        // Print the output of our system call
                        Character c = new Character((char) err.read());
                        System.out.print(c);
                    }
                    // Ask the process for its exitValue. If the process
                    // is not finished, an IllegalThreadStateException
                    // is thrown. If it is finished, we fall through and
                    // the variable finished is set to true.
                    exitValue = process.exitValue();
                    finished = true;
                } catch (IllegalThreadStateException e) {
                    // Process is not finished yet;
                    // Sleep a little to save on CPU cycles
                    Thread.currentThread().sleep(500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (err != null) {
                try {
                    err.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return exitValue;
    }

}
