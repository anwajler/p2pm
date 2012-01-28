package pl.edu.pjwstk.mteam.pubsub.util.persistence.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.h2.tools.RunScript;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;

/**
 *
 * @author Piotr Bucior
 * @version 1.0
 */
public class DBConnection {

    public static Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.util.persistent.db.DBConnection");
    private final String DB_URL = "jdbc:h2:file:cache/pubsub;FILE_LOCK=NO;SCHEMA=PUBLIC;TRACE_LEVEL_FILE=3";
    private final String DB_SCHEME = "/pl/edu/pjwstk/mteam/pubsub/util/persistence/db/sql/create_topiccache.sql";
    private Connection connection;
    private static DBConnection dbConnection;
    private static final String SQL_ADD_NEW_TOPIC = "insert into topic values(DEFAULT,?,?,?)";
    private PreparedStatement PS_ADD_NEW_TOPIC;
    private static final String SQL_INSERT_NOTIFY_INDICATION = "INSERT INTO NOTIFY_INDICATIONS VALUES(?,?,?,?,?,?,?,?)";
    private PreparedStatement PS_INSERT_NOTIFY_INDICATION;
    private static final String SQL_UPDATE_TOPIC_LATEST_OPERATION_ID = "UPDATE TOPIC SET LATEST_OPERATION_ID = ? WHERE TOPIC_ID = ?";
    private PreparedStatement PS_UPDATE_TOPIC_LATEST_OPERATION_ID;
    private static final String SQL_GET_TOPIC = "SELECT * FROM TOPIC WHERE TOPIC_ID = ?";
    private PreparedStatement PS_GET_TOPIC;
    private static final String SQL_GET_PUBLISH_NOTIFY_INDICATIONS = "SELECT * FROM NOTIFY_INDICATIONS WHERE OPERATION_ID > ? ORDER BY OPERATION_ID";
    private PreparedStatement PS_GET_PUBLISH_NOTIFY_INDICATIONS;
    private PreparedStatement PS_CLEAR_TOPIC_CACHE;
    private static final String SQL_CLEAR_TOPIC_CACHE = "DELETE FROM NOTIFY_INDICATIONS WHERE T_ID = ?";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            log.fatal("H2 database driver not found. Check if you have added h2.jar to your classpath.", e);
            System.exit(1);
        }
    }
    private final Properties p;

    private DBConnection() {
        p = new Properties();
        p.put("user", "user");
        p.put("password", "pass");
    }

    public static synchronized DBConnection getConnection() {
        if (dbConnection == null) {
            dbConnection = new DBConnection();
        }
        try {
            dbConnection.checkConnection();
        } catch (SQLException e) {
            log.fatal("An error while getting connection to topics database!", e);
        }
        return dbConnection;
    }

    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            log.info("Using persistent topic's cache storage: " + DB_URL);
            try {
                connection = DriverManager.getConnection(DB_URL + ";IFEXISTS=TRUE", p);
                log.info("Using previous persistent topic's cache storage");
            } catch (SQLException e) {
                log.info("Persistent topic's cache storage used for the first time, creating database ...");
                connection = DriverManager.getConnection(DB_URL, p);
                try {
                    InputStream is = getClass().getResourceAsStream(DB_SCHEME);
                    InputStreamReader isr = new InputStreamReader(is);
                    RunScript.execute(connection, new BufferedReader(isr));
                    log.info("New topic's cache storage created!");
                } catch (Exception e1) {
                    log.error("An error while creating new topic's cache storage", e1);
                }
            }
            if (PS_ADD_NEW_TOPIC == null) {
                PS_ADD_NEW_TOPIC = connection.prepareStatement(SQL_ADD_NEW_TOPIC);

                if (PS_INSERT_NOTIFY_INDICATION == null) {
                    PS_INSERT_NOTIFY_INDICATION = connection.prepareStatement(SQL_INSERT_NOTIFY_INDICATION);
                }
                if (PS_UPDATE_TOPIC_LATEST_OPERATION_ID == null) {
                    PS_UPDATE_TOPIC_LATEST_OPERATION_ID = connection.prepareStatement(SQL_UPDATE_TOPIC_LATEST_OPERATION_ID);
                }
                if (PS_GET_TOPIC == null) {
                    PS_GET_TOPIC = connection.prepareStatement(SQL_GET_TOPIC);
                }
            if(PS_GET_PUBLISH_NOTIFY_INDICATIONS==null){
                    PS_GET_PUBLISH_NOTIFY_INDICATIONS = connection.prepareStatement(SQL_GET_PUBLISH_NOTIFY_INDICATIONS);
                }
            if(PS_CLEAR_TOPIC_CACHE==null){
                    PS_CLEAR_TOPIC_CACHE = connection.prepareStatement(SQL_CLEAR_TOPIC_CACHE);
                }
            }
        }
        connection.commit();

    }

    synchronized void addTopic(String id, String owner_id, int latestOperationID) throws SQLException {
        checkConnection();
        PS_ADD_NEW_TOPIC.setString(1, id);
        PS_ADD_NEW_TOPIC.setString(3, owner_id);
        PS_ADD_NEW_TOPIC.setInt(2, latestOperationID);

        //try {
        PS_ADD_NEW_TOPIC.executeUpdate();
        // }
//        finally {
//            try {
//                PS_ADD_NEW_TOPIC.close();
//            } catch (Exception e) {
//            }
//        }
    }

    synchronized void insertPublishNotify(int operationID, String topicID, short eventType, boolean historical, String message, String publisher, byte[] details) throws SQLException {
        checkConnection();
        PS_INSERT_NOTIFY_INDICATION.setInt(1, operationID);
        PS_INSERT_NOTIFY_INDICATION.setString(2, topicID);
        PS_INSERT_NOTIFY_INDICATION.setInt(3, eventType);
        PS_INSERT_NOTIFY_INDICATION.setBoolean(4, historical);
        PS_INSERT_NOTIFY_INDICATION.setString(5, message);
        PS_INSERT_NOTIFY_INDICATION.setString(6, publisher);
        PS_INSERT_NOTIFY_INDICATION.setBytes(7, details);
        PS_INSERT_NOTIFY_INDICATION.setString(8, ""+operationID+""+topicID);
        PS_INSERT_NOTIFY_INDICATION.executeUpdate();
        increaseLatestOperationID(topicID, operationID);
    }

    private synchronized void increaseLatestOperationID(String topic_id, int opID) throws SQLException {
        checkConnection();
        PS_UPDATE_TOPIC_LATEST_OPERATION_ID.setInt(1, opID);
        PS_UPDATE_TOPIC_LATEST_OPERATION_ID.setString(2, topic_id);
        PS_UPDATE_TOPIC_LATEST_OPERATION_ID.executeUpdate();
    }

    synchronized Topic getTopic(String topicID) throws SQLException {
        checkConnection();
        ResultSet rs;
        try {
            Topic t = null;
            PS_GET_TOPIC.setString(1, topicID);
            rs = PS_GET_TOPIC.executeQuery();
            if (!rs.next()) {
                return null;
            }
            t = new Topic(topicID, rs.getInt(3));
            t.setOwner(new Subscriber(rs.getString(4), t));
            return t;
        } finally {
            rs = null;
        }
    }

    synchronized ResultSet getPublishNotifyIndication(int fromID) throws SQLException{
        checkConnection();
        PS_GET_PUBLISH_NOTIFY_INDICATIONS.setInt(1, fromID);
        return PS_GET_PUBLISH_NOTIFY_INDICATIONS.executeQuery();
    }

    void clearTopicState(String tID) throws SQLException {
        checkConnection();
        PS_CLEAR_TOPIC_CACHE.setString(1, tID);
        PS_CLEAR_TOPIC_CACHE.executeUpdate();
        increaseLatestOperationID(tID, 0);
    }
}
