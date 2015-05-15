package pt.inesc.termite.simplechat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper  extends SQLiteOpenHelper {
    //version number to upgrade database version
    //each time if you Add, Edit table, you need to change the
    //version number.
    private static final int DATABASE_VERSION = 18;

    // Database Name
    private static final String DATABASE_NAME = "airDesk";

    public DBHelper(Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here

        String CREATE_TABLE_FILE = "CREATE TABLE " + File.TABLE  + "("
                + File.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + File.KEY_title + " TEXT, "
                + File.KEY_content + " TEXT, "
                + File.KEY_author + " TEXT, "
                + File.KEY_createdAt + " TEXT, "
                + File.KEY_ws + " INTEGER, "
                + File.KEY_size + " INTEGER, "
                + File.KEY_locked_by + " TEXT)";

        db.execSQL(CREATE_TABLE_FILE);

        String CREATE_TABLE_WS = "CREATE TABLE " + Workspace.TABLE  + "("
                + Workspace.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Workspace.KEY_title + " TEXT, "
                + Workspace.KEY_createdAt + " TEXT, "
                + Workspace.KEY_public + " INTEGER, "
                + Workspace.KEY_sizeLimit + " INTEGER)";

        db.execSQL(CREATE_TABLE_WS);

        String CREATE_TABLE_USER = "CREATE TABLE " + User.TABLE  + "("
                + User.KEY_email  + " TEXT PRIMARY KEY, "
                + User.KEY_keywords  + " TEXT, "
                + User.KEY_fullName + " TEXT)";


        db.execSQL(CREATE_TABLE_USER);

        String CREATE_TABLE_INVITE = "CREATE TABLE " + Invite.TABLE  + "("
                + Invite.KEY_invited  + " INTEGER, "
                + Invite.KEY_keyword  + " TEXT, "
                + Invite.KEY_workspaceID  + " INTEGER, "
                + Invite.KEY_email + " TEXT)";

        db.execSQL(CREATE_TABLE_INVITE);

        String CREATE_TABLE_KEYWORD = "CREATE TABLE " + Keywords.TABLE  + "("
                + Keywords.KEY_keyword  + " TEXT, "
                + Keywords.KEY_workspaceID + " INTEGER)";

        db.execSQL(CREATE_TABLE_KEYWORD);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed, all data will be gone
        db.execSQL("DROP TABLE IF EXISTS " + File.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + User.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Workspace.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Invite.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Keywords.TABLE);

        // Create tables again
        onCreate(db);

    }

}