package dk.dtu.host;


import org.jspace.SpaceRepository;

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
    public static int getExchangePort() {
        return hostPort;
    }
    public static int getLobbyPort() { return hostPort + 1; }
    public static int getBankPort() { return hostPort + 2; }
    public static int getClockPort() { return hostPort + 3; }
    public static int getIrsPort() { return hostPort + 4; }

}
