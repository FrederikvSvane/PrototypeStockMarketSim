package dk.dtu;

public class Client {
    private int amountOfTraders = 1;
    static String lobbyToTraderName = "lobbyToTrader"; //TODO find anden måde at gøre dette end at passe som argument til constructor
    static String traderToLobbyName = "traderToLobby";


    public static void main(String[] args) {
        ClientUtil.initialize();

        HumanTrader humanTrader = new HumanTrader(traderToLobbyName, lobbyToTraderName);
        new Thread(humanTrader).start();

        }

    }

