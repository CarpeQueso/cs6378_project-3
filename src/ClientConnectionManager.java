import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Queue;
import java.util.Map;


public class ClientConnectionManager implements Runnable {

    private Socket socket;

    private Map<MessageType, Queue<Message>> messageQueueMap;

    private volatile boolean running;

    public ClientConnectionManager(Socket socket,
                                   Map<MessageType, Queue<Message>> messageQueueMap) {
        this.socket = socket;
        this.messageQueueMap = messageQueueMap;
    }

    public void run() {
        try {
            BufferedReader reader
                = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            String messageString = reader.readLine();

            while (messageString != null) {
                Message message = Message.parseMessage(messageString);
                if (message != null) {
                    if (messageQueueMap.containsKey(message.getType())) {
                        messageQueueMap.get(message.getType()).offer(message);
                    } else {
                        System.err.println("No queue found for message of type "
                                           + message.getType().name());
                    }
                }

                messageString = reader.readLine();
            }

            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
