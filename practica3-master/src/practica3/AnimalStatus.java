/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

/**
 *
 * @author Francisco Jesus Forte Jimenez
 * @author Raul Alberto Calderon Lopez
 */

public enum AnimalStatus{
    SEARCHING(0),FOUND(1),FINISHED(2), CRASHED(3), IDLE(4);
    private int animalStatus;
    
    AnimalStatus(int type){
        this.animalStatus = type;
    }
};