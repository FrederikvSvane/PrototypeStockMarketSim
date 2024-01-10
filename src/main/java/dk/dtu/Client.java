package dk.dtu;

public class Client {
    private int amountOfTraders = 1;
    static String lobbyToTraderName = "lobbyToTrader"; //TODO find anden måde at gøre dette end at passe som argument til constructor
    static String traderToLobbyName = "traderToLobby";


    public static void main(String[] args) {
        HostUtil.initialize();
        ClientUtil.initialize();
        Trader trader = new Trader(traderToLobbyName, lobbyToTraderName);
        new Thread(trader).start();
    }
}
