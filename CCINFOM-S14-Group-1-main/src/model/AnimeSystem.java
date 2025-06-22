package src.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.sql.*;

/**
 */
public class AnimeSystem {
    public final static String PATH = "jdbc:mysql://localhost:3306/dbanime";
    private Connection dbConnection;
    private Statement dbStatement;
    private ResultSet dbResultSet;
    private ResultSetMetaData dbMetaData;

    public AnimeSystem(String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConnection = DriverManager.getConnection(PATH, username, password);
            dbStatement = dbConnection.createStatement();
        } catch (Exception e) {
            System.err.println("Unable to establish connection");
            e.printStackTrace();
        }
    }

    // # Records

    // ## Anime

    /**
     * * Modified to allow multiple record names
     * 
     * @param recordNames
     * @return list of column names
     */
    public String[] getRecordColNames(String... recordNames) {
        StringBuffer recordNamesString = new StringBuffer();
        int i = 0;
        for (String s : recordNames) {
            if (i > 0) {
                recordNamesString.append(",");
            }
            s = String.format("'%s'", s);
            recordNamesString.append(s);
            i++;
        }
        System.out.println("Columns of " + recordNamesString);

        ArrayList<String> returnVal = new ArrayList<String>();
        String query = String.format("""
                SELECT `TABLE_NAME`, `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS`
                WHERE `TABLE_SCHEMA` = 'dbanime'
                AND `TABLE_NAME` IN (%s)
                """, recordNamesString);
        System.out.println(query);

        try {
            dbResultSet = dbStatement.executeQuery(query);
            while (dbResultSet.next()) {
                returnVal.add(dbResultSet.getString("TABLE_NAME") + "." + dbResultSet.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            System.err.println("Query to 'dbanime' Failed.");
            e.printStackTrace();
        }

        return returnVal.toArray(new String[0]);
    }

    // public boolean checkIfExists(int value, String column, String table){

    // }

    public void callProcedure(String procedure, String... arguments) throws SQLException {
        PreparedStatement statement = dbConnection.prepareStatement("CALL " + procedure);
        for (int i = 0; i < arguments.length; i++) {
            statement.setString(i + 1, arguments[i]);
        }
        statement.execute();
    }

    public String getProcedureSingleResult(String procedure) {
        try {
            dbResultSet = dbStatement.executeQuery("CALL " + procedure + ";");
            dbResultSet.next();
            return dbResultSet.getString(1);
        } catch (Exception e) {
            System.err.println("Procedure" + " Failed to Execute.");
            e.printStackTrace();
            return null;
        }
    }

    public String[][] getProcedureResults(String procedure, Object... arguments) throws SQLException {
        ArrayList<String[]> data = new ArrayList<String[]>();
        PreparedStatement statement = dbConnection.prepareStatement("CALL " + procedure);
        for (int i = 0; i < arguments.length; i++) {
            statement.setObject(i + 1, arguments[i]);
        }
        System.out.println(statement);
        dbResultSet = statement.executeQuery();
        dbMetaData = dbResultSet.getMetaData();
        int columnCount = dbMetaData.getColumnCount();
        while (dbResultSet.next()) {
            String[] rowData = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                rowData[i] = dbResultSet.getString(i + 1);
            }
            data.add(rowData);
        }
        return data.toArray(new String[0][0]);
    }

    public String singleQuery(String query) {
        try {
            dbResultSet = dbStatement.executeQuery(query);
            dbResultSet.next();
            return dbResultSet.getString(1);
        } catch (Exception e) {
            System.err.println("Query to 'dbanime' Failed.");
            e.printStackTrace();
            return null;
        }
    }

    public String[][] rawQuery(String query) throws SQLException {
        System.out.println(query);
        ArrayList<String[]> data = new ArrayList<String[]>();
        dbResultSet = dbStatement.executeQuery(query);
        dbMetaData = dbResultSet.getMetaData();
        int columnCount = dbMetaData.getColumnCount();
        while (dbResultSet.next()) {
            String[] rowData = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                rowData[i] = dbResultSet.getString(i + 1);
            }
            data.add(rowData);
        }
        return data.toArray(new String[0][0]);
    }

    /**
     * Select columns from a record. This is a "shortcut" function, returning
     * essentially
     * 
     * <pre>
     * SELECT `columns` FROM `record` ORDER BY `column to sort by`
     * </pre>
     * 
     * where <code>column to sort by</code> is defined in the
     * {@link Records#getSortByColumn(String)} function, and is thus not a parameter
     * here.
     * 
     * @param columns
     * @param record
     * @return
     */
    public String[][] selectColumns(String[] columns, String record) throws SQLException {
        String columnsString = new String();
        for (String column : columns) {
            columnsString += column + ", ";
        }
        columnsString = columnsString.substring(0, columnsString.length() - 2);
        String query = String.format("""
                SELECT %s FROM %s ORDER BY %s
                """,
                columnsString,
                record,
                Records.getSortByColumn(record));
        return this.rawQuery(query);
    }

    public String minimize(String string) {
        string = string.toLowerCase();
        string.replaceAll(" ", "");
        return string;
    }

    public void rawUpdate(String query) throws SQLException {
        System.out.println(query);
        dbStatement.executeUpdate(query);
        System.err.println("Query to 'dbanime' Failed.");
    }

    public void safeUpdate(String query, String... arguments) throws SQLException {
        PreparedStatement statement = dbConnection.prepareStatement(query);
        for (int i = 0; i < arguments.length; i++) {
            statement.setString(i + 1, arguments[i]);
        }
        System.out.println(statement);
        statement.executeUpdate();
    }

    /**
     * Execute a query with arguments expecting one row returned, returning a
     * HashMap mapping column name to its value.
     * 
     * @param query
     * @param arguments
     * @return
     * @throws SQLException
     */
    public HashMap<String, String> safeSingleQuery(String query, String... arguments) throws SQLException {
        PreparedStatement statement = dbConnection.prepareStatement(query);
        for (int i = 0; i < arguments.length; i++) {
            System.out.println(arguments[i]);
            statement.setString(i + 1, arguments[i]);
        }
        System.out.println(statement);

        HashMap<String, String> data = new HashMap<>();
        dbResultSet = statement.executeQuery();
        dbMetaData = dbResultSet.getMetaData();
        dbResultSet.next();
        for (int i = 1; i <= dbMetaData.getColumnCount(); i++) {
            System.out.println(dbMetaData.getColumnCount() + " " + i);
            data.put(dbMetaData.getColumnLabel(i), dbResultSet.getString(i));
        }
        return data;
    }
}
