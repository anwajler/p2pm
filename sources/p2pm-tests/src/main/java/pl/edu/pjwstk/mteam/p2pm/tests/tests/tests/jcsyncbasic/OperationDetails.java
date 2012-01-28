package pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Piotr Bucior
 */
public class OperationDetails implements Serializable{
    String publisher;
    String operationID;
    Long creationTime = new Long(0);
    Long requestInvokingTime =new Long(0);
    Long indicationReceivingTime =new Long(0);
    
    public OperationDetails(String publisher, String operationID, long creationTime){
        this.publisher = publisher;
        this.operationID = operationID;
        this.creationTime = creationTime;
    }

    public Long getIndicationReceivingTime() {
        return indicationReceivingTime;
    }

    public void setIndicationReceivingTime(Long indicationReceivingTime) {
        this.indicationReceivingTime = indicationReceivingTime;
    }

    public Long getRequestInvokingTime() {
        return requestInvokingTime;
    }

    public void setRequestInvokingTime(Long requestInvokingTime) {
        this.requestInvokingTime = requestInvokingTime;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public String getOperationID() {
        return operationID;
    }

    public String getPublisher() {
        return publisher;
    }
    public String toString(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS");
        StringBuilder sb = new StringBuilder();
        Date date;
        sb.append(this.publisher);
        sb.append(",");
        sb.append(this.operationID);
        sb.append(",");
        date = new Date(this.creationTime);
        sb.append(sdf.format(date));
        sb.append(",");
        date = null;
        date = new Date(this.requestInvokingTime);
        sb.append(sdf.format(date));
        sb.append(",");
        date = null;
        date = new Date(this.indicationReceivingTime);
        sb.append(sdf.format(date));   
        date = null;
        sdf = null;                
        return sb.toString();
    }
}
