package dk.dtu.company;

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

    SpaceRepository hostRepo;

    ArrayList<String> tickers = new ArrayList<>();
    Map<String, LocalDateTime> tickerIPODateTime  = new HashMap<>();
    Map<String,String> tickerCompanyName = new HashMap<>();



    private void initializeTickers() {
        // Add ticker symbols to the list
        tickers.add("IBM");
        tickers.add("GE");
        tickers.add("DIS");
        tickers.add("KO");
        tickers.add("MCD");
        tickers.add("WMT");
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
        tickers.add("VOC");
    }

    private void initializeCompanyIPOYears() {

        // Populate the HashMap with ticker symbols and IPO years
        // Adding IPO dates as LocalDateTime objects
        tickerIPODateTime.put("IBM", LocalDateTime.of(1978, 1, 19, 0, 0));
        tickerIPODateTime.put("GE", LocalDateTime.of(1892, 4, 15, 0, 0));
        tickerIPODateTime.put("DIS", LocalDateTime.of(1957, 11, 12, 0, 0));
        tickerIPODateTime.put("KO", LocalDateTime.of(1919, 9, 5, 0, 0));
        tickerIPODateTime.put("MCD", LocalDateTime.of(1965, 4, 21, 0, 0));
        tickerIPODateTime.put("WMT", LocalDateTime.of(1970, 10, 1, 0, 0));
        tickerIPODateTime.put("PG", LocalDateTime.of(1890, 12, 31, 0, 0));
        tickerIPODateTime.put("JNJ", LocalDateTime.of(1944, 9, 24, 0, 0));
        tickerIPODateTime.put("XOM", LocalDateTime.of(1978, 1, 13, 0, 0));
        tickerIPODateTime.put("INTC", LocalDateTime.of(1971, 10, 13, 0, 0));
        tickerIPODateTime.put("AAPL", LocalDateTime.of(1980, 12, 12, 0, 0));
        tickerIPODateTime.put("MSFT", LocalDateTime.of(1986, 3, 13, 0, 0));
        tickerIPODateTime.put("CSCO", LocalDateTime.of(1990, 2, 16, 0, 0));
        tickerIPODateTime.put("HWP", LocalDateTime.of(1957, 11, 6, 0, 0)); // Assuming HWP for Hewlett-Packard (now HPQ)
        tickerIPODateTime.put("GS", LocalDateTime.of(1999, 5, 4, 0, 0));
        tickerIPODateTime.put("GOOG", LocalDateTime.of(2004, 8, 19, 0, 0));
        tickerIPODateTime.put("VOC", LocalDateTime.of(1602, 8, 19, 0, 0));


    }

    private void initializeCompanyNames() {
        // Populate the HashMap with ticker symbols and company names
        tickerCompanyName.put("IBM", "International Business Machines");
        tickerCompanyName.put("GE", "General Electric");
        tickerCompanyName.put("DIS", "Walt Disney");
        tickerCompanyName.put("KO", "Coca-Cola");
        tickerCompanyName.put("MCD", "McDonald's");
        tickerCompanyName.put("WMT", "Walmart");
        tickerCompanyName.put("PG", "Procter & Gamble");
        tickerCompanyName.put("JNJ", "Johnson & Johnson");
        tickerCompanyName.put("XOM", "Exxon Mobil");
        tickerCompanyName.put("INTC", "Intel Corporation");
        tickerCompanyName.put("AAPL", "Apple Inc.");
        tickerCompanyName.put("MSFT", "Microsoft Corporation");
        tickerCompanyName.put("CSCO", "Cisco Systems");
        tickerCompanyName.put("HWP", "Hewlett-Packard"); // Assuming HWP for Hewlett-Packard (now HPQ)
        tickerCompanyName.put("GS", "Goldman Sachs");
        tickerCompanyName.put("GOOG", "Google");
        tickerCompanyName.put("VOC", "Verenigde Oostindische Compagnie");
    }

    public static String getFundamentalsSpaceName(String ticker)
    {
        return "fundamentals" + ticker;
    }


    public IRS(SpaceRepository hostRepo)
    {
        this.hostRepo = hostRepo;
        System.out.println("Constructed IRS");
    }

    public void establishCompany(String companyName , String ticker, LocalDateTime ipoDateTime, String typeOfCompany) throws Exception {
        //TODO: Test this!!!

        //TODO: Add an option to select if we want realistic or dummy companies
        Space fundamentalsSpace = new SequentialSpace();
        fundamentalsSpace.put("readTicket");
        hostRepo.add(getFundamentalsSpaceName(ticker), fundamentalsSpace);
        switch (typeOfCompany)
        {
            case "stochastic":
                System.out.println("Starting stochastic company: " + ticker);
                new Thread(new StochasticCompany(companyName,ticker,ipoDateTime,fundamentalsSpace)).start();
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
                    establishCompany(this.tickerCompanyName.get(ticker),ticker,this.tickerIPODateTime.get(ticker),"stochastic");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
    }
}
