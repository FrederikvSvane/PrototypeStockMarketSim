package dk.dtu;

import org.jspace.*;

import java.util.List;

public class Exchange implements Runnable {

    Space companyToFromHost = new SequentialSpace();
    SpaceRepository repository;

    public Exchange(String hostIp, int hostPort, SpaceRepository repository) {
        this.repository = repository;
        repository.addGate("tcp://" + hostIp + ":" + hostPort + "/?keep");

        //
        repository.add("AAPL", companyToFromHost);


        //add company spaces to repository, one for each company
        Space companySpace = new SequentialSpace();
        repository.add("companySpace", companySpace);


    }

    public void run() {

        boolean resifed = false;
        while (true) {
            System.out.println("Host is running");
            Space companySpace = repository.get("AAPL");
            Object[] request = null;


            try {
                while (!resifed){
                    request = companySpace.query(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Order.class));
                    if (request != null){
                        resifed = true;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Host received request: "+ request[0].toString()+ " " + request[1].toString()+ " " + request[2].toString());
            Order order = (Order) request[3];
            String orderinfo = order.toString();
            System.out.println("Host received request: " + " " + request[2].toString()+ " " + orderinfo);
            resifed = false;
            request = null;
        }
    }


    // getp
    public void getOrders() {

    }

    public void makeCompany(String[] companys) {


    }

    private String[] getNotInitilizedCompany() throws InterruptedException {
        List<Object[]> company = companyToFromHost.getAll();

        if (company.size() > 0) {

            // aktier støkpris
            String[] companyNames = new String[company.size()];

        }
        return new String[0];
    }
}
