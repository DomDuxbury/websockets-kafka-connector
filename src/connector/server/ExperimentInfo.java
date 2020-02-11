package connector.server;

public class ExperimentInfo {
    private int userId;
    private String firstScenario;
    private String secondScenario;
    private String thirdScenario;
    private boolean preferences;
    private boolean dynamic_refresh;
    private boolean explanation;

    public ExperimentInfo(int userId, String firstScenario, String secondScenario, String thirdScenario, boolean preferences, boolean dynamic_refresh, boolean explanation) {
        this.userId = userId;
        this.firstScenario = firstScenario;
        this.secondScenario = secondScenario;
        this.thirdScenario = thirdScenario;
        this.preferences = preferences;
        this.dynamic_refresh = dynamic_refresh;
        this.explanation = explanation;
    }

    public int getUserId() {
        return userId;
    }

    public String getFirstScenario() {
        return firstScenario;
    }

    public String getSecondScenario() {
        return secondScenario;
    }

    public String getThirdScenario() {
        return thirdScenario;
    }

    @Override
    public String toString() {
        return "ExperimentInfo{" +
                "userId=" + userId +
                ", firstScenario='" + firstScenario + '\'' +
                ", secondScenario='" + secondScenario + '\'' +
                ", thirdScenario='" + thirdScenario + '\'' +
                ", preferences=" + preferences +
                ", dynamic_refresh=" + dynamic_refresh +
                ", explanation=" + explanation +
                '}';
    }
}
