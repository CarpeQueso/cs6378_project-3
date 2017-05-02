import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.*;
import java.net.Socket;


public class Node implements Runnable {

    private final int id;

    private final String hostname;

    private final int port;

    private final int timeBetweenRequests;

    private final int timeInCriticalSection;

    private final int numTotalRequests;

    private final String mutexAlgorithm;

    private int numRequestsSatisfied;

    private MutexController mutexController;

    private ServerController serverController;

    private Map<Integer, Neighbor> neighbors;

    private Queue<Message> haltMessageQueue;

    public Node(int id, String hostname, int port, String mutexAlgorithm,
                int timeBetweenRequests, int timeInCriticalSection, int numTotalRequests) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
        this.timeBetweenRequests = timeBetweenRequests;
        this.timeInCriticalSection = timeInCriticalSection;
        this.numTotalRequests = numTotalRequests;
        this.mutexAlgorithm = mutexAlgorithm;

        this.numRequestsSatisfied = 0;

        this.serverController = new ServerController(port);
        this.neighbors = new HashMap<>();
        this.haltMessageQueue = new ConcurrentLinkedQueue<>();

        this.serverController.register(MessageType.HALT, this.haltMessageQueue);
    }

    public void run() {
		//String filename = "/home/012/j/ja/jac161530/CS6378/cs6378_project-3/shared-" + id + ".out";
		String filename = "/home/012/j/ja/jac161530/CS6378/cs6378_project-3/shared.out";
        while (numRequestsSatisfied < numTotalRequests) {
            mutexController.csEnter();
            long timeEnter = System.currentTimeMillis();

            try(FileWriter fw = new FileWriter(filename, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
                out.println(id + ":" + mutexController.getClockValue() + ":enter");
                while (System.currentTimeMillis() - timeEnter < timeInCriticalSection);
                out.println(id + ":" + mutexController.getClockValue() + ":exit");
				out.flush();
            } catch (IOException e) {
                System.err.println("Could not open file! Possible inconsistency.");
            }

            mutexController.csLeave();

            numRequestsSatisfied++;

			long waitTimer = System.currentTimeMillis();
            while (System.currentTimeMillis() - waitTimer < timeBetweenRequests) {
				mutexController.handleIncomingMessages();
			}
        }

		System.out.printf("Id: %d, Requests Satisfied: %d\n", this.id, this.numRequestsSatisfied);

        broadcast(new Message(MessageType.HALT, this.id, "None"));

        boolean shouldHalt = false;
        int numHaltMessagesReceived = 0;

        while (!shouldHalt) {
			mutexController.handleIncomingMessages();
            if (!haltMessageQueue.isEmpty()) {
                Message message = haltMessageQueue.poll();

                numHaltMessagesReceived++;
                if (numHaltMessagesReceived == neighbors.size()) {
                    shouldHalt = true;
                }
            }
        }
    }

    public void startServer() {
        if (mutexAlgorithm.equals("lamport")) {
            mutexController = new LamportMutexController(id, this.serverController, neighbors.size() + 1);
        } else if (mutexAlgorithm.equals("ricart-agrawala")) {
            mutexController = new RicartAgrawalaMutexController(id, this.serverController, neighbors.size() + 1);
        } else {
            mutexController = null;
            System.err.println("No mutex algorithm found!");
        }

		for (Neighbor n : neighbors.values()) {
        	mutexController.addNeighbor(n.getId(), n.getHostname(), n.getPort());
		}

        this.serverController.start();
    }

    public void stopServer() {
        this.serverController.stop();
    }

    public void addNeighbor(int neighborId, String neighborHostname, int neighborPort) {
        this.neighbors.put(neighborId, new Neighbor(neighborId, neighborHostname, neighborPort));
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
