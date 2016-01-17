package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 *
 * @author Raul Alberto Calderon Lopez
 * @author Francisco Jesus Forte Jimenez
 * 
 */
public class Practica3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // declaracion de agentes.
        AgentAnimal bot, bot2, bot3, bot4;
        AgentController controlador;
        
        // conexion con el servidor
        AgentsConnection.connect("isg2.ugr.es", 6000, "Denebola", "Leon", "Russo", false);
        
        // instanciacion de los agentes
        bot = new AgentAnimal(new AgentID("A1"));
        bot2 = new AgentAnimal(new AgentID("A2"));
        bot3 = new AgentAnimal(new AgentID("A3"));
        bot4 = new AgentAnimal(new AgentID("A4"));
        
        //instanciacion del agente Controlador
        controlador = new AgentController(new AgentID("Controlador"));
               
        // comienzo de la ejecucion de los agentes  
        
        
        bot.start();
        bot2.start();
        bot3.start();
        bot4.start();   
        
        controlador.start();
    }
    
}
