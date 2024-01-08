package dk.dtu;

import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.util.List;

public class Exchange implements Runnable {

    Space companyToFromHost = new SequentialSpace();
    Space orderSpace = new SequentialSpace();
    SpaceRepository repository;

    public Exchange(String hostIp, int hostPort, SpaceRepository repository) {
        this.repository = repository;
        repository.addGate("tcp://" + hostIp + ":" + hostPort + "/?keep");

        //
        repository.add("orderSpace", orderSpace);
        repository.add("company", companyToFromHost);


        //add company spaces to repository, one for each company
        Space companySpace = new SequentialSpace();
        repository.add("companySpace", companySpace);


    }

    public void run() {

        while (true) {
            try {
                makeCompany(getNotInitilizedCompany());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            getOrders();

            // initilize company
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
