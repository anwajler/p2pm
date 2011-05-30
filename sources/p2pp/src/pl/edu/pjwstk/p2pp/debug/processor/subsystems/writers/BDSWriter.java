package pl.edu.pjwstk.p2pp.debug.processor.subsystems.writers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.debug.DebugFields;
import pl.edu.pjwstk.p2pp.debug.DebugInformation;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystemFactoryFunctor;

public class BDSWriter extends SubSystem implements IWriter, SubSystemFactoryFunctor {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constants
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Instance of org.apache.log4j.Logger
     */
    private static final Logger LOG = Logger.getLogger(BDSWriter.class);

    private static final int TIMEOUT = 1000;

    private static final String INSERT_DEBUG_INFO_SQL = "INSERT INTO debug_info VALUES(?,?,?,?,?,?," +
            "INET_ATON(?),?,INET_ATON(?),?,?,?,?,?,?,?,?,?,?,?,?,?);";

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Data members
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Buffer where debug information to be written are stored. The size of the buffer is defined in
     * settings passed to the {@link #init(java.lang.Object[])} method.
     *
     * @see pl.edu.pjwstk.p2pp.debug.processor.DebugWriter#options
     */
    private LinkedBlockingQueue<DebugInformation> buffer;

    private BasicDataSource bds;

    private PreparedStatement stmt;

    private boolean running = true;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constructors
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Constructs a new RawTextWriter.
     */
    public BDSWriter() {
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Methods
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private boolean createStmt() {
        Connection conn = null;
        try {
            conn = this.bds.getConnection();
            this.stmt = conn.prepareStatement(INSERT_DEBUG_INFO_SQL);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Functions
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private static PreparedStatement setStmtParameter(PreparedStatement stmt, int stmtIndex, Byte debugFieldIndex, Object debugField) throws SQLException {
        String className = DebugFields.FIELD_TYPES.get(debugFieldIndex).getName();

        if ("java.lang.Boolean".equals(className)) {
            stmt.setBoolean(stmtIndex, (Boolean) debugField);
        } else if ("java.lang.Byte".equals(className)) {
            stmt.setByte(stmtIndex, (Byte) debugField);
        } else if ("java.lang.Integer".equals(className)) {
            stmt.setInt(stmtIndex, (Integer) debugField);
        } else if ("java.lang.String".equals(className)) {
            stmt.setString(stmtIndex, debugField.toString());
        } else if ("java.lang.Long".equals(className)) {
            stmt.setLong(stmtIndex, (Long) debugField);
        } else if ("java.sql.Timestamp".equals(className)) {
            stmt.setTimestamp(stmtIndex, (Timestamp) debugField);
        }

        return stmt;
    }

    private static String generateMessageHash(DebugInformation debugInfo) {
        String hash = "";
        byte[] bytes = debugInfo.toString().getBytes();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            byte messageDigest[] = algorithm.digest();
            StringBuffer hexString = new StringBuffer();
            for (byte messageByte : messageDigest) {
                hexString.append(Integer.toHexString(0xFF & messageByte));
            }
            hash = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    /**
     * @param stmt
     * @param debugInfo
     */
    private void writeDebugInformationActual(PreparedStatement stmt, DebugInformation debugInfo) {
        if (LOG.isDebugEnabled()) LOG.debug("Writing: " + debugInfo);
        PreparedStatement pstmt = stmt;
        try {
            pstmt.setString(1, generateMessageHash(debugInfo));
            pstmt = setStmtParameter(pstmt, 2, DebugFields.SENT_OR_RECEIVED, debugInfo.get(DebugFields.SENT_OR_RECEIVED));
            pstmt = setStmtParameter(pstmt, 3, DebugFields.TIMESTAMP, debugInfo.get(DebugFields.TIMESTAMP));
            pstmt = setStmtParameter(pstmt, 4, DebugFields.MESSAGE_CLASS, debugInfo.get(DebugFields.MESSAGE_CLASS));
            pstmt = setStmtParameter(pstmt, 5, DebugFields.OVER_RELIABLE, debugInfo.get(DebugFields.OVER_RELIABLE));
            pstmt = setStmtParameter(pstmt, 6, DebugFields.ENCRYPTED, debugInfo.get(DebugFields.ENCRYPTED));
            pstmt = setStmtParameter(pstmt, 7, DebugFields.SENDER_ADDRESS, debugInfo.get(DebugFields.SENDER_ADDRESS));
            pstmt = setStmtParameter(pstmt, 8, DebugFields.SENDER_PORT, debugInfo.get(DebugFields.SENDER_PORT));
            pstmt = setStmtParameter(pstmt, 9, DebugFields.RECEIVER_ADDRESS, debugInfo.get(DebugFields.RECEIVER_ADDRESS));
            pstmt = setStmtParameter(pstmt, 10, DebugFields.RECEIVER_PORT, debugInfo.get(DebugFields.RECEIVER_PORT));
            pstmt = setStmtParameter(pstmt, 11, DebugFields.PROTOCOL_NAME, debugInfo.get(DebugFields.PROTOCOL_NAME));
            pstmt = setStmtParameter(pstmt, 12, DebugFields.PROTOCOL_VERSION, debugInfo.get(DebugFields.PROTOCOL_VERSION));
            pstmt = setStmtParameter(pstmt, 13, DebugFields.MESSAGE_TYPE, debugInfo.get(DebugFields.MESSAGE_TYPE));
            pstmt = setStmtParameter(pstmt, 14, DebugFields.ACK_BYPEER_RECURSIVE, debugInfo.get(DebugFields.ACK_BYPEER_RECURSIVE));
            pstmt = setStmtParameter(pstmt, 15, DebugFields.RESERVED_RESPONSE_CODE, debugInfo.get(DebugFields.RESERVED_RESPONSE_CODE));
            pstmt = setStmtParameter(pstmt, 16, DebugFields.REQUEST_RESPONSE_TYPE, debugInfo.get(DebugFields.REQUEST_RESPONSE_TYPE));
            pstmt = setStmtParameter(pstmt, 17, DebugFields.TTL, debugInfo.get(DebugFields.TTL));
            pstmt = setStmtParameter(pstmt, 18, DebugFields.TRANSACTION_ID, debugInfo.get(DebugFields.TRANSACTION_ID));
            pstmt = setStmtParameter(pstmt, 19, DebugFields.MESSAGE_LENGTH, debugInfo.get(DebugFields.MESSAGE_LENGTH));
            pstmt = setStmtParameter(pstmt, 20, DebugFields.SOURCE_ID, debugInfo.get(DebugFields.SOURCE_ID));
            pstmt = setStmtParameter(pstmt, 21, DebugFields.RESPONSE_ID, debugInfo.get(DebugFields.RESPONSE_ID));
            pstmt = setStmtParameter(pstmt, 22, DebugFields.UNHASHED_ID, debugInfo.get(DebugFields.UNHASHED_ID));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Error while writing " + debugInfo, e);
        }
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Interface
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public void init(Object[] settings) {
        if (null == settings) {
            throw new IllegalArgumentException("Argument passed to BDSWriter cannot be null");
        }
        if (settings.length != 4) {
            throw new IllegalArgumentException("Argument passed to BDSWriter #init() must be a Object" +
                    " array consisting of a username, password url and a buffer size");
        }
        for (int i = 0; i < settings.length - 1; i++) {
            if (null != settings[i] && "".equals(settings[i])) {
                throw new IllegalArgumentException(i + " field in an argument passed to "
                        + this.getClass().getSimpleName() + "#init() cannot be null nor empty");
            }
        }
        if (null == settings[3]) {
            throw new IllegalArgumentException("Fourth field in an argument passed to "
                    + this.getClass().getSimpleName() + " cannot be null");
        }

        this.bds = new BasicDataSource();
        this.bds.setDriverClassName("com.mysql.jdbc.Driver");
        this.bds.setUsername(settings[0].toString());
        this.bds.setPassword(settings[1].toString());
        this.bds.setUrl(settings[2].toString());
        this.buffer = new LinkedBlockingQueue<DebugInformation>((Integer) settings[3]);

        if (LOG.isDebugEnabled()) LOG.debug("BDSWriter initialized");
    }

    public void halt() {
        this.running = false;
    }

    public boolean hasRequests() {
        Object tmp = null;
        try {
            tmp = this.buffer.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("This should not happen", e);
        }
        return tmp != null;
    }

    public void writeDebugInformation(DebugInformation debugInfo) {
        if (!this.isAlive()) {
            LOG.warn("Trying to make stopped BDSWriter write. Won't happen.");
            return;
        }
        try {
            this.buffer.offer(debugInfo, 200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Buffer in BDSWriter is full. Failed to write debug information.");
        }
    }


    public SubSystem makeInstance() {
        if (!(_instance instanceof BDSWriter)) {
            _instance = new BDSWriter();
        }
        return _instance;
    }

    @Override
    public void run() {
        if (LOG.isDebugEnabled()) LOG.debug(this.getClass().getSimpleName() + " started");

        if (this.buffer == null) {
            LOG.error("Trying to run an uninitialized BDSWriter");
            return;
        }

        if (!createStmt()) {
            LOG.error("BDSWriter could not connect to the given " + this.bds.getUrl() + "resource. Stopping writer.");
            return;
        }

        while (running) {
            /*
             * Synchronizing in order to obtain a bulk of data  representing whole buffer instead of
             * polling (taking) one record every loop run. The idea is to reduce output operations.
             */
            synchronized (this.buffer) {

                Object[] bufferArray = this.buffer.toArray();
                for (Object debugInfo : bufferArray) {
                    try {
                        writeDebugInformationActual(this.stmt, (DebugInformation) debugInfo);
                    } catch (Throwable e) {
                        LOG.error("BDSWriter could not write to the given " +
                                this.bds.getUrl() + " resource. Stopping writer.");
                        return;
                    }
                }
                this.buffer.clear();
            }

            synchronized (this) {
                try {
                    sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    LOG.error("This should not happen", e);
                }
            }
        }
    }

}
