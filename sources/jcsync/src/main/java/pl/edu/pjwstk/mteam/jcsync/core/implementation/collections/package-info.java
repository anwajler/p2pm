/**
 * Contains extended collection classes and other necessary classes.
 * <p>
 * <strong>Understanding method invocation on extended collections:</strong><br>
 * Every 'write' methods types are overrides and it is looks similar to this one:
 * <pre>
 * <code>
 * public boolean add(E e) {
        Boolean retVal = false;
        try {
            Class[] aT = {Object.class};
            Serializable[] aV = {(Serializable) e};
            retVal = (Boolean)((SharedCollectionObject) shared_object).publishWriteOperation("add", aT, aV);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
        return retVal.booleanValue();
    }
 * </code>
 * </pre>
 * Each invoked 'write' type methods are published in the layer, and after the 
 * indication is received then the {@link DefaultConsistencyManager DefaultConsistencyManager}
 * invoke proper method which will make changes in the collection:
 * <pre>
 * <code>
 * public Object invoke(String methodName, Class[] argTypes, Object[] argValues) {
        methodName = methodName + '_';
        Object retVal = null;
        Method m = null;
        try {
            if (argTypes != null && argTypes.length > 0) {
                m = getClass().getDeclaredMethod(methodName, argTypes);
                m.setAccessible(true);
                retVal = m.invoke(this, argValues);
            } else {
                m = getClass().getDeclaredMethod(methodName);
                m.setAccessible(true);
                retVal = m.invoke(this);
            }

        } catch (Exception e) {
            retVal = e;
        }
        return retVal;
    }
 * </code>
 * </pre>
 * 
 * which results is a call the method with added <tt>'_'</tt> character at the 
 * and of name, in this case it will be <tt>private boolean add_(Object e)</tt>
 * <pre>
 * <code>
 * private boolean add_(E e) {
        return super.add(e);
   }
 * </code>
 * </pre>
 * And the result of it is passed by the {@link DefaultConsistencyManager DefaultConsistencyManager} to the invoked method as a result of it: <tt>retVal = (Boolean)((SharedCollectionObject) shared_object).publishWriteOperation("add", aT, aV);</tt>
 * @author Piotr Bucior
 */
package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;



