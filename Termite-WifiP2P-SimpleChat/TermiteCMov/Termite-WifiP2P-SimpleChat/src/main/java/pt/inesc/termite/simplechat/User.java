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

    public void setUser(String email, String fullName, String keywords) {
        this.email = email;
        this.fullName = fullName;
        this.keywords = keywords;
        insert(this);
    }


    //This is a comment
    // Labels table name
    public static final String TABLE = "User";

    // Labels Table Columns names
    public static final String KEY_email = "email";
    public static final String KEY_fullName = "fullName";
    public static final String KEY_keywords = "keywords";

    // property help us to keep data
    public String email;
    public String fullName;
    public String keywords;

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
                User.KEY_fullName + ", " +
                User.KEY_keywords +
                " FROM " + User.TABLE;

        User user = this;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                user.email =cursor.getString(cursor.getColumnIndex(User.KEY_email));
                user.fullName =cursor.getString(cursor.getColumnIndex(User.KEY_fullName));
                user.keywords =cursor.getString(cursor.getColumnIndex(User.KEY_keywords));

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
        values.put(User.KEY_keywords, user.keywords);

        // Inserting Row
        db.insert(User.TABLE, null, values);
        db.close(); // Closing database connection
    }

    public void update(String email, String fullName , String keywords) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(User.KEY_email,email);
        values.put(User.KEY_fullName, fullName);
        values.put(User.KEY_keywords, keywords);

        db.update(User.TABLE, values, User.KEY_email + "= ?", new String[] { email });
        db.close(); // Closing database connection
    }
}
