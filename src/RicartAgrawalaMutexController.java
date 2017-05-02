
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RicartAgrawalaMutexController extends MutexController {

    private int nodeNum;
    private int requestClockValue;
    private boolean requestingCriticalSection;
    private HashMap<Integer, Boolean> replyReceived;
    private LinkedList<Integer> deferredRequests;

    public RicartAgrawalaMutexController(int id, ServerController serverController, int numNodes) {
        super(id, serverController);
        this.replyReceived = new HashMap<>();
        for (int i = 0; i < numNodes; i++) {
            this.replyReceived.put(i, false);
        }
        this.replyReceived.put(id, true);
        this.requestingCriticalSection = false;
        this.requestClockValue = 0;
        this.deferredRequests = new LinkedList<>();
        serverController.register(MessageType.RICART_AGRAWALA, messageQueue);
    }

    public void csEnter() {
        /*  On generating a critical section request
            -  Broadcast the request 
        */
        boolean shouldEnterCriticalSection = false;

        this.requestingCriticalSection = true;
        this.requestClockValue = this.getClockValue();

        Message broadcastMessage = new Message(MessageType.RICART_AGRAWALA,
                                               id,
                                               "Request:" + this.requestClockValue);
        this.incrementClock();
        this.broadcast(broadcastMessage);

        while (!shouldEnterCriticalSection) {
            if (!messageQueue.isEmpty()) {
                Message message = messageQueue.poll();
                processMessage(message);
            }

            if (replyReceived.containsValue(false)) {
                // Do nothing
            } else {
                shouldEnterCriticalSection = true;
            }
        }
    }

    public void csLeave() {
        /*  On leaving critical section
            - Send all deferred messages.
        */
        this.requestingCriticalSection = false;
        this.incrementClock();
        // Treat as multicast message and send same clock value with all requests
        int clockValue = this.getClockValue();
        while (!deferredRequests.isEmpty()) {
            int receiverId = deferredRequests.poll();
            this.unicast(receiverId, new Message(MessageType.RICART_AGRAWALA,
                                                 this.id,
                                                 "Reply:" + clockValue));
        }

        for (Map.Entry<Integer, Boolean> entry : this.replyReceived.entrySet()) {
            if (entry.getKey() != this.id) {
                entry.setValue(false);
            }
        }
    }

    public void processMessage(Message message) {
        String[] messageInfo = message.getBody().split(":");
        int senderId = message.getSenderId();
        String type = messageInfo[0];
        int clockValue = Integer.parseInt(messageInfo[1]);

        this.updateClock(clockValue);
        this.incrementClock();

        if (type.equals("Reply")) {
            // On receiving reply message, insert into replyReceived map;
            replyReceived.put(senderId, true);
        } else if (type.equals("Request")) {
            /*On receiving request message
              - Send a reply message to the requesting process 
              if there is no unfulfilled request or
              unfulfilled request has larger timestamp than the requested process.
            */
            if (this.requestingCriticalSection) {
                if (this.requestClockValue > clockValue) {
                    this.incrementClock();
                    this.unicast(senderId, new Message(MessageType.RICART_AGRAWALA,
                                                       id,
                                                       "Reply:" + this.getClockValue()));
                } else {
                    deferredRequests.offer(senderId);
                }
            } else {
                this.incrementClock();
                this.unicast(senderId, new Message(MessageType.RICART_AGRAWALA,
                                                   id,
                                                   "Reply:" + this.getClockValue()));
            }
        }
    }
}
