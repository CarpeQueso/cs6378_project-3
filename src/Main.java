import java.util.concurrent.TimeUnit;


public class Main {

    /**
     * The main method expects an argument list as described below. Execution
     * will fail without them.
     *
     * args[0] - The unique integer id this node will use to identify itself.
     * args[1] - The hostname of the node.
     * args[2] - The port number on which this node will receive messages.
     * args[3] - Time between requests
     * args[4] - Time to execute critical section
     * args[5] - Number of requests each process will generate
     * args[6] - The name of the mutual exclusion algorithm to use ("lamport" or "ricart-agrawala")
     * args[7] - This node's immediate neighbors. A string of the form:
     *           node0Id:node0Hostname:node0Port,node1Id:node1Hostname:node1Port,etc.
     */
    public static void main(String[] args) {
        int id = Integer.parseInt(args[0]);
        String hostname = args[1];
        int port = Integer.parseInt(args[2]);
        int timeBetweenRequests = Integer.parseInt(args[3]);
        int timeInCriticalSection = Integer.parseInt(args[4]);
        int numTotalRequests = Integer.parseInt(args[5]);
        String mutexAlgorithmName = args[6];
        String neighborString = args[7];

        System.out.println("Id: " + id);
        System.out.println("Hostname: " + hostname);
        System.out.println("Port: " + port);

        Node node = new Node(id, hostname, port, mutexAlgorithmName, timeBetweenRequests,
                             timeInCriticalSection, numTotalRequests);

        String[] neighbors = neighborString.split(",");

        for (String n : neighbors) {
            String[] neighborParams = n.split(":");

            int neighborId = Integer.parseInt(neighborParams[0]);
            String neighborHostname = neighborParams[1];
            int neighborPort = Integer.parseInt(neighborParams[2]);

            if (neighborId != id) {
                node.addNeighbor(neighborId, neighborHostname, neighborPort);
            }
        }

        node.startServer();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        node.run();

        node.stopServer();
    }
}
