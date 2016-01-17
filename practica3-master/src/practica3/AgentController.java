package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.util.Pair;

/**
 * <code>AgentController</code>. Es la clase Controlador, la encargada de
 * coordinar a los agentes y de dirimir disputas en el acercamiento al objetivo
 *
 * @author Raul Alberto Calderon Lopez
 * @author Francisco Jesus Forte Jimenez
 *
 */
public class AgentController extends SingleAgent {

    //Variables para almacenar el estado interno del agente

    private boolean exit;

    //Variables para la comunicación
    private ACLMessage inbox;
    private ACLMessage outbox;
    private ArrayList<Pair<AgentID, AnimalStatus>> IDanimals;
    private JsonObject result1;
    private JsonObject result2;
    private JsonObject result3;
    private JsonObject result4;
    private AgentID IDserver;
    private AgentID IDdenebola;
    private String key;
    private String map;

    //Variables para la heurística
    private boolean found;              //Objetivo encontrado

    //Variables para la solución
    private int goalPosX;
    private int goalPosY;

    // numero de animales que ya han llegado al objetivo (0 <= 4)
    private int finished;

    /**
     * Constructor de la clase <code>AgentController</code>. Inicialización de
     * variables
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     * @param aid ID del agente.
     *
     * @throws java.lang.Exception
     *
     */
    public AgentController(AgentID aid) throws Exception {
        super(aid);
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        inbox = new ACLMessage();
        IDserver = aid;
        IDdenebola = new AgentID("Denebola");
        IDanimals = new ArrayList(4);
        IDanimals.add(new Pair(new AgentID("A1"), AnimalStatus.SEARCHING));
        IDanimals.add(new Pair(new AgentID("A2"), AnimalStatus.SEARCHING));
        IDanimals.add(new Pair(new AgentID("A3"), AnimalStatus.SEARCHING));
        IDanimals.add(new Pair(new AgentID("A4"), AnimalStatus.SEARCHING));
        map = "map1";

        result1 = Json.object();
        result2 = Json.object();
        result3 = Json.object();
        result4 = Json.object();
    }

    /**
     * Inicializador de la clase <code>AgentController</code>.
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    @Override
    public void init() {
    }

    /**
     * Método principal del agente <code>Controller</code>. Lleva el peso de
     * toda la actividad de la clase. Es el núcleo de decisión y control
     *
     * @author Raul ALberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    @Override
    public void execute() {
        if (!SubscribeWorld()) {
            exit = true;
        } else {
            for (int i = 0; i < 4; i++) {
                sendMessage(IDanimals.get(i).getKey(), ACLMessage.INFORM, key, "key");
            }

        }

        //Los agentes ya tienen asignados los roles, y el controlador espera los datos de cada animal
        while (allStatus(AnimalStatus.SEARCHING)) {
            // recibimos los 4 mensajes de los agentes o menos
            receiveAllData();

            /**
             * *******************
             *
             * HEURISTICA DEL CONTROLADOR
             *
             */
           // Enviamos movimientos a cada agente FALTA
           //Vemos si la heuristica nos dice que se ha encontrado y cambiamos los estados a FOUND o CRASHED
            receiveStatus(AnimalStatus.FOUND);

            if (AnyStatus(AnimalStatus.FOUND)) {
                sendAllStatus(AnimalStatus.FOUND);
            } else {
                sendAllStatus(AnimalStatus.SEARCHING);
                receiveStatus(AnimalStatus.CRASHED);
            }

           // esperamos resultados de los movimientos (Cambiamos los estados de los agentes al correspondiente)
            //Generamos imagen
        }

        while (AnyStatusNotFinished()) {

            // recibimos los 4 mensajes de los agentes o menos
            receiveAllData();

            /**
             * *******************
             *
             * HEURISTICA DEL CONTROLADOR
             *
             */
           // Enviamos movimientos a cada agente FALTA
            //Vemos si la heuristica nos dice que se ha encontrado y cambiamos los estados a FINISHED o CRASHED
            receiveStatus(AnimalStatus.FINISHED);

            sendAllStatus(AnimalStatus.FOUND);
            receiveStatus(AnimalStatus.CRASHED);

            //Generamos imagen
        }

        int finished = 0;

        for (int i = 0; i < IDanimals.size(); i++) {
            if (IDanimals.get(i).getValue() == AnimalStatus.FINISHED) {
                finished++;
            }
        }

        if (finished > 0) {
            System.out.println("Han llegado al objetivo " + finished + " agentes!!!");
        } else {
            System.out.println("No se ha conseguido llegar al objetivo");
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
        //Desconectar del servidor
        Cancel(IDdenebola);
        super.finalize();
    }

    /**
     * Función encargada de mandar mensajes totalmente parametrizable a través
     * de un solo campo. Envía el mensaje que se desee mediante el nombre del
     * campo que se desee.
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     * @param receiver Agente objetivo.
     * @param performative Performativa del mensaje.
     * @param content Mensaje a enviar al servidor.
     * @param dataname Nombre del campo donde se envía la información.
     */
    public void sendMessage(AgentID receiver, int perfomative, String content, String dataname) {
        outbox.setReceiver(receiver);
        JsonObject message = Json.object();
        message.add(dataname, content);
        outbox.setContent(message.toString());
        outbox.setPerformative(perfomative);
        outbox.setSender(IDserver);
        this.send(outbox);
    }

    /**
     * Función encargada de mandar el mensaje de cancelación de conexión. Envía
     * el mensaje de cancelación de conexión.
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     * @param receiver Agente objetivo.
     */
    public void Cancel(AgentID receiver) {
        outbox.setReceiver(receiver);
        JsonObject message = Json.object();
        message.add("key", key);
        outbox.setContent(message.toString());
        outbox.setPerformative(ACLMessage.CANCEL);
        this.send(outbox);
    }

    /**
     * Función encargada subscribir el agente <code>Controller</code>. Envía una
     * subscripción al mapa deseado y captura la respuesta para recibir la key.
     *
     * @return <code>true</code> si se ha subscrito con éxito <code>false</code>
     * si no se ha subscrito con éxito
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    public boolean SubscribeWorld() {
        boolean result = false;
        JsonObject connect = Json.object();
        connect.add("world", map);
        String connectS = connect.toString();
        outbox.setReceiver(IDdenebola);
        outbox.setContent(connectS);
        outbox.setPerformative(ACLMessage.SUBSCRIBE);

        this.send(outbox);

        try {
            inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
        }

        JsonObject answer;

        switch (inbox.getPerformativeInt()) {
            case ACLMessage.INFORM:
                answer = Json.parse(inbox.getContent()).asObject();
                key = answer.get("result").asString();
                //System.out.println(key + "\n");
                result = true;
                break;
            case ACLMessage.FAILURE:
                answer = Json.parse(inbox.getContent()).asObject();
                key = answer.get("result").asString();
                System.out.println(key + "\n");
                System.out.println("Fallo.");
                result = false;
                break;
            case ACLMessage.NOT_UNDERSTOOD:
                System.out.println("Mensaje mal formulado en la suscripción.");
                result = false;
                break;
            default:
                System.out.println("Error desconocido en la suscripción.");

        }
        return result;
    }

    /**
     * Función encargada de comprobar que todos los agentes
     * <code>AgentAnimal</code> tengan un mismo estado concreto. La función
     * comprueba que todos los agentes <code>AgentAnimal</code> tienen el mismo
     * estado que el estado pasado por parámetro.
     *
     * @param as Estado del Agente Animal pasado por parámetro
     *
     * @return <code>true</code> si todos tienen el mismo estado que el
     * parámetro <code>false</code> en caso contrario
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    private boolean allStatus(AnimalStatus as) {
        boolean status = false;

        for (int i = 0; i < IDanimals.size(); i++) {
            if (IDanimals.get(i).getValue() == as) {
                status = true;
            }
        }

        return status;
    }

    /**
     * Método encargado de recibir todos los datos de todos los agentes
     * <code>AgentAnimal</code> activos activos y actualizarlos en el agente
     * <code>Controlador</code>. El método recibe los datos de todos los agentes
     * <code>AgentAnimal</code> y actualizarlo.
     *
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    private void receiveAllData() {
        for (int i = 0; i < IDanimals.size(); i++) {
            if (IDanimals.get(i).getValue() != AnimalStatus.FINISHED && IDanimals.get(i).getValue() != AnimalStatus.CRASHED) {
                try {
                    inbox = this.receiveACLMessage();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                switch (inbox.getSender().toString()) {
                    case "A1":
                        result1 = Json.parse(inbox.getContent()).asObject();
                        break;
                    case "A2":
                        result2 = Json.parse(inbox.getContent()).asObject();
                        break;
                    case "A3":
                        result3 = Json.parse(inbox.getContent()).asObject();
                        break;
                    case "A4":
                        result4 = Json.parse(inbox.getContent()).asObject();
                        break;

                }
            }

        }
    }

    /**
     * Método encargado de recibir el estado de todos los agentes
     * <code>AgentAnimal</code> activos y actualizarlos en el agente
     * <code>Controlador</code>. El método recibe los estados de todos los
     * agentes <code>AgentAnimal</code> activos y actualizarlos.
     *
     * @param as Estado del Agente Animal pasado por parámetro
     *
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    private void receiveStatus(AnimalStatus as) {
        for (int i = 0; i < IDanimals.size(); i++) {
            if (IDanimals.get(i).getValue() != AnimalStatus.FINISHED && IDanimals.get(i).getValue() != AnimalStatus.CRASHED) {
                try {
                    inbox = this.receiveACLMessage();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                JsonObject auxjson = Json.parse(inbox.getContent()).asObject();

                switch (inbox.getSender().toString()) {
                    case "A1":
                        if (auxjson.get("status").asInt() != as.ordinal()) {
                            IDanimals.set(0, new Pair(inbox.getSender(), as));
                        }

                        break;
                    case "A2":
                        if (auxjson.get("status").asInt() != as.ordinal()) {
                            IDanimals.set(1, new Pair(inbox.getSender(), as));
                        }

                        break;
                    case "A3":
                        if (auxjson.get("status").asInt() != as.ordinal()) {
                            IDanimals.set(2, new Pair(inbox.getSender(), as));
                        }
                        break;
                    case "A4":
                        if (auxjson.get("status").asInt() != as.ordinal()) {
                            IDanimals.set(3, new Pair(inbox.getSender(), as));
                        }
                        break;
                }
            }
        }
    }

    /**
     * Función encargada de comprobar que al menos un agente
     * <code>AgentAnimal</code> tiene el mismo estado que el pasado por
     * parámetro. La función comprueba que al menos uno de los agentes
     * code>AgentAnimal</code> tiene el mismo estado que el estado pasado por
     * parámetro.
     *
     * @param as Estado del Agente Animal pasado por parámetro
     *
     * @return <code>true</code> si al menos uno tiene el mismo estado que el
     * parámetro <code>false</code> en caso contrario
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    public boolean AnyStatus(AnimalStatus as) {

        boolean resultstatus = false;

        for (int i = 0; i < IDanimals.size() || resultstatus; i++) {
            if (IDanimals.get(i).getValue() != AnimalStatus.FINISHED && IDanimals.get(i).getValue() != AnimalStatus.CRASHED) {
                if (IDanimals.get(i).getValue() == as) {

                    resultstatus = true;

                    for (int j = 0; j < IDanimals.size(); j++) {
                        if (IDanimals.get(j).getValue() != AnimalStatus.FINISHED) {
                            IDanimals.set(1, new Pair(inbox.getSender(), as));

                        }
                    }
                }
            }
        }
        return resultstatus;
    }

    /**
     * Función encargada de comprobar que haya algún agente
     * <code>AgentAnimal</code> que no haya terminado ni chocado. La función
     * comprueba que siga existiendo algún agente <code>AgentAnimal</code> que
     * ni haya encontrado el objetivo ni haya chocado.
     *
     *
     * @return <code>true</code> si hay alguno <code>false</code> en caso
     * contrario
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    private boolean AnyStatusNotFinished() {
        boolean status = false;
        for (int i = 0; i < 4 && !status; i++) {
            if (IDanimals.get(i).getValue() != AnimalStatus.CRASHED && IDanimals.get(i).getValue() != AnimalStatus.FINISHED) {
                status = true;
            }
        }

        return status;
    }

    /**
     * Método encargado de enviar un mensaje a todos los agentes
     * <code>AgentAnimal</code> no finalizados ni chocados para que actualicen
     * su estado al pasado por parámetro. El método envía un mensaje de
     * actualización de estado a todos los agentes <code>AgentAnimal</code> al
     * pasado por parámetro.
     *
     * @param as Estado del Agente Animal pasado por parámetro
     *
     *
     * @author Raul Alberto Calderon Lopez
     * @author Francisco Jesus Forte Jimenez
     *
     */
    private void sendAllStatus(AnimalStatus as) {
        for (int i = 0; i < 4; i++) {
            if (IDanimals.get(i).getValue() != AnimalStatus.CRASHED && IDanimals.get(i).getValue() != AnimalStatus.FINISHED) {
                sendMessage(IDanimals.get(i).getKey(), ACLMessage.INFORM, as.toString(), "status");
            }
        }
    }
}
