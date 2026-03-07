package explorer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class WebApp {
    private final Database database = new Database();
    private List<Map<String, Object>> ships;
    private List<Map<String, Object>> allSectorsData;
    private List<Map<String, Object>> shipsScanData;
    private List<Map<String, Object>> shipsRadarData;
    private List<Map<String, Object>> submarinesData;

    public void startWebApplication() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", this::handleRequest);
        server.createContext("/static/", this::handleStaticFiles);
        server.start();
        System.out.println("Open http://localhost:8000/");
    }

    public void handleRequest(HttpExchange exchange){
        try {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/")) {
                renderHomepage(exchange, "homepage.html");
            } else if (path.equals("/ships")) {
                renderShips(exchange);
            } else if (path.equals("/sectors")) {
                renderSectorData(exchange);
            } else if (path.equals("/submarines")) {
                renderSubmarineData(exchange);
            }
            else if (path.contains("/scan-results") && path.contains("/ship")) {
                String[] parts = path.split("/");
                int shipID = Integer.parseInt(parts[2]);
                renderShipScanData(exchange, shipID);
            }
            else if (path.contains("/radar-results") && path.contains("/ship")) {
                String[] parts = path.split("/");
                String shipID = parts[2];
                renderShipRadarData(exchange, shipID);
            }
            else if (path.contains("/sector") && path.contains("/submarine-measurements") ) {
                String[] parts = path.split("/");
                int sectorID = Integer.parseInt(parts[2]);
                renderSectorSubmarineMeasurements(exchange, sectorID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String filePath = "src/explorer/view" + path;

        File file = new File(filePath);
        if (!file.exists()) {
            exchange.sendResponseHeaders(404, 0);
            exchange.getResponseBody().close();
            return;
        }

        String contentType = path.endsWith(".css") ? "text/css" : "text/plain";
        exchange.getResponseHeaders().add("Content-Type", contentType);
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private String getHTMLBase(String htmlTemplateName, StringBuilder rows){
        StringBuilder data = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new FileReader("src/explorer/view/templates/" + htmlTemplateName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data.toString().formatted(rows);
    }

    private void renderSectorSubmarineMeasurements(HttpExchange exchange, int sectorID) throws SQLException, IOException {
        submarinesData = database.getSubmarineMeasurementsForSector(sectorID);
        StringBuilder rows = new StringBuilder();
        for (Map<String, Object> submarineData : submarinesData) {
            rows.append(String.format("""
            <tr>
                <td data-label='Submarine ID'>%s</td>
                <td data-label='Vec X'>%s</td>
                <td data-label='Vec Y'>%s</td>
                <td data-label='Vec Z'>%s</td>
            </tr>
            """,
                    submarineData.get("submarineID"),
                    submarineData.get("vec_x"),
                    submarineData.get("vec_y"),
                    submarineData.get("vec_z")
            ));
        }

        String html = getHTMLBase("sectorSubmarineData.html", rows);
        renderHTML(exchange, html);
    }

    private void renderSectorData(HttpExchange exchange) throws IOException, SQLException {
        allSectorsData = database.getAllSectorData();
        StringBuilder rows = new StringBuilder();
        for (Map<String, Object> sector : allSectorsData) {
            rows.append(String.format("""
            <tr>
                <td data-label='Sector ID'>%s</td>
                <td data-label='Position X'>%s</td>
                <td data-label='Position Y'>%s</td>
                <td data-label='Depth Average'>%s</td>
                <td data-label='Standard Deviation'>%s</td>
                <td data-label='Height'>%s</td>
                <td data-label='Ground'>%s</td>
                <td data-label='Navigable'>%s</td>
                <td data-label='Submarine depth measurements'>
                    <a class="back-button" href="http://localhost:8000/sector/%s/submarine-measurements">Show submarine data</a>
                </td>
            </tr>
            """,
                    sector.get("sectorID"),
                    sector.get("position_x"),
                    sector.get("position_y"),
                    sector.getOrDefault("total_depth_average", "No Data"),
                    sector.getOrDefault("standard_deviation", "No Data"),
                    sector.getOrDefault("height", "No Data"),
                    sector.getOrDefault("ground", "No Data"),
                    sector.getOrDefault("navigable", "No Data"),
                    sector.get("sectorID")
            ));
        }

        String html = getHTMLBase("sectorData.html", rows);
        renderHTML(exchange, html);
    }

    private void renderHomepage(HttpExchange exchange, String htmlTemplateName) throws SQLException, IOException {
        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/explorer/view/templates/" + htmlTemplateName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String html = data.toString();
        renderHTML(exchange, html);
    }

    private void renderHTML(HttpExchange exchange, String html) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, html.getBytes("UTF-8").length);
        OutputStream os = exchange.getResponseBody();
        os.write(html.getBytes("UTF-8"));
        os.close();
    }

    private void renderShipRadarData(HttpExchange exchange, String shipID) throws SQLException, IOException {
        shipsRadarData = database.getShipRadarData(shipID);
        StringBuilder rows = new StringBuilder();
        for (Map<String, Object> shipRadarData : shipsRadarData) {
            rows.append(String.format("""
            <tr>
                <td data-label='Sector ID'>%s</td>
                <td data-label='Height'>%s</td>
                <td data-label='Ground'>%s</td>
                <td data-label='Navigable'>%s</td>
            </tr>
            """,
                    shipRadarData.get("sectorID"),
                    shipRadarData.get("height"),
                    shipRadarData.get("ground"),
                    shipRadarData.get("navigable")
            ));
        }

        String html = getHTMLBase("shipRadarData.html", rows);
        renderHTML(exchange, html);
    }

    private void renderShips(HttpExchange exchange) throws SQLException, IOException {
        ships = database.getAllShips();
        StringBuilder rows = new StringBuilder();
        for (Map<String, Object> ship : ships) {
            rows.append(String.format("""
                <tr>
                    <td data-label='Ship ID'>%s</td>
                    <td data-label='Name'>%s</td>
                    <td data-label='Type'>%s</td>
                    <td data-label='Active'>%s</td>
                    <td data-label='Ship data'>
                        <div class="button-container">
                            <a class="back-button" href="http://localhost:8000/ship/%s/scan-results">Show ship scan data</a>
                            <a class="back-button" href="http://localhost:8000/ship/%s/radar-results">Show ship radar data</a>
                        </div>
                    </td>
                </tr>
                """,
                    ship.get("shipID"),
                    ship.get("name"),
                    ship.get("typ"),
                    ship.get("active"),
                    ship.get("shipID"),
                    ship.get("shipID")
            ));
        }

        String html = getHTMLBase("shipData.html", rows);
        renderHTML(exchange, html);
    }

    private void renderShipScanData(HttpExchange exchange, int shipID) throws SQLException, IOException {
        shipsScanData = database.getShipScanData(shipID);
        StringBuilder rows = new StringBuilder();
        for (Map<String, Object> shipScanData : shipsScanData) {
            rows.append(String.format("""
            <tr>
                <td data-label='Sector ID'>%s</td>
                <td data-label='Depth Average'>%s</td>
                <td data-label='Standard Deviation'>%s</td>
            </tr>
            """,
                    shipScanData.get("sectorID"),
                    shipScanData.get("total_depth_average"),
                    shipScanData.get("standard_deviation")
            ));
        }

        String html = getHTMLBase("shipScanData.html", rows);
        renderHTML(exchange, html);
    }

    public void renderSubmarineData(HttpExchange exchange) throws SQLException, IOException {
        submarinesData = database.getAllSubmarineData();
        StringBuilder rows = new StringBuilder();
        for (Map<String, Object> submarineData : submarinesData) {
            rows.append(String.format("""
            <tr>
                <td data-label='Submarine ID'>%s</td>
                <td data-label='Ship name'>%s</td>
                <td data-label='Sink position x'>%s</td>
                <td data-label='Sink position y'>%s</td>
                <td data-label='Sink position z'>%s</td>
                <td data-label='Arise position x'>%s</td>
                <td data-label='Arise position y'>%s</td>
                <td data-label='Arise position z'>%s</td>
                <td data-label='Active'>%s</td>
                <td data-label='Sunk'>%s</td>
            </tr>
            """,
                    submarineData.get("submarineID"),
                    submarineData.get("shipName"),
                    submarineData.get("sinkPositionX"),
                    submarineData.get("sinkPositionY"),
                    submarineData.get("sinkPositionZ"),
                    submarineData.get("arisePositionX"),
                    submarineData.get("arisePositionY"),
                    submarineData.get("arisePositionZ"),
                    submarineData.get("active"),
                    submarineData.get("sunk")
            ));
        }

        String html = getHTMLBase("submarineData.html", rows);
        renderHTML(exchange, html);
    }
}