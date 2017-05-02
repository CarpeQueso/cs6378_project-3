import java.util.*;

public class LamportMutexController extends MutexController{
    private boolean allReplyReceived;
    private PriorityQueue<LamportRequest> pq;
    private HashMap<Integer,Boolean> replyReceived;
    private int[] lastRequestClockReceived;
    private boolean enterCriticalSection;
    private LamportRequest currentRequest;
    private int nodeNum;

    public LamportMutexController(int id, ServerController serverController, int numNodes) {
        super(id, serverController);
        this.replyReceived = new HashMap<>();
        this.lastRequestClockReceived = new int[numNodes];
        for (int i = 0; i < numNodes; i++){
            this.replyReceived.put(i, false);
        }
        this.replyReceived.put(id, true);
        this.enterCriticalSection = false;
        Comparator<LamportRequest> comparator = new Comparator<LamportRequest>() {
                public int compare(LamportRequest r1, LamportRequest r2) {
                    // Compare clock values
                    int clockValueCompareResult 
                    = Integer.compare(r1.getClockValue(), r2.getClockValue());
                    if (clockValueCompareResult == 0) {
                        return Integer.compare(r1.getId(), r2.getId());
                    }
                    return clockValueCompareResult;
                }
            };
        this.pq = new PriorityQueue<>(11, comparator);
        serverController.register(MessageType.LAMPORT, messageQueue);
    }

    public void csEnter() {
        /*
         * On generating request message
         * 1.Insert request into priority queue
         * 2.Broadcast request message to all nodes
         */	

        // Local clockvalue and sender pair to add into priority queue
        this.currentRequest = new LamportRequest(this.id, this.getClockValue());

        pq.add(currentRequest);

        this.incrementClock();
        this.broadcast(new Message(MessageType.LAMPORT,
                                   this.id,
                                   "Request:" + this.currentRequest.getClockValue()));

        while (!enterCriticalSection) {
            if (!messageQueue.isEmpty()) {
                Message message = messageQueue.poll();
                String[] messageInfo = message.getBody().split(":");
                int clockValue = Integer.parseInt(messageInfo[1]);

                if (clockValue > this.currentRequest.getClockValue()) {
                    replyReceived.put(message.getSenderId(), true);
                }			

                processMessage(message);
            }

            /*For entering critical section 
             * 1.Received all reply message;
             * 2.Own request is at the top of it's queue;
             */
            if (replyReceived.containsValue(false)) {
                // Do nothing
            } else {
                if (pq.peek().getId() == this.id) {
                    enterCriticalSection = true;
                }
            }
        }
    }

    public void csLeave() {
        /* 
         * On leaving critical section
         * 1.Remove the request from the queue
         * 2.Broadcast a release message to all process
         */
        this.incrementClock();
        this.broadcast(new Message(MessageType.LAMPORT,id,"Release:" + this.getClockValue()));
        pq.remove(this.currentRequest);
        for (Map.Entry<Integer, Boolean> entry : replyReceived.entrySet()) {
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

        if (type.equals("Reply")){
            // Do nothing
        } else if (type.equals("Request")){
            /* 
             * On receiving request message
             * 1.Insert the request into the priority queue
             * 2.Send a reply message to the requesting process
             */
            LamportRequest incomingRequest = new LamportRequest(senderId, clockValue);
            pq.add(incomingRequest);
            this.lastRequestClockReceived[senderId] = clockValue;
            this.incrementClock();
            this.unicast(senderId, new Message(MessageType.LAMPORT, 
                                               this.id,
                                               "Reply:" + this.getClockValue()));
        } else if (type.equals("Release")) {
            /* 
             * On receiving a release message
             * 1. Remove request from the queue
             */
            pq.remove(new LamportRequest(senderId, this.lastRequestClockReceived[senderId]));
        } else {
            System.err.println("No message of type: " + type);
        }
    }
}
