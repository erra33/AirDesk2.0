package pt.inesc.termite.simplechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class User {

    private DBHelper dbHelper;

    public User(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void setUser(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
        insert(this);
    }


    //This is a comment
    // Labels table name
    public static final String TABLE = "User";

    // Labels Table Columns names
    public static final String KEY_email = "email";
    public static final String KEY_fullName = "fullName";

    // property help us to keep data
    public String email;
    public String fullName;

    public static boolean isEmailAddress(String m){
//        if (m.isEmpty()) return false;
//        if (!m.contains("@")) return false; //email must contain an @
//
//        String[] mArray = m.split("@");
//        if (mArray.length != 2 ) return false; // only one @ allowedWifiP2P
//        if (!mArray[1].contains(".")) return false; //There must be a dot after the @

        return true;
    }

    public User getUser(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                User.KEY_email + ", " +
                User.KEY_fullName +
                " FROM " + User.TABLE;

        User user = this;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                user.email =cursor.getString(cursor.getColumnIndex(User.KEY_email));
                user.fullName =cursor.getString(cursor.getColumnIndex(User.KEY_fullName));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return user;
    }

    public void insert(User user) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(User.KEY_email,user.email);
        values.put(User.KEY_fullName, user.fullName);

        // Inserting Row
        db.insert(User.TABLE, null, values);
        db.close(); // Closing database connection
    }
}
