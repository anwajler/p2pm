/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.pjwstk.mteam.jcsync.lang.reflect;

import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 *
 * @author pb
 */
public class JCSyncCreateCollectionMethod extends JCSyncMethod implements Serializable {

    private String c = null;
    private Class collectionClass;    
    private Parameter[] params = null;
    private byte[] additionalData;

    public JCSyncCreateCollectionMethod(String collID, Class collClass, String c, Parameter... params) {
        super(collID, CREATE_COLLECTION);
        this.c = c;
        this.collectionClass = collClass;
        this.params = params;
    }
    public Class getCollectionClass(){
        return this.collectionClass;
    }
    public Parameter[] getParameters(){
        return this.params;
    }
    public Class[] getParamTypes(){
        Class[] param_types;
        if(params==null) param_types = null;
        else{
            param_types = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            param_types[i] = params[i].type;
        }
        }
        return param_types;
    }
    public Object [] getValues(){
        Object[] param_values;
        if(params==null) param_values = null;
        else{
            param_values = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            param_values[i] = params[i].value;
        }
        }
        return param_values;
    }
    public void setAdditionalData(byte [] data){
        this.additionalData = data;
    }
    public byte [] getAdditionalData(){
        return this.additionalData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "-->Type: CREATE_COLLECTION"
                + "\n-->CollectionType: " + this.collectionClass.getName()
                + "\n-->Constructor: " + this.c.toString()
                + "\n-->Params: ");
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i]);
            }
        } else {
            sb.append("{blank}");
        }
        return sb.toString();
    }
}
