package dk.dtu.company;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dk.dtu.ClientUtil;
import dk.dtu.CompanyBroker;
import dk.dtu.GlobalClock;
import dk.dtu.Order;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;


public abstract class Company implements Runnable{
    protected final String companyId;
    protected final String companyName;
    protected final String companyTicker;
    protected LocalDateTime ipoDateTime;

    //Shares outstanding -> Shares in public circulation on the stock exchange.
    private int sharesOutstanding;

    //The total amount of shares at the time this company IPO'ed
    private final int totalNrShares;

    protected boolean isPubliclyTraded = false;

    protected final Space fundamentalsSpace;

    public Company(String companyName, String companyTicker, LocalDateTime ipoDateTime, Space fundamentalsSpace) {

        this.companyId = UUID.randomUUID().toString();
        this.companyName = companyName;
        this.companyTicker = companyTicker;
        this.ipoDateTime = ipoDateTime;
        this.totalNrShares = calculateTotalNrShares();
        this.fundamentalsSpace = fundamentalsSpace;

    }

    @Override
    public void run() {

        while(!isPubliclyTraded)
        {

            LocalDateTime simulatedDateTime = GlobalClock.getSimulatedDateTimeNow();

        try {

            //System.out.println("The date is now " + simulatedDateTime + " and company " + this.companyTicker  + " has not IPO'd yet.\nIts original IPO date was: " + ipoDateTime);
            //First things first; we gotta IPO
            if(isTimeToIPO(ipoDateTime,simulatedDateTime))
            {
                //Calculate fundamentals and push them to fundamentals space
                updateFundamentalData(simulatedDateTime);
                this.isPubliclyTraded = true;

                System.out.println("The date is now " + simulatedDateTime + " and company " + this.companyTicker  + " has IPO'd.\nIts original IPO date was: " + ipoDateTime);

                //Then we IPO!!!
                int IPOFloating = this.getIPOSharesFloated();
                float IPOSharePrice = this.calculateIPOPrice();
                this.sharesOutstanding = getIPOSharesFloated();
                Order IPO = makeOrder(IPOFloating, IPOSharePrice);
                sendRequestToCompanyBroker("IPO", IPO);
            }


            while(true)
            {
                simulatedDateTime = GlobalClock.getSimulatedDateTimeNow();

                //We need to wait until the right time to update, to ensure, that we have enough financial date to last the game
                if(isTimeToUpdateFundamentals(simulatedDateTime))
                {
                    updateFundamentalData(simulatedDateTime);
                }

            }

        }
        catch (Exception e)
        {
            System.out.println("Company got error");
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
            }
        }
    }


    private void sendRequestToCompanyBroker(String orderType, Order order) throws InterruptedException {
        CompanyBroker companyBroker = new CompanyBroker();
        new Thread(companyBroker).start();
        companyBroker.getRequestSpace().put(orderType, companyId,companyName,companyTicker, order);
/*
        try {
            if (orderType.equals("IPO")) {
                sendIpoRequestToExchange(companyBroker, order);
            } else if (orderType.equals("buy")) {
                sendBuyOrder(companyId, companyBroker, order);
            } else if (orderType.equals("sell")) {
                sendSellOrder(companyId, companyBroker, order);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
 */
    }

    private void sendIpoRequestToExchange(CompanyBroker companyBroker, Order order) throws IOException, InterruptedException {
        String uri = ClientUtil.getHostUri("exchangeRequestSpace");  //TODO den skal have et rigtig room navn
        String uriConnection = ClientUtil.setConnectType(uri,"keep");
        //TODO er keep den rigtige forbindelse her? Og hvad med alle andre steder? STOR TODO
        Space exchangeRequestSpace = new RemoteSpace(uriConnection);
        exchangeRequestSpace.put(order.getOrderId());

    }

    private void sendBuyOrder(String companyId, CompanyBroker companyBroker, Order order) {
        //TODO samme logik som i Trader.java
    }

    private void sendSellOrder(String companyId, CompanyBroker companyBroker, Order order) {
        //TODO samme logik som i Trader.java
    }

    private Order makeOrder(int amount, float price) {
        return new Order(companyId, companyName,companyTicker, amount, price);
    }

    //Total numbers of shares at IPO
    abstract int calculateTotalNrShares();

    //Number of shares floated (offered publicly) at IPO
    abstract int getIPOSharesFloated();

    abstract float calculateIPOPrice();

    //Is it time for IPO?
    abstract boolean isTimeToIPO(LocalDateTime ipoYear, LocalDateTime simulatedDateTime);


    public String getCompanyName() { return companyName; }
    public String getCompanyTicker() { return companyTicker; }
    public String getCompanyId() { return companyId; }

    public int getIpoDateTime(){ return ipoDateTime.getYear();}

    public abstract void updateFundamentalData(LocalDateTime ingameDate);

    public abstract float getFundamentalData(String financialPost);


    public int getSharesOutstanding() {
        return sharesOutstanding;
    }

    public int getTotalNrShares() {
        return totalNrShares;
    }

    //(String companyTicker, LocalDateTime irlTimeStamp, LocalDateTime simulatedGameTime , String financialStatement, String financialPost, float financialValue)
    // Standardized way of getting fundamentals from our fundamentals space
    public List<Object[]> getFundamentalsFromSpace(String companyTicker) throws InterruptedException {
        fundamentalsSpace.get(new ActualField("readTicket"));
        return fundamentalsSpace.queryAll(new ActualField(companyTicker), new FormalField(LocalDateTime.class), new FormalField(LocalDateTime.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Float.class));
    }

    //(String companyTicker, LocalDateTime irlTimeStamp, LocalDateTime simulatedGameTime , String financialStatement, String financialPost, float financialValue)
    // Standardized way of putting fundamentals
    public void putFundamentals(String companyTicker, LocalDateTime irlTimeStamp, LocalDateTime simulatedDateTime, String financialStatement, String financialPost, float financialValue) throws InterruptedException {
        fundamentalsSpace.put(companyTicker, irlTimeStamp, simulatedDateTime, financialStatement, financialPost, financialValue);
        fundamentalsSpace.put("readTicket");
        System.out.println("Company " + companyTicker + "put fundamentals" + financialPost + ":" + financialValue);
    }

    public abstract boolean isTimeToUpdateFundamentals(LocalDateTime simulateDateTime);


}

/**
 * This class is used to store the fundamentals of a company so we can iterate over them and update them with the API.
 */
class CompanyFundamentals
{
    private ArrayList<String> financialPosts;
    private HashMap<String,String> financialPostToStatementMap;
    private HashMap<String,Float> financialPostToValueMap;

    
}