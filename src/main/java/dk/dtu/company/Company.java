package dk.dtu.company;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import dk.dtu.client.ClientUtil;
import dk.dtu.host.GlobalClock;
import dk.dtu.client.Order;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;


public class Company implements Runnable{
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
            if (this.companyTicker == "VOC")
            {
                System.out.println("Getting the datetime");
            }
            LocalDateTime inGameDateTime = GlobalClock.getSimulatedDateTimeNow();

        try {

            //System.out.println("The date is now " + inGameDateTime + " and company " + this.companyTicker  + " has not IPO'd yet.\nIts original IPO date was: " + ipoDateTime);
            //First things first; we gotta IPO
            if(isTimeToIPO(ipoDateTime,inGameDateTime))
            {
                //Calculate fundamentals and push them to fundamentals space
                updateFundamentalData(inGameDateTime);
                this.isPubliclyTraded = true;

                System.out.println("The date is now " + inGameDateTime + " and company " + this.companyTicker  + " has IPO'd.\nIts original IPO date was: " + ipoDateTime);

                //Then we IPO!!!
                int IPOFloating = this.getIPOSharesFloated();
                System.out.println("Got shares floated");
                float IPOSharePrice = this.calculateIPOPrice();
                System.out.println("Calculated IPO price");
                this.sharesOutstanding = getIPOSharesFloated();
                System.out.println("Got shares outstanding");
                Order IPO = makeOrder(IPOFloating, IPOSharePrice);
                System.out.println("Made IPO order");
                sendRequestToCompanyBroker("IPO", IPO);
                System.out.println("Sent request to company broker");
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

    public float getFundamentalData(String financialPost){
        return 0; // TODO what is this supposed to do?
    }


    public int getSharesOutstanding() {
        return sharesOutstanding;
    }

    public int getTotalNrShares() {
        return totalNrShares;
    }


}

//TODO: Add a CompanyFundamentals class which can standardize the way we update the fundamentals
