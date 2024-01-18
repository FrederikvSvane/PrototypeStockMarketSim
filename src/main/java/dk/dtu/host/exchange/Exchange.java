package dk.dtu.host.exchange;

import dk.dtu.client.ClientUtil;
import dk.dtu.client.Order;
import dk.dtu.company.Company;
import dk.dtu.host.HostUtil;
import org.jspace.*;

public class Exchange implements Runnable {
    private SpaceRepository exchangeRepository;

    //This space contains the companies, whose stocks are traded at the exchange, and their current respective prices
    //Structure: (companyId, companyName, companyTicker, currentStockPrice) + Ticket
    private Space companiesAndPriceHistorySpace = new SequentialSpace();

    //This space contains the orders that the exchange has to process
    //Structure: (orderId, orderType, companyId, companyName, companyTicker, amount, price)
    private Space exchangeRequestSpace = new SequentialSpace();

    //Structure: (companyTicker, orderId, orderType, order)
    private Space companyOrderSpace;

    public Exchange(SpaceRepository exchangeRepository) throws InterruptedException {
        this.exchangeRepository = exchangeRepository;


        this.companiesAndPriceHistorySpace.put("ticket");
        this.exchangeRepository.add("companiesAndPriceHistorySpace", companiesAndPriceHistorySpace);
        this.exchangeRepository.add("exchangeRequestSpace", exchangeRequestSpace);
        this.exchangeRepository.addGate(ClientUtil.getHostUri("", HostUtil.getExchangePort(),"keep"));

    }

    public void run() {
        while (true) {
            try {
                // Structure: orderId, orderType, Company, amount, price
                Object[] currentRequest = exchangeRequestSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Float.class));
                if (currentRequest != null) {
                    String orderType = currentRequest[1].toString();
                    switch (orderType) {
                        case "IPO": //Initial Public Offering - The first time a company sells its stocks at the exchange - this is when it is registered
                            String companyName = (String) currentRequest[3];
                            companyName = companyName.toLowerCase();
                            String companyId = (String) currentRequest[2];
                            String companyTicker = (String) currentRequest[4];
                            int amount = (int) currentRequest[5];
                            float price = (float) currentRequest[6];

                            Space companiesAndPricesSpace = exchangeRepository.get("companiesAndPriceHistorySpace");
                            Object[] currentCompanyStatus = companiesAndPricesSpace.queryp(new ActualField(companyId), new FormalField(String.class) /*companyName*/, new FormalField(String.class) /*companyTicker*/, new FormalField(Float.class) /*price*/);

                            boolean companyExists = currentCompanyStatus != null;
                            if (companyExists) {
                                throw new RuntimeException("IPO failed: Company is already listed on the exchange");
                            } else {
                                // Laver et nyt space med ticker navnet, som indeholder alle de aktier, som er til salg for den pågældende virksomhed
                                createCompanyStockSpace(companyTicker);
                                Space companyStockSpace = exchangeRepository.get(companyTicker);

                                Order order = new Order(companyId, companyName, companyTicker, amount, price);
                                String orderId = order.getOrderId();
                                companyStockSpace.put(companyTicker, orderId, "sell", order);

                                // Here the currentStockPrice is set as the IPO price. This is an exception. Normally the currentStockPrice is set by latest sold price
                                //TODO den skal laves om til en Queue, så vi kan se historikken
                                companiesAndPricesSpace.put(companyId, companyName, companyTicker, price);
                                Object[] resultingStockOrder = companyStockSpace.get(new ActualField(companyTicker), new FormalField(String.class), new FormalField(String.class), new FormalField(Order.class));
                                String currCompanyTicker = (String) resultingStockOrder[0];
                                Order resultingOrder = (Order) resultingStockOrder[3];
                                System.out.println("IPO order added to the space of " + currCompanyTicker + ": " + resultingOrder.toString());

                            }
                            break;
                    }
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    public void createCompanyStockSpace(String companyTicker) throws InterruptedException {
        Space space = new SequentialSpace();
        space.put("ticket");
        exchangeRepository.add(companyTicker, space);
    }

    public String getCompanyTickerFromId(String companyId) throws InterruptedException {
        Object[] company = companiesAndPriceHistorySpace.queryp(new ActualField(companyId), new FormalField(Company.class), new FormalField(Float.class));
        if (company == null) {
            throw new RuntimeException("Company does not exist");
        }
        return company[2].toString();
    }
}
