/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.edu.pjwstk.mteam.jcsync.lang.reflect;

import java.io.Serializable;

/**
 *
 * @author pb
 */
public class Parameter implements Serializable{
    public Class type;
    public Object value;

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
    public static Parameter[] getParams(Object... params) {
        if(params==null)return null;
        Parameter [] prm = new Parameter[params.length];
        for (int i = 0; i < params.length; i++) {
            prm[i] =new Parameter(params[i].getClass(),params[i]);
        }
        return prm;
    }

}
