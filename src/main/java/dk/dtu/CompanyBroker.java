package dk.dtu;

import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;

public class CompanyBroker extends Broker implements Runnable {
    String hostIp;
    int hostPort;
    public CompanyBroker(String hostIp, int hostPort) {
        super(hostIp, hostPort);
    }

    public void run() {
        while (true) {
            try {
                //TODO find en bedre måde at nedarve requestSpace fra Broker.java
                //TODO lyt på strukturen af tuplen på en bedre måde, fordi company ikke nødvendigvis kommer med, hvis det er en salg/købs order
                Object[] request = super.getRequestSpace().get(new FormalField(String.class), new FormalField(Company.class), new FormalField(Order.class));
                String orderType = request[0].toString();
                Company company = (Company) request[1];
                Order order = (Order) request[2];
                int amount = order.getAmount();
                float price = order.getPrice();

                if (orderType.equals("IPO")) {
                    String hostUri = "tcp://" + hostIp + ":" + hostPort + "/exchangeRequestSpace?keep"; //TODO er keep den rigtige forbindelse her? Og hvad med alle andre steder? STOR TODO
                    Space exchangeRequestSpace = new RemoteSpace(hostUri);
                    exchangeRequestSpace.put(order.getOrderId(), orderType, company, amount, price); //TODO måske skal order bare sendes videre?

                } else if (orderType.equals("buy")) {
                    //TODO send buy request to exchange
                } else if (orderType.equals("sell")) {
                    //TODO send sell request to exchange
                }


            } catch (Exception e) {
                throw new RuntimeException("Error in company broker");
            }
        }
    }

    @Override
    public Space getRequestSpace() { return super.getRequestSpace(); }
}
