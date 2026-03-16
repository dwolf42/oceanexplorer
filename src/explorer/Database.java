package explorer;

import ocean.RadarEcho;
import ocean.Vec2D;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private Connection conn;
    private Statement statement;
    private PreparedStatement stmt, stmt2;

    // Database connection parameters
    // !!!
    // TODO: DB_URL and PASS should be adjusted depending on the real credentials for the database
    // !!!
    static final String DB_URL = "jdbc:mariadb://localhost:3306/ocean_explorer";
    static final String USER = "root";
    static final String PASS = "password";

    public Database() throws SQLException {
        this.conn = this.getConnection();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public synchronized void insertSector(int x, int y) throws SQLException {
        // position_x and position_y are set as UNIQUE, so if they already exist in the database the insert will be ignored (via INSERT IGNORE)
        String sql = "INSERT IGNORE INTO sector (position_x, position_y) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, x);
        stmt.setInt(2, y);
        int affectedRows = stmt.executeUpdate();

        if (affectedRows == 0) {
            System.out.println("Sector already exists in the database, insert skipped");
        }
    }

    public synchronized int insertShipData(String shipIdentifierFromServer, String shipName) throws SQLException {
        String sql = "INSERT INTO ship (name, active, server_ship_id) VALUES (?, 'Yes', ?)";

        stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, shipName);
        stmt.setString(2, shipIdentifierFromServer);
        stmt.executeUpdate();

        int shipDatabaseIdentifier = 0;
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                shipDatabaseIdentifier = rs.getInt(1);
            }
        }

        stmt.close();
        return shipDatabaseIdentifier;
    }

    public synchronized void insertShipScanData(int totalDepthAverage,
                                                float standardDeviation,
                                                Vec2D sectorCoordinates,
                                                int shipDatabaseIdentifier) throws SQLException {

        String sql = "INSERT INTO scan_results (total_depth_average, standard_deviation) " +
                "VALUES (?, ?)";

        stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, totalDepthAverage);
        stmt.setFloat(2, standardDeviation);
        stmt.executeUpdate();

        int scanResultID = getScanResultID(stmt.getGeneratedKeys());
        int sectorID = getSectorID(sectorCoordinates);

        String sql2 = "INSERT INTO ship_scan_results (scan_resultID, sectorID, shipID) " +
                "VALUES (?, ?, ?)";

        stmt = conn.prepareStatement(sql2);

        stmt.setInt(1, scanResultID);
        stmt.setInt(2, sectorID);
        stmt.setInt(3, shipDatabaseIdentifier);
        stmt.executeUpdate();

        stmt.close();
    }

    public synchronized void insertShipRadarData(int shipDatabaseIdentifier, List<RadarEcho> echos) throws SQLException {
        String sql = "INSERT IGNORE INTO radar_results (ground, navigable, shipID, sectorID, height) " +
                "VALUES (?, ?, ?, ?, ?)";
        stmt = conn.prepareStatement(sql);

        for (RadarEcho echo : echos) {
            String ground = echo.getGround().toString();

            if(!ground.equals("None")) {
                String navigable = "Yes";

                int height = echo.getHeight();
                if(height > 0) {
                    navigable = "No";
                }

                Vec2D echoSectorVec = echo.getSector();
                int sectorID = getSectorID(echoSectorVec);

                // If the sector does not exist in the sectors table (getSectorID returns 0), we insert it first
                // Then we insert the radar result referencing this new inserted sector
                if(sectorID == 0) {
                    insertSector(echoSectorVec.getX(), echoSectorVec.getY());
                    sectorID = getSectorID(echoSectorVec);
                }

                stmt.setString(1, ground);
                stmt.setString(2, navigable);
                stmt.setInt(3, shipDatabaseIdentifier);
                stmt.setInt(4, sectorID);
                stmt.setInt(5, height);

                stmt.addBatch();
            }
        }

        stmt.executeBatch();
        stmt.close();
    }

    public synchronized int insertSubmarineData(int shipDatabaseIdentifier, String serverSubID) throws SQLException {
        String sql = "INSERT INTO submarine (shipID, server_sub_id) VALUES (?,?)";
        stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, shipDatabaseIdentifier);
        stmt.setString(2, serverSubID);

        int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            System.out.println("Submarine already exists in the database, insert skipped");
        }

        int lastGeneratedKey = getGeneratedKey(stmt);
        stmt.close();

        return lastGeneratedKey;
    }

    public synchronized void insertSubArisePosition (
            int x,
            int y,
            int subIdentifier
    ) throws SQLException
    {
        String sql = "INSERT INTO submarine_arise_position (position_x, position_y) VALUES (?, ?)";
        stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, x);
        stmt.setInt(2, y);
        stmt.executeUpdate();

        int lastGeneratedKey = getGeneratedKey(stmt);

        stmt.close();

        String sql2 = "UPDATE submarine SET arise_positionID = ?, surfaced = ? WHERE submarineID = ?";

        stmt2 = conn.prepareStatement(sql2);
        stmt2.setInt(1, lastGeneratedKey);
        stmt2.setString(2, "No");
        stmt2.setInt(3, subIdentifier);
        stmt2.executeUpdate();

        stmt2.close();
    }

    public synchronized void insertSubSunkPosition (
            int x,
            int y,
            int z,
            int subIdentifier
    ) throws SQLException {
        String sql = "INSERT INTO submarine_sink_position (position_x, position_y, position_z) VALUES (?, ?, ?)";
        stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, x);
        stmt.setInt(2, y);
        stmt.setInt(3, z);
        stmt.executeUpdate();

        int lastGeneratedKey = getGeneratedKey(stmt);

        stmt.close();

        String sql2 = "UPDATE submarine SET sink_positionID = ?, surfaced = ?, sunk = ? WHERE submarineID = ?";

        stmt2 = conn.prepareStatement(sql2);
        stmt2.setInt(1, lastGeneratedKey);
        stmt2.setString(2, "No");
        stmt2.setString(3, "Yes");
        stmt2.setInt(4, subIdentifier);
        stmt2.executeUpdate();

        stmt2.close();
    }

    public void insertSubMeasurements(int subID, int sectorID, int x, int y, int z) throws SQLException {
        String sql = "INSERT INTO submarine_measurements" +
                "(submarineID, sectorID, vec_x, vec_y, vec_z) " +
                "VALUES (?, ?, ?, ?, ?)";

        stmt = conn.prepareStatement(sql);

        stmt.setInt(1, subID);
        stmt.setInt(2, sectorID);
        stmt.setInt(3, x);
        stmt.setInt(4, y);
        stmt.setInt(5, z);

        stmt.executeUpdate();
        stmt.close();
    }

    public synchronized List<Map<String, Object>> getAllShips() throws SQLException {
        List<Map<String, Object>> ships = new ArrayList<>();
        String sql = "SELECT * FROM ship";

        conn = getConnection();
        statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            Map<String, Object> ship = new HashMap<>();
            ship.put("shipID", rs.getInt("shipID"));
            ship.put("name", rs.getString("name"));
            ship.put("active", rs.getString("active"));
            ship.put("server_ship_id", rs.getString("server_ship_id"));
            ship.put("crash_position_x", rs.getString("crash_position_x"));
            ship.put("crash_position_y", rs.getString("crash_position_y"));
            ship.put("crash_position_z", rs.getString("crash_position_z"));
            ships.add(ship);
        }

        conn.close();
        statement.close();

        return ships;
    }

    public synchronized List<Map<String, Object>> getAllSectors() throws SQLException {
        List<Map<String, Object>> sectors = new ArrayList<>();
        String sql = "SELECT * FROM sector ORDER BY sectorID";

        conn = getConnection();
        statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            Map<String, Object> sector = new HashMap<>();
            sector.put("sectorID", rs.getInt("sectorID"));
            sector.put("position_x", rs.getInt("position_x"));
            sector.put("position_y", rs.getInt("position_y"));
            sectors.add(sector);
        }

//        conn.close();
//        statement.close();
        return sectors;
    }

    public synchronized List<Map<String, Object>> getShipScanData(int identifier) throws SQLException {
        List<Map<String, Object>> shipScanData = new ArrayList<>();
        String sql = "SELECT * FROM ship_scan_results WHERE shipID = ? ";

        conn = getConnection();
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, identifier);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Map<String, Object> shipData = new HashMap<>();
            int scan_resultID = rs.getInt("scan_resultID");
            int sectorID = rs.getInt("sectorID");
            shipData.put("sectorID", sectorID);

            String sql2 = "SELECT total_depth_average, standard_deviation FROM scan_results WHERE scan_resultID = ?";
            stmt = conn.prepareStatement(sql2);
            stmt.setInt(1, scan_resultID);
            ResultSet rs2 = stmt.executeQuery();

            while (rs2.next()) {
                shipData.put("total_depth_average", rs2.getInt("total_depth_average"));
                shipData.put("standard_deviation", rs2.getDouble("standard_deviation"));
            }

            shipScanData.add(shipData);
        }

        conn.close();
        stmt.close();
        return shipScanData;
    }

    public synchronized List<Map<String, Object>> getAllSectorData() throws SQLException {
        List<Map<String, Object>> sectors = getAllSectors();
        List<Map<String, Object>> allSectorData = new ArrayList<>();

        for (Map<String, Object> sector : sectors) {
            int x = (int) sector.get("position_x");
            int y = (int) sector.get("position_y");

            Vec2D sectorVec = new Vec2D(x,y);
            int sectorID = getSectorID(sectorVec);

            Map<String, Object> scanData = getScanResultsForSector(sectorID);
            sector.putAll(scanData);

            Map<String, Object> radarData = getRadarResultsForSector(sectorID);
            sector.putAll(radarData);

            allSectorData.add(sector);
        }
        return allSectorData;
    }

    private int getGeneratedKey(PreparedStatement stmt) {
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private synchronized Map<String, Object> getScanResultsForSector(int sectorID) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        String sql = """
        SELECT sr.total_depth_average, sr.standard_deviation
        FROM ship_scan_results ssr
        JOIN scan_results sr ON ssr.scan_resultID = sr.scan_resultID
        WHERE ssr.sectorID = ?
        LIMIT 1
    """;

        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, sectorID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            result.put("total_depth_average", rs.getInt("total_depth_average"));
            result.put("standard_deviation", rs.getDouble("standard_deviation"));
        }

        rs.close();
        stmt.close();
        conn.close();

        return result;
    }

    private synchronized Map<String, Object> getRadarResultsForSector(int sectorID) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        String sql = "SELECT height, ground, navigable FROM radar_results WHERE sectorID = ? ORDER BY sectorID";

        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, sectorID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            result.put("height", rs.getInt("height"));
            result.put("ground", rs.getString("ground"));
            result.put("navigable", rs.getString("navigable"));
        }

        rs.close();
        stmt.close();
        conn.close();

        return result;
    }

    public synchronized List<Map<String, Object>> getSubmarineMeasurementsForSector(int sectorID) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT submarineID, vec_x, vec_y, vec_z FROM submarine_measurements WHERE sectorID = ?";

        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, sectorID);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            row.put("submarineID", rs.getInt("submarineID"));
            row.put("vec_x", rs.getInt("vec_x"));
            row.put("vec_y", rs.getInt("vec_y"));
            row.put("vec_z", rs.getInt("vec_z"));
            result.add(row);
        }

        rs.close();
        stmt.close();
        conn.close();

        return result;
    }

    public synchronized List<Map<String, Object>> getShipRadarData(int shipID) throws SQLException {
        List<Map<String, Object>> shipRadarData = new ArrayList<>();
        String sql = "SELECT * FROM radar_results WHERE shipID = ?";

        conn = getConnection();
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, shipID);
        ResultSet rs = stmt.executeQuery();

        String shipName = getShipName(shipID);

        while (rs.next()) {
            Map<String, Object> shipData = new HashMap<>();
            int sectorID = rs.getInt("sectorID");

            Vec2D sectorCoordinates = getSectorCoordinates(sectorID);

            int sectorPosX = sectorCoordinates.getX();
            int sectorPosY = sectorCoordinates.getY();

            shipData.put("shipName", shipName);
            shipData.put("ground", rs.getString("ground"));
            shipData.put("height", rs.getInt("height"));
            shipData.put("navigable", rs.getString("navigable"));
            shipData.put("sectorPosX", sectorPosX);
            shipData.put("sectorPosY", sectorPosY);

            shipRadarData.add(shipData);
        }
        conn.close();
        stmt.close();
        return shipRadarData;
    }

    public synchronized List<Map<String, Object>> getAllSubmarineData() throws SQLException {
        List<Map<String, Object>> submarines = new ArrayList<>();
        String sql = "SELECT " +
                "s.submarineID, " +
                "ship.`name` AS ship_name," +
                "ssp.position_x AS sink_position_x," +
                "ssp.position_y AS sink_position_y," +
                "ssp.position_z AS sink_position_z," +
                "sap.position_x AS arise_position_x," +
                "sap.position_y AS arise_position_y," +
                "s.`surfaced`," +
                "s.sunk " +
                "FROM submarine s " +
                "INNER JOIN `ship` ON s.shipID = ship.shipID " +
                "LEFT JOIN `submarine_sink_position` ssp ON s.sink_positionID = ssp.sink_positionID " +
                "LEFT JOIN `submarine_arise_position` sap ON s.arise_positionID = sap.arise_positionID";

        conn = getConnection();
        statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            Map<String, Object> submarine = new HashMap<>();
            submarine.put("submarineID", rs.getInt("submarineID"));
            submarine.put("shipName", rs.getString("ship_name"));
            submarine.put("sinkPositionX", rs.getInt("sink_position_x"));
            submarine.put("sinkPositionY", rs.getInt("sink_position_y"));
            submarine.put("sinkPositionZ", rs.getInt("sink_position_z"));
            submarine.put("arisePositionX", rs.getInt("arise_position_x"));
            submarine.put("arisePositionY", rs.getInt("arise_position_y"));
            submarine.put("surfaced", rs.getString("surfaced"));
            submarine.put("sunk", rs.getString("sunk"));
            submarines.add(submarine);
        }

        conn.close();
        statement.close();
        return submarines;
    }

    private synchronized String getShipName(int shipID) throws SQLException {
        String sql = "SELECT name FROM ship WHERE shipID = ?";

        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, shipID);
        ResultSet rs = stmt.executeQuery();

        String shipName = null;
        while (rs.next()) {
            shipName = rs.getString("name");
        }

        stmt.close();
        return shipName;
    }

    synchronized int getSectorID(Vec2D sector) throws SQLException {
        int positionX = sector.getX();
        int positionY = sector.getY();

        String sql = "SELECT sectorID FROM sector WHERE position_x = ? AND position_y = ?";

        stmt2 = conn.prepareStatement(sql);
        stmt2.setInt(1, positionX);
        stmt2.setInt(2, positionY);
        ResultSet rs = stmt2.executeQuery();

        int sectorID = 0;

        while (rs.next()) {
            sectorID = rs.getInt("sectorID");
        }

        stmt2.close();
        return sectorID;
    }

    private synchronized int getScanResultID(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    private Vec2D getSectorCoordinates(int sectorID) throws SQLException {
        String sql = "SELECT position_x, position_y FROM sector WHERE sectorID = ?";

        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, sectorID);
        ResultSet rs = stmt.executeQuery();

        int vecX = 0;
        int vecY = 0;
        while (rs.next()) {
            vecX = rs.getInt("position_x");
            vecY = rs.getInt("position_y");
        }

        Vec2D sectorCoordinates = new Vec2D(vecX, vecY);

        stmt.close();
        return sectorCoordinates;
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
               conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertShipCrashData(int shipID, int crashPosX, int crashPosY, int crashPosZ) throws SQLException {
        String sql = "UPDATE ship " +
                "SET crash_position_x = ?, crash_position_y = ?, crash_position_z = ? " +
                "WHERE shipID = ?";

        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, crashPosX);
        stmt.setInt(2, crashPosY);
        stmt.setInt(3, crashPosZ);
        stmt.setInt(4, shipID);
        stmt.executeUpdate();

        stmt.close();
    }

    public void updateShipState(int shipID) throws SQLException {
        String sql = "UPDATE ship SET active = ? WHERE shipID = ?";

        stmt = conn.prepareStatement(sql);
        stmt.setString(1, "No");
        stmt.setInt(2, shipID);
        stmt.executeUpdate();

        stmt.close();
    }
}