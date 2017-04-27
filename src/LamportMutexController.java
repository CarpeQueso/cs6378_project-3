import java.util.*;

public class LamportMutexController extends MutexController{
	private boolean allReplyReceived;
	private int localClockValue;
	private PriorityQueue<int[]> pq;
	private HashMap<Integer,Boolean> replyReceived;
	private boolean enterCriticalSection;
	private int nodeNum;
	
	public LamportMutexController(int id, ServerController serverController) {
       super(id, serverController);
       this.localClockValue = getClockValue();
       this.replyReceived = new HashMap<Integer,Boolean>();
       this.nodeNum = neighbors.size();
       for(int i=0;i<nodeNum;i++){
    	   this.replyReceived.put(i, false);
       }
       this.replyReceived.put(id, true);
       this.allReplyReceived =false;
       this.enterCriticalSection = false;
       Comparator<int[]> comparator = new ArrayComparator();
   	   this.pq = new PriorityQueue<int[]>(3,comparator);
       serverController.register(MessageType.LAMPORT, messageQueue);
	}

    public void csEnter() {
    	/*On generating request message
    	 * 1.Insert request into priority queue
    	 * 2.Broadcast request message to all nodes
    	 */	
    	int[] clockSender = new int[2]; //local clockvalue and sender pair to add into priority queue
    	clockSender[0] =this.getClockValue();
    	clockSender[1] =id;
    	pq.add(clockSender);
    	Message message = new Message(MessageType.LAMPORT,id,"Request"+":"+localClockValue);
    	this.broadcast(message);
    	this.incrementClock();
    	//On receiving message
    	while(enterCriticalSection == false){
    	while(!messageQueue.isEmpty()){
	    	for(int i=0;i<messageQueue.size();i++){
	    		String[] messageInfo = messageQueue.poll().toString().split(":");
	    		int senderId = Integer.parseInt(messageInfo[1]);
	    		String type = messageInfo[2];
    			
	    		//On receiving reply message,insert into replyReceived map;
	    		if(type.equals("Reply")){
	    			int clockValue = Integer.parseInt(messageInfo[3]);
	    			replyReceived.put(senderId, true);
	    			this.updateClock(clockValue);
	      			this.incrementClock();
	    		}
	    		/*On receiving request message
	        	 * 1.Insert the request into the priority queue
	        	 * 2.Send a reply message to the requesting process
	        	 */
	    		else if(type.equals("Request")){
	    			int[] clocksender = new int[2];
	    			int clockValue = Integer.parseInt(messageInfo[3]);
	        		clocksender[0] = clockValue;
	        		clocksender[1] = senderId;
	    			pq.add(clocksender);
	    			this.updateClock(clockValue);
	      			this.incrementClock();
	    			if(enterCriticalSection == false){
	    				this.unicast(senderId, new Message(MessageType.LAMPORT,id,"Reply"+":"+localClockValue));
	    				this.incrementClock();
	    			}
	    		}
	    		/*On receiving a release message
	        	 * 1.Remove request from the queue
	        	 */
	    		else if(type.equals("Release")){
	    			pq.remove();
	    			this.incrementClock();
	    		}
	    		else{
	    			System.err.printf("No message of type"+type);
	    		}
		    }
			
		    	/*For entering critical section 
		    	 * 1.Received all reply message;
		    	 * 2.Own request is at the top of it's queue;
		    	 */
		    	if(replyReceived.containsValue(false)){
		    		allReplyReceived = false;
		    	}
		    	else{
		    		allReplyReceived = true;
		    	}
		    	
		    	
		    	if((allReplyReceived==true)&&(pq.peek()[1]==id)){
		    		enterCriticalSection = true;
		    	}
    		}
    	}
    }

    public void csLeave() {
        /*On leaving critical section
         * 1.Remove the request from the queue
         * 2.Broadcast a release message to all process
         */
    	this.broadcast(new Message(MessageType.LAMPORT,id,"Release"));
		this.incrementClock();
    	pq.remove();
    }
}
