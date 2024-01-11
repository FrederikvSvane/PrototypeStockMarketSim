package dk.dtu;

import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.List;

public class DataFetcher implements Runnable {
    public DataFetcher(Space dataSpace) {

    }

    public void run() {
        while(true){
            String hostUri = ClientUtil.getHostUri("companiesAndPriceHistorySpace");
            ClientUtil.setConnectType(hostUri,"keep");
            try {
                RemoteSpace companiesAndPriceHistorySpace = new RemoteSpace(hostUri);
                List<Object[]> result = companiesAndPriceHistorySpace.queryAll(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Object.class));
                //TODO christoffer du må gerne parse resultatet og sende det til Trader ;)
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
