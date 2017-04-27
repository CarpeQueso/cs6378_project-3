
import java.util.HashMap;
import java.util.Map.Entry;

public class RicartAgrawalaMutexController extends MutexController {

    private boolean enterCS;
    private boolean outstandingReplies;
    private HashMap<Integer, Boolean> replyReceived;
    private boolean requestingCS;
    private int nodeNum;
    private int localClockValue;
    private HashMap<Integer, Message> deferrredRequests;

    public RicartAgrawalaMutexController(int id, ServerController serverController) {
        super(id, serverController);
        this.replyReceived = new HashMap<Integer, Boolean>();
        this.requestingCS = false;
        nodeNum = this.neighbors.size();
        for (int i = 0; i < nodeNum; i++) {
            this.replyReceived.put(i, false);
        }
        this.replyReceived.put(id, true);
        this.localClockValue = getClockValue();
        this.outstandingReplies = true;
        this.enterCS = false;
        serverController.register(MessageType.RICART_AGRAWALA, messageQueue);
    }

    public void csEnter() {
        /*  On generating a critical section request
            -  Broadcast the request 
         */
        requestingCS = true;
        localClockValue = getClockValue();
        Message message = new Message(MessageType.RICART_AGRAWALA, id, "Request" + ":" + localClockValue);
        this.broadcast(message);
        this.incrementClock();
        //On receiving message
        while (enterCS == false) {
            if (!messageQueue.isEmpty()) {
                String[] messageInfo = messageQueue.poll().toString().split(":");
                int senderId = Integer.parseInt(messageInfo[1]);
                String type = messageInfo[2];
                this.incrementClock();

                //On receiving reply message,insert into replyReceived map;
                if (type.equals("Reply")) {
                    int clockValue = Integer.parseInt(messageInfo[3]);
                    replyReceived.put(senderId, true);
                    this.updateClock(clockValue);
                    this.incrementClock();
                } /*On receiving request message
                    - Send a reply message to the requesting process 
                    if there is no unfulfilled request or
                    unfulfilled request has larger timestamp than the requested process.
                 */ else if (type.equals("Request")) {
                    int clockValue = Integer.parseInt(messageInfo[3]);
                    if ((!requestingCS) || (localClockValue > clockValue)) {
                        this.unicast(senderId, new Message(MessageType.RICART_AGRAWALA, id, "Reply" + ":" + localClockValue));
                        this.incrementClock();
                    } /* else defer the request until CS execution.*/ else {
                        Message msg = new Message(MessageType.RICART_AGRAWALA, id, "Reply" + ":" + localClockValue);
                        deferrredRequests.put(senderId, msg);
                    }
                }
            }
            /*For entering critical section 
              - Received all REPLY message.
             */
            if (replyReceived.containsValue(false)) {
                outstandingReplies = true;
            } else {
                outstandingReplies = false;
            }
            if (outstandingReplies == false) {
                enterCS = true;
            }
        }
    }

    public void csLeave() {
        /*  On leaving critical section
            - Send all deferred messages.
         */
        for (Entry<Integer, Message> entry : deferrredRequests.entrySet()) {
            this.unicast(entry.getKey(), entry.getValue());
            this.incrementClock();
        }
    }
}
