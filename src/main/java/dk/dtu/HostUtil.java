package dk.dtu;



public class HostUtil {
    private static String hostIp;
    private static int hostPort;
    private HostUtil() {
        // Private constructor to prevent instantiation
    }

    public static void initialize() {
        hostIp = "localhost";
        hostPort = 10155;

    }

    public static String getHostIp() {
        return hostIp;
    }
    public static int getHostPort() {
        return hostPort;
    }


}
