package dk.dtu.company;


import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.Date;

/**
 * Company whose fundamentals are based off stochastic processes or directly probability distributions
 */
public class StochasticCompany extends Company {

    public StochasticCompany(String companyName, String companyTicker, int ipoYear, Space fundamentalsSpace) {
        super(companyName, companyTicker, ipoYear, fundamentalsSpace);
        NormalDistribution normalDistribution = new NormalDistribution(ipoYear,3);

        //A normal distribution centered around the real ipoYear with approx. a 95% chance of being IPO'ed between +/- 5 years of the real date
        ipoYear = (int) normalDistribution.sample();

    }

    @Override
    int calculateTotalNrShares() {
        BinomialDistribution binomialDistribution = new BinomialDistribution(10000,0.9);
        return binomialDistribution.sample();
    }

    @Override
    int getIPOSharesFloated() {
        BinomialDistribution binomialDistribution = new BinomialDistribution(getTotalNrShares(),0.2);
        return binomialDistribution.sample();
    }

    @Override
    float calculateIPOPrice() {
        NormalDistribution normalDistribution = new NormalDistribution(100,10);
        return (float) normalDistribution.sample();
    }

    @Override
    boolean isTimeToIPO(int ipoYear, int ingameDate) {
        return (ipoYear>=ingameDate);
    }


    @Override
    public void updateFundamentalData(int ingameDate) {
        try {
            Object[] previousFundamentals = fundamentalsSpace.getp(new ActualField(this.companyTicker),new ActualField(Date.class), new FormalField(String.class));

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public float getFundamentalData(String financialPost) {
        return 0;
    }
}
