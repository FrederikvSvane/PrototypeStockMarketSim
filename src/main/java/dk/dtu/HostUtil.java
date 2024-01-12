package dk.dtu;


import org.jspace.SpaceRepository;

public class HostUtil {
    private static String hostIp = "10.209.145.90";;
    private static int hostPort = 10157;;
    private static SpaceRepository hostRepo;
    private HostUtil() {
        // Private constructor to prevent instantiation
    }

    public static void initialize(String connectionType)
    {
        hostRepo = new SpaceRepository();
        hostRepo.addGate("tcp://" + hostIp + ":" + hostPort + "/?" + connectionType);
    }

    public static String getHostIp() {
        return hostIp;
    }
    public static int getHostPort() {
        return hostPort;
    }

    public static SpaceRepository getHostRepo()
    {
        if(hostRepo == null)
        {
            throw new RuntimeException("You're not on the host machine and can therefore not access the host repo like this!");
        }

        return hostRepo;
    }


}
