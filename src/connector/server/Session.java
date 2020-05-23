package connector.server;

import com.google.gson.internal.LinkedTreeMap;
import connector.PostgresInterface;

public class Session {
    private int stage;
    private final int MAX_TIME_STEPS = 300;
    private int timeStep;
    private final User sessionUser;

    public Session(User user) {
        this.sessionUser = user;
        this.stage = 0;
        this.timeStep = 0;
    }

    public int getSessionTimeRemaining() {
        int stagesRemaining = 2 - stage;
        int timeStepsRemaining = MAX_TIME_STEPS - timeStep;
        return (stagesRemaining * MAX_TIME_STEPS) + timeStepsRemaining;
    }

    public boolean sessionBelongsToUser(User user) {
        return sessionUser.getConnectionId() == user.getConnectionId();
    }

    public boolean isFinalStage() {
        return stage == 3;
    }

    public void recordScore(Message message, PostgresInterface db) {
        LinkedTreeMap<String, Double> harbourState = (LinkedTreeMap) message.getPayload();
        int score = (int) Math.round(harbourState.get("score"));
        db.recordScore(sessionUser, stage, score);
        sessionUser.resetRoutesTaken();
    }

    public User getSessionUser() {
        return sessionUser;
    }

    public int getStage() {
        return stage;
    }

    public void nextStage() {
        stage++;
        timeStep = 0;
    }
}
