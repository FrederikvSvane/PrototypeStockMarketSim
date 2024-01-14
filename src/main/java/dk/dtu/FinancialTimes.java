package dk.dtu;


import dk.dtu.company.IRS;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.time.LocalDateTime;
import java.util.List;

/**
 * It gets fundamental data and news for the Trader, which he/she uses for his/her trading algorithm.
 */
public class FinancialTimes extends DataFetcher implements Runnable{

    String companyTicker;
    String financialPost;


    public FinancialTimes(Space traderDataSpace, int sleepTime, String ticker, String financialPost)
    {
        super(traderDataSpace, sleepTime, IRS.getFundamentalsSpaceName(ticker));
        this.companyTicker = ticker;
        this.financialPost = financialPost;

    }

    //(String companyTicker, LocalDateTime irlTimeStamp, LocalDateTime simulatedGameTime , String financialStatement, String financialPost, float financialValue)
    // Standardized way of getting fundamentals from our fundamentals space
    public Object[] getFundamentalsFromSpace(String companyTicker, String financialPost) throws InterruptedException {
        return super.companyDataSpace.queryp(new ActualField(companyTicker), new FormalField(LocalDateTime.class), new FormalField(LocalDateTime.class), new FormalField(String.class), new ActualField(financialPost), new FormalField(Float.class));
    }

    /**
     * We don't use this, as we don't get a List<Object[]>, just an Object[]
     */
    @Override
    void updateCompanyData(List<Object[]> companyData) throws InterruptedException
    {

    }

    void updateCompanyData(Object[] fundamentalsData)
    {

    }

    @Override
    public void run()
    {
        try {
            //We want to make sure, that the reason that we can't get the specified financial post isn't because the company is working updating the fundamentals.
            companyDataSpace.query(new ActualField("readTicket"));
            Object[] fundamentalsRequest = getFundamentalsFromSpace(companyTicker,financialPost);

            if(fundamentalsRequest == null)
            {
                System.out.println("No financial post named " + financialPost + " for company " + companyTicker);
                return;
            }

            updateCompanyData(fundamentalsRequest);

        } catch (InterruptedException e) {
            System.out.println("FinancialTimes failed getting " + financialPost + " from company " + companyTicker);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
