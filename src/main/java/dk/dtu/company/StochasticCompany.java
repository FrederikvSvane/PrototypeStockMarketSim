package dk.dtu.company;


import org.apache.commons.math3.distribution.NormalDistribution;
import org.jspace.Space;

import java.time.LocalDateTime;

/**
 * Company whose fundamentals are based off stochastic processes or directly probability distributions
 */
public class StochasticCompany extends Company {

    public StochasticCompany(String companyName, String companyTicker, LocalDateTime ipoDateTime, Space fundamentalsSpace) {
        super(companyName, companyTicker, ipoDateTime, fundamentalsSpace);


        //A normal distribution with a 95% C.I between [-6,6]
        NormalDistribution normalDistribution = new NormalDistribution(0,3);
        int deltaMonth = (int) normalDistribution.sample();
        if(deltaMonth<0)
        {
            //System.out.println("Changing ipoDateTime from " + ipoDateTime + " to " + ipoDateTime.minusMonths(deltaMonth));
            ipoDateTime = ipoDateTime.minusMonths(deltaMonth);
            //System.out.println("Changed ipoDateTime to " + ipoDateTime);
        }
        else
        {
            //System.out.println("Changing ipoDateTime from " + ipoDateTime + " to " + ipoDateTime.plusMonths(deltaMonth));
            ipoDateTime = ipoDateTime.plusMonths(deltaMonth);
            //System.out.println("Changed ipoDateTime to " + ipoDateTime);
        }

    }
}
