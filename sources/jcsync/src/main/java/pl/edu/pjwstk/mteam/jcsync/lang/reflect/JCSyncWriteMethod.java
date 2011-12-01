package pl.edu.pjwstk.mteam.jcsync.lang.reflect;

import java.io.Serializable;

/**
 * keeps all information to invoke method such as generic method name, its arguments.
 * @author Piotr Bucior
 * @version 1.0
 */
public class JCSyncWriteMethod extends JCSyncMethod implements Serializable{
    private String methodGenericName = null;
    private Class collectionClass;
    private Parameter[] params = null;    

    /**
     * create new method with given arguments
     * @param collID collection ID for which the method is created
     * @param collClass collection class for which the method is created
     * @param methodName generic method name (from Java Reflective API)
     * @param params variable count of parameters
     */
    public JCSyncWriteMethod(String collID, Class collClass, String methodName, Parameter... params){
        super(collID, WRITE_OPERATION);
        this.methodGenericName = methodName;
        this.collectionClass = collClass;
        this.params = params;        
    }

    @Override
    protected void finalize() throws Throwable {
        this.methodGenericName = null;
        this.collectionClass = null;
        this.params = null;
        super.finalize();
    }

    StringBuilder sb;
    @Override
    public String toString() {
        try{
         sb = new StringBuilder();
        sb.append(
                "-->Type: WRITE_METHOD"
                + "\n-->CollectionType: " + this.collectionClass.getName()
                + "\n-->Method: " + this.methodGenericName
                + "\n-->Params: ");
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i]);
            }
        } else {
            sb.append("{blank}");
        }
        return sb.toString();}finally{
            sb = null;
        }
    }
    /**
     * returns generic method name
     */
    public String getGenericMethodName(){
        return this.methodGenericName;
    }
    /**
     * returns collection class for which the method is created
     */
    public Class getDeclaredClass(){
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
    public Object [] getParamValues(){
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

}
