package ru.easyjava.data.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * JDBC statements example.
 */
public final class App {
    /**
     * Number of items in order.
     */
    private static final int MAX_ITEMS = 5;

    /**
     * We only ha3 clients.
     */
    private static final int MAX_CLIENTS = 3;

    /**
     * Query that create table.
     */
    private static final String CREATE_QUERY =
            "CREATE TABLE ORDER_ITEMS ("
                    + "ID IDENTITY PRIMARY KEY, "
                    + "CLIENT_ID INTEGER NOT NULL, "
                    + "ORDER_ID INTEGER NOT NULL, "
                    + "ITEM_ID INTEGER NOT NULL"
                    + ")";

    /**
     * Do not construct me.
     */
    private App() {
    }

    /**
     * Entry point.
     *
     * @param args Command line args. Not used.
     */
    public static void main(final String[] args) {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:")) {
            try (Statement dataQuery = db.createStatement()) {
                dataQuery.execute(CREATE_QUERY);

                for (int i = 1; i <= MAX_ITEMS; i++) {
                    dataQuery.executeUpdate("INSERT INTO ORDER_ITEMS "
                            + "(CLIENT_ID, ORDER_ID, ITEM_ID) "
                            + "values (1, 1, " + i + ")");
                }
            }

            try (PreparedStatement query =
                         db.prepareStatement("INSERT INTO ORDER_ITEMS "
                                 + "(CLIENT_ID, ORDER_ID, ITEM_ID) "
                                 + "values (1, 2, ?)")) {
                for (int i = 1; i <= MAX_ITEMS; i++) {
                    query.setInt(1, i);
                    query.executeUpdate();
                }
            }

            try (PreparedStatement batch =
                         db.prepareStatement("INSERT INTO ORDER_ITEMS "
                                 + "(CLIENT_ID, ORDER_ID, ITEM_ID) "
                                 + "values (1, ?, ?)")) {
                for (int i = 1; i <= MAX_ITEMS; i++) {
                    batch.setInt(1, MAX_CLIENTS);
                    batch.setInt(2, i);
                    batch.addBatch();
                }
                batch.executeBatch();
            }

            try (Statement results = db.createStatement()) {
                try (ResultSet rs =
                            results.executeQuery("SELECT * FROM ORDER_ITEMS")) {
                    while (rs.next()) {
                        System.out.println(
                                String.format(
                                        "client=%d, order=%d, item=%d",
                                        rs.getInt("CLIENT_ID"),
                                        rs.getInt("ORDER_ID"),
                                        rs.getInt("ITEM_ID")));
                    }
                }
            }

            try (CallableStatement upperProc =
                         db.prepareCall("{ ? = call upper( ? ) }")) {
                upperProc.registerOutParameter(1, Types.VARCHAR);
                upperProc.setString(2, "lowercase to uppercase");
                upperProc.execute();
                System.out.println(upperProc.getString(1));
            }
        } catch (SQLException ex) {
            System.out.println("Database connection failure: "
                    + ex.getMessage());
        }
    }
}
