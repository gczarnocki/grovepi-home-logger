package derby;

import derby.model.SensorDataEntity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gczarnocki on 2017-07-08.
 */
public class Loader {
    // private static final String DB_NAME = "";
    // private static final String DB_PATH = "";
    private static final String DB_FRAMEWORK = "embedded";
    private static final String DB_PROTOCOL = "jdbc:derby:";

    private static final String SENSOR_INFO = "SENSOR_INFO";

    public static void main(String[] args) throws SQLException {
        DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());

        if (args.length == 0) {
            System.out.println("USAGE: derby.Loader <file_path>");
            return;
        }

        new Loader().go(args);
        System.out.println("derby.Loader zakończył działanie.");
    }

    private void go(String[] args) {
        Connection conn = null;
        PreparedStatement psInsert;
        Statement s;
        List<Statement> statements = new ArrayList<>();
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_PROTOCOL + DB_PATH);
            System.out.println("Poprawnie podłączono się do bazy danych.");
            conn.setAutoCommit(false);

            String filePath = args[0];
            BufferedReader br = null;

            s = conn.createStatement();
            statements.add(s);
            rs = s.executeQuery("select count(*) from SENSOR_INFO");

            if(!rs.next()) {
                System.out.println("No rows!");
            } else {
                int rows = rs.getInt(1);
                System.out.println(String.format("W bazie obecnych jest: %d wpisów", rows));
            }

            try {
                br = new BufferedReader(new FileReader(filePath));

                String line = br.readLine();

                while (line != null) {
                    System.out.println(line);

                    String[] tokens = line.split(" ");
                    SensorDataEntity entity = new SensorDataEntity(tokens);

                    psInsert = conn.prepareStatement(
                            "insert into " +
                            "SENSOR_INFO(LOG_TIME, TEMPERATURE, HUMIDITY, LIGHT, SOUND, PROXIMITY, THRESHOLD) " +
                            "values (?, ?, ?, ?, ?, ?, ?)");
                    statements.add(psInsert);

                    psInsert.setTimestamp(1, entity.getLogTime());
                    psInsert.setFloat(2, entity.getTemperature());
                    psInsert.setFloat(3, entity.getHumidity());
                    psInsert.setFloat(4, entity.getLight());
                    psInsert.setFloat(5, entity.getSound());
                    psInsert.setFloat(6, entity.getProximity());
                    psInsert.setFloat(7, entity.getThreshold());

                    statements.add(psInsert);
                    psInsert.executeUpdate();

                    line = br.readLine();
                }

                conn.commit();
                System.out.println("Transakcja została zatwierdzona pomyślnie.");
            } catch (FileNotFoundException fnfe) {
                System.err.println("Wystąpił błąd: plik nie istnieje!");
            } catch(IOException ioe) {
                System.err.println("Wystąpił błąd: I/O!");
                ioe.printStackTrace();
            }
        } catch(SQLException se) {
            System.err.println("Wystąpił błąd z połączeniem do bazy danych!");
            se.printStackTrace();
        } finally {
            // W tym miejscu wyczyścimy wszystkie zasoby, zamkniemy bazę danych.

            try {
                DriverManager.getConnection(DB_PROTOCOL + DB_PATH);
            } catch(SQLException se) {
                if((se.getErrorCode() == 50000)
                        && ("XJ015".equals(se.getSQLState()))) {
                    System.out.println("Silnik wyłączył się pomyślnie.");
                } else {
                    System.err.println("Silnik nie wyłączył się pomyślnie.");
                }
            }

            try {
                if(rs != null) rs.close();

                while(!statements.isEmpty()) {
                    Statement st = statements.remove(0);
                    if (st != null) st.close();
                }

                if (conn != null) conn.close();
            } catch(SQLException sqle) {
                printSQLException(sqle);
            }
        }
    }

    /**
     * Wypisuje szczegóły dot. SQLException do strumienia System.err
     * @param sqle SQLException, którego szczegóły są do wypisania
     */
    private void printSQLException(SQLException sqle) {
        while (sqle != null) {
            System.err.println("SQL Exception:");
            System.err.println("SQL State: " + sqle.getSQLState());
            System.err.println("Error Code: " + sqle.getErrorCode());
            System.err.println("Message: " + sqle.getMessage());

            sqle = sqle.getNextException();
        }
    }
}