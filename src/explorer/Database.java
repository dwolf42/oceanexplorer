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
    static final String DB_URL = "jdbc:mariadb://localhost:3307/ocean_explorer";
    static final String USER = "root";
    static final String PASS = "OvoJeSamoPswZaMariaDB5.(0)!";

    public Database() throws SQLException {
        this.conn = this.getConnection();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public synchronized void insertSector(int x, int y) throws SQLException {
        String sql = "INSERT IGNORE INTO sector (position_x, position_y) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, x);
        stmt.setInt(2, y);
        stmt.executeUpdate();
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
        stmt2 = conn.prepareStatement(sql);

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

                stmt2.setString(1, ground);
                stmt2.setString(2, navigable);
                stmt2.setInt(3, shipDatabaseIdentifier);
                stmt2.setInt(4, sectorID);
                stmt2.setInt(5, height);

                stmt2.addBatch();
            }

        }

        stmt2.executeBatch();
        stmt2.close();
    }

    public synchronized void insertSubmarineData(int shipDatabaseIdentifier) throws SQLException {
        String sql = "INSERT INTO submarine (shipID) VALUES (?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, shipDatabaseIdentifier);
        stmt.executeUpdate();
    }

    public synchronized void insertSubArisePosition(int x, int y) throws SQLException {
        String sql = "INSERT INTO submarine_arise_position (position_x, position_y) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, x);
        stmt.setInt(2, y);
        stmt.executeUpdate();
    }

    public synchronized void insertSubSunkPosition(int x, int y, int z) throws SQLException {
        String sql = "INSERT INTO submarine_sink_position (position_x, position_y, position_z) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, x);
        stmt.setInt(2, y);
        stmt.setInt(3, z);
        stmt.executeUpdate();
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
            ships.add(ship);
        }

        conn.close();
        statement.close();

        return ships;
    }

    public synchronized List<Map<String, Object>> getAllSectors() throws SQLException {
        List<Map<String, Object>> sectors = new ArrayList<>();
        String sql = "SELECT * FROM sector";

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

        conn.close();
        statement.close();
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
            int sectorID = (Integer) sector.get("sectorID");

            Map<String, Object> scanData = getScanResultsForSector(sectorID);
            sector.putAll(scanData);

            Map<String, Object> radarData = getRadarResultsForSector(sectorID);
            sector.putAll(radarData);

            allSectorData.add(sector);
        }
        return allSectorData;
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
        String sql = "SELECT s.submarineID, ship.`name` AS ship_name," +
                "ssp.position_x AS sink_position_x, ssp.position_y AS sink_position_y, ssp.position_z AS sink_position_z," +
                "sap.position_x AS arise_position_x, sap.position_y AS arise_position_y," +
                "s.`active`, s.sunk " +
                "FROM submarine s " +
                "INNER JOIN `ship` ON s.shipID = ship.shipID " +
                "INNER JOIN `submarine_sink_position` ssp ON s.sink_positionID = ssp.sink_positionID " +
                "INNER JOIN `submarine_arise_position` sap ON s.arise_positionID = sap.arise_positionID";

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
            submarine.put("arisePositionZ", rs.getInt("arise_position_z"));
            submarine.put("active", rs.getString("active"));
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

    private synchronized int getSectorID(Vec2D sector) throws SQLException {
        int positionX = sector.getX();
        int positionY = sector.getY();

        String sql = "SELECT sectorID FROM sector WHERE position_x = ? AND position_y = ?";

        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, positionX);
        stmt.setInt(2, positionY);
        ResultSet rs = stmt.executeQuery();

        int sectorID = 0;

        while (rs.next()) {
            sectorID = rs.getInt("sectorID");
        }

        stmt.close();
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
}