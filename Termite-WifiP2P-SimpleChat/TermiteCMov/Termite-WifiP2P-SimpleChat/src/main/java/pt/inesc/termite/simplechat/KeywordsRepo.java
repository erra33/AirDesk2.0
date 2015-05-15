package pt.inesc.termite.simplechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class KeywordsRepo {

    private DBHelper dbHelper;

    public KeywordsRepo(Context context) {
        dbHelper = new DBHelper(context);
    }
    public void insert(String[] keywords, int wsID) {
        deleteKeywordsByID(wsID);
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
    public String getKeywordsById(int id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " +
                Keywords.TABLE
                + " WHERE " +
                Keywords.KEY_workspaceID + "=?";

        String listOfWs = "";
        Cursor cursor = db.rawQuery(selectQuery, new String[] { id+"" } );

        if (cursor.moveToFirst()) {
            do {
                Log.d("cursor.getColumnIndex(Keywords.KEY_keyword", cursor.getColumnIndex(Keywords.KEY_keyword)+"");
                listOfWs = listOfWs + cursor.getString(cursor.getColumnIndex(Keywords.KEY_keyword)) + " ";
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listOfWs;
    }

    public void deleteKeywordsByID(int id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Keywords.TABLE, Keywords.KEY_workspaceID + "= ?", new String[] { String.valueOf(id) });
        db.close(); // Closing database connection

    }
}