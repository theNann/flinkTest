import org.apache.flink.streaming.api.functions.source.SocketTextStreamFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.IOUtils;
import org.apache.flink.util.Preconditions;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by pyn on 2018/5/7.
 */
public class SocketByteStreamFunction implements SourceFunction<byte[]>{

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SocketByteStreamFunction.class);
    private static final int DEFAULT_CONNECTION_RETRY_SLEEP = 500;
    private static final int CONNECTION_TIMEOUT_TIME = 0;
//    private final String hostname;
//    private final int port;
    private transient Socket currentSocket;
    private volatile boolean isRunning;
    private final long maxNumRetries;
    private final long delayBetweenRetries;
    private int byteNum;
//    private transient int tmp;
    public SocketByteStreamFunction(Socket socket, int byteNum, long maxNumRetries) {
        this.currentSocket = socket;
        System.out.println("構造函數： " + this.currentSocket);
        this.byteNum = byteNum;
        this.isRunning = true;
        this.maxNumRetries = maxNumRetries;
        this.delayBetweenRetries = 500L;
    }

    public void run(SourceContext<byte[]> ctx) throws Exception {
        long attempt = 0L;
        System.out.println("function socket : " + this.currentSocket);//null?
        while(this.isRunning) {
//            Socket socket = this.currentSocket;

            Throwable var6 = null;

            try {
//                this.currentSocket = socket;
//                LOG.info("Connecting to server socket " + this.hostname + ':' + this.port);
//                socket.connect(new InetSocketAddress(this.hostname, this.port), 0);
                InputStream input = currentSocket.getInputStream();
                byte[] in = new byte[byteNum];//因为每个数据是68byte,8160/68=120,也就是每次读120个数据
                int bytesRead;
                while(this.isRunning && (bytesRead = input.read(in, 0, byteNum)) != -1) {
                    while(bytesRead < byteNum) {
                        bytesRead += input.read(in, bytesRead, byteNum-bytesRead);
                    }
                    ctx.collect(in);
                }
//                ctx.collect(in);
            } catch (Throwable var19) {
                var6 = var19;
            } finally {
                if(currentSocket != null) {
                    currentSocket.close();
                }
            }
            if(this.isRunning) {
                ++attempt;
                if(this.maxNumRetries != -1L && attempt >= this.maxNumRetries) {
                    break;
                }

                LOG.warn("Lost connection to server socket. Retrying in " + this.delayBetweenRetries + " msecs...");
                Thread.sleep(this.delayBetweenRetries);
            }
        }
    }

    public void cancel() {
//        this.isRunning = false;
//        Socket theSocket = this.currentSocket;
//        if(theSocket != null) {
//            IOUtils.closeSocket(theSocket);
//        }

    }
}
