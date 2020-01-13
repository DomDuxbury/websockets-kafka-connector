package connector.server;

public class ExperimentInfo {
    private int userId;
    private String scenarioName;
    private boolean preferences;
    private boolean dynamic_refresh;
    private boolean explanation;

    public ExperimentInfo(int userId, String scenarioName, boolean preferences, boolean dynamic_refresh, boolean explanation) {
        this.userId = userId;
        this.scenarioName = scenarioName;
        this.preferences = preferences;
        this.dynamic_refresh = dynamic_refresh;
        this.explanation = explanation;
    }

    public int getUserId() {
        return userId;
    }

    public String getScenarioName() {
        return scenarioName;
    }
}
