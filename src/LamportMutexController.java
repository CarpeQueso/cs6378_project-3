


public class LamportMutexController extends MutexController {

    public LamportMutexController(int id, ServerController serverController) {
        super(id, serverController);

        serverController.register(MessageType.LAMPORT, messageQueue);
    }

    public void csEnter() {
        
    }

    public void csLeave() {
        
    }
}
