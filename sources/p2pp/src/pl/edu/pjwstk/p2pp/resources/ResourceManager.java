package pl.edu.pjwstk.p2pp.resources;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.entities.Node;
import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.Owner;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RLookup;
import pl.edu.pjwstk.p2pp.objects.ResourceID;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.RoutingTable;
import pl.edu.pjwstk.p2pp.services.Service;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * Manager of resources and services of P2PP node.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public class ResourceManager {

    private Logger logger = org.apache.log4j.Logger.getLogger(ResourceManager.class);

    /**
     * Hash algorithm ID (as used in {@link P2PPUtils} class). Initialized in {@link #hashResourceIDs(byte, byte)}.
     */
    private byte hashAlgorithm;
    /**
     * Hash length. Initialized in {@link #hashResourceIDs(byte, byte)}.
     */
    @SuppressWarnings("unused")
	private byte hashLength;

    /**
     * Owner object used for creating resource objects.
     */
    @SuppressWarnings("unused")
	private Owner owner;

    /**
     * Time after which a resource is considered expired.
     */
    @SuppressWarnings("unused")
	private int expirationTime;

    /**
     * Map of content types (Integer) as keys and hashtable as a value. Value has a content subtype as key (Integer) and
     * hashtable as value. That table has a ResourceID object as key and Hashtable as value. Keys are Owner objects and
     * values are ResourceObjects.
     */
    private Hashtable<Integer, Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>>> resourceObjectsMap = new Hashtable<Integer, Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>>>();

    /**
     * Map of registered services. Integer objects are keys (content type) and Service objects are values. TODO I'm no
     * sure if services have only contentType.
     */
    private Hashtable<Integer, Service> services = new Hashtable<Integer, Service>();

    /**
     * Table for resource-objects a peer has published in the overlay. It must periodically refresh the resource-objects
     * before the passage of their refresh time interval.
     */
    protected PublishTable publishTable = new PublishTable();

    /**
     * Constructor of resources manager.
     */
    public ResourceManager() {

    }

    /**
     * Adds given service to list of registered services.
     *
     * @param service
     * @return Returns true if service was added. False if there's already a service of that type.
     */
    public boolean addService(Service service) {
        services.put(new Integer(service.getContentType()), service);

        // FIXME now always returns true
        return true;
    }

    /**
     * Returns service for given content type. Constant content types are defined in {@link P2PPUtils} class. Returns
     * null if there's no Service object of given content type.
     *
     * @param contentType
     * @return
     */
    public Service getService(int contentType) {
        try {
            return services.get(new Integer(contentType));
        } catch (NullPointerException e) {
            return null;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Returns bytes that will be used as for generating ResourceID of service's resourceID. Service can't be null.
     *
     * @return
     */
    public static byte[] getServiceResourceIDSeed(Service service) {
        byte contentType = service.getContentType();
        byte[] seed = null;
        switch (contentType) {
            case P2PPUtils.STUN_CONTENT_TYPE: {
                seed = P2PPUtils.STUN_SERVICE_ID;
                break;
            }
            case P2PPUtils.TURN_CONTENT_TYPE: {
                seed = P2PPUtils.TURN_SERVICE_ID;
                break;
            }
            case P2PPUtils.STUN_TURN_ICE_CONTENT_TYPE: {
                seed = P2PPUtils.ICE_SERVICE_ID;
                break;
            }
        }
        return seed;
    }

    /**
     * If owner is not null, returns list with one one resource object identified by given parameters. If owner is null,
     * returns a list of resource objects. If there are no resource objects for given parameters (it doesn't matter if
     * owner is null or not null), null is returned.
     *
     * @param contentType
     * @param contentSubtype
     * @param resourceID     Can't be null.
     * @param owner          Can be null.
     * @return
     */
    public List<ResourceObject> getResourceObject(int contentType, int contentSubtype, ResourceID resourceID, Owner owner) {

        if (logger.isTraceEnabled()) {
            logger.trace("contentType=" + contentType + " subtype=" + contentSubtype + " resourceID="
                    + ByteUtils.byteArrayToHexString(resourceID.getResourceID()) + " owner=" + owner);
            logger.trace("There are ResourceObjects of " + resourceObjectsMap.keySet().size() + " content types.");
        }

        List<ResourceObject> searchedResources = null;

        try {

			Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>> contentSubTypeMap = resourceObjectsMap.get(contentType);
            if (contentSubTypeMap == null) {
                if (logger.isTraceEnabled()) logger.trace("There's no ResourceObject for given contentType=" + contentType + ".");
            }
			Hashtable<ResourceID, Hashtable<Owner, ResourceObject>> resourceIDMap =  contentSubTypeMap.get(contentSubtype);
            if (resourceIDMap == null) {
                if (logger.isTraceEnabled()) logger.trace("There's no ResourceObject for given contentSubType=" + contentSubtype + ".");
            }
			Hashtable<Owner, ResourceObject> ownersMap = resourceIDMap.get(resourceID);
            if (ownersMap == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("There's no ResourceObject for given resourceID=" + ByteUtils.byteArrayToHexString(resourceID.getResourceID()) +
                        " ownersMap=" + ownersMap);
                }
            }

            // if there's no Owner subobject in given RLookup
            if (owner == null) {

				Set<Owner> owners = ownersMap.keySet();

                // if there's no resource objects for given RLookup
                if (owners.size() == 0) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("There's no ResourceObject for given resourceID=" + ByteUtils.byteArrayToHexString(resourceID.getResourceID()));
                    }
                    return null;
                } else {
                    // creates and fills a list of ResourceObjects for all owners
                    searchedResources = new ArrayList<ResourceObject>(owners.size());
                    for (Owner currentOwner : owners) {
                        ResourceObject currentResource = ownersMap.get(currentOwner);
                        searchedResources.add(currentResource);
                    }
                }
            } // if search concerns to resource owned by particular owner
            else {
                ResourceObject searchedResourceObject = (ResourceObject) ownersMap.get(owner);
                searchedResources = new ArrayList<ResourceObject>(1);
                searchedResources.add(searchedResourceObject);
            }

        } catch (ClassCastException e) {
            // this happens if there's no value for one of keys
            return null;
        } catch (NullPointerException e) {
            // this happens if there's no value for one of keys
            return null;
        }
        return searchedResources;
    }

    /**
     * Returns resource object identified by given RLookup object (list contains one object) if there's Owner subobject
     * in RLookup. If multiple owners had published data under the content type, subtype and resourceID being searched,
     * and if the owner is not specified in RLookup the peer storing the resource-object includes all objects in
     * returned list. If null is returned, there's no ResourceObject for given RLookup.
     *
     * @param resourceLookup RLookup object describing a searched ResourceObject.
     * @return
     */
    public List<ResourceObject> getResourceObject(RLookup resourceLookup) {
        Integer contentType = new Integer(resourceLookup.getContentType());
        Integer contentSubType = new Integer(resourceLookup.getContentSubtype());
        ResourceID resourceID = resourceLookup.getResourceID();

        return getResourceObject(contentType, contentSubType, resourceID, resourceLookup.getOwner());
    }

    /**
     * Adds own ResourceObject. Object is added to publish table that is part of this manager. Then, if object has to be
     * send to other nodes, this will be done internally.
     *
     * @param resourceObject
     */
    public void addSelfPublishedResourceObject(ResourceObject resourceObject) {
        // TODO what about returned value?
        publishTable.addSelfPublishedResourceObject(resourceObject);

    }

    /**
     * <p>
     * Stores given resource object (with owner subobject) in this manager. If resource is stored here already, it is
     * refreshed. It has to be used when remote node sent a publish object request (and this peer decided not to forward
     * this message) or local node doesn't know better peer to publish its resource.
     * </p>
     * <p>
     * Can't be used before {@link #hashResourceIDs(byte, byte)} method was invoked because hash can't be computed then.
     * </p>
     * TODO compute hash
     *
     * @param resourceObject
     * @return True if object was already stored in this manager. False otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean storeResourceObject(ResourceObject resourceObject) {

        boolean isAlreadyInMap = false;

        // gets properties of resource object
        Integer contentType = new Integer(resourceObject.getContentType());
        Integer contentSubType = new Integer(resourceObject.getContentSubtype());
        ResourceID resourceID = resourceObject.getResourceID();

        /*if (logger.isDebugEnabled()) {
            logger.debug("ResourceObject=" + resourceObject + " contentType=" + contentType + " contentSubType=" + contentSubType + " resourceID=" +
                    ByteUtils.byteArrayToHexString(resourceID.getResourceID()));
        }*/

        // gets map for content type
        Object o = resourceObjectsMap.get(contentType);
        Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>> contentSubTypeMap;
        if (o == null) {
            contentSubTypeMap = new Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>>();
            resourceObjectsMap.put(contentType, contentSubTypeMap);
        } else {
            contentSubTypeMap = (Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>>) o;
        }

        // gets map for content subtype
        o = contentSubTypeMap.get(contentSubType);
        Hashtable<ResourceID, Hashtable<Owner, ResourceObject>> resourceIDMap;
        if (o == null) {
            resourceIDMap = new Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>();
            contentSubTypeMap.put(contentSubType, resourceIDMap);
        } else {
            resourceIDMap = (Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>) o;
        }

        // gets owners map for resource id
        o = resourceIDMap.get(resourceID);
        Hashtable<Owner, ResourceObject> ownersMap;
        if (o == null) {
            ownersMap = new Hashtable<Owner, ResourceObject>();
            resourceIDMap.put(resourceObject.getResourceID(), ownersMap);
        } else {

            ownersMap = (Hashtable<Owner, ResourceObject>) o;
        }

        // gets same resource from owners map
        o = ownersMap.get(resourceObject.getOwner());
        // if there's no resource object in owners map
        if (o == null) {
            if (logger.isTraceEnabled()) logger.trace("Resource object put " + resourceObject);
            ownersMap.put(resourceObject.getOwner(), resourceObject);
        } // if this resource object is already in this manager
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Already stored here - ResourceObject=" + resourceObject + " contentType=" + contentType
                        + " contentSubtType=" + contentSubType + " resourceID="
                        + ByteUtils.byteArrayToHexString(resourceID.getResourceID()) + "]");
            }
            isAlreadyInMap = true;
            ResourceObject resourceFromMap = (ResourceObject) o;
            resourceFromMap.refresh();
        }

        return isAlreadyInMap;
    }

    /**
     * TODO Invoked when this object has time for do its things.
     *
     * @param node Node that this manager is part of.
     */
    public void onTimeSlot(Node node) {

        // gives time slot to node
        publishTable.onTimeSlot(node, this);

        // TODO iterating through ResourceObjects and checking if they expired

    }

    /**
     * Return true if this resource manager manages given contentType. False otherwise.
     */
    public boolean isManagingContentType(int contentType) {
        Integer contentTypeAsInteger = new Integer(contentType);
        return services.containsKey(contentTypeAsInteger) || resourceObjectsMap.containsKey(contentTypeAsInteger);
    }

    /**
     * Returns true if given contentType identifies service. False otherwise.
     *
     * @return
     */
    public boolean isService(int contentType) {
        return services.containsKey(new Integer(contentType));
    }

    /**
     * Returns true if given contentType identifies resource object. False otherwise.
     */
    public boolean isResourceObject(int contentType) {
        return resourceObjectsMap.containsKey(new Integer(contentType));
    }

    /**
     * Sets properties of resource manager.Intended for usage after peer has been bootstrapped (it's the moment when he
     * starts to know what hash algorithm is used by an overlay). Hash algorithm and length are saved in resource
     * manager so that hashing of resources can be done. TODO probably hashing of resources and creating resource
     * objects for services should be done.
     *
     * @param hashAlgorithm  ID of hash algorithm
     * @param hashLength     Length of hash.
     * @param owner          Owner object that will be used for creating ResourceObjects.
     * @param expirationTime Time (seconds) after which a resource object is considered expired.
     */
    public void setProperties(byte hashAlgorithm, byte hashLength, Owner owner, int expirationTime) {
        this.hashAlgorithm = hashAlgorithm;
        this.hashLength = hashLength;
        this.owner = owner;
        this.expirationTime = expirationTime;
    }

    /**
     * Returns true if this manager is managing a resource or service described using given parameters. False otherwise.
     * If owner is null, there may be more than one ResourceObject managed. In other case, there may be only one.
     *
     * @param contentType    Content type of resource or service.
     * @param contentSubtype Content subtype of resource or service.
     * @param resourceID     ResourceID of searched resource or service.
     * @param owner          Owner of searched resource or service. May be null.
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isManaging(int contentType, int contentSubtype, ResourceID resourceID, Owner owner) {
        boolean result = false;

        if (isManagingContentType(contentType)) {
            if (isResourceObject(contentType)) {

                // gets map for content type
                Object o = resourceObjectsMap.get(contentType);
				Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>> contentSubTypeMap;
                if (o != null) {
					contentSubTypeMap = (Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>>) o;

                    // gets map for content subtype
                    o = contentSubTypeMap.get(contentSubtype);
					Hashtable<ResourceID, Hashtable<Owner, ResourceObject>> resourceIDMap;
                    if (o != null) {
						resourceIDMap = (Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>) o;

                        // gets owners map for resource id
                        o = resourceIDMap.get(resourceID);
						Hashtable<Owner, ResourceObject> ownersMap;
                        if (o != null) {
							ownersMap = (Hashtable<Owner, ResourceObject>) o;

                            // if object is searched without owner
                            if (owner == null) {
                                // if there are any owner objects
                                if (ownersMap.keySet().size() > 0) {
                                    result = true;
                                }
                            } else {
                                // gets same resource from owners map
                                o = ownersMap.get(owner);
                                // if there's resource object
                                if (o != null) {
                                    result = true;
                                }
                            }
                        }
                    }
                }

            }
        }

        return result;
    }

    /**
     * Returns true if given resource object is already managed by this manager. False otherwise.
     *
     * @param resourceObject
     * @return
     */
    public boolean isManaging(ResourceObject resourceObject) {
        return isManaging(resourceObject.getContentType(), resourceObject.getContentSubtype(), resourceObject
                .getResourceID(), resourceObject.getOwner());
    }

    /**
     * Returns ResourceObject for service identified by given content type.
     *
     * @param contentType
     * @param ownPeerInfo Object to be added as value.
     * @return
     */
    public ResourceObject getResourceObjectForService(int contentType, PeerInfo ownPeerInfo) {

        if (logger.isTraceEnabled()) {
            logger.trace("ResourceObject is created for service with contentType=" + contentType + " and ownPeerInfo=" + ownPeerInfo);
        }

        try {
            Service service = getService(contentType);

            // creates ResourceID basing on hash created from "seed"
            byte[] resourceIDSeed = getServiceResourceIDSeed(service);
            byte[] hash = P2PPUtils.hash(resourceIDSeed, hashAlgorithm);
            ResourceID resourceID = new ResourceID(hash);

            ResourceObject resourceObject = null;

            switch (contentType) {
                case P2PPUtils.SIP_CONTENT_TYPE: {
                    resourceObject = new SIPServiceResourceObject(ownPeerInfo);
                    break;
                }
                case P2PPUtils.STUN_CONTENT_TYPE: {
                    resourceObject = new STUNServiceResourceObject(ownPeerInfo);
                    break;
                }
                case P2PPUtils.TURN_CONTENT_TYPE: {
                    break;
                }
                case P2PPUtils.STUN_TURN_ICE_CONTENT_TYPE: {
                    resourceObject = new ICEServiceResourceObject(ownPeerInfo);
                    break;
                }
            }

            if (resourceObject != null) {
                resourceObject.setResourceID(resourceID);
                resourceObject.setOwner(new Owner(ownPeerInfo.getPeerID().getPeerIDBytes()));
            }

            return resourceObject;
        } catch (NoSuchAlgorithmException e) {
            // TODO probably can't happen but I'm not sure..
            logger.error("Error while creating ResourceObject", e);
        }

        return null;
    }

    /**
     * Returns a list of ResourceObjects for services that this manager is managing. If null is returned, there's no
     * local services.
     *
     * @param ownPeerInfo
     * @return
     */
    public List<ResourceObject> getResourceObjectsForServices(PeerInfo ownPeerInfo) {
        if (services.keySet().isEmpty()) {
            return null;
        }

        List<ResourceObject> list = new ArrayList<ResourceObject>();
        Enumeration<Integer> enumeration = services.keys();

        while (enumeration.hasMoreElements()) {
            Integer currentContentType = enumeration.nextElement();

            ResourceObject resourceObject = getResourceObjectForService(currentContentType.intValue(), ownPeerInfo);

            list.add(resourceObject);
        }

        if (list.isEmpty()) {
            return null;
        } else {
            return list;
        }
    }

    /**
     * Method that returns a list of resources that are closer to given id than to local node's id. Returns null if
     * there are no resources closer to given remoteID that to localID.
     *
     * @param localHashedID  ID of local node.
     * @param remoteHashedID ID of remote node.
     * @param node           Local node that will be used for computing the distance between IDs.
     * @return
     */
    public List<ResourceObject> getResourceObjectsCloserTo(byte[] localHashedID, byte[] remoteHashedID, Node node) {
        List<ResourceObject> resources = new ArrayList<ResourceObject>();

        // iterates over all resource objects and checks the distance between them and local and remote ID
        for (Integer currentContentType : resourceObjectsMap.keySet()) {
            Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>> contentSubtypeMap = resourceObjectsMap
                    .get(currentContentType);

            // iterates over content subtype map
            for (Integer currentContentSubtype : contentSubtypeMap.keySet()) {
                Hashtable<ResourceID, Hashtable<Owner, ResourceObject>> idsMap = contentSubtypeMap
                        .get(currentContentSubtype);

                // iterates over resourceIDs
                for (ResourceID currentID : idsMap.keySet()) {

                    byte[] resourceIDAsBytes = currentID.getResourceID();

                    // calculates distance between [currentResourceID, localHashedID] and [currentResourceID,
                    // remoteHashedID]
                    BigInteger distanceToLocal = node.getDistanceBetweenHashed(resourceIDAsBytes, localHashedID);
                    BigInteger distanceToRemote = node.getDistanceBetweenHashed(resourceIDAsBytes, remoteHashedID);

                    // if remoteID is closer to resourceID than localID
                    if (distanceToRemote.compareTo(distanceToLocal) < 0) {
                        // copies all the resources to a list of resources that will be returned
                        Hashtable<Owner, ResourceObject> ownersMap = idsMap.get(currentID);
                        for (Owner currentOwner : ownersMap.keySet()) {
                            resources.add(ownersMap.get(currentOwner));
                        }
                    }

                }
            }

        }

        // if there are no resource closer to given remoteID, nullifies list
        if (resources.size() == 0) {
            resources = null;
        }
        return resources;
    }

    /**
     * Returns all the resource objects from this manager. If there are no ResourceObjects, null is returned.
     *
     * @return
     */
    public List<ResourceObject> getAllResourceObjects() {
        if (logger.isTraceEnabled()) logger.trace("Returns all the resource objects.");
        List<ResourceObject> resources = new ArrayList<ResourceObject>();

        // iterates over all resource objects to copy them
        Set<Integer> contentTypesSet = resourceObjectsMap.keySet();
        for (Integer currentContentType : contentTypesSet) {
            Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>> contentSubtypeMap = resourceObjectsMap
                    .get(currentContentType);

            // iterates over content subtype map
            Set<Integer> contentSubtypesSet = contentSubtypeMap.keySet();
            for (Integer currentSubtype : contentSubtypesSet) {
                Hashtable<ResourceID, Hashtable<Owner, ResourceObject>> resourceIDMap = contentSubtypeMap
                        .get(currentSubtype);

                // iterates over resourceID map
                Set<ResourceID> resourceIDsSet = resourceIDMap.keySet();
                for (ResourceID currentResourceID : resourceIDsSet) {
                    Hashtable<Owner, ResourceObject> ownerMap = resourceIDMap.get(currentResourceID);

                    Set<Owner> ownersSet = ownerMap.keySet();
                    for (Owner currentOwner : ownersSet) {
                        ResourceObject currentResource = ownerMap.get(currentOwner);

                        resources.add(currentResource);
                    }
                }
            }
        }

        if (resources.isEmpty()) {
            resources = null;
        }
        return resources;
    }

    /**
     * Removes resource described by given arguments. If there's a resource for those arguments, it is returned. If it
     * isn't stored here, null is returned.
     *
     * @param contentType
     * @param contentSubtype
     * @param resourceID
     * @param owner
     * @return
     */
    public ResourceObject removeResourceObject(int contentType, int contentSubtype, ResourceID resourceID, Owner owner) {
        ResourceObject removedResource = null;

        Hashtable<Integer, Hashtable<ResourceID, Hashtable<Owner, ResourceObject>>> contentSubtypeMap = resourceObjectsMap
                .get(contentType);

        if (contentSubtypeMap != null) {
            Hashtable<ResourceID, Hashtable<Owner, ResourceObject>> resourceIDMap = contentSubtypeMap.get(contentSubtype);

            if (resourceIDMap != null) {
                Hashtable<Owner, ResourceObject> ownerMap = resourceIDMap.get(resourceID);

                if (ownerMap != null) {
                    // removes resource from Owners map
                    removedResource = ownerMap.remove(owner);
                    if (logger.isDebugEnabled()) {
                        StringBuilder strb = new StringBuilder("Resource removed: contentType=").append(contentType).append(", contentSubtype=").
                                append(contentSubtype).append(", resourceID=").append(ByteUtils.byteArrayToHexString(resourceID.getResourceID())).
                                append(", owner=").append(ByteUtils.byteArrayToHexString(owner.getPeerIDValue()));
                        logger.debug(strb.toString());
                    }
                    // if there are no resources left in owners map, owners map is removed from resourceIDs map
                    if (ownerMap.isEmpty()) {
                        resourceIDMap.remove(resourceID);
                    }
                }

            }
            // if there are no entries left in resourceIDs map, this map is removed from contentSubtype map
            if (resourceIDMap != null && resourceIDMap.isEmpty()) {
                contentSubtypeMap.remove(contentSubtype);
            }
            // if there are no entries left in contentSubtype map, this map is removed from contentType map
            if (contentSubtypeMap.isEmpty()) {
                resourceObjectsMap.remove(contentSubtype);
            }
        }

        return removedResource;
    }

    /**
     * Removes all ResourceObjects from this manager and returns them in the list. If there are no resources in this
     * manager, null is returned.
     *
     * @return
     */
    public List<ResourceObject> removeAllResourceObjects() {
        List<ResourceObject> resources = getAllResourceObjects();

        for (ResourceObject currentResource : resources) {
            if (currentResource != null) {
                removeResourceObject(currentResource.getContentType(), currentResource.getContentSubtype(),
                        currentResource.getResourceID(), currentResource.getOwner());
            } else {
                if (logger.isTraceEnabled()) logger.error("One of resources is null... That's bad!");
            }
        }

        if (logger.isTraceEnabled()) logger.trace(resources.size() + " resources removed.");

        if (resources.isEmpty()) {
            resources = null;
        }
        return resources;
    }

    /**
     * Resets properties.
     */
    public void leaveReset() {
        hashAlgorithm = 0;
        hashLength = 0;
        owner = null;
        expirationTime = 0;

        publishTable.removeAllResourceObjects();
	}

	/**
     * @param routingTable
     */
	public void setRoutingTable(RoutingTable routingTable) {
		publishTable.setRoutingTable(routingTable);
	}

	/**
     * @param neighborTable
     */
	public void setNeighborTable(NeighborTable neighborTable) {
		publishTable.setNeighborTable(neighborTable);
	}
}