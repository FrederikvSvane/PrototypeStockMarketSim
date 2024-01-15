package dk.dtu.client;

import dk.dtu.host.HostUtil;

public class ClientUtil {
    private static String hostIp;
    private static int hostPort;

    private ClientUtil() {
        // Private constructor to prevent instantiation
    }

    public static void initialize() {
        hostIp = HostUtil.getHostIp();
        hostPort = HostUtil.getExchangePort(); // Totalt hacket måde at finde den på, men det virker.
    }

    public static String getHostUri(String roomName) {
        // Ensure that initialize has been called
        if (hostIp == null || hostPort == 0) {
            throw new IllegalStateException("UriUtility is not initialized");
        }
        return "tcp://" + hostIp + ":" + hostPort + "/" + roomName;
    }

    public static String getHostUri(String roomName, int port) {
        // Ensure that initialize has been called
        if (hostIp == null || hostPort == 0) {
            throw new IllegalStateException("UriUtility is not initialized");
        }
        return "tcp://" + hostIp + ":" + port + "/" + roomName;
    }

    public static String getHostUri(String roomName, int port, String connectionType) {
        // Ensure that initialize has been called
        if (hostIp == null || hostPort == 0) {
            throw new IllegalStateException("UriUtility is not initialized");
        }
        return "tcp://" + hostIp + ":" + port + "/" + roomName + "?" + connectionType;
    }

    public static String setConnectType(String hostUri, String connectionType) {
        if (connectionType == "keep" || connectionType == "conn") {
            return hostUri + "?" + connectionType;
        }else {
                throw new IllegalArgumentException("Connection type unknown");
            }
    }
}