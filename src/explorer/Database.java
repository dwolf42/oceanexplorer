package explorer;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private Connection conn;
    private Statement statement;
    private PreparedStatement stmt;

    // Database connection parameters
    // !!!
    // TODO: DB_URL and PASS should be adjusted depending on the real credentials for the database
    // !!!
    static final String DB_URL = "jdbc:mariadb://localhost:???/???";  // !!! Enter here the correct port and the correct db name
    static final String USER = "root";
    static final String PASS = "???";  // !!! Enter here the db psw

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
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
            ship.put("typ", rs.getString("typ"));
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
        String sql = """
        SELECT rr.height, rr.ground, rr.navigable
        FROM radar_results rr
        JOIN ship_radar_results shipRadarResults ON rr.radar_resultID = shipRadarResults.ship_radar_resultID
        WHERE shipRadarResults.sectorID = ?
        LIMIT 1
    """;

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

    public synchronized List<Map<String, Object>> getShipRadarData(String shipID) throws SQLException {
        List<Map<String, Object>> shipRadarData = new ArrayList<>();
        String sql = "SELECT * FROM ship_radar_results WHERE shipID = ?";

        conn = getConnection();
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, shipID);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Map<String, Object> shipData = new HashMap<>();
            int radar_resultID = rs.getInt("radar_resultID");
            int sectorID = rs.getInt("sectorID");
            shipData.put("sectorID", sectorID);

            String sql2 = "SELECT height, ground, navigable FROM radar_results WHERE radar_resultID = ?";
            stmt = conn.prepareStatement(sql2);
            stmt.setInt(1, radar_resultID);
            ResultSet rs2 = stmt.executeQuery();

            while (rs2.next()) {
                shipData.put("height", rs2.getInt("height"));
                shipData.put("ground", rs2.getString("ground"));
                shipData.put("navigable", rs2.getString("navigable"));
            }
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
                "sap.position_x AS arise_position_x, sap.position_y AS arise_position_y, sap.position_z AS arise_position_z," +
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
}