package pt.inesc.termite.simplechat;

public class Workspace {
    // Labels table name
    public static final String TABLE = "Workspace";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_title = "title";
    public static final String KEY_createdAt = "createdAt";
    public static final String KEY_public = "public";
    public static final String KEY_sizeLimit = "sizeLimit";

    // property help us to keep data
    public int ws_ID;
    public String title;
    public String createdAt;
    public int publicWs;
    public int sizeLimit;
}
