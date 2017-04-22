import java.util.*;

public class LamportMutexController extends MutexController{
	private boolean allReplyReceived;
	private int localClockValue;
	private PriorityQueue<int[]> pq;
	private int[] clockSender; //the clockvalue and sender pair to add into priority queue
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
       this.clockSender = new int[2];
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
    	clockSender[0] =localClockValue;
    	clockSender[1] =id;
    	pq.add(clockSender);
    	Message message = new Message(MessageType.LAMPORT,id,"Request"+":"+localClockValue);
    	this.broadcast(message);
    	this.incrementClock();
    	//On receiving message
    	while(enterCriticalSection == false){
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
	    			int clockValue = Integer.parseInt(messageInfo[3]);
	        		clockSender[0] = clockValue;
	        		clockSender[1] = senderId;
	    			pq.add(clockSender);
	    			this.updateClock(clockValue);
	      			this.incrementClock();
	    			if(enterCriticalSection == false){
	    				this.unicast(senderId, new Message(MessageType.LAMPORT,id,"Reply"+":"+localClockValue));
	    				this.incrementClock();
	    			}
	    			System.out.println(pq.size());
	    			System.out.println(pq.peek()[1]);
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
		    	
		    	clockSender = pq.peek();
		    	if((allReplyReceived==true)&&(clockSender[1]==id)){
		    		enterCriticalSection = true;
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
    
	    public static void main(String[] args){
	    	Message m1 = new Message(MessageType.LAMPORT,1,"Request"+":"+"2");
	    	Message m2 = new Message(MessageType.LAMPORT,2,"Request"+":"+"0");
	    	LamportMutexController l = new LamportMutexController(0,new ServerController(1234));
	    	l.messageQueue.add(m1);
	    	l.messageQueue.add(m2);
	    	l.csEnter();
//	    	int[] a ={0,0};
//	    	int[] b ={0,1};
//	    	int[] c ={1,2};
//	    	l.pq.add(b);
//	    	l.pq.add(a);
//	    	l.pq.add(c);
//	    	System.out.println(l.pq.poll()[1]);
//	    	System.out.println(l.pq.poll()[1]);
//	    	System.out.println(l.pq.poll()[1]);
	    }
}
