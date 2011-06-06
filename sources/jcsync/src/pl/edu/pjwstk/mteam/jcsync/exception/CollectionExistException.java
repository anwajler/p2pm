/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.edu.pjwstk.mteam.jcsync.exception;

/**
 *
 * @author Piotr Bucior
 */
public class CollectionExistException extends Exception{
    private static CollectionExistException instance = null;
    private CollectionExistException(){
        super("Collection exists!");
    }
    public static CollectionExistException getInstance(){
        if(instance==null) instance = new CollectionExistException();
        return instance;
    }
}
