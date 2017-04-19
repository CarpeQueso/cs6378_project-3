


public class Node implements Runnable {

    private final int id;

    private final String hostname;

    private final int port;

    private final int timeBetweenRequests;

    private final int timeInCriticalSection;

    private final int numTotalRequests;

    private final MutexController mutexController;

    private int numRequestsSatisfied;

    public Node(int id, String hostname, int port, String mutexAlgorithm,
                int timeBetweenRequests, int timeInCriticalSection, int numTotalRequests) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
        this.timeBetweenRequests = timeBetweenRequests;
        this.timeInCriticalSection = timeInCriticalSection;
        this.numTotalRequests = numTotalRequests;

        this.numRequestsSatisfied = 0;

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
        while (numRequestsSatisfied < numTotalRequests) {
            mutexController.csEnter();

            // Do something

            mutexController.csLeave();

            numRequestsSatisfied++;

            try {
                TimeUnit.MILLISECONDS.sleep(timeBetweenRequests);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
