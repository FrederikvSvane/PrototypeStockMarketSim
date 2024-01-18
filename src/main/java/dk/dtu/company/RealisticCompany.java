package dk.dtu.company;

import dk.dtu.company.api.FinancialData;
import dk.dtu.host.GlobalClock;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.time.LocalDateTime;
import java.util.List;

public class RealisticCompany extends Company{
    Space latentSpace;
    int lastYearSinceFundamentalUpdate = 0;

    public RealisticCompany(String companyName, String companyTicker, LocalDateTime ipoDateTime, Space fundamentalsSpace, Space latentSpace) {
        super(companyName, companyTicker, ipoDateTime, fundamentalsSpace);
        this.latentSpace = latentSpace;
    }

    @Override
    public void updateFundamentalData(LocalDateTime ingameDate) {
        try{
            if(isPubliclyTraded){
                int year = ingameDate.getYear();
                fundamentalsSpace.getAll(new ActualField(this.companyTicker),new ActualField(LocalDateTime.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Float.class));
                Object[] incomeStatement = latentSpace.get(new ActualField(this.companyTicker),new ActualField("Income Statement"), new ActualField(year), new FormalField(FinancialData.class));
                Object[] balanceSheet = latentSpace.get(new ActualField(this.companyTicker),new ActualField("Balance Sheet"), new ActualField(year), new FormalField(FinancialData.class));
                FinancialData financialData = (FinancialData) incomeStatement[3];
                FinancialData financialData2 = (FinancialData) balanceSheet[3];
                long newRevenue = financialData.getRevenue();
                long newCost = financialData.getCostOfRevenue();
                long newGrossProfit = financialData.getGrossProfit();
                long newAssets = financialData2.getTotalAssets();
                long newLiabilities = financialData2.getTotalDebt();
                long newEquity = financialData2.getNetReceiveable();
                long newCash = financialData2.getCash();

                FinancialData financialData3 = new FinancialData(newRevenue,newCost,newGrossProfit,newAssets,newLiabilities,newEquity,newCash);
                System.out.println("Fundamentals is updated for " + this.companyTicker + " with the following data: " + financialData3.getRevenue() + " " + financialData3.getCostOfRevenue()+ " " + financialData3.getGrossProfit());
                fundamentalsSpace.put(this.companyTicker, year,"Finance Statement",financialData3);
//                fundamentalsSpace.put(this.companyTicker, 2019,"income statement","revenue",newRevenue);
//                fundamentalsSpace.put(this.companyTicker, 2019,"income statement","cost of revenue",newCost);
//                fundamentalsSpace.put(this.companyTicker, ingameDate.getYear(),"income statement","gross profit",newGrossProfit);
                //fundamentalsSpace.put(this.companyTicker, ingameDate.getYear(),"balance sheet","total assets",newAssets);
                //fundamentalsSpace.put(this.companyTicker, ingameDate.getYear(),"balance sheet","total liabilities",newLiabilities);
                //fundamentalsSpace.put(this.companyTicker, ingameDate.getYear(),"balance sheet","total stockholders equity",newEquity);
                //fundamentalsSpace.put(this.companyTicker, ingameDate.getYear(),"balance sheet","cash",newCash);
                //TODO: Add more fundamentals
                //TODO: Maybe just add the FinancialData Object to the tuple space instead.

            }else{
                System.out.println("Company " + this.companyTicker + " is not publicly traded yet, so it cannot update its fundamentals");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean isTimeToUpdateFundamentals(LocalDateTime ingameDateTime) {
        if(ingameDateTime.getYear() == lastYearSinceFundamentalUpdate){
            return false;
        }else {
            lastYearSinceFundamentalUpdate = ingameDateTime.getYear();
            return true;
        }
    }


}
