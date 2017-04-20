


public class RicartAgrawalaMutexController extends MutexController {

    public RicartAgrawalaMutexController(int id, ServerController serverController) {
        super(id, serverController);

        serverController.register(MessageType.RICART_AGRAWALA, messageQueue);
    }

    public void csEnter() {
        
    }

    public void csLeave() {
        
    }
}
