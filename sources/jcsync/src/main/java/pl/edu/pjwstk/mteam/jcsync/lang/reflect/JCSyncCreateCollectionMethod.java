package pl.edu.pjwstk.mteam.jcsync.lang.reflect;

import java.io.Serializable;

/**
 * keeps all information needed to create collection. 
 * Used to sent this information through the pverlay.
 * @author Piotr Bucior
 * @version 1.0
 */
public class JCSyncCreateCollectionMethod extends JCSyncMethod implements Serializable {

    private String constructorGenericName = null;
    private Class collectionClass;    
    private Parameter[] params = null;
    private byte[] additionalData;

    /**
     * makes new method with given arguments
     * @param collID the ID of the collection to create
     * @param collClass the class of the collection to create
     * @param constructorName generic constructor name which will be used to create collection
     * @param params variable count of parameters (depends on the choosen constructor)
     */
    public JCSyncCreateCollectionMethod(String collID, Class collClass, String constructorName, Parameter... params) {
        super(collID, CREATE_COLLECTION);
        this.constructorGenericName = constructorName;
        this.collectionClass = collClass;
        this.params = params;
    }
    /**
     * returns the declared collection class, which will be created
     */
    public Class getCollectionClass(){
        return this.collectionClass;
    }
    /**
     * returns given parameters as array
     */
    public Parameter[] getParameters(){
        return this.params;
    }
    /**
     * returns method arguments classes types as array
     */
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
    /**
     * returns arguments values classes as array
     */
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
    /**
     * sets additional data to method
     */
    public void setAdditionalData(byte [] data){
        this.additionalData = data;
    }
    /**
     * return additional data
     */
    public byte [] getAdditionalData(){
        return this.additionalData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "-->Type: CREATE_COLLECTION"
                + "\n-->CollectionType: " + this.collectionClass.getName()
                + "\n-->Constructor: " + this.constructorGenericName.toString()
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
