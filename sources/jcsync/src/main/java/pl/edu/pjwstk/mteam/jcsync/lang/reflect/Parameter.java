

package pl.edu.pjwstk.mteam.jcsync.lang.reflect;

import java.io.Serializable;

/**
 * Class <tt>Parameter</tt> keeps an reflective information about the arguments 
 * which was passed to invoke method.
 * <p>It is used to send arguments types and its values thorugh overlay to 
 * another nodes.
 * @author Piotr Bucior
 */
public class Parameter implements Serializable{
    /**
     * Class instance of the argument
     */
    public Class type;
    /**
     * The value of the argument
     */
    public Object value;

    /**
     * makes new <tt>Parameter</tt> with given arguments
     * @param type class-type of the argument, e.g. <tt>String.class</tt>
     * @param value value of the argument, e.g. <tt>"String value"</tt>
     */
    public Parameter(Class type, Object value) {
        this.type = type;
        this.value = value;
    }
    @Override
    public String toString(){
        return new String("["+this.type.getName()+":"+this.value+"]");
    }

    @Override
    protected void finalize() throws Throwable {
        this.type = null;
        this.value = null;
        super.finalize();
    }
    /**
     * returns set of a parameters as a array.
     * @param params variable count of parameters
     * @return params as array
     */
    public static Parameter[] getParams(Object... params) {
        if(params==null)return null;
        Parameter [] prm = new Parameter[params.length];
        for (int i = 0; i < params.length; i++) {
            prm[i] =new Parameter(params[i].getClass(),params[i]);
        }
        return prm;
    }

}
