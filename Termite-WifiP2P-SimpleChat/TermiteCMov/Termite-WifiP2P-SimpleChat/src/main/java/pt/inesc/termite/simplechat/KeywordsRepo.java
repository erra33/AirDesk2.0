package pt.inesc.termite.simplechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class KeywordsRepo {

    private DBHelper dbHelper;

    public KeywordsRepo(Context context) {
        dbHelper = new DBHelper(context);
    }
    public void insert(String[] keywords, int wsID) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (int i = 0; i < keywords.length; i++){
            ContentValues values = new ContentValues();
            values.put(Keywords.KEY_workspaceID, wsID);
            values.put(Keywords.KEY_keyword, keywords[i]);

            // Inserting Row
            long file_Id = db.insert(Keywords.TABLE, null, values);
        }
        db.close(); // Closing database connection
    }
}