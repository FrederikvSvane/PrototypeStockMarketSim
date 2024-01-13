package dk.dtu;

import dk.dtu.company.Company;
import org.jspace.*;

public class Exchange implements Runnable {
    private SpaceRepository exchangeRepository;

    //This space contains the companies, whose stocks are traded at the exchange, and their current respective prices
    //Structure: (companyId, companyName, companyTicker, currentStockPrice) + Ticket
    private Space companiesAndPriceHistorySpace = new SequentialSpace();

    //This space contains the orders that the exchange has to process
    //Structure: (orderId, orderType, Company, amount, price)
    private Space exchangeRequestSpace = new SequentialSpace();

    public Exchange(SpaceRepository exchangeRepository) throws InterruptedException {
        this.exchangeRepository = exchangeRepository;


        this.companiesAndPriceHistorySpace.put("ticket");
        this.exchangeRepository.add("companiesAndPricesHistorySpace", companiesAndPriceHistorySpace);
        this.exchangeRepository.add("exchangeRequestSpace", exchangeRequestSpace);
    }

    public void run() {
        while (true) {
            try {
                // Structure: orderId, orderType, Company, amount, price
                Object[] currentRequest = exchangeRequestSpace.getp(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Float.class));
                if (currentRequest != null) {
                    String orderType = currentRequest[1].toString();
                    switch (orderType) {
                        case "IPO": //Initial Public Offering - The first time a company sells its stocks at the exchange - this is when it is registered
                            String companyName = (String) currentRequest[3];
                            String companyId = (String) currentRequest[2];
                            String companyTicker = (String) currentRequest[4];
                            int amount = (int) currentRequest[5];
                            float price = (float) currentRequest[6];

                            Space companiesAndPricesSpace = exchangeRepository.get("companiesAndPricesHistorySpace");
                            Object[] currentCompanyStatus = companiesAndPricesSpace.queryp(new ActualField(companyId), new FormalField(Company.class), new FormalField(Float.class));
                            boolean companyExists = currentCompanyStatus != null;
                            if (companyExists) {
                                throw new RuntimeException("IPO failed: Company is already listed on the exchange");
                            } else {
                                // Laver et nyt space med ticker navnet, som indeholder alle de aktier, som er til salg for den pågældende virksomhed
                                createCompanyStockSpace(companyTicker);
                                Space companyStockSpace = exchangeRepository.get(companyTicker);

                                Order order = new Order(companyId, companyName,companyTicker, amount, price);
                                String orderId = order.getOrderId();
                                companyStockSpace.put(companyId, orderId, "sell", order);

                                // Here the currentStockPrice is set as the IPO price. This is an exception. Normally the currentStockPrice is set by latest sold price
                                //TODO den skal laves om til en Queue, så vi kan se historikken
                                companiesAndPricesSpace.put(companyId, companyName, companyTicker, price);

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



}
