
public class Message {

    private MessageType type;

    private int senderId;

    private String body;

    public Message(MessageType type, int senderId, String body) {
        this.type = type;
        this.senderId = senderId;
        this.body = body;
    }

    public MessageType getType() {
        return this.type;
    }

    public int getSenderId() {
        return this.senderId;
    }
  
    public String getBody() {
        return this.body;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        // TODO: make these the name() values of the enum type
        switch (type) {
        case LAMPORT:
            sb.append("LAMPORT");
            break;
        case RICART_AGRAWALA:
            sb.append("RICART_AGRAWALA");
            break;
        case HALT:
            sb.append("HALT");
            break;
        default:
            sb.append("UNKNOWN");
        }
        sb.append(":");
        sb.append(senderId);
        sb.append(":");
        sb.append(body);
    
        return sb.toString();
    }

    public static Message parseMessage(String messageString) {
        String[] messageComponents = messageString.split(":", 3);

        MessageType type;
        int senderId;
        String body;

        if (messageComponents[0].equals("LAMPORT")) {
            type = MessageType.LAMPORT;
        } else if (messageComponents[0].equals("RICART_AGRAWALA")) {
            type = MessageType.RICART_AGRAWALA;
        } else if (messageComponents[0].equals("HALT")) {
            type = MessageType.HALT;
        } else {
            type = MessageType.UNKNOWN;
        }

        senderId = Integer.parseInt(messageComponents[1]);
        body = messageComponents[2];
		
        return new Message(type, senderId, body);
    }
}
