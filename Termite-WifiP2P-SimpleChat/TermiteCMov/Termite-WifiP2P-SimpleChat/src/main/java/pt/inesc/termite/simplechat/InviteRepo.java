package pt.inesc.termite.simplechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class InviteRepo {

    private DBHelper dbHelper;

    public InviteRepo(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void insert(ArrayList<String> inviteList, int wsID) {

        deleteWs(wsID);

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (int i = 0; i < inviteList.size(); i++){
            ContentValues values = new ContentValues();
            values.put(Invite.KEY_workspaceID, wsID);
            values.put(Invite.KEY_email, inviteList.get(i));

            // Inserting Row
            long file_Id = db.insert(Invite.TABLE, null, values);
        }
        db.close(); // Closing database connection
    }

    public ArrayList<String> getInviteListByWsId(int WsId){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Invite.KEY_email +
                " FROM " + Invite.TABLE
                + " WHERE " +
                Invite.KEY_workspaceID + "=?";

        ArrayList<String> inviteList = new ArrayList<String>();


        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(WsId) } );

        if (cursor.moveToFirst()) {
            do {
                inviteList.add(cursor.getString(cursor.getColumnIndex(Invite.KEY_email)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return inviteList;
    }

    public void deleteWs(int ws_Id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Invite.TABLE, Invite.KEY_workspaceID + "= ?", new String[] { String.valueOf(ws_Id) });
        db.close(); // Closing database connection
    }


}
