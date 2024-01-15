package dk.dtu.client.datafetcher;

import dk.dtu.company.StochasticCompany;
import org.jspace.Space;

import java.io.IOException;
import java.util.List;

public class NameDataFetcher extends DataFetcher implements Runnable{

    public NameDataFetcher(Space traderDataSpace) {
        super(traderDataSpace, 1000, "companiesAndPricesHistorySpace");
    }

    public void run()  {
        // Tilkobl til Host data space
        try {
            connectToDataSpace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            // Hent data fra Host data space
            List<Object[]> companyData;
            try {
                companyData = QueryAllCompanies();
                updateCompanyData(companyData);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }


    @Override
    void updateCompanyData(List<Object[]> companyData) throws InterruptedException {
        for(Object[] companyList : companyData) {
            System.out.println("Updating company data");
            String companyId = (String) companyList[0];
            String companyName = (String) companyList[1];
            String companyTicker = (String) companyList[2];
            if(companyNotInTraderSpace(companyId)) {
                traderDataSpace.put(companyId, companyName, companyTicker);
            }

        }
    }
}