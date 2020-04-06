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
    private String prodUrl = "jdbc:postgresql://35.246.0.142:5432/postgres";

    public PostgresInterface() throws Exception {
        Properties props = new Properties();
        props.setProperty("user",user);
        props.setProperty("password", password);
        conn = DriverManager.getConnection(url, props);
    }

    private PreparedStatement buildCreateUserStatement(String firstScenario, String secondScenario, String thirdScenario,
                                                       Boolean explanation, Boolean preferences, Boolean dynamicRefresh) throws Exception {
        String createUserQuery = "INSERT INTO users " +
                "(scenario_1, scenario_2, scenario_3, timestep, explanation, preferences, dynamic_refresh, scenario_1_score, scenario_2_score, scenario_3_score) VALUES (?, ?, ?, 0, ?, ?, ?, 0, 0, 0)" +
                "RETURNING user_id";
        PreparedStatement statement = conn.prepareStatement(createUserQuery);
        statement.setString(1, firstScenario);
        statement.setString(2, secondScenario);
        statement.setString(3, thirdScenario);
        statement.setBoolean(4, explanation);
        statement.setBoolean(5, preferences);
        statement.setBoolean(6, dynamicRefresh);
        return statement;
    }

    private PreparedStatement buildRecordScoreStatement(Integer userId, Integer timeStep, Integer scenarioNumber, Integer score) throws Exception {
        String createUserQuery = "UPDATE users SET timestep = ?, scenario_" + scenarioNumber + "_score = ? WHERE user_id = ?";
        PreparedStatement statement = conn.prepareStatement(createUserQuery);
        statement.setInt(1, timeStep);
        statement.setInt(2, score);
        statement.setInt(3, userId);
        return statement;
    }

    // Creates a user and returns the userId
    public ExperimentInfo createUser(String firstScenario, String secondScenario, String thirdScenario,
                                     Boolean explanation, Boolean preferences, Boolean dynamicRefresh) {
        try {
            PreparedStatement statement = buildCreateUserStatement(firstScenario, secondScenario, thirdScenario, explanation, preferences, dynamicRefresh);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            int userId = resultSet.getInt(1);
            return new ExperimentInfo(userId, firstScenario, secondScenario, thirdScenario, explanation, preferences, dynamicRefresh);
        } catch (Exception e) {
            System.out.println("Creating user failed...");
            e.printStackTrace();
            return null;
        }
    }

    public void recordScore(Integer userId, Integer timeStep, Integer scenarioNumber, Integer score) {
        try {
            PreparedStatement statement = buildRecordScoreStatement(userId, timeStep,scenarioNumber, score);
            statement.executeUpdate();
        } catch (Exception e) {
            System.out.println("Recording score failed...");
            e.printStackTrace();
        }
    }
}
