package connector.server;

public class User {
    private static int latestId = 0;
    private int id;
    private boolean authorised;

    public User() {
        this.id = latestId;
        this.authorised = false;
        latestId++;
    }

    public int getId() {
        return id;
    }

    public boolean isAuthorised() {
        return authorised;
    }

    public void authoriseUser() {
        authorised = true;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", authorised=" + authorised +
                '}';
    }
}
