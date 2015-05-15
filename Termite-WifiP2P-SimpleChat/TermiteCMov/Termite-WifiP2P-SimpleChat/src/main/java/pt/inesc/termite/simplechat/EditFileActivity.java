package pt.inesc.termite.simplechat;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;


public class EditFileActivity extends ActionBarActivity implements SimWifiP2pManager.GroupInfoListener {

    private int _File_Id=0;
    private String originalTitle;
    File file;
    Intent intent;
    boolean userWithLockIsAway = true;
    FileRepo repo;
    User user;
    int devicesCounter;
    int networksDevices;

    @Override
    protected void onStart(){
        super.onStart();
        setContentView(R.layout.activity_add_file);

        setTitle("Edit File");
        user = new User(this);

        intent = getIntent();
        _File_Id = intent.getIntExtra("file_Id", 0);
        repo = new FileRepo(this);
        file = repo.getFileById(_File_Id);


        Log.d("locke by ",file.lockedBy );
        if (file.lockedBy.equals(user.getUser().email) || file.lockedBy.equals("Unlocked") || (MainActivity.mManager == null)){
            file.lockedBy = user.email;
            repo.update(file);

            final EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
            EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
            editTextContent.setText(file.content);
            editTextTitle.setText(file.title);

            originalTitle = file.title;

            ((Button) findViewById(R.id.saveBtn)).setEnabled(true); //Ok to save without making any edits
            TextWatcher tW = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void afterTextChanged(Editable s) {
                    Button b = (Button) findViewById(R.id.saveBtn);
                    if (!editTextTitle.getText().toString().isEmpty()) {
                        b.setEnabled(true);
                    } else b.setEnabled(false);
                }
            };
            editTextTitle.addTextChangedListener(tW);

        }else {
            Toast.makeText(getApplicationContext(), "This file is locked by " + file.lockedBy +"!", Toast.LENGTH_SHORT).show();
            MainActivity.mManager.requestGroupInfo(MainActivity.mChannel, (SimWifiP2pManager.GroupInfoListener) this);
            final EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
            EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
            editTextContent.setText(file.content);
            editTextTitle.setText(file.title);
            editTextContent.setEnabled(false);
            editTextTitle.setEnabled(false);
            Button bSave = (Button) findViewById(R.id.saveBtn);
            Button bCancel = (Button) findViewById(R.id.cancelBtn);
            bSave.setEnabled(false);
            bCancel.setEnabled(false);
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Called when the user clicks the save button
    public void saveFile(View view) {

        FileRepo repo = new FileRepo(this);
        file = repo.getFileById(_File_Id);

        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
        file.title = editTextTitle.getText().toString();
        file.content = editTextContent.getText().toString();

        if (!file.title.equals(originalTitle) && repo.existsAFileWithTitleInWorkspace(file.title,file.ws)){
            Toast.makeText(this, "File with the same name already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        WorkspaceRepo wsRepo = new WorkspaceRepo(this);
        Workspace workspace = wsRepo.getWorkspaceById(file.ws);
        int oldSize = file.size;
        file.setSize();
        if(workspace.sizeLimit-repo.getFileSizes(file.ws)+oldSize >= file.size) {
            repo.update(file);
            Toast.makeText(this, "File updated", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MyWorkspaceActivity.class);
            intent.putExtra("ws_Id",file.ws);
            file.lockedBy = "Unlocked";
            repo.update(file);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "File is too big.", Toast.LENGTH_SHORT).show();
        }


    }

    // Called when the user clicks the cancel button
    public void cancel(View view) {
        Intent intent = new Intent(this, ReadFileActivity.class);
        intent.putExtra("file_Id",_File_Id);
        file.lockedBy = "Unlocked";
        repo.update(file);
        startActivity(intent);
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        // compile list of network members
        StringBuilder peersStr = new StringBuilder();

        networksDevices = groupInfo.getDevicesInNetwork().size();
        Log.d("CHANGIN NETWORKDEVICES NOW ", networksDevices + "");
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device.getVirtIp() + "");
            }

    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {
        SimWifiP2pSocket mCliSocket;

        @Override
        protected String doInBackground(String... params) {
            try {
                mCliSocket = new SimWifiP2pSocket(params[0],
                        Integer.parseInt(getString(R.string.port)));

                Log.d("någoting", params[0]);

                JSONObject json = new JSONObject();
                json.put("ip", params[0]);
                json.put("command", "getEmail");

                mCliSocket.getOutputStream().write((json.toString()+"\n").getBytes());

            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            } catch (JSONException e) {
                return e.getMessage();
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
                 Log.d("get email ", json.getString("email"));
                 if(json.getString("email").equals(file.lockedBy)) {
                     userWithLockIsAway = false;
                 }

                 devicesCounter++;
                    //Toast.makeText(getApplicationContext(), "devicesCounter " + devicesCounter +"!", Toast.LENGTH_SHORT).show();
                    Log.d("DevicesCounter is ", " devicesCounter" + " " + networksDevices);
                 if(devicesCounter == networksDevices && userWithLockIsAway == true)  {
                     Toast.makeText(getApplicationContext(), "Now it's unlocked ", Toast.LENGTH_SHORT).show();
                     file.lockedBy = "Unlocked";
                     repo.update(file);
                 }else{
                     Toast.makeText(getApplicationContext(), "It's still locked ", Toast.LENGTH_SHORT).show();
                 }

                }catch (JSONException e) {
                 Log.d("error", e.getMessage());
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

    @Override
    public void onBackPressed() {
       if (file.lockedBy.equals(user.email)){
           file.lockedBy = "Unlocked";
           FileRepo repo = new FileRepo(this);
           repo.update(file);
           finish();
       }
        else  finish();
    }



}
