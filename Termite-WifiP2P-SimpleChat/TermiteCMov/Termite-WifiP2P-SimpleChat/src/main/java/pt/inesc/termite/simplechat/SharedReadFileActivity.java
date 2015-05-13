package pt.inesc.termite.simplechat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;


public class SharedReadFileActivity extends ActionBarActivity {
    TextView fileContent, fileCreatedAt, fileAuthor;
    private int _File_Id=0;
    private String _File_ip;
    FileRepo repo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_file);

        Bundle extras = getIntent().getExtras();
        _File_Id = Integer.parseInt(extras.getString("file_Id"));
        _File_ip = extras.getString("file_ip");
        String _File_title = extras.getString("file_title");

        setTitle(_File_title);

        // kolla om ipen finns


        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _File_ip);

//        fileContent = (TextView) findViewById(R.id.file_content);
//        fileCreatedAt = (TextView) findViewById(R.id.file_createdAt);
//        fileAuthor = (TextView) findViewById(R.id.file_author);
//
//        _File_Id =0;
//        Intent intent = getIntent();
//        _File_Id =intent.getIntExtra("file_Id", 0);
//
//
//        _Ws_Id = file.ws;
//        setTitle(file.title);
//
//        fileContent.setText(file.content);
//        fileCreatedAt.setText(file.createdAt);
//        fileAuthor.setText(file.author);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {

            //popup window

            repo.delete(_File_Id);
//            Intent intent = new Intent(this, SharedWorkspace.class);
//            intent.putExtra("ws_Id",_Ws_Id);
//            startActivity(intent);
        }
        else if (id == R.id.action_edit) {
//            Intent intent = new Intent(this, EditFileActivityNetwork.class);
//            intent.putExtra("file_Id",_File_Id);
//            startActivity(intent);
        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {
        SimWifiP2pSocket mCliSocket;

        @Override
        protected String doInBackground(String... params) {
            try {
                mCliSocket = new SimWifiP2pSocket(params[0],
                        Integer.parseInt(getString(R.string.port)));


                JSONObject json = new JSONObject();
                json.put("ip",params[0]);
                json.put("id",_File_Id);
                json.put("command", "getFileById");

                Log.d("Jason from request sharedreadfileactivity  ", json.toString());

                mCliSocket.getOutputStream().write((json.toString()+"\n").getBytes());

            } catch (UnknownHostException e) {
                Log.d("error ",e.getMessage());
            } catch (IOException e) {
                Log.d("error ",e.getMessage());
            } catch (JSONException e) {
                Log.d("error ",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            ReceiveCommTask mComm = null;
            mComm = new ReceiveCommTask();
            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
        }
    }

    public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, String, String> {
        SimWifiP2pSocket s;

        @Override
        protected void onPreExecute() {
        }


        @Override
        protected String doInBackground(SimWifiP2pSocket... params) {
            BufferedReader sockIn;
            String  st;

            s = params[0];
            try {
                sockIn = new BufferedReader(new InputStreamReader(s.getInputStream()));


                while ((st = sockIn.readLine()) != null) {
                    publishProgress(st);
                }
            } catch (IOException e) {
                Log.d("Error reading socket:", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            JSONObject json = null;


            try {
                json = new JSONObject(values[0]);
//


            }catch (JSONException e) {
                Log.d("error", e.getMessage());
            }


        fileContent = (TextView) findViewById(R.id.file_content);
        fileCreatedAt = (TextView) findViewById(R.id.file_createdAt);
        fileAuthor = (TextView) findViewById(R.id.file_author);

        _File_Id =0;
        Intent intent = getIntent();
        _File_Id =intent.getIntExtra("file_Id", 0);


            try {
                fileContent.setText(json.getString("content"));
                fileCreatedAt.setText(json.getString("createdAt"));
                fileAuthor.setText(json.getString("author"));
            } catch (JSONException e) {
                e.printStackTrace();
            }




        }

        @Override
        protected void onPostExecute(String result) {
            if (!s.isClosed()) {
                try {
                    s.close();
                }
                catch (Exception e) {
                    Log.d("Error closing socket:", e.getMessage());
                }
            }
            s = null;
        }
    }



}
