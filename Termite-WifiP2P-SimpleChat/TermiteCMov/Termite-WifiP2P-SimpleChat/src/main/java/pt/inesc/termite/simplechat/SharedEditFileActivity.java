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
import android.widget.Button;
import android.widget.EditText;
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


public class SharedEditFileActivity extends ActionBarActivity {
    TextView fileContent, fileCreatedAt, fileAuthor;
    private int _File_Id=0;
    private String _File_ip;
    FileRepo repo;
    User user;

    String startTitle;
    String startContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_file);

        Bundle extras = getIntent().getExtras();
        _File_Id = Integer.parseInt(extras.getString("file_Id"));
        _File_ip = extras.getString("file_ip");


        Log.d("file id",_File_Id+"");

        //Check if it's any user reg
        user = new User(this);
        user.getUser();
        Button b = (Button) findViewById(R.id.saveBtn);
        b.setEnabled(true); // fix check for real

        // kolla om ipen finns


        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _File_ip);

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
                json.put("doLock",true);
                json.put("user",user.email);


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

            Log.d("Jason on recive ",values[0]);

            JSONObject json = null;
            try {
                json = new JSONObject(values[0]);

            }catch (JSONException e) {
                Log.d("error", e.getMessage());
            }


            try {


                if (json.getString("lockedBy").equals(user.email)) {
                    final EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
                    EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
                    editTextContent.setText(json.getString("content"));
                    startContent = json.getString("content");
                    startTitle = json.getString("file_title");
                    editTextTitle.setText(json.getString("file_title"));

                }
                else {
                    Toast.makeText(getApplicationContext(), "File locked by " + json.getString("lockedBy")+"!", Toast.LENGTH_SHORT).show();
                    finish();
                }
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

    // Called when the user clicks the cancel button
    public void cancel(View view) {
        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
        editTextTitle.setText(startTitle);
        editTextContent.setText(startContent);
        new OutgoingCommTaskSendButton().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _File_ip);
    }

    public void saveFile(View view) { //Not really save but tied to button

        new OutgoingCommTaskSendButton().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _File_ip);
//
//            FileRepo repo = new FileRepo(this);
//            File file = repo.getFileById(_File_Id);
//
//            EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
//            EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
//            file.title = editTextTitle.getText().toString();
//            file.content = editTextContent.getText().toString();
//
//            if (!file.title.equals(originalTitle) && repo.existsAFileWithTitleInWorkspace(file.title,file.ws)){
//                Toast.makeText(this, "File with the same name already exists.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            WorkspaceRepo wsRepo = new WorkspaceRepo(this);
//            Workspace workspace = wsRepo.getWorkspaceById(file.ws);
//            int oldSize = file.size;
//            file.setSize();
//            if(workspace.sizeLimit-repo.getFileSizes(file.ws)+oldSize >= file.size) {
//                repo.update(file);
//                Toast.makeText(this, "File updated", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(this, MyWorkspaceActivity.class);
//                intent.putExtra("ws_Id",file.ws);
//                startActivity(intent);
//            }
//            else{
//                Toast.makeText(this, "File is too big.", Toast.LENGTH_SHORT).show();
//            }
//
//
    }

    public class OutgoingCommTaskSendButton extends AsyncTask<String, Void, String> {
        SimWifiP2pSocket mCliSocket;

        @Override
        protected String doInBackground(String... params) {
            try {
                mCliSocket = new SimWifiP2pSocket(params[0],
                        Integer.parseInt(getString(R.string.port)));
                EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
                EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
                String title = editTextTitle.getText().toString();
                String content = editTextContent.getText().toString();

                JSONObject json = new JSONObject();
                json.put("ip",params[0]);
                json.put("id",_File_Id);
                json.put("command", "editFileUpdateByID");
                json.put("user",user.email);
                json.put("content", content);
                json.put("title", title);

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
//            ReceiveCommTaskSendButton mComm = null;
//            mComm = new ReceiveCommTaskSendButton();
//            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
            if (!mCliSocket.isClosed()) {
                try {
                    mCliSocket.close();
                }
                catch (Exception e) {
                    Log.d("Error closing socket:", e.getMessage());
                }
            }
            mCliSocket = null;
            finish();
            return;
        }
    }

    @Override
    public void onBackPressed() {

        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
        editTextTitle.setText(startTitle);
        editTextContent.setText(startContent);
        new OutgoingCommTaskSendButton().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _File_ip);

        finish();
    }

}
