package dk.dtu.company;

import java.io.IOException;
import java.time.LocalDateTime;

import java.time.Month;
import java.util.*;

import dk.dtu.client.ClientUtil;
import dk.dtu.company.api.FinancialData;
import dk.dtu.host.GlobalClock;
import dk.dtu.client.Order;
import dk.dtu.company.IRS;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import org.jspace.*;


public class Company implements Runnable{
    protected static LocalDateTime ingameDate;
    protected final String companyId;
    protected final String companyName;
    protected final String companyTicker;
    protected LocalDateTime ipoDateTime;

    //Shares outstanding -> Shares in public circulation on the stock exchange.
    private int sharesOutstanding;

    //The total amount of shares at the time this company IPO'ed
    private final int totalNrShares;

    protected boolean isPubliclyTraded = false;

    protected static Space fundamentalsSpace;

    public Company(String companyName, String companyTicker, LocalDateTime ipoDateTime, Space fundamentalsSpace) {
        this.companyId = UUID.randomUUID().toString();
        this.companyName = companyName;
        this.companyTicker = companyTicker;
        this.ipoDateTime = ipoDateTime;
        this.totalNrShares = calculateTotalNrShares();
        Company.fundamentalsSpace = fundamentalsSpace;

    }

    @Override
    public void run() {

        while(!isPubliclyTraded)
        {
            if (this.companyTicker == "VOC")
            {
                System.out.println("Getting the datetime");
            }

            LocalDateTime simulatedDateTime = GlobalClock.getSimulatedDateTimeNow();

        try {

            //System.out.println("The date is now " + simulatedDateTime + " and company " + this.companyTicker  + " has not IPO'd yet.\nIts original IPO date was: " + ipoDateTime);
            //First things first; we have to IPO
            if(isTimeToIPO(ipoDateTime,simulatedDateTime))
            {
                //Calculate fundamentals and push them to fundamentals space
                this.isPubliclyTraded = true;
                updateFundamentalData(simulatedDateTime);


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


    private void sendRequestToCompanyBroker(String orderType, Order order) throws InterruptedException, IOException {
        CompanyBroker companyBroker = new CompanyBroker();
        new Thread(companyBroker).start();
        companyBroker.getRequestSpace().put(orderType, companyId,companyName,companyTicker, order);
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
        return new Order(companyId, companyName, companyTicker, amount, price);
    }

    //Total numbers of shares at IPO
    public int calculateTotalNrShares(){
        BinomialDistribution binomialDistribution = new BinomialDistribution(10000,0.9);
        return binomialDistribution.sample();
    }

    //Number of shares floated (offered publicly) at IPO
    public int getIPOSharesFloated(){
        BinomialDistribution binomialDistribution = new BinomialDistribution(getTotalNrShares(),0.2);
        return binomialDistribution.sample();
    }

    public float calculateIPOPrice(){
        NormalDistribution normalDistribution = new NormalDistribution(100,10);
        return (float) normalDistribution.sample();
    }

    //Is it time for IPO?
    public boolean isTimeToIPO(LocalDateTime ipoYear, LocalDateTime ingameDateTime){
        return (ipoYear.isBefore(ingameDateTime));
    }


    public String getCompanyName() { return companyName; }
    public String getCompanyTicker() { return companyTicker; }
    public String getCompanyId() { return companyId; }

    public int getIpoDateTime(){ return ipoDateTime.getYear();}

    public void updateFundamentalData(LocalDateTime ingameDate){
        try {
            if(isPubliclyTraded)
            {
                NormalDistribution growthDetermination = new NormalDistribution(0.2,0.2);

                List<Object[]> previousFundamentals = fundamentalsSpace.getAll(new ActualField(this.companyTicker),new ActualField(LocalDateTime.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Float.class));
                float previousRevenue = (float) previousFundamentals.get(0)[0];
                float revenueGrowth = (float) (previousRevenue*growthDetermination.sample());
                float newRevenue = revenueGrowth + previousRevenue;
                fundamentalsSpace.put(this.companyTicker, GlobalClock.getIRLDateTimeNow(),"income statement","revenue",newRevenue);
            }
            else
            {
                System.out.println("Company " + this.companyTicker + " is not publicly traded yet, so it cannot update its fundamentals");
                NormalDistribution X = new NormalDistribution(100,10);
                fundamentalsSpace.put(this.companyTicker, GlobalClock.getIRLDateTimeNow(),"income statement","revenue",(float) X.sample());
                System.out.println("Put fundamentals");
            }
        } catch (InterruptedException e) {
            System.out.println("Error in updateFundamentalData");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public static FinancialData getFundamentalDataOverview(String nameOrTicker, int year) throws InterruptedException, IOException {

        //Querying tuple with the form (companyTicker, year, financialStatement, financialPost, financialValue)
        Space companyFundamentalsSpace = IRS.getFundamentalsSpace(nameOrTicker);
        Object[] data = companyFundamentalsSpace.query(new ActualField(nameOrTicker), new ActualField(year),new ActualField("Finance Statement"), new FormalField(FinancialData.class)); //Retrieves fundamental data for a company
        FinancialData financialData = (FinancialData) data[3];
        System.out.println("Got fundamentals" + financialData.getRevenue() + " " + financialData.getCostOfRevenue() + " " + financialData.getGrossProfit());
        return financialData;


    }


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
    }

    public boolean isTimeToUpdateFundamentals(LocalDateTime ingameDateTime)
    {
        if(ingameDateTime.getMonth() != Month.JANUARY)
        {
            //System.out.println(ingameDateTime + " vs " + GlobalCock.getIRLDateTimeNow());
        }
        return (ingameDateTime.getDayOfMonth() == 1 || ingameDateTime.getMonth() == Month.JANUARY || ingameDateTime.getMonth() == Month.APRIL || ingameDateTime.getMonth() == Month.JULY || ingameDateTime.getMonth() == Month.NOVEMBER);
    }

}