


public class Node implements Runnable {

    private final int id;

    private final String hostname;

    private final int port;

    private final MutexController mutexController;

    public Node(int id, String hostname, int port, String mutexAlgorithm) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;

        if (mutexAlgorithm.equals("lamport")) {
            mutexController = new LamportMutexController(id, hostname, port);
        } else if (mutexAlgorithm.equals("ricart-agrawala")) {
            mutexController = new RicartAgrawalaMutexController(id, hostname, port);
        } else {
            mutexController = null;
            System.err.println("No mutex algorithm found!");
        }
    }

    public void run() {
        while (true) {
            mutexController.csEnter();

            // Do something

            mutexController.csLeave();
        }
    }

    public void startServer() {
        mutexController.startServer();
    }

    public void stopServer() {
        mutexController.stopServer();
    }

    public void addNeighbor(int neighborId, String neighborHostname, int neighborPort) {
        mutexController.addNeighbor(neighborId, neighborHostname, neighborPort);
    }

}
