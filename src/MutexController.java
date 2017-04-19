import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.HashMap;
import java.net.Socket;
import java.io.*;


public abstract class MutexController {

    protected final int id;

    protected final String hostname;

    protected final int port;

    private int clockValue;

    private ServerController serverController;

    protected Queue<Message> messageQueue;

    protected Map<Integer, Neighbor> neighbors;

    public MutexController(int id, String hostname, int port) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
        this.clockValue = 0;

        this.neighbors = new HashMap<>();
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.serverController = new ServerController(this.port, this.messageQueue);
    }

    public void startServer() {
        new Thread(serverController).start();
    }

    public void stopServer() {
        serverController.stop();
    }

    public void addNeighbor(int neighborId, String neighborHostname, int neighborPort) {
        neighbors.put(neighborId, new Neighbor(neighborId, neighborHostname, neighborPort));
    }

    public int getClockValue() {
        return this.clockValue;
    }

    public void incrementClock() {
        this.clockValue++;
    }

    public void unicast(int nodeId, Message message) {
        Neighbor neighbor = neighbors.get(nodeId);

        if (neighbor != null) {
            try (Socket s = new Socket(neighbor.getHostname(), neighbor.getPort());
                 PrintWriter out = new PrintWriter(s.getOutputStream())) {
                out.println(message.toString());
            } catch (IOException e) {
                System.err.printf("Unable to send unicast message from %d to %d", this.id, nodeId);
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

    public abstract void csEnter();

    public abstract void csLeave();
}
