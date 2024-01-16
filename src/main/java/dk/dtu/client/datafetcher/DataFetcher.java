package dk.dtu.client.datafetcher;

import dk.dtu.client.ClientUtil;
import dk.dtu.company.Company;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import java.io.IOException;
import java.util.List;

abstract class DataFetcher {

    protected Space traderDataSpace;
    protected Space companyDataSpace;;
    protected String hostDataSpaceName;
    protected int sleepTime;

    public DataFetcher(Space traderDataSpace, int sleepTime, String hostDataSpaceName)
    {
        this.traderDataSpace = traderDataSpace;
        this.hostDataSpaceName = hostDataSpaceName;
    }
    public void connectToDataSpace() throws IOException
    {
        String uri = ClientUtil.getHostUri(hostDataSpaceName);
        String uriConnection = ClientUtil.setConnectType(uri,"keep");
        companyDataSpace = new RemoteSpace(uriConnection);
    }

    public List<Object[]> QueryAllCompanies() throws InterruptedException {
        // Structure: (companyId, companyName, companyTicker , float price (TODO senere lav til QueueList<PriceHistory>)) {PriceHistory: <price, date>}
        return companyDataSpace.queryAll(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Float.class));
    }

    abstract void updateCompanyData(List<Object[]> companyData) throws InterruptedException;


    /**
     * Checks if a company is not already in the trader space
     * @param companyId
     * @return true if company is not in trader space
     * @throws InterruptedException
     */
    public boolean companyNotInTraderSpace(String companyId) throws InterruptedException {
        Object[] result = traderDataSpace.queryp(new ActualField(companyId), new FormalField(String.class), new FormalField(String.class));
        return result == null;
    }

}