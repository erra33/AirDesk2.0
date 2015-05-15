package pt.inesc.termite.simplechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class InviteRepo {
    SQLiteDatabase db;
    private DBHelper dbHelper;

    public InviteRepo(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void insert(ArrayList<String> inviteList, int wsID, String keyword, int isInvitedForAll) {


        //if (isInvitedForAll == 1) deleteWsOnlyInvited(wsID);

        //Open connection to write data
        db = dbHelper.getWritableDatabase();

        for (int i = 0; i < inviteList.size(); i++) {
            if (existsInviteWithEmailInWorkspace(inviteList.get(i), wsID)) continue;
            ContentValues values = new ContentValues();
            values.put(Invite.KEY_workspaceID, wsID);
            values.put(Invite.KEY_email, inviteList.get(i));
            values.put(Invite.KEY_keyword, keyword);
            values.put(Invite.KEY_invited, isInvitedForAll);


            // Inserting Row
            long file_Id = db.insert(Invite.TABLE, null, values);
        }
        db.close(); // Closing database connection
    }

    public ArrayList<String> getInviteListByWsId(int WsId) {
        db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT  " +
                Invite.KEY_email + "," +
                Invite.KEY_invited +
                " FROM " + Invite.TABLE
                + " WHERE " +
                Invite.KEY_workspaceID + "=?";

        ArrayList<String> inviteList = new ArrayList<String>();


        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(WsId)});

        if (cursor.moveToFirst()) {
            do {
//                inviteList.add(cursor.getString(cursor.getColumnIndex(Invite.KEY_email))+ "  " + cursor.getInt(cursor.getColumnIndex(Invite.KEY_invited)));
                inviteList.add(cursor.getString(cursor.getColumnIndex(Invite.KEY_email)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return inviteList;
    }

    private boolean existsInviteWithEmailInWorkspace(String email, int wsId) {
        boolean toReturn = true;
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " +
                Invite.TABLE
                + " WHERE " +
                Invite.KEY_email + "=?"
                + " AND " +
                Invite.KEY_workspaceID + "=?"
                + " AND " +
                Invite.KEY_invited + "=1";

        File file = new File();

        Cursor cursor = db.rawQuery(selectQuery, new String[]{email, String.valueOf(wsId)});

        if (cursor.getCount() <= 0) {
            toReturn = false;
        }

        cursor.close();
//        db.close();

        return toReturn;
    }


    public void deleteWs(int ws_Id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Invite.TABLE, Invite.KEY_workspaceID + "= ?", new String[]{String.valueOf(ws_Id)});
        db.close(); // Closing database connection
    }

    public void deleteWsOnlyInvited(int ws_Id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Invite.TABLE, Invite.KEY_workspaceID + "= ? AND " + Invite.KEY_invited + "=1", new String[]{String.valueOf(ws_Id)});
        db.close(); // Closing database connection
    }

    public void deletByEmailAndWsID(String email, int ws_Id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Invite.TABLE, Invite.KEY_workspaceID + "= ? AND " + Invite.KEY_email + "=?", new String[]{String.valueOf(ws_Id), email});
        db.close(); // Closing database connection
    }

    public void deleteByEmail(String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Invite.TABLE, Invite.KEY_email + "=? AND " + Invite.KEY_invited + "=0", new String[]{email});
        db.close(); // Closing database connection
    }

    public void addNewKeyword(String email, String keyword, int wsId) {

        db=dbHelper.getWritableDatabase();
        String insertQuery = "INSERT INTO " +
            Invite.TABLE +
            " (" + Invite.KEY_invited + "," + Invite.KEY_keyword + "," + Invite.KEY_workspaceID + "," + Invite.KEY_email +
            ") SELECT 0,?,?,? WHERE NOT EXISTS(SELECT 1 FROM "+ Invite.TABLE +
            " WHERE "+ Invite.KEY_email + "=? AND " + Invite.KEY_keyword + "=? AND "+ Invite.KEY_workspaceID+"=?)";

        db.execSQL(insertQuery, new String[]{keyword, String.valueOf(wsId), email, email, keyword, String.valueOf(wsId)});
    }

    public void removeOldKeywords(String email, ArrayList<String> keywords) {

        db=dbHelper.getReadableDatabase();
        String selectQuery = "SELECT " +
                Invite.KEY_keyword + " FROM " + Invite.TABLE + " WHERE " + Invite.KEY_email +" =? AND " + Invite.KEY_invited +"=0" ;

        Cursor cursor = db.rawQuery(selectQuery, new String[]{email});



        if (cursor.moveToFirst()) {
            do {
                String oldKeyword = cursor.getString(cursor.getColumnIndex(Invite.KEY_keyword));
                if (!keywords.contains(oldKeyword)){
                    Log.d("Borde ta bord old keyword", oldKeyword + "");
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete(Invite.TABLE, Invite.KEY_email + "=? AND " + Invite.KEY_keyword + "=?", new String[]{email,oldKeyword});
                    db.close(); // Closing database connection
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }
}