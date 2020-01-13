package connector.server;

import connector.PostgresInterface;

import java.util.Random;

public class User {
    private static int latestId = 0;
    private int connectionId;
    private boolean authorised;
    private ExperimentInfo info;
    private String[] scenarioNames = { "IW18", "SDKDemo" };

    public User() {
        this.connectionId = latestId;
        this.authorised = false;
        latestId++;
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

    public void createExperimentInfo(PostgresInterface db) {
        String scenarioName = scenarioNames[new Random().nextInt(scenarioNames.length)];
        boolean preferences = new Random().nextBoolean();
        boolean dynamic_refresh = new Random().nextBoolean();
        boolean explanation = new Random().nextBoolean();
        this.info = db.createUser(scenarioName, preferences, dynamic_refresh, explanation);
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
