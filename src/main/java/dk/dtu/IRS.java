package dk.dtu;

import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

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
    Map<String, Integer> tickerIPOYears  = new HashMap<>();
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
    }

    private void initializeCompanyIPOYears() {
        // Populate the HashMap with ticker symbols and IPO years
        tickerIPOYears.put("IBM", 1915);
        tickerIPOYears.put("GE", 1892);
        tickerIPOYears.put("DIS", 1957);
        tickerIPOYears.put("KO", 1919);
        tickerIPOYears.put("MCD", 1965);
        tickerIPOYears.put("WMT", 1970);
        tickerIPOYears.put("PG", 1891);
        tickerIPOYears.put("JNJ", 1944);
        tickerIPOYears.put("XOM", 1978);
        tickerIPOYears.put("INTC", 1971);
        tickerIPOYears.put("AAPL", 1980);
        tickerIPOYears.put("MSFT", 1986);
        tickerIPOYears.put("CSCO", 1990);
        tickerIPOYears.put("HWP", 1957); // Assuming HWP for Hewlett-Packard (now HPQ)
        tickerIPOYears.put("GS", 1999);
        tickerIPOYears.put("GOOG", 2004);
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
    }


    public IRS(SpaceRepository hostRepo)
    {
        this.hostRepo = hostRepo;

    }

    public void establishCompany(String companyName ,String ticker,int ipoYear) throws Exception {
        //TODO: Test this!!!

        //TODO: Add an option to select if we want realistic or dummy companies
        Space fundamentalsSpace = new SequentialSpace();

        hostRepo.add("fundamentals" + ticker, fundamentalsSpace);
        //new Thread(new Company(companyName,ticker,ipoYear,fundamentalsSpace)).start();
    }

    public void run()
    {
        while(true)
        {
            //Establish companies
            for (String ticker : this.tickers)
            {
                establishCompany(this.tickerCompanyName.get(ticker),ticker,this.tickerIPOYears.get(ticker));
            }


        }

    }

}
