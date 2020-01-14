package connector;

import connector.server.ExperimentInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class PostgresInterface {
    private Connection conn;
    private String user = "postgres";
    private String password = "mcdaUser";
    private String url = "jdbc:postgresql://localhost:5432/postgres";

    public PostgresInterface() throws Exception {
        Properties props = new Properties();
        props.setProperty("user",user);
        props.setProperty("password", password);
        conn = DriverManager.getConnection(url, props);
    }

    private PreparedStatement buildCreateUserStatement(String scenario, Boolean explanation,
                                                       Boolean preferences, Boolean dynamicRefresh) throws Exception {
        String createUserQuery = "INSERT INTO users " +
                "(scenario, timestep, explanation, preferences, dynamic_refresh, scenario_score, connected) VALUES (?, 0, ?, ?, ?, 0, true)" +
                "RETURNING user_id";
        PreparedStatement statement = conn.prepareStatement(createUserQuery);
        statement.setString(1, scenario);
        statement.setBoolean(2, explanation);
        statement.setBoolean(3, preferences);
        statement.setBoolean(4, dynamicRefresh);
        return statement;
    }

    private PreparedStatement buildRecordScoreStatement(Integer userId, Integer timeStep, Integer score) throws Exception {
        String createUserQuery = "UPDATE users SET timestep = ?, scenario_score = ? WHERE user_id = ?";
        PreparedStatement statement = conn.prepareStatement(createUserQuery);
        statement.setInt(1, timeStep);
        statement.setInt(2, score);
        statement.setInt(3, userId);
        return statement;
    }

    // Creates a user and returns the userId
    public ExperimentInfo createUser(String scenario, Boolean explanation, Boolean preferences, Boolean dynamicRefresh) {
        try {
            PreparedStatement statement = buildCreateUserStatement(scenario, explanation, preferences, dynamicRefresh);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            int userId = resultSet.getInt(1);
            System.out.println(userId);
            return new ExperimentInfo(userId, scenario, explanation, preferences, dynamicRefresh);
        } catch (Exception e) {
            System.out.println("Creating user failed...");
            e.printStackTrace();
            return null;
        }
    }

    public void recordScore(Integer userId, Integer timeStep, Integer score) {
        try {
            PreparedStatement statement = buildRecordScoreStatement(userId, timeStep, score);
            statement.executeUpdate();
        } catch (Exception e) {
            System.out.println("Recording score failed...");
            e.printStackTrace();
        }
    }
}
