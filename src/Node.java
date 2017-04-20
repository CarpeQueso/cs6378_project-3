import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.Socket;


public class Node implements Runnable {

    private final int id;

    private final String hostname;

    private final int port;

    private final int timeBetweenRequests;

    private final int timeInCriticalSection;

    private final int numTotalRequests;

    private int numRequestsSatisfied;

    private MutexController mutexController;

    private ServerController serverController;

    private Map<Integer, Neighbor> neighbors;

    public Node(int id, String hostname, int port, String mutexAlgorithm,
                int timeBetweenRequests, int timeInCriticalSection, int numTotalRequests) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
        this.timeBetweenRequests = timeBetweenRequests;
        this.timeInCriticalSection = timeInCriticalSection;
        this.numTotalRequests = numTotalRequests;

        this.numRequestsSatisfied = 0;

        this.serverController = new ServerController(port);
        this.neighbors = new HashMap<>();

        if (mutexAlgorithm.equals("lamport")) {
            mutexController = new LamportMutexController(id, this.serverController);
        } else if (mutexAlgorithm.equals("ricart-agrawala")) {
            mutexController = new RicartAgrawalaMutexController(id, this.serverController);
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
        this.serverController.start();
    }

    public void stopServer() {
        this.serverController.stop();
    }

    public void addNeighbor(int neighborId, String neighborHostname, int neighborPort) {
        this.neighbors.put(neighborId, new Neighbor(neighborId, neighborHostname, neighborPort));
        mutexController.addNeighbor(neighborId, neighborHostname, neighborPort);
    }

    public void unicast(int neighborId, Message message) {
        Neighbor neighbor = neighbors.get(neighborId);

        if (neighbor != null) {
            try (Socket s = new Socket(neighbor.getHostname(), neighbor.getPort());
                 PrintWriter out = new PrintWriter(s.getOutputStream())) {
                out.println(message.toString());
            } catch (IOException e) {
                System.err.printf("Unable to send unicast message from %d to %d", this.id,
                                  neighborId);
            }
        }
    }

    public void broadcast(Message message) {
        for (Neighbor neighbor : neighbors.values()) {
            try (Socket s = new Socket(neighbor.getHostname(), neighbor.getPort());
                 PrintWriter out = new PrintWriter(s.getOutputStream())) {
                out.println(message.toString());
            } catch (IOException e) {
                System.err.printf("Unable to send broadcast message from %d to %d", this.id,
                                  neighbor.getId());
            }
        }
    }



}
