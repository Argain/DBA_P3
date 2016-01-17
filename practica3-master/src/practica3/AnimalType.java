package practica3;

/**
 * Enumerados para controlar el tipo de agente.
 * Actualmente no se utiliza porque es un coñazo tener
 * que controlar el número que se recibe del servidor y
 * declararlo en función de eso. Se puede estudiar usarlo o no.
 * 
 * @author Samuel Peralta Antequera
 */
public enum AnimalType{
    Fly(0),Bird(1),Hawk(2);
    private int animalType;
    AnimalType(int type){
        this.animalType = type;
    }
};
