package bgu.spl181.net.api.bidi;

/**
 * this class simulate a movie of user.
 * has been built to help us read or write from or to Json.
 */
public class movieOfUser {
    private String name;
    private long id;
    public movieOfUser(String name,long id){
        this.id=id;
        this.name=name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
