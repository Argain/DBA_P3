package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import org.apache.xmlbeans.impl.common.IdentityConstraint;

/**
 * <code>AgentAnimal</code> es la clase agente. Se encarga de las actividades
 * concretas de cada uno de los agentes que participan en la labor de rescate.
 *
 * @author Raul Alberto Calderon Lopez
 * @author Francisco Jesus Forte Jimenez
 *
 */
public class AgentAnimal extends SingleAgent {

    //Variables para almacenar el estado interno del agente

    private AnimalType agentType;
    private AnimalStatus status;
    private int battery;
    private int posX;
    private int posY;
    private ArrayList<Integer> radar;

    //Variables para la comunicación
    private ACLMessage inbox;
    private ACLMessage outbox;
    private AgentID IDserver;
    private AgentID IDdenebola;
    private String key;

    //Variables para la heurística
    private boolean found;              //Objetivo encontrado
    private boolean goal;               //Mientras no se llegue al objetivo
    private boolean exit;               //Por si se produce algún error
    private JsonObject result;

    // Variables para la solución
    private int goalPosX;
    private int goalPosY;

    /**
     * Constructor de la clase <code>AgentAnimal</code>. Se encarga de
     * inicializar varios parámetros necesarios para la comunicación y la
     * gestión interna del agente.
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     * @param aid ID del agente.
     *
     */
    public AgentAnimal(AgentID aid) throws Exception {
        super(aid);

        outbox = new ACLMessage();
        inbox = new ACLMessage();

        key = "LLAVE_VACIA";
        IDserver = new AgentID("Controlador");
        IDdenebola = new AgentID("Denebola");
        status = AnimalStatus.SEARCHING;
        goal = false;
        found = false;
        exit = false;
    }

    /**
     * Método sendData. Método que envía datos al <code>Controlador</code>
     *
     * @author Raul ALberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    public void sendData() {
        outbox.setSender(getAid());
        outbox.setReceiver(IDserver);
        outbox.setContent(result.toString());

        //envio del mensage al servidor
        this.send(outbox);
    }

    /**
     * Inicializador de la clase <code>AgentAnimal</code>. Se encarga de
     * inicializar varios parámetros necesarios para la comunicación.
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    @Override
    public void init() {
    }

    /**
     * Método principal del agente <code>AgentAnimal</code>. Lleva el peso de
     * toda la actividad de la clase. Es el núcleo de decisión y control
     *
     * @author Raul ALberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    @Override
    public void execute() {
        // Recibimos la key del controlador
        try {
            inbox = this.receiveACLMessage();
            JsonObject answer = Json.parse(inbox.getContent()).asObject();

            if (inbox.getSender().getLocalName().equals("Controlador")) {

                if (!inbox.getPerformative().equals("INFORM")) {
                    exit = true;
                } else {
                    key = answer.get("key").asString();
                }
            } else {
                exit = true;
            }

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        System.out.println(key);

        if (registryAgent()) {

            switch (agentType) {
                case Fly:
                    System.out.println("Soy una mosca");
                    break;
                case Bird:
                    System.out.println("Soy un pajaro");
                    break;
                case Hawk:
                    System.out.println("Soy un halcon");
                    break;
                default:
                    System.out.println("Sin rol");
            }
        }

        initializeRadar();

        // mientras estamos buscando
        while ((status == AnimalStatus.SEARCHING) && !exit) {

            askServer();
            sendData();

            //recibimos la lista de movimientos posibles
            //REALIZAR HEURISTICA DEL AGENTE
            //si el estado ha cambiado a FOUND o no, enviamos el estado al controlador
            sendMessage(IDserver, ACLMessage.INFORM, status.toString());
            //recibimos el estado en el que se encuentra el grupo de animales (SEARCHING O FOUND)
            status = receiveStatus();

            if (status != AnimalStatus.FOUND) {

                //realizamos el movimiento HAY QUE DECLARAR EL MOVIMIENTO QUE DIGA LA HEURISTICA, si no está a FOUND
                sendMessage(IDdenebola, ACLMessage.REQUEST, MOVIMIENTO);
                status = statusMovement();

                //mandamos el movimiento al controlador
                sendMessage(IDserver, ACLMessage.INFORM, status.toString());

                /*if (status == AnimalStatus.CRASHED)
                 exit = true;

                 */
            }
        }

        while ((status == AnimalStatus.FOUND) && !exit) {

            askServer();
            sendData();

            //recibimos la lista de movimientos posibles
            //REALIZAR HEURISTICA DEL AGENTE
            //si el estado ha cambiado a FINISHED o no, enviamos el estado al controlador
            if (status == AnimalStatus.FINISHED) {
                sendMessage(IDserver, ACLMessage.INFORM, status.toString());
                exit = true;
            } else {
                //recibimos el estado en el que se encuentra el grupo de animales (SEARCHING O FOUND)
                status = receiveStatus();

                //realizamos el movimiento HAY QUE DECLARAR EL MOVIMIENTO QUE DIGA LA HEURISTICA
                sendMessage(IDdenebola, ACLMessage.REQUEST, MOVIMIENTO);
                status = statusMovement();

                //mandamos el movimiento al controlador
                sendMessage(IDserver, ACLMessage.INFORM, status.toString());
            }
        }
    }

    /**
     * Termina la ejecución del agente <code>Controller</code>. Código que se
     * ejecuta al terminar la ejecución
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    @Override
    public void finalize() {
        super.finalize();
    }

    /**
     * Función encargada de mandar mensajes. Envía el mensaje que se desee.
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     * @param receiver Agente objetivo.
     * @param performative Performativa del mensaje.
     * @param content Mensaje a enviar al servidor.
     */
    public void sendMessage(AgentID receiver, int perfomative, String content) {

        outbox.setReceiver(receiver);
        JsonObject message = Json.object();
        if (receiver == IDdenebola) {
            if (perfomative == ACLMessage.REQUEST) {   //Solo es necesario añadir command para moverser, repostar o pedir rol
                message.add("command", content);
            }

            message.add("key", key);                //La key es necesaria siempre
        } else {
            message.add("status", content);         // Enviamos el estado (es la unica comunición que habrá con el controlador)                          
        }
        outbox.setContent(message.toString());
        outbox.setPerformative(perfomative);
        outbox.setSender(this.getAid());
        this.send(outbox);

    }

    /**
     * Función encargada de hacer el checkin de cada agente. Envía el mensaje de
     * checkin de cada agente <code>AgentAnimal</code> al servidor.
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     * @return result      <code>true</code> en caso de éxito <code>false</code> en
     * caso contrario
     */
    public boolean registryAgent() {
        boolean result = false;

        sendMessage(IDdenebola, ACLMessage.REQUEST, "checkin");

        try {
            inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        switch (inbox.getPerformativeInt()) {
            case ACLMessage.INFORM:
                int typeaux;
                typeaux = Json.parse(inbox.getContent()).asObject().get("rol").asInt();

                switch (typeaux) {
                    case 0:
                        agentType = AnimalType.Fly;
                        break;
                    case 1:
                        agentType = AnimalType.Bird;
                        break;
                    case 2:
                        agentType = AnimalType.Hawk;
                        break;
                }
                result = true;
                break;
            case ACLMessage.REFUSE:
                System.out.println("Clave incorrecta en el registro del bot " + this.getAid() + ".");
                result = false;
                break;
            case ACLMessage.NOT_UNDERSTOOD:
                System.out.println("Mesanje mal formulado en el registro del bot " + this.getAid() + ".");
                result = false;
                break;
            default:
                System.out.println("Error desconocido en la suscripción del bot " + this.getAid() + ".");
        }

        return result;
    }

    /**
     * Método encargado de inicializar el radar del agente      <code>AgentAnimal>/code> según su tipo. 
     * Método que inicializa el radar del agente <code>AgentAnimal>/code>
     * según el tipo de agente que sea.
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    public void initializeRadar() {
        switch (agentType) {
            case Fly:
                radar = new ArrayList(9);
                break;
            case Bird:
                radar = new ArrayList(25);
                break;
            case Hawk:
                radar = new ArrayList(121);
                break;
        }
    }

    /**
     * Método encargado de pedir las lecturas de los sensores al servidor.
     * Método que pide las lecturas de todos los sensores al servidor.
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    public void askServer() {

        sendMessage(IDdenebola, ACLMessage.QUERY_REF, key);

        try {
            inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        JsonObject data = Json.parse(inbox.getContent()).asObject();
        result = data.get("result").asObject();

        switch (inbox.getPerformativeInt()) {
            case ACLMessage.INFORM: {
                battery = result.get("battery").asInt();
                posX = result.get("x").asInt();
                posY = result.get("y").asInt();

                JsonArray auxArray = result.get("sensor").asArray(); // Radar

                radar.clear();

                for (int i = 0; i < radar.size(); i++) {
                    radar.add(auxArray.get(i).asInt());
                }

                goal = result.get("goal").asBoolean();

                break;
            }

            case ACLMessage.REFUSE:
                System.out.println("Clave incorrecta al preguntar al servidor del bot " + this.getAid() + ".");
                exit = true;
                break;
            case ACLMessage.NOT_UNDERSTOOD:
                System.out.println("Mesanje mal formulado al preguntar al servidor del bot " + this.getAid() + ".");
                exit = true;
                break;
            default:
                System.out.println("Error desconocido al preguntar al servidor del bot " + this.getAid() + ".");
                exit = true;
                break;
        }
    }

    /**
     * Función encargada de recibir el estado que el <code>Controlador</code> le
     * comunique. Función que recibe el estado comunicado por el
     * <code>Controlador</code> y actualiza el estado del
     * <code>AgentAnimal</code>
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     * @return aux Estado del Agente Animal
     */
    private AnimalStatus receiveStatus() {
        AnimalStatus aux;

        try {
            inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        JsonObject data = Json.parse(inbox.getContent()).asObject();
        aux = AnimalStatus.valueOf(data.get("status").toString());

        return aux;
    }

    /**
     * Función encargada de recibir el estado que el movimiento ha provocado.
     * Función que recibe el estado provocado por el movimiento del agente
     * <code>AgentAnimal</code>
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     * @return aux Estado del Agente Animal
     */
    private AnimalStatus statusMovement() {
        AnimalStatus aux = status;

        try {
            inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        JsonObject data = Json.parse(inbox.getContent()).asObject();

        if (data.get("result").toString().equals("CRASHED")) {
            aux = AnimalStatus.CRASHED;
        }

        return aux;
    }

}
