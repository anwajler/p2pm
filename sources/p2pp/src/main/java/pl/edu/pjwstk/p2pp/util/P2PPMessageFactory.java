package pl.edu.pjwstk.p2pp.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.messages.Acknowledgment;
import pl.edu.pjwstk.p2pp.messages.MalformedP2PPMessageException;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.indications.LeaveIndication;
import pl.edu.pjwstk.p2pp.messages.requests.*;
import pl.edu.pjwstk.p2pp.messages.responses.*;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.Certificate;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.Owner;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RLookup;
import pl.edu.pjwstk.p2pp.objects.ResourceID;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.ResourceObjectValue;
import pl.edu.pjwstk.p2pp.objects.Signature;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.objects.Uptime;
import pl.edu.pjwstk.p2pp.socialcircle.PathID;
import pl.edu.pjwstk.p2pp.socialcircle.SocialPath;
import pl.edu.pjwstk.p2pp.socialcircle.messages.requests.SocialCircleSetUpRequest;
import pl.edu.pjwstk.p2pp.socialcircle.messages.requests.SocialLookupNeighbourRequest;
import pl.edu.pjwstk.p2pp.socialcircle.messages.responses.SocialCircleSetUpResponse;
import pl.edu.pjwstk.p2pp.socialcircle.messages.responses.SocialLookupNeighbourResponse;
import pl.edu.pjwstk.p2pp.superpeer.messages.indications.IndexPeerIndication;
import pl.edu.pjwstk.p2pp.superpeer.messages.requests.IndexRequest;
import pl.edu.pjwstk.p2pp.superpeer.messages.requests.LookupIndexRequest;
import pl.edu.pjwstk.p2pp.superpeer.messages.requests.LookupPeerIndexRequest;
import pl.edu.pjwstk.p2pp.superpeer.messages.responses.IndexResponse;
import pl.edu.pjwstk.p2pp.superpeer.messages.responses.LookupIndexResponse;
import pl.edu.pjwstk.p2pp.superpeer.messages.responses.LookupPeerIndexResponse;

/**
 * Class with utility methods for creating messages as they're defined in P2PP (draft 01).
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public final class P2PPMessageFactory extends AbstractMessageFactory {

    private static Logger LOG = org.apache.log4j.Logger.getLogger(P2PPMessageFactory.class);

    @Override
    public P2PPMessage readAndInterpret(InputStream stream, byte sourceIDLength) throws MalformedP2PPMessageException,
            UnsupportedGeneralObjectException {
        P2PPMessage message = null;
        // TODO now not proper
        byte[] headerBuffer = new byte[16];
        try {

            // Reads the constant part (in terms of size) of common header.
            if (stream.read(headerBuffer) != 20) {
                throw new MalformedP2PPMessageException("There's too little data for common header to be filled.");
            }

            boolean[] protocol = determineProtocol(headerBuffer);
            boolean[] messageType = determineMessageType(headerBuffer);
            boolean isAcknowledgement = determineAcknowledgement(headerBuffer);
            boolean isSentByPeer = determineIsSentByPeer(headerBuffer);
            boolean isRecursive = determineIsRecursive(headerBuffer);
            boolean[] reservedOrResponseCode = determineResponseCode(headerBuffer);
            byte requestType = determineRequestType(headerBuffer);
            byte ttl = determineTTL(headerBuffer);
            // FIXME probably check if magic cookie is ok
            int magicCookie = determineMagicCookie(headerBuffer);
            int senderPort = determineSenderPort(headerBuffer);
            byte[] transactionID = determineTransactionID(headerBuffer);
            byte[] messageLengthBytes = determineMessageLength(headerBuffer);
            int messageLength = (int) ByteUtils.bytesToLong(messageLengthBytes[0], messageLengthBytes[1],
                    messageLengthBytes[2], messageLengthBytes[3]);
            byte[] sourceID = null;
            byte[] responseID = null;
            byte[] messageBody = null;

            if (magicCookie != P2PPMessage.MAGIC_COOKIE) {
                throw new MalformedP2PPMessageException("This is not P2PP message.");
            }

            // In bootstrap and authenticate sourceID is 4 bytes long.
            if (!(requestType == P2PPMessage.BOOTSTRAP_MESSAGE_TYPE || requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE)) {
                sourceIDLength = 4;
            }

            sourceID = new byte[sourceIDLength];
            if (stream.read(sourceID) != sourceIDLength) {
                throw new MalformedP2PPMessageException("There's too little data for sourceID to be filled.");
            }

            boolean isResponse = false;
            boolean isResponseACK = false;
            boolean isIndication = false;
            boolean isRequest = false;

            if (Arrays.equals(messageType, P2PPMessage.RESPONSE_MESSAGE_TYPE)) {
                isResponse = true;
                responseID = new byte[sourceIDLength];
                if (stream.read(responseID) != sourceIDLength) {
                    throw new MalformedP2PPMessageException("There's too little data for responseID to be filled.");
                }
            } else if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
                isRequest = true;
            } else if (Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)) {
                isResponseACK = true;
                responseID = new byte[sourceIDLength];
                if (stream.read(responseID) != sourceIDLength) {
                    throw new MalformedP2PPMessageException("There's too little data for responseID to be filled.");
                }
            } else if (Arrays.equals(messageType, P2PPMessage.INDICATION_MESSAGE_TYPE)) {
                isIndication = true;
            }

            // reads body of message
            messageBody = new byte[messageLength];
            if (stream.read(messageBody) != messageLength) {
                throw new MalformedP2PPMessageException("Length of data doesn't match the declared value.");
            }

            if (requestType == P2PPMessage.ENROLL_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Enroll message.");
            } else if (requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Authenticate message.");
            } else if (requestType == P2PPMessage.BOOTSTRAP_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Bootstrap message.");

                if (isResponse || isResponseACK) {
                    message = new BootstrapResponse();
                    setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer, isRecursive,
                            reservedOrResponseCode, requestType, ttl, senderPort, transactionID, messageLengthBytes, sourceID,
                            responseID);
                    setMessageValue(message, messageBody, sourceIDLength, isRequest, isResponse, isResponseACK,
                            isIndication, false);

                }

            } else if (requestType == P2PPMessage.JOIN_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Join message.");
                if (isResponse || isResponseACK) {
                    message = new JoinResponse();
                    setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer, isRecursive,
                            reservedOrResponseCode, requestType, ttl, senderPort, transactionID, messageLengthBytes, sourceID,
                            responseID);
                    setMessageValue(message, messageBody, sourceIDLength, isRequest, isResponse, isResponseACK,
                            isIndication, false);

                }
            } else if (requestType == P2PPMessage.LEAVE_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Leave message.");
                if (isIndication) {
                    message = new LeaveIndication();
                    setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer, isRecursive,
                            reservedOrResponseCode, requestType, ttl, senderPort, transactionID, messageLengthBytes, sourceID,
                            responseID);
                    setMessageValue(message, messageBody, sourceIDLength, isRequest, isResponse, isResponseACK,
                            isIndication, false);
                }
            } else if (requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Keep alive message.");
            } else if (requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Lookup peer message.");
            } else if (requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Exchange table message.");
            } else if (requestType == P2PPMessage.QUERY_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Query message.");
            } else if (requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Publish message.");
            } else if (requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Lookup object message.");
            } else if (requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Remove object message.");
            } else if (requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Replicate message.");
            } else if (requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Transfer message.");
            } else if (requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Tunnel message.");
            } else if (requestType == P2PPMessage.CONNECT_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Connect message.");
            } else if (requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE) {
                if (LOG.isTraceEnabled()) LOG.trace("Get diagnostics message.");
            }

        } catch (IOException e) {
            if (LOG.isTraceEnabled()) LOG.trace("IO Error while reading from stream in " + "Message.readAndInterpret().");
            message = null;
        } catch (IndexOutOfBoundsException e) {
            if (LOG.isTraceEnabled()) LOG.trace("Corrupted message in stream.");
            throw new MalformedP2PPMessageException();
        }

        return message;
    }

    @Override
    public P2PPMessage interpret(byte[] data, byte sourceIDLength) throws MalformedP2PPMessageException, UnsupportedGeneralObjectException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Interpretting message dateLength=" + data.length + " sourceIDLength=" + sourceIDLength);
        }
        try {
            P2PPMessage message = null;

            // interprets common header
            boolean[] protocol = determineProtocol(data);
            boolean[] messageType = determineMessageType(data);
            boolean isAcknowledgement = determineAcknowledgement(data);
            boolean isSentByPeer = determineIsSentByPeer(data);
            boolean isRecursive = determineIsRecursive(data);
            boolean[] reservedOrResponseCode = determineResponseCode(data);
            int reservedOrResponseCodeAsInt = ByteUtils.booleanArrayToInt(reservedOrResponseCode);
            byte requestType = determineRequestType(data);
            byte ttl = determineTTL(data);
            int magicCookie = determineMagicCookie(data);
            int senderPort = determineSenderPort(data);
            byte[] transactionID = determineTransactionID(data);
            byte[] messageLength = determineMessageLength(data);

            // In bootstrap and authenticate sourceID is 4 bytes long.
            if (requestType == P2PPMessage.BOOTSTRAP_MESSAGE_TYPE
                    || requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE
                    || requestType == P2PPMessage.LOOKUP_NEIGHBOUR_MESSAGE_TYPE
                    || requestType == P2PPMessage.INDEX_PEER_MESSAGE_TYPE
                    || (requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE && !isSentByPeer)) {
                sourceIDLength = 4;
            }

            byte[] sourceID = determineSourceID(data, sourceIDLength);
            byte[] responseID = null;

            boolean isRequest = false;
            boolean isIndication = false;
            boolean isResponse = false;
            boolean isResponseACK = false;

            // Checks if this message is a P2PP message.
            if (magicCookie != P2PPMessage.MAGIC_COOKIE) {
                if (LOG.isTraceEnabled()) LOG.trace("Message appears not be a P2PP message");
                return null;
            }

            if (Arrays.equals(messageType, P2PPMessage.RESPONSE_MESSAGE_TYPE)) {
                // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                // "\tresponse.");
                isResponse = true;
                responseID = determineResponseID(data, sourceIDLength);
            } else if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
                // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                // "\trequest.");
                isRequest = true;
            } else if (Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)) {
                // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                // "\tresponseACK");
                isResponseACK = true;
                responseID = determineResponseID(data, sourceIDLength);
            } else if (Arrays.equals(messageType, P2PPMessage.INDICATION_MESSAGE_TYPE)) {
                // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                // "\tindication.");
                isIndication = true;
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("requestType=" + requestType + "; sourceIDLength=" + sourceIDLength + "; isRequest=" + isRequest + "; isResponse=" +
                        isResponse + "; isResponseACK=" + isResponseACK);
            }

            // if is ACK
            if (isAcknowledgement) {
                // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()", "\tACK.");
                // TODO are two last arguments (isOverReliable and isEncrypted) properly added?
                message = new Acknowledgment(protocol, messageType, isSentByPeer, isRecursive, reservedOrResponseCode,
                        requestType, ttl, transactionID, sourceID, responseID, false, false);
            } else {

                // if contains OK response code or 0 (so that it is a request)
                if (reservedOrResponseCodeAsInt == Response.RESPONSE_CODE_OK || reservedOrResponseCodeAsInt == 0) {
                    // checks message types
                    if (requestType == P2PPMessage.ENROLL_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.TYPES_ALL, LoggerLevelInfo.LEVELS_ALL,
                        // "P2PPMessageFactory.interpret()", "\tEnroll message.");
                    } else if (requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.TYPES_ALL, LoggerLevelInfo.LEVELS_ALL,
                        // "P2PPMessageFactory.interpret()", "\tAuthenticate message.");
                    } else if (requestType == P2PPMessage.BOOTSTRAP_MESSAGE_TYPE) {

                        if (isResponse || isResponseACK) {
                            message = new BootstrapResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        } else if (isRequest) {
                            message = new BootstrapRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }

                    } else if (requestType == P2PPMessage.JOIN_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tJoin message.");
                        if (isResponse || isResponseACK) {
                            message = new JoinResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        } else if (isRequest) {
                            message = new JoinRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        }
                    } else if (requestType == P2PPMessage.LEAVE_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tLeave message.");
                        if (isIndication) {
                            message = new LeaveIndication();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.INDEX_PEER_MESSAGE_TYPE) {
                        if (isIndication) {
                            message = new IndexPeerIndication();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tKeep alive message.");
                    } else if (requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tLookup peer message.");
                        if (isResponse || isResponseACK) {
                            if (LOG.isDebugEnabled()) LOG.debug("Lookup peer response message");
                            message = new LookupPeerResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        } else if (isRequest) {
                            if (LOG.isDebugEnabled()) LOG.debug("Lookup peer request message");
                            message = new LookupPeerRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tExchange table message.");
                    } else if (requestType == P2PPMessage.QUERY_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tQuery message.");
                    } else if (requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tPublish message.");
                        if (isResponse || isResponseACK) {
                            message = new PublishObjectResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        } else if (isRequest) {
                            message = new PublishObjectRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        }
                    } else if (requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tLookup object message.");
                        if (isResponse || isResponseACK) {
                            message = new LookupObjectResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        } else if (isRequest) {
                            message = new LookupObjectRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        }
                    } else if (requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE) {
                        if (LOG.isTraceEnabled()) LOG.trace("Remove object message.");
                        if (isResponse || isResponseACK) {
                            message = new RemoveObjectResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        } else if (isRequest) {
                            message = new RemoveObjectRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tReplicate message.");
                    } else if (requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tTransfer message.");
                        if (isResponse || isResponseACK) {
                            message = new TransferResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        } else if (isRequest) {
                            message = new TransferRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tTunnel message.");
                    } else if (requestType == P2PPMessage.CONNECT_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tConnect message.");
                    } else if (requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tGet diagnostics message.");
                    } else if (requestType == P2PPMessage.LOOKUP_NEIGHBOUR_MESSAGE_TYPE) {
                        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.interpret()",
                        // "\tTransfer message.");
                        if (isResponse || isResponseACK) {
                            message = new SocialLookupNeighbourResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        } else if (isRequest) {
                            message = new SocialLookupNeighbourRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.SET_UP_MESSAGE_TYPE) {
                        if (isResponse || isResponseACK) {
                            message = new SocialCircleSetUpResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        } else if (isRequest) {
                            message = new SocialCircleSetUpRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.INDEX_MESSAGE_TYPE) {
                        if (isResponse || isResponseACK) {
                            if (LOG.isDebugEnabled()) LOG.debug("Index response message");
                            message = new IndexResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        } else if (isRequest) {
                            if (LOG.isDebugEnabled()) LOG.debug("Index request message");
                            message = new IndexRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.LOOKUP_INDEX_MESSAGE_TYPE) {
                        if (isResponse || isResponseACK) {
                            if (LOG.isDebugEnabled()) LOG.debug("Lookup index response message");
                            message = new LookupIndexResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        } else if (isRequest) {
                            if (LOG.isDebugEnabled()) LOG.debug("Lookup index request message");
                            message = new LookupIndexRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.LOOKUP_PEER_INDEX_MESSAGE_TYPE) {
                        if (isResponse || isResponseACK) {
                            if (LOG.isDebugEnabled()) LOG.debug("Lookup peer index response message");
                            message = new LookupPeerIndexResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        } else if (isRequest) {
                            if (LOG.isDebugEnabled()) LOG.debug("Lookup peer index request message");
                            message = new LookupPeerIndexRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.SEND_MESSAGE_MESSAGE_TYPE) {
                        if (isResponse || isResponseACK) {
                            if (LOG.isDebugEnabled()) LOG.debug("Send message response message");
                            message = new SendMessageResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        } else if (isRequest) {
                            if (LOG.isDebugEnabled()) LOG.debug("Send message request message");
                            message = new SendMessageRequest();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    }
                } // if received message is with next hop response code
                else if (reservedOrResponseCodeAsInt == Response.RESPONSE_CODE_NEXT_HOP) {
                    if (LOG.isDebugEnabled()) LOG.debug("Next hop response message");
                    message = new NextHopResponse();
                    setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer, isRecursive,
                            reservedOrResponseCode, requestType, ttl, senderPort, transactionID, messageLength, sourceID,
                            responseID);
                    setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK, isIndication,
                            true);
                } else if (reservedOrResponseCodeAsInt == Response.RESPONSE_CODE_NOT_FOUND) {
                    if (requestType == P2PPMessage.LOOKUP_INDEX_MESSAGE_TYPE) {
                        if (isResponse || isResponseACK) {
                            if (LOG.isDebugEnabled()) LOG.debug("Lookup index response message");
                            message = new LookupIndexResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.LOOKUP_PEER_INDEX_MESSAGE_TYPE) {
                        if (LOG.isDebugEnabled()) LOG.debug("Lookup peer index response message");
                        message = new LookupPeerIndexResponse();
                        setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                messageLength, sourceID, responseID);
                        setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                isIndication, true);
                    }
                } else if (reservedOrResponseCodeAsInt == Response.RESPONSE_CODE_REQUEST_REJECTED) {
                    if (LOG.isTraceEnabled()) LOG.trace("BootstrapResponse message");
                    if (requestType == P2PPMessage.BOOTSTRAP_MESSAGE_TYPE) {
                        if (isResponse || isResponseACK) {
                            message = new BootstrapResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);
                        }
                    } else if (requestType == P2PPMessage.JOIN_MESSAGE_TYPE) {
                        if (isResponse || isResponseACK) {
                            message = new JoinResponse();
                            setCommonHeaderData(message, protocol, messageType, isAcknowledgement, isSentByPeer,
                                    isRecursive, reservedOrResponseCode, requestType, ttl, senderPort, transactionID,
                                    messageLength, sourceID, responseID);
                            setMessageValue(message, data, sourceIDLength, isRequest, isResponse, isResponseACK,
                                    isIndication, true);

                        }
                    }
                }
            }

            // verifies message
            if (!message.verify()) {
                throw new MalformedP2PPMessageException(message.getClass().getName() + " isn't proper.");
            }

            return message;
        } catch (IndexOutOfBoundsException e) {
            // TODO some kind of additional info as a string?
            throw new MalformedP2PPMessageException("Index was out of bounds.");
        } catch (NullPointerException e) {
            // TODO some kind of additional info as a string?
            e.printStackTrace();
            throw new MalformedP2PPMessageException("Null was thrown." + e.getMessage());

        }
    }

    /**
     * Sets message value (after common header). It assumes that the message header is filled with data, so that this
     * method may use them.
     *
     * @param message        Message to be filled with data.
     * @param data           Data that a massage will be filled with.
     * @param sourceIDLength Length (in bytes) of sourceID field (responseID also, if the message is a response).
     * @param isRequest
     * @param isResponse
     * @param isResponseACK
     * @param isIndication
     * @param containsHeader True if the starts with common header. False if it starts with message body.
     * @throws UnsupportedGeneralObjectException
     *          Thrown when this method is given corrupted data or GeneralObject that can't be handled by current
     *          implementation.
     */
    private void setMessageValue(P2PPMessage message, byte[] data, byte sourceIDLength, boolean isRequest, boolean isResponse, boolean isResponseACK,
                                 boolean isIndication, boolean containsHeader) throws UnsupportedGeneralObjectException, MalformedP2PPMessageException {

        int index = 0;
        int messageSize = data.length;
        if (containsHeader) {
            index = (P2PPMessage.COMMON_HEADER_CONSTANT_SIZE / 8) + sourceIDLength;
            messageSize -= (P2PPMessage.COMMON_HEADER_CONSTANT_SIZE / 8) + sourceIDLength;
            if (isResponse || isResponseACK) {
                index += sourceIDLength;
                messageSize -= sourceIDLength;
            }
        }

        // Analyzes objects one by one.
        boolean hasMoreObjects = true;
        if ((isResponse || isResponseACK) && message instanceof LookupPeerResponse) hasMoreObjects = false; //ugly temp hack
        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.INFO, "P2PPMessageFactory.setMessageValue()",
        // "\tStarts parsing of message of type=" + message.getRequestOrResponseType() + " and size="
        // + messageSize);
        int remainingBytes = (int) message.getMessageLengthAsLong();

        if (LOG.isTraceEnabled()) {
            LOG.trace("setMessageValue -> sourceIDLength=" + sourceIDLength + " index=" + index + " messageSize=" + messageSize + " dataLength=" +
                    data.length + " hasMoreObjects=" + hasMoreObjects + " remainingBytes=" + remainingBytes);
        }

        while (hasMoreObjects) {
            if (index < messageSize) {

                int objectWithHeaderSize = parseGeneralObject(data, index, remainingBytes, null, message);
                // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.INFO, "P2PPMessageFactory.setMessageValue()",
                // "\tadded object (with header) of size=" + objectWithHeaderSize);
                index += objectWithHeaderSize;
                remainingBytes -= objectWithHeaderSize;

            } else {
                hasMoreObjects = false;
            }

        }
        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.INFO, "P2PPMessageFactory.setMessageValue()",
        // "\tParsing ended.");

    }

    /**
     * Parses given data starting at given index. When finds an object, it adds it to a message or to an outerObject.
     * Which situation takes place depends on which argument is used (outerObject xor message MUST be a null).
     *
     * @param data           Data having an object.
     * @param currentIndex   Index of first byte to read.
     * @param remainingBytes Number of bytes remaining to fill an outer object.
     * @param outerObject    GeneralObject that is to be filled an object that will be an outcome of this method.
     * @param message        Message that will be filled with objects. Used only by outer methods.
     * @return Number of bytes read from data array.
     * @throws UnsupportedGeneralObjectException
     *          Thrown when this method is given corrupted data or GeneralObject that can't be handled by current
     *          implementation.
     */
    public int parseGeneralObject(byte[] data, int currentIndex, int remainingBytes, GeneralObject outerObject, P2PPMessage message)
            throws UnsupportedGeneralObjectException, MalformedP2PPMessageException {
        int indexBeforeOuterObjectFilled = currentIndex;
        // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
        // "\tbefore indexBeforeOuterObjectFilled=" + indexBeforeOuterObjectFilled);
        GeneralObject object = null;
        while (remainingBytes > 0) {

            // reads the common header
            int indexBeforeObjectRead = currentIndex;
            byte objectType = data[currentIndex];
            byte abReserved = data[currentIndex + 1];
            int objectLength = (data[currentIndex + 2] << 8) + (data[currentIndex + 3] & 0xFF);

            // moves index after the header and "consumes" the header size
            currentIndex += 4;
            remainingBytes -= 4;

            if (LOG.isTraceEnabled()) {
                LOG.trace("indexBeforeObjectRead=" + indexBeforeObjectRead);
                LOG.trace("objectLength=" + objectLength);
                LOG.trace("currentIndex=" + currentIndex + " remainingBytes=" + remainingBytes);

                //ExtendedBitSet ebs = new ExtendedBitSet();
                //ebs.set(0, data); // ???
                //logger.trace("BitSet length = " + ebs.getFixedLength() + ebs.toStringBytes());
            }

            switch (objectType) {
                case GeneralObject.PEER_INFO_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tPeerInfo");
                    object = new PeerInfo();
                    //	remainingBytes = objectLength;
                    if (LOG.isTraceEnabled()) LOG.trace("parseGeneralObject -> PeerInfo: " + object);
                    break;
                case GeneralObject.PEER_ID_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tPeerID");
                    object = new PeerID(ByteUtils.subarray(data, currentIndex, objectLength));

                    currentIndex += objectLength;
                    remainingBytes -= objectLength;
                    break;
                case GeneralObject.ADDRESS_INFO_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tAddressInfo");
                    // TODO why it's 1 byte long sometimes (BootstrapResponse) in
                    // reference implementation
                    if (objectLength >= 15) {
                        byte numberOfICE = data[currentIndex++];
                        byte rResvAndIPVer = data[currentIndex++];
                        boolean rFlag = ByteUtils.getBitsFromByteArray(new byte[]{rResvAndIPVer}, 0, 1)[0];
						boolean[] resv = ByteUtils.getBitsFromByteArray(new byte[]{rResvAndIPVer}, 1, 3);
                        boolean[] ipVer = ByteUtils.getBitsFromByteArray(new byte[]{rResvAndIPVer}, 4, 4);
                        byte foundation = data[currentIndex++];
                        byte componentID = data[currentIndex++];
                        int priority = (int) ByteUtils.bytesToLong(data[currentIndex], data[currentIndex + 1],
                                data[currentIndex + 2], data[currentIndex + 3]);
                        currentIndex += 4;
                        byte ttAndHt = data[currentIndex++];
                        boolean[] tt = ByteUtils.getBitsFromByteArray(new byte[]{ttAndHt}, 0, 4);
                        boolean[] ht = ByteUtils.getBitsFromByteArray(new byte[]{ttAndHt}, 4, 4);
                        int port = (data[currentIndex] << 8) + ((data[currentIndex + 1]) & 0xFF);
                        currentIndex += 2;
                        byte[] address = null;
                        if (Arrays.equals(ipVer, AddressInfo.IP_V4)) {
                            address = ByteUtils.subarray(data, currentIndex, 4);
                        } else if (Arrays.equals(ipVer, AddressInfo.IP_V6)) {
                            address = ByteUtils.subarray(data, currentIndex, 16);
                        }
                        object = new AddressInfo(numberOfICE, rFlag, ipVer, foundation, componentID, priority, tt, ht,
                                port, address);

                        currentIndex += address.length;
                        remainingBytes -= objectLength;
                    } else {
                        currentIndex += objectLength;
                        remainingBytes -= objectLength;
                    }
                    break;
                case GeneralObject.UNHASHED_ID_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tUnhashedID");
                    byte[] unshashedID = ByteUtils.subarray(data, currentIndex, objectLength);
                    object = new UnhashedID(unshashedID);
                    currentIndex += objectLength;

                    remainingBytes -= objectLength;
                    break;
                case GeneralObject.UPTIME_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tUptime");
                    byte[] uptimeBytes = ByteUtils.subarray(data, currentIndex, 4);
                    object = new Uptime((int) ByteUtils.bytesToLong(uptimeBytes[0], uptimeBytes[1], uptimeBytes[2],
                            uptimeBytes[3]));
                    currentIndex += 4;
                    remainingBytes -= 4;
                    break;

                case GeneralObject.P2P_OPTIONS_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tP2POptions");
                    byte hashAlgorithm = data[currentIndex++];
                    byte hashAlgorithmLength = data[currentIndex++];
                    byte p2pAlgorithm = data[currentIndex++];
                    byte base = data[currentIndex++];
                    byte overlayIDLength = data[currentIndex++];
                    byte[] overlayID = ByteUtils.subarray(data, currentIndex, overlayIDLength);

                    object = new P2POptions(hashAlgorithm, hashAlgorithmLength, p2pAlgorithm, base, overlayID);

                    currentIndex += overlayIDLength;
                    remainingBytes -= objectLength;
                    break;
                case GeneralObject.REQUEST_OPTIONS_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tRequestOptions");
                    break;
                case GeneralObject.DIAGNOSTICS_OPTIONS_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tDiagnostics");
                    break;
                case GeneralObject.ROUTING_TABLE_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tRoutingTable");
                    break;
                case GeneralObject.NEIGHBOR_TABLE_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tNeighborTable");
                    break;
                case GeneralObject.P_LOOKUP_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tPLookup");
                    break;
                case GeneralObject.RESOURCE_ID_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tResourceID");
                    byte[] resourceID = ByteUtils.subarray(data, currentIndex, objectLength);
                    currentIndex += objectLength;
                    object = new ResourceID(resourceID);
                    remainingBytes -= objectLength;
                    break;
                case GeneralObject.R_LOOKUP_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tRLookup");

                    byte contType = data[currentIndex++];
                    byte subType = data[currentIndex++];

                    object = new RLookup(contType, subType, null, null);
                    //remainingBytes = objectLength - 2;
                    remainingBytes = remainingBytes - 2;
                    objectLength = objectLength - 2;
                    break;
                case GeneralObject.RESOURCE_OBJECT_OBJECT_TYPE:
                    byte contentType = data[currentIndex++];
                    byte contentSubType = data[currentIndex++];
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tResourceObject with contentType=" + contentType + " contentSubtype=" + contentSubType);


                    object = ResourceObject.create(contentType, contentSubType);
                    //remainingBytes = objectLength - 2;
                    remainingBytes = remainingBytes - 2;
                    objectLength = objectLength - 2;
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tResourceObject with remainingBytes=" + remainingBytes);
                    break;
                case GeneralObject.EXPIRES_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tExpires");
                    byte[] expiresData = ByteUtils.subarray(data, currentIndex, 4);
                    int seconds = (int) ByteUtils.bytesToLong(expiresData[0], expiresData[1], expiresData[2],
                            expiresData[3]);
                    object = new Expires(seconds);
                    currentIndex += 4;
                    remainingBytes -= 4;
                    break;
                case GeneralObject.OWNER_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tOwner");
                    byte[] unhashedIDValue = ByteUtils.subarray(data, currentIndex, objectLength);
                    object = new Owner(unhashedIDValue);
                    currentIndex += objectLength;
                    remainingBytes -= objectLength;
                    break;
                case GeneralObject.CERTIFICATE_SIGN_REQUEST_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tCertificateSignRequest");
                    break;
                case GeneralObject.X509_CERTIFICATE_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tX509Certificate");
                    byte[] certificateValue = ByteUtils.subarray(data, currentIndex, objectLength);
                    object = new Certificate(false, certificateValue);
                    currentIndex += objectLength;
                    remainingBytes -= objectLength;
                    break;
                case GeneralObject.X509_CER7_SIGNATURE_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tX509CertSignature");
                    byte[] signatureValue = ByteUtils.subarray(data, currentIndex, objectLength);
                    object = new Signature(signatureValue);
                    currentIndex += objectLength;
                    remainingBytes -= objectLength;
                    break;
                case GeneralObject.TIME_WINDOW_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tTimeWindow");
                    break;
                case GeneralObject.CONNECTIONS_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tConnections");
                    break;
                case GeneralObject.NODE_RESOURCE_UTILIZATION_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tNodeResourceUtilization");
                    break;
                case GeneralObject.MESSAGES_RECEIVED_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tMessagesReceived");
                    break;
                case GeneralObject.AS_NUMBER_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tAsNumber");
                    break;
                case GeneralObject.ERROR_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tError");
                    break;
                case GeneralObject.RESOURCE_OBJECT_VALUE_OBJECT_TYPE:
                    // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                    // "\tResourceObjectValue");
                    byte[] value = ByteUtils.subarray(data, currentIndex, objectLength);
                    object = new ResourceObjectValue(value);
                    remainingBytes -= objectLength;
                    currentIndex += objectLength;
                    break;
                case GeneralObject.SOCIAL_PATH_OBJECT_TYPE:
                    object = new SocialPath();
                    //	currentIndex += objectLength;
                    //	remainingBytes = objectLength;
                    break;
                case GeneralObject.PATH_ID_OBJECT_TYPE:

                    byte[] pathID = ByteUtils.subarray(data, currentIndex, objectLength);
                    object = new PathID(pathID);
                    currentIndex += objectLength;

                    remainingBytes -= objectLength;
                    break;
                default:
                    throw new UnsupportedGeneralObjectException("UNKNOWN GeneralObject=" + objectType + " on index " + currentIndex);
            }

            // sets AB and object length in object
            object.setAb(ByteUtils.getBitsFromByteArray(new byte[]{abReserved}, 0, 2));
            object.setLength((short) objectLength);

            if (outerObject != null) {
                // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                // "\tAdds object of type=" + object.getType() + " to object of type=" + outerObject.getType()
                // + "; remainingBytes=" + remainingBytes);
                outerObject.addSubobject(object);
            } else if (message != null) {
                // logger.debug("P2PPMessageFactory.parseGeneralObject()" +
                //  "\tAdds object of type=" + object.getType() + " to message of type="
                //  + message.getRequestOrResponseType() + "; remainingBytes=" + remainingBytes);
                message.addObject(object);
            }

            /*
                * //Logger.log(LoggerTypeInfo.TYPES_ALL, LoggerLevelInfo.LEVELS_ALL,
                * "P2PPMessageFactory.parseGeneralObject()", tabs + "after before indexBeforeObjectRead=" +
                * indexBeforeObjectRead + "; before objectLength=" + objectLength + "; before currentIndex=" + currentIndex
                * + "before remainingBytes=" + remainingBytes);
                */
            int objectBytesRead = currentIndex - indexBeforeObjectRead;
            // Checks whether the outer object (or message) is entirely filled with subobjects.
            if (objectLength + 4 > objectBytesRead) {

                // replaced remainningBytes to objectLength
                int innerObjectLength = parseGeneralObject(data, currentIndex, objectLength, object, null);
                // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "P2PPMessageFactory.parseGeneralObject()",
                // "\tafter innerObjectLength=" + innerObjectLength);
                currentIndex += innerObjectLength;

                // CHANGED : currentIndex - objectBytesRead;
                remainingBytes -= innerObjectLength;
            }
            /*
                * //Logger.log(LoggerTypeInfo.TYPES_ALL, LoggerLevelInfo.LEVELS_ALL,
                * "P2PPMessageFactory.parseGeneralObject()", tabs + "after indexBeforeObjectRead=" + indexBeforeObjectRead
                * + "; before objectLength=" + objectLength + "; before currentIndex=" + currentIndex +
                * "before remainingBytes=" + remainingBytes);
                */
        }

        return currentIndex - indexBeforeOuterObjectFilled;
    }

    /**
     * Determines sourceID from given set of data.
     *
     * @param data           Array having at least common header.
     * @param sourceIDLength
     * @return
     */
    private static byte[] determineSourceID(byte[] data, byte sourceIDLength) {
        return ByteUtils.subarray(data, 20, sourceIDLength);
    }

    /**
     * Determines resourceID from given set of data.
     *
     * @param data           Array having at least common header.
     * @param sourceIDLength
     * @return
     */
    private static byte[] determineResponseID(byte[] data, byte sourceIDLength) {
        return ByteUtils.subarray(data, 20 + sourceIDLength, sourceIDLength);
    }

    /**
     * @param message
     * @param protocol
     * @param messageType
     * @param isAcknowledgement
     * @param isSentByPeer
     * @param isRecursive
     * @param reservedOrResponseCode
     * @param requestType
     * @param ttl
     * @param transactionID
     * @param messageLength
     * @param sourceID
     * @param responseID             May be null if message is request or indication.
     */
    private static void setCommonHeaderData(P2PPMessage message, boolean[] protocol, boolean[] messageType,
                                            boolean isAcknowledgement, boolean isSentByPeer, boolean isRecursive, boolean[] reservedOrResponseCode,
                                            byte requestType, byte ttl, int senderPort, byte[] transactionID, byte[] messageLength, byte[] sourceID, 
                                            byte[] responseID) {

        message.setProtocolVersion(protocol);
        message.setMessageType(messageType);
        message.setAcknowledgment(isAcknowledgement);
        message.setByPeer(isSentByPeer);
        message.setRecursive(isRecursive);
        message.setReservedOrResponseCode(reservedOrResponseCode);
        message.setRequestOrResponseType(requestType);
        message.setTtl(ttl);
        message.setSenderPort(senderPort);
        message.setTransactionID(transactionID);
        message.setMessageLength(messageLength);
        message.setSourceID(sourceID);
        message.setResponseID(responseID);

    }

    /**
     * Determines length of message's body (after sourceID in header) whose common header (or the whole message) is
     * given as an argument.
     *
     * @param data Common header or the whole message.
     * @return Length of the message
     */
    private static byte[] determineMessageLength(byte[] data) {
        return ByteUtils.subarray(data, 16, 4);
    }

    /**
     * Returns transaction ID from given common header.
     *
     * @param data
     * @return
     */
    private static byte[] determineTransactionID(byte[] data) {
        return ByteUtils.subarray(data, 12, 4);
    }

    /**
     * Returns magic cookie included in message (or common header) given as an argument.
     *
     * @param data Common header or the whole message.
     * @return Magic cookie field
     */
    private static int determineMagicCookie(byte[] data) {
        return (int) ByteUtils.bytesToLong(data[4], data[5], data[6], data[7]);
    }

    private static int determineSenderPort(byte[] data) {
        return (int) ByteUtils.bytesToLong(data[8], data[9], data[10], data[11]);
    }

    private static byte determineTTL(byte[] data) {
        return data[3];
    }

    private static boolean[] determineResponseCode(byte[] data) {
        return ByteUtils.getBitsFromByteArray(data, 7, 9);
    }

    private static boolean determineIsRecursive(byte[] data) {
        return ByteUtils.getBitsFromByteArray(data, 6, 1)[0];
    }

    private static boolean determineIsSentByPeer(byte[] data) {
        return ByteUtils.getBitsFromByteArray(data, 5, 1)[0];
    }

    /**
     * Returns value of acknowledgement field in given common header of the message.
     *
     * @param data Common header or the whole message.
     * @return
     */
    private static boolean determineAcknowledgement(byte[] data) {
        return ByteUtils.getBitsFromByteArray(data, 4, 1)[0];
    }

    private static boolean[] determineProtocol(byte[] data) {
        return ByteUtils.getBitsFromByteArray(data, 0, 2);
    }

    /**
     * Determines message type by a message header. Argument may be a whole message or just a common header. Message
     * types are request, response, responseACK and indication. They're defined in Message class.
     *
     * @param array Whole message or just a common header.
     * @return
     * @see P2PPMessage
     */
    private static boolean[] determineMessageType(byte[] array) {
        return ByteUtils.getBitsFromByteArray(array, 2, 2);

    }

    /**
     * Returns a number that represents request or indication type.
     *
     * @param array Array having at least a common header.
     * @return request type
     */
    private static byte determineRequestType(byte[] array) {
        return array[2];
	}

}
