import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class ServerController implements Runnable {

    private int port;

    private Map<MessageType, Queue<Message>> messageQueueMap;

    private volatile boolean running;

    public ServerController(int port) {
        this.port = port;
        messageQueueMap = new HashMap<>();
    }

    // For now, only one queue per type.
    public boolean register(MessageType type, Queue<Message> queue) {
        if (!this.messageQueueMap.containsKey(type)) {
            this.messageQueueMap.put(type, queue);
            return true;
        }
        return false;
    }

    public void run() {
        running = true;

        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            while (running) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientConnectionManager(socket, messageQueueMap)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop() {
        running = false;
    }
}
