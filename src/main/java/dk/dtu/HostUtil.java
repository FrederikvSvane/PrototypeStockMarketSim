package dk.dtu;



public class HostUtil {
    private static String hostIp;
    private static int hostPort;

    private static String lobbyToTraderName = "lobbyToTrader"; //TODO find anden måde at gøre dette end at passe som argument til constructor
    private static String traderToLobbyName = "traderToLobby";
    private HostUtil() {
        // Private constructor to prevent instantiation
    }

    public static void initialize() {
        hostIp = "10.209.94.154";
        hostPort = 10155;
    }

    public static String getHostIp() {
        return hostIp;
    }
    public static int getHostPort() {
        return hostPort;
    }

    public static String getLobbyToTraderName() {
        return lobbyToTraderName;
    }

    public static String getTraderToLobbyName() {
        return traderToLobbyName;
    }


}
