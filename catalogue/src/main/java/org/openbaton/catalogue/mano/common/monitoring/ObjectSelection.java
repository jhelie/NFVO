package org.openbaton.catalogue.mano.common.monitoring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mob on 18.11.15.
 */
public class ObjectSelection implements Serializable{

    /*
    * Identifies the object instances for which performance information should be collected.
    * The object instances for this information element will be virtualised resources.
    * These resources shall be known by the Virtualised Resource Management interface.
    * One of the two alternatives (objectType+ objectFilter or objectInstanceId) shall be present. 
    * */
    //hostnames for the moment
    private List<String> objectInstanceIds;
    private List<String> objectTypes;
    private Object filter;

    public ObjectSelection(){
        objectInstanceIds=new ArrayList<>();
    }

    public void addObjectInstanceId(String objectInstanceId){
        objectInstanceIds.add(objectInstanceId);
    }

    public List<String> getObjectInstanceIds() {
        return objectInstanceIds;
    }
}
