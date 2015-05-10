package pt.inesc.termite.simplechat;

public class File {
    // Labels table name
    public static final String TABLE = "File";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_title = "title";
    public static final String KEY_content = "content";
    public static final String KEY_author = "author";
    public static final String KEY_createdAt = "createdAt";
    public static final String KEY_ws = "ws";
    public static final String KEY_size = "size";


    // property help us to keep data
    public int file_ID;
    public String title;
    public String content;
    public String author;
    public String createdAt;
    public int ws;
    public int size;

    public void setSize() {
        size = ((title.length()+content.length()+author.length())*2);
    }

}
