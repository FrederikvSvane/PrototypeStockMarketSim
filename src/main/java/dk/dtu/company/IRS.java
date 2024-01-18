package dk.dtu.company;

import dk.dtu.company.api.ApiDataFetcher;
import dk.dtu.host.HostUtil;
import dk.dtu.client.ClientUtil;

import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * The IRS establishes companies by giving them a company name, a ticker, an IPO year and a fundamentalsSpace where they kan keep their fundamentals.
 *
 */
public class IRS implements Runnable {

    SpaceRepository IrsRepo;
    ArrayList<String> tickers = new ArrayList<>();
    Map<String, LocalDateTime> tickerIPODateTime  = new HashMap<>();
    Map<String,String> tickerCompanyName = new HashMap<>();
    private String companyType;



    private void initializeTickers() {
        // Add ticker symbols to the list
        tickers.add("IBM");
        tickers.add("GE");
        tickers.add("DIS");
        tickers.add("KO");
        tickers.add("MCD");
        /*tickers.add("WMT");
        tickers.add("PG");
        tickers.add("JNJ");
        tickers.add("XOM");
        tickers.add("INTC");
        tickers.add("AAPL");
        tickers.add("MSFT");
        tickers.add("CSCO");
        tickers.add("HWP"); // Assuming HWP for Hewlett-Packard (now HPQ)
        tickers.add("GS");
        tickers.add("GOOG");
        tickers.add("VOC");*/
    }

    private void initializeCompanyIPOYears() {

        // Populate the HashMap with ticker symbols and IPO years
        // Adding IPO dates as LocalDateTime objects
        tickerIPODateTime.put("IBM", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("GE", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("DIS", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("KO", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("MCD", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("WMT", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("PG", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("JNJ", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("XOM", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("INTC", LocalDateTime.of(2019, 11, 1, 0, 1));
        tickerIPODateTime.put("AAPL", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("MSFT", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("CSCO", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("HWP", LocalDateTime.of(2019, 1, 1, 0, 1)); // Assuming HWP for Hewlett-Packard (now HPQ)
        tickerIPODateTime.put("GS", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("GOOG", LocalDateTime.of(2019, 1, 1, 0, 1));
        tickerIPODateTime.put("VOC", LocalDateTime.of(2019, 1, 1, 0, 1));


    }


    private void initializeCompanyNames() {
        // Populate the HashMap with ticker symbols and company names
        tickerCompanyName.put("IBM", "International Business Machines");
        tickerCompanyName.put("GE", "General Electric");
        tickerCompanyName.put("DIS", "Walt Disney");
        tickerCompanyName.put("KO", "Coca Cola");
        tickerCompanyName.put("MCD", "McDonalds");
        tickerCompanyName.put("WMT", "Walmart");
        tickerCompanyName.put("PG", "Procter & Gamble");
        tickerCompanyName.put("JNJ", "Johnson & Johnson");
        tickerCompanyName.put("XOM", "Exxon Mobil");
        tickerCompanyName.put("INTC", "Intel Corporation");
        tickerCompanyName.put("AAPL", "Apple");
        tickerCompanyName.put("MSFT", "Microsoft");
        tickerCompanyName.put("CSCO", "Cisco");
        tickerCompanyName.put("HWP", "Hewlett Packard"); // Assuming HWP for Hewlett-Packard (now HPQ)
        tickerCompanyName.put("GS", "Goldman Sachs");
        tickerCompanyName.put("GOOG", "Google");
        tickerCompanyName.put("VOC", "Verenigde Oostindische Compagnie");
    }

    public static String getFundamentalsSpaceName(String ticker)
    {
        return "fundamentals" + ticker;
    }


    public IRS(SpaceRepository IrsRepo, String companyType)
    {
        this.IrsRepo = IrsRepo;
        IrsRepo.addGate(ClientUtil.getHostUri("", HostUtil.getIrsPort(), "keep"));
        this.companyType = companyType;
    }

    public void establishCompany(String companyName , String ticker, LocalDateTime ipoDateTime, String typeOfCompany) throws Exception {
        //TODO: Test this!!!

        //TODO: Add an option to select if we want realistic or dummy companies
        Space fundamentalsSpace = new SequentialSpace();
        IrsRepo.add("fundamentals" + ticker, fundamentalsSpace);
        fundamentalsSpace.put("readTicket");
        switch (typeOfCompany)
        {
            case "stochastic":
                System.out.println("Starting stochastic company: " + ticker);
                new Thread(new StochasticCompany(companyName,ticker,ipoDateTime,fundamentalsSpace)).start();
                break;
            case "realistic":
                Space latentSpace = new SequentialSpace();
                IrsRepo.add("latent" + ticker, latentSpace);
                latentSpace.put("readTicket");
                ApiDataFetcher.sendRequestIncome(ticker,latentSpace);
                ApiDataFetcher.sendRequestBalanceSheet(ticker,latentSpace);
                System.out.println("Starting realistic company: " + ticker);
                new Thread(new RealisticCompany(companyName,ticker,ipoDateTime,fundamentalsSpace,latentSpace)).start();


                //TODO: GetAPI data for that ticker
                //TODO: Extract yearsOfFundamentalsUpdates'
                //TODO: Instantiate API company
        }
    }

    public void run()
    {
        System.out.println("Started the IRS thread");
        initializeTickers();
        initializeCompanyNames();
        initializeCompanyIPOYears();
            //Establish companies
            for (String ticker : this.tickers)
            {
                try {
                    //establishCompany(this.tickerCompanyName.get(ticker),ticker,this.tickerIPODateTime.get(ticker),"stochastic");
                    establishCompany(this.tickerCompanyName.get(ticker), ticker, this.tickerIPODateTime.get(ticker), this.companyType);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
    }
}
