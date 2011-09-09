package pl.edu.pjwstk.mteam.jcsync.collections.implementation;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter;

/**
 *
 * @author Piotr Bucior
 */
public class JCSyncHashMapTest {

    public JCSyncHashMapTest() {
    }

//    @Before
//    public void setUp() {
//    }
//
//    @After
//    public void tearDown() {
//    }
    @Test
    public void blankConstructor2() {
       
            System.out.println("* JCSyncHashMapTest - blankConstructor(String id)");
            String id = "testID";
            try {
                JCSyncHashMap hm = (JCSyncHashMap) AbstractCollectionsManager.getInstance().requestCreateCollection(JCSyncHashMap.class, id, new Parameter(int.class, 10),new Parameter(float.class, 100));
                System.out.println("zzz");
            } catch (InstantiationException ex) {
                Logger.getLogger(JCSyncHashMapTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(JCSyncHashMapTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(JCSyncHashMapTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(JCSyncHashMapTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        
    }

}