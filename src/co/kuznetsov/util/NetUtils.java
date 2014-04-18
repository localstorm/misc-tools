package co.kuznetsov.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author localstorm
 *         Date: 4/2/14
 */
public class NetUtils {
    public static String getHostName() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }
}
