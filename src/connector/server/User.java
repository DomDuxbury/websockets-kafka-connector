package connector.server;

import connector.PostgresInterface;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Random;

public class User {
    private static int latestId = 0;
    private final int connectionId;
    private boolean authorised;
    private boolean sessionStarted;
    private ExperimentInfo info;
    private String mTurkWorkerId;
    private int numberOfRecommendedRoutesTaken = 0;
    private int numberOfDrawnRoutesTaken = 0;
    private int numberOfRoutesDeactivated = 0;

    private String[] scenarioNames = {
            "introduction_scenario",
            "porth_neigwl",
            "intermediate_scenario",
    };

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

    public boolean sessionStarted() {
        return sessionStarted;
    }

    public void setSessionStarted() {
        sessionStarted = true;
    }

    public void recordActivatedRoute(String type) {
        if (type.equals("RECOMMENDED")) {
            numberOfRecommendedRoutesTaken++;
        } else if (type.equals("DRAWN")) {
            numberOfDrawnRoutesTaken++;
        } else if (type.equals("DEACTIVATED")) {
            numberOfRoutesDeactivated++;
        }
    }

    public int getNumberOfRecommendedRoutesTaken() {
        return numberOfRecommendedRoutesTaken;
    }

    public int getNumberOfDrawnRoutesTaken() {
        return numberOfDrawnRoutesTaken;
    }

    public void resetRoutesTaken() {
        numberOfRecommendedRoutesTaken = 0;
        numberOfDrawnRoutesTaken = 0;
    }

    private String getScenario() {
        int index = new Random().nextInt(scenarioNames.length);
        String scenario = scenarioNames[index];
        scenarioNames = ArrayUtils.remove(scenarioNames, index);
        return scenario;
    }

    public void createExperimentInfo(PostgresInterface db, String mTurkWorkerId) {
        String firstScenario = scenarioNames[0];
        String secondScenario = scenarioNames[1];
        String thirdScenario = scenarioNames[2];
        boolean preferences = new Random().nextBoolean();
        boolean dynamic_refresh = new Random().nextBoolean();
        boolean explanation = new Random().nextBoolean();
        this.info = db.createUser(mTurkWorkerId, firstScenario, secondScenario, thirdScenario, preferences, dynamic_refresh, explanation);
    }

    @Override
    public String toString() {
        return "User{" +
                "connectionId=" + connectionId +
                ", authorised=" + authorised +
                ", sessionStarted=" + sessionStarted +
                ", info=" + info +
                ", mTurkWorkerId='" + mTurkWorkerId + '\'' +
                '}';
    }
}
