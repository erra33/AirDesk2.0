package pt.inesc.termite.simplechat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;


public class SharedAddFileActivity extends ActionBarActivity {
    TextView fileContent, fileCreatedAt, fileAuthor;
    private String _File_ip;
    private int _workspace_id;
    FileRepo repo;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_file);

        Bundle extras = getIntent().getExtras();
        _File_ip = extras.getString("file_ip");
        _workspace_id = extras.getInt("ws_id");


        Log.d("SharedAddFileActivity + ",_File_ip+"");

        Button b = (Button) findViewById(R.id.saveBtn);
        b.setEnabled(true); // fix check for real

        //new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _File_ip);

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


    // Called when the user clicks the cancel button
    public void cancel(View view) {
        finish();
    }

    public void saveFile(View view) { //Not really save but tied to button

        new OutgoingCommTaskSendButton().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _File_ip);
    }

    public class OutgoingCommTaskSendButton extends AsyncTask<String, Void, String> {
        SimWifiP2pSocket mCliSocket;

        @Override
        protected String doInBackground(String... params) {
            try {
                user = new User(getApplicationContext());
                user.getUser();

                mCliSocket = new SimWifiP2pSocket(params[0],
                        Integer.parseInt(getString(R.string.port)));
                EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
                EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
                String title = editTextTitle.getText().toString();
                String content = editTextContent.getText().toString();

                JSONObject json = new JSONObject();
                json.put("ip",params[0]);
                json.put("wsid",_workspace_id);
                json.put("command", "addFile");
                json.put("user",user.email);
                json.put("content", content);
                json.put("title", title);

                Log.d("Jason from request addFile  ", json.toString());

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

//        @Override
//        protected void onPostExecute(String result) {
////            ReceiveCommTaskSendButton mComm = null;
////            mComm = new ReceiveCommTaskSendButton();
////            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
//            if (!mCliSocket.isClosed()) {
//                try {
//                    mCliSocket.close();
//                }
//                catch (Exception e) {
//                    Log.d("Error closing socket:", e.getMessage());
//                }
//            }
//            mCliSocket = null;
//            finish();
//            return;
//        }
    }

    public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, String, String> {
        SimWifiP2pSocket s;

        @Override
        protected void onPreExecute() {
        }


        @Override
        protected String doInBackground(SimWifiP2pSocket... params) {
            BufferedReader sockIn;
            String st;

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

            Log.d("Jason on recive ", values[0]);

            JSONObject json = null;
            try {
                json = new JSONObject(values[0]);

            } catch (JSONException e) {
                Log.d("error", e.getMessage());
            }


            try {


                if (json.getString("status").equals("accepted")) {
                    Toast.makeText(getApplicationContext(), "File added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (json.getString("status").equals("size")) {
                    Toast.makeText(getApplicationContext(), "File too big!", Toast.LENGTH_SHORT).show();
                }
                else if (json.getString("status").equals("duplicateTitle")) {
                    Toast.makeText(getApplicationContext(), "Duplicate file title!", Toast.LENGTH_SHORT).show();
                }

//                else if(json.getString("status").equals("deleted")){
//                    Toast.makeText(getApplicationContext(), "Workspace no longer exists!", Toast.LENGTH_SHORT).show();
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            if (!s.isClosed()) {
                try {
                    s.close();
                } catch (Exception e) {
                    Log.d("Error closing socket:", e.getMessage());
                }
            }
            s = null;
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
