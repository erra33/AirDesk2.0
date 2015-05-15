package pt.inesc.termite.simplechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class FileRepo {
    private DBHelper dbHelper;

    public FileRepo(Context context) {
        dbHelper = new DBHelper(context);
    }

    public int insert(File file) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(File.KEY_content,file.content);
        values.put(File.KEY_title, file.title);
        values.put(File.KEY_author, file.author);

        java.util.Date date= new java.util.Date();
        String timestamp = new Timestamp(date.getTime()).toString();
        values.put(File.KEY_createdAt, timestamp);
        values.put(File.KEY_ws, file.ws);
        values.put(File.KEY_size, file.size);
        values.put(File.KEY_locked_by, "Unlocked");


        // Inserting Row
        long file_Id = db.insert(File.TABLE, null, values);
        db.close(); // Closing database connection
        return (int) file_Id;
    }

    public void delete(int file_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(File.TABLE, File.KEY_ID + "= ?", new String[] { String.valueOf(file_Id) });
        db.close(); // Closing database connection
    }

    public void update(File file) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(File.KEY_content,file.content);
        values.put(File.KEY_title, file.title);
        values.put(File.KEY_author, file.author);
        values.put(File.KEY_size, file.size);
        values.put(File.KEY_locked_by, file.lockedBy);

        db.update(File.TABLE, values, File.KEY_ID + "= ?", new String[] { String.valueOf(file.file_ID) });
        db.close(); // Closing database connection
    }

    public ArrayList<HashMap<String, String>>  getFileList(int ws) {
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT " +
                File.KEY_ID + "," +
                File.KEY_title +
                " FROM " + File.TABLE +
                " WHERE " +
                File.KEY_ws + "=?";

        //File file = new File();
        ArrayList<HashMap<String, String>> fileList = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(ws) });
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> file = new HashMap<String, String>();
                file.put("id", cursor.getString(cursor.getColumnIndex(File.KEY_ID)));
                file.put("title", cursor.getString(cursor.getColumnIndex(File.KEY_title)));
                fileList.add(file);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return fileList;

    }

    public File getFileById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " +
                File.TABLE
                + " WHERE " +
                File.KEY_ID + "=?";

        File file = new File();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {
                file.file_ID =cursor.getInt(cursor.getColumnIndex(File.KEY_ID));
                file.title =cursor.getString(cursor.getColumnIndex(File.KEY_title));
                file.content =cursor.getString(cursor.getColumnIndex(File.KEY_content));
                file.author =cursor.getString(cursor.getColumnIndex(File.KEY_author));
                file.createdAt =cursor.getString(cursor.getColumnIndex(File.KEY_createdAt));
                file.ws =cursor.getInt(cursor.getColumnIndex(File.KEY_ws));
                file.size = cursor.getInt(cursor.getColumnIndex(File.KEY_size));
                file.lockedBy = cursor.getString(cursor.getColumnIndex(File.KEY_locked_by));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return file;
    }

    public boolean existsAFileWithTitleInWorkspace(String title,int wsId){
        boolean toReturn = true;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " +
                File.TABLE
                + " WHERE " +
                File.KEY_title + "=?"
                + " AND " +
                File.KEY_ws + "=?";

        File file = new File();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { title, String.valueOf(wsId) } );

        if(cursor.getCount() <= 0){
            toReturn = false;
        }

        cursor.close();
        db.close();

        return toReturn;
    }

    public int getFileSizes(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT " +
                File.KEY_size +
                " FROM " +
                File.TABLE +
                " WHERE " +
                File.KEY_ws + "=?";


        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );
        int fileSizes = 0;
        if (cursor.moveToFirst()) {
            do {
                fileSizes = fileSizes + cursor.getInt(cursor.getColumnIndex(File.KEY_size));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return fileSizes;
    }

}
