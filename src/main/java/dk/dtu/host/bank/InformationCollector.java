package dk.dtu.host.bank;

import dk.dtu.client.ClientUtil;
import dk.dtu.client.Order;
import dk.dtu.host.HostUtil;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.io.IOException;

public class InformationCollector implements Runnable {

    private Space informationSpace;
    private String name;
    private RemoteSpace traderAccountSpace;
    private int portBank = HostUtil.getBankPort();

    public InformationCollector(Space informationSpace, String name) {
        this.informationSpace = informationSpace;
        this.name = name;
    }

    @Override
    public void run() {
        switch (name) {
            case "GetBuyer":
            case "GetSeller":
                try {
                    getTraderAccount();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            case "QueryInfo":
                try {
                    queryInfo();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
        }

    }

    private void queryInfo() throws IOException{

        try {
            // spaceName, orderId, companyTicker
            Object[] info = informationSpace.get(new ActualField("in" + name), new FormalField(String.class), new FormalField(String.class));
            String orderId = (String) info[1];
            String companyTicker = (String) info[2];
            RemoteSpace companyStockSpace = new RemoteSpace(ClientUtil.getHostUri(companyTicker, HostUtil.getExchangePort(), "keep"));
            // TraderId, OrderId, OrderType, Order, reservedAmount
            Object[] order = companyStockSpace.query(new FormalField(String.class), new ActualField(orderId), new ActualField("sell"), new FormalField(Order.class), new FormalField(Integer.class));
            informationSpace.put(("out" + name));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getTraderAccount() throws IOException {
        try {
            traderAccountSpace = new RemoteSpace(ClientUtil.getHostUri("bankInformationSpace", portBank, "keep"));
            informationSpace.get(new ActualField("token"));
            Object[] traderAccount = informationSpace.get(new ActualField("in" + name), new FormalField(String.class));
            String traderId = (String) traderAccount[1];
            Object[] account = traderAccountSpace.get(new ActualField(traderId), new FormalField(BankAccount.class));
            BankAccount bankAccount = (BankAccount) account[1];
            informationSpace.put(("out" + name), traderId, bankAccount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
