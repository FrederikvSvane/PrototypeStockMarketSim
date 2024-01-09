package dk.dtu;

public class DistributedClient {

    public String createUri(String hostIp, int hostPort, String roomName) {
        return "tcp://" + hostIp + ":" + hostPort + "/" + roomName + "?keep";
    }
}
