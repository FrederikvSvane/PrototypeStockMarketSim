package dk.dtu;

import org.jspace.*;

public class Exchange implements Runnable {
    private SpaceRepository exchangeRepository;

    //This space contains the companies, whose stocks are traded at the exchange, and their current respective prices
    //Structure: (companyId, companyName, companyTicker, currentStockPrice) + Ticket
    private Space companiesAndPricesSpace = new SequentialSpace();

    //This space contains the orders that the exchange has to process
    //Structure: (orderId, orderType, Company, amount, price)
    private Space exchangeRequestSpace = new SequentialSpace();

    public Exchange(SpaceRepository exchangeRepository) throws InterruptedException {
        this.exchangeRepository = exchangeRepository;
        this.companiesAndPricesSpace.put("ticket");
        this.exchangeRepository.add("companiesAndPricesSpace", companiesAndPricesSpace);
        this.exchangeRepository.add("exchangeRequestSpace", exchangeRequestSpace);
        String uri = ClientUtil.getHostUri("");
        String uriConnection = ClientUtil.setConnectType(uri, "keep"); // TODO skriv til alberdo om vi skal bruge keep eller ingenting
        exchangeRepository.addGate(uriConnection);
    }

    public void run() {
        while (true) {
            try {
                // Structure: orderId, orderType, Company, amount, price
                Object[] currentRequest = exchangeRequestSpace.getp(new FormalField(String.class), new FormalField(String.class), new FormalField(Company.class), new FormalField(Integer.class), new FormalField(Float.class));
                if (currentRequest != null) {
                    String orderType = currentRequest[1].toString();
                    switch (orderType) {
                        case "IPO": //Initial Public Offering - The first time a company sells its stocks at the exchange
                            Company company = (Company) currentRequest[2];
                            String companyName = company.getCompanyName();
                            String companyId = company.getCompanyId();
                            String companyTicker = company.getCompanyTicker();
                            int amount = (int) currentRequest[3];
                            float price = (float) currentRequest[4];

                            Space companiesAndPricesSpace = exchangeRepository.get("companiesAndPricesSpace");
                            Object[] currentCompanyStatus = companiesAndPricesSpace.queryp(new ActualField(companyId), new ActualField(companyName), new ActualField(companyTicker), new FormalField(Float.class));
                            boolean companyExists = currentCompanyStatus != null;
                            if (companyExists) {
                                throw new RuntimeException("IPO failed: Company is already listed at the exchange");
                            } else {
                                createCompanyStockSpace(companyTicker);
                                Space companyStockSpace = exchangeRepository.get(companyTicker);

                                Order order = new Order(companyId, companyName, amount, price);
                                String orderId = order.getOrderId();
                                companyStockSpace.put(companyId, orderId, "sell", order);

                                // Here the currentStockPrice is set as the IPO price. This is an exception. Normally the currentStockPrice is set by latest sold price
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
