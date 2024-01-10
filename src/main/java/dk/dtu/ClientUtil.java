package dk.dtu;

public class ClientUtil {
    private static String hostIp;
    private static int hostPort;

    private ClientUtil() {
        // Private constructor to prevent instantiation
    }

    public static void initialize() {
        hostIp = HostUtil.getHostIp();
        hostPort = HostUtil.getHostPort();
    }

    public static String getHostUri(String roomName) {
        // Ensure that initialize has been called
        if (hostIp == null || hostPort == 0) {
            throw new IllegalStateException("UriUtility is not initialized");
        }
        return "tcp://" + hostIp + ":" + hostPort + "/" + roomName;
    }

    public String setConnectType(String hostUri, String connectionType) {
        if (connectionType == "keep" || connectionType == "conn") {
            return hostUri + "?" + connectionType;
        }else {
                throw new IllegalArgumentException("Connection type unknown");
            }
    }
}