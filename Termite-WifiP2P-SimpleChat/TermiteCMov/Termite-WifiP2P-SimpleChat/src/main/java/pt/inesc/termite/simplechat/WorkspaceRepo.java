package pt.inesc.termite.simplechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class WorkspaceRepo {
        private DBHelper dbHelper;

        public WorkspaceRepo(Context context) {
            dbHelper = new DBHelper(context);
        }

        public int insert(Workspace ws) {

            //Open connection to write data
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Workspace.KEY_title, ws.title);
            java.util.Date date= new java.util.Date();
            String timestamp = new Timestamp(date.getTime()).toString();
            values.put(Workspace.KEY_createdAt, timestamp);
//            values.put(Workspace.KEY_public, ws.publicWs);
            values.put(Workspace.KEY_sizeLimit, ws.sizeLimit);


            // Inserting Row
            long ws_Id = db.insert(Workspace.TABLE, null, values);
            db.close(); // Closing database connection
            return (int) ws_Id;
        }

        public void delete(int ws_Id) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(Keywords.TABLE, Keywords.KEY_workspaceID + "= ?", new String[] { String.valueOf(ws_Id) });
            db.delete(Invite.TABLE, Invite.KEY_workspaceID + "= ?", new String[] { String.valueOf(ws_Id) });
            db.delete(File.TABLE, File.KEY_ws + "= ?", new String[] { String.valueOf(ws_Id) });
            db.delete(Workspace.TABLE, Workspace.KEY_ID + "= ?", new String[] { String.valueOf(ws_Id) });
            db.close(); // Closing database connection
        }

        public int update(Workspace ws) {

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(Workspace.KEY_title, ws.title);
            values.put(Workspace.KEY_sizeLimit, ws.sizeLimit);

            db.update(Workspace.TABLE, values, Workspace.KEY_ID + "= ?", new String[] { String.valueOf(ws.ws_ID) });
            db.close(); // Closing database connection
            return ws.ws_ID;
        }

        public ArrayList<HashMap<String, String>> getWorkspaceList() {
            //Open connection to read only
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selectQuery =  "SELECT  " +
                    Workspace.KEY_ID + "," +
                    Workspace.KEY_title +
                    " FROM " + Workspace.TABLE;

            //Workspace ws = new Workspace();
            ArrayList<HashMap<String, String>> wsList = new ArrayList<HashMap<String, String>>();

            Cursor cursor = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list

            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> ws = new HashMap<String, String>();
                    ws.put("id", cursor.getString(cursor.getColumnIndex(Workspace.KEY_ID)));
                    ws.put("title", cursor.getString(cursor.getColumnIndex(Workspace.KEY_title)));
                    wsList.add(ws);

                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return wsList;

        }

        public Workspace getWorkspaceById(int Id){
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selectQuery =  "SELECT  " +
                    Workspace.KEY_ID + "," +
                    Workspace.KEY_title + "," +
                    Workspace.KEY_createdAt + "," +
                    Workspace.KEY_sizeLimit +
                    " FROM " + Workspace.TABLE
                    + " WHERE " +
                    Workspace.KEY_ID + "=?";

            Workspace ws = new Workspace();

            Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

            if (cursor.moveToFirst()) {
                do {
                    ws.ws_ID =cursor.getInt(cursor.getColumnIndex(Workspace.KEY_ID));
                    ws.title =cursor.getString(cursor.getColumnIndex(Workspace.KEY_title));
                    ws.createdAt =cursor.getString(cursor.getColumnIndex(Workspace.KEY_createdAt));
                    ws.sizeLimit =cursor.getInt(cursor.getColumnIndex(Workspace.KEY_sizeLimit));

                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return ws;
        }


}
