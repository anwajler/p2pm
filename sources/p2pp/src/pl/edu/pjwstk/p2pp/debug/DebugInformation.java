package pl.edu.pjwstk.p2pp.debug;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class DebugInformation extends Hashtable<Byte,Object> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 175873063359648101L;

	/**
     * Returns JSON-like formatted debug information
     *
     * @return String representation of debug information array
     */
    @Override
    public String toString() {
        String debugInfoString = "[";

        List<Object> debugFields =  Arrays.asList(this.keySet().toArray());
        Collections.reverse(debugFields);
        int debugInfo_length = this.keySet().size();
        for (int i=0; i<debugInfo_length; i++) {
            byte debugFieldIndex = (Byte)debugFields.get(i);
            debugInfoString += debugFieldIndex+":\""+this.get(debugFieldIndex)+"\"";
            if (i < debugInfo_length-1) {
                debugInfoString += ",";
            }
        }

        return debugInfoString + "]";
    }

}
