package connector.server;

import connector.PostgresInterface;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Random;

public class User {
    private static int latestId = 0;
    private int stage;
    private int connectionId;
    private boolean authorised;
    private ExperimentInfo info;
    private String[] scenarioNames = {
            "harbourTest",
            "harbourTest",
            "harbourTest",
    };

    public User() {
        this.connectionId = latestId;
        this.authorised = false;
        this.stage = 0;
        latestId++;
    }

    public void incrementStage() {
        stage++;
    }

    public int getStage() {
        return stage;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public ExperimentInfo getInfo() {
        return info;
    }

    public boolean isAuthorised() {
        return authorised;
    }

    public void authoriseUser() {
        authorised = true;
    }

    private String getScenario() {
            int index = new Random().nextInt(scenarioNames.length);
            String scenario = scenarioNames[index];
            scenarioNames = ArrayUtils.remove(scenarioNames, index);
            return scenario;
    }

    public void createExperimentInfo(PostgresInterface db) {
        String firstScenario = getScenario();
        String secondScenario = getScenario();
        String thirdScenario = getScenario();
        boolean preferences = new Random().nextBoolean();
        boolean dynamic_refresh = new Random().nextBoolean();
        boolean explanation = new Random().nextBoolean();
        this.info = db.createUser(firstScenario, secondScenario, thirdScenario, preferences, dynamic_refresh, explanation);
        System.out.println(info);
        System.out.println(getInfo());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + connectionId +
                ", authorised=" + authorised +
                '}';
    }
}
