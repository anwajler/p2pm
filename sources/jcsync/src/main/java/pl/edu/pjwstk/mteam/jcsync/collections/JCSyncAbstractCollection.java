package pl.edu.pjwstk.mteam.jcsync.collections;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;

/**
 * JCSyncAbstractCollection is a interface is a set of methods, which must be implemented by each collection implementation.
 * 
 * @author Piotr Bucior
 * @version 1.0
 */
public interface JCSyncAbstractCollection {
		
    /**
     * returns collection identifier name in the PUB-SUB layer
     * @return PUB-SUB ID's name
     */
    public pl.edu.pjwstk.mteam.pubsub.core.Topic getcollectionID();

    /**
     * 
     * add new {@code JCSyncCollectionStateListener} to the collection listeners list
     * @param listener of the collection
     */
    public void addStateListener(JCSyncCollectionStateListener listener);
    /**
     * Deserialize core collection data from the byte array.
     * <p>
     * Method invoked when collection constructor is called with additional data (if that collection already exists in the PUB-SUB layer), e.g. in the JCSyncHashMap the code is:
     * <pre>
     * 
     * {@code 
     *  //the constructor:
        protected JCSyncHashMap(Topic collectionID, Constructor collectionConstructor, JCSyncCreateCollectionMethod jcsDetails, Object... params) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.collectionID_ = collectionID.getID();
        this.collectionIdentifier = collectionID;
        this.constructorDetails = jcsDetails;
        if (this.constructorDetails.getAdditionalData() != null) {
            deserialize(this.constructorDetails.getAdditionalData());
        }else{
            this.collection = (HashMap) collectionConstructor.newInstance(params);
        }
        this.isInitialised = true;
     * }
     * }
     * //deserialize method:
     * {@code
     * public final void deserialize(byte[] data) {
        ByteArrayInputStream istream = new ByteArrayInputStream(data);
        ObjectInputStream ostr_ = null;
        try {
            ostr_ = new ObjectInputStream(istream);
            this.collection = (HashMap) ostr_.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                if(ostr_!=null)
                ostr_.close();
            } catch (IOException ex) {
            }
            istream = null;
            ostr_ = null;
        }
        }
     
     * </pre>
     * @param raw_data byte array with serialized collection
     */
    public void deserialize(byte [] raw_data);
    /**
     * Returns CollectionManager that handle this collection
     * @return <tt>AbstractCollectionManager</tt> instance
     */
    public AbstractCollectionsManager getCollectionManager();
    /**
     * Returns constructor details, which was used to create this collection instance.
     * @return JCSyncCreateCollectionMethod used to make collection
     */
    public JCSyncCreateCollectionMethod getConstructorDetails();
    
    /**
     * Returns <tt> reflective</tt> constructor specified by the given <tt>genericName</tt>
     * @param genericName the name of the constructor
     * @return the instance of the Constructor class specified by the given name
     */
    public Constructor getConstructor(String genericName);
    /**
     * returns count of invoked operation on this collection
     * @return the number of the last operation in long
     */
    public long getCurrentOperationID(); 
    /**
     * returns the declared class for this collection implementation (the core collection type), e.g. for JCSyhcHashMap it is <tt>java.util.HashMap.class</tt>
     * @return
     */
    public Class getDeclaredClass();
    /**
     * returns the set of the sticked listeners to this collection 
     * @return set of the listeners as <tt>ArrayList</tt>
     */
    public ArrayList<JCSyncCollectionStateListener> getListeners();
    /**
     * returns the <tt>reflective</tt> method by the given method's name
     * @param genericName the name of the method which will be returned
     * @return method instance
     */
    public Method getMethod(String genericName);

    /**
     * called when the new operation is delivered from PUB-SUB layer
     * @param method <tt>reflective</tt> method instance, which will be called 
     * @param operationID assigned operation ID
     * @param localOperation <tt>true</tt> if the method will be invoked by a local user
     * @param params array of the arguments delivered to make this operation
     * @return method return value or null if the method is <tt>void</tt>type
     * @throws Exception if something goes wrong
     */
    public Object invokeMethod(Method method,long operationID,boolean localOperation, Object ... params)throws Exception;

    /**
     * Remove given state listener from sticked listeners list
     * @param lst listener to remove
     * @return <tt>true</tt> if the listener was succesfully removed
     */
    public boolean removeStateListener(JCSyncCollectionStateListener lst);
    
    /**
     * Serialize core collection data to the byte array.
     * <p>
     * Called by the <tt>JCSync core</tt> to send it through the PUB-SUB layer to other user.
     * <pre>
     * {@code 
     * public byte[] serialize() {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        ObjectOutputStream ostr = null;
        raw_data = null;
        try {
            ostr = new ObjectOutputStream(ostream);
            ostr.writeObject(this.collection);
            raw_data = ostream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ostream.close();
            } catch (IOException ex) {
            }
            ostream = null;
            ostr = null;
        }
        return raw_data;
    }
     * }
     * </pre>
     * @return serialized data 
     */
    public byte[] serialize();
    /**
     * returns item count contained in the collection
     * @return item count as <tt>int</tt>
     */
    public int size();

    
    
    
           
}