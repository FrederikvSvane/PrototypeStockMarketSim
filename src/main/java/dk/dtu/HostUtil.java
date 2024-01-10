package dk.dtu;

public class HostUtil {
    private static String hostIp;
    private static int hostPort;
    private HostUtil() {
        // Private constructor to prevent instantiation
    }

    public static void initialize(String ip) {
        hostIp = ip;
        hostPort = 10151;
    }

    public static String getHostIp() {
        return hostIp;
    }
    public static int getHostPort() {
        return hostPort;
    }

}
