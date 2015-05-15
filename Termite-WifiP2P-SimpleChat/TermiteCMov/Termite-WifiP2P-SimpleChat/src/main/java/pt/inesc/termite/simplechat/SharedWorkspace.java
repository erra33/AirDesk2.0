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
import java.util.ArrayList;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;


public class SharedWorkspace extends ActionBarActivity {

    ArrayList<HashMap<String,String>> fileList = new ArrayList<HashMap<String, String>>();
    private int _Ws_Id=0;
    TextView file_Id;
    String _Ws_ip;
    Intent intent;

    @Override
    protected void onStart() {
        super.onStart();

      fileList = new ArrayList<HashMap<String, String>>();
        Bundle extras = intent.getExtras();
        _Ws_Id = Integer.parseInt(extras.getString("ws_Id"));
        _Ws_ip = extras.getString("ws_ip");
        String _Ws_title = extras.getString("ws_title");

        setTitle(_Ws_title);

        Log.d("Ws id  ", _Ws_Id + "");
        Log.d("Ws ip  ", _Ws_ip);


        // kolla om ipen finns

        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _Ws_ip);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_workspace);
        intent = getIntent();


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shared_workspace, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
//            Intent intent = new Intent(this,AddFileActivityNetwork.class);
//            intent.putExtra("ws_Id",_Ws_Id);
//            startActivity(intent);
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
                    json.put("id",_Ws_Id);
                    json.put("command", "getWsById");

                    Log.d("Jason  ",json.toString());

                    mCliSocket.getOutputStream().write((json.toString()+"\n").getBytes());

                }
                catch (IOException e) {

                    Log.d("error ",e.getMessage());
                    Log.d("FINISHING IN SHARED WORKSPACE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",e.getMessage());
                    return e.getMessage();
                }
                catch (JSONException e) {
                    Log.d("error ",e.getMessage());
                    return e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null){
                    ReceiveCommTask mComm = null;
                    mComm = new ReceiveCommTask();
                    mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Networking problems", Toast.LENGTH_SHORT).show();
                    finish();
                }

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
                    return e.getMessage();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {

                JSONObject json = null;


                try {
                    json = new JSONObject(values[0]);
                    JSONArray JsonList = json.getJSONArray("fileList");

                    setTitle(json.getString("ws_title"));



//                Log.d("json",json+"");
//                Log.d("jsonList",JsonList+"");
//                Log.d("längdpåjson",JsonList.length()+"");
//
                    for(int i = 0; i<JsonList.length(); i++){

                        String value = JsonList.getString(i);
                        Log.d("VALUE IS ", value);
                        value = value.substring(1, value.length()-1);
                        String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
                        HashMap<String,String> map = new HashMap<>();

                        map.put("ip",json.getString("ip"));
                        for(String pair : keyValuePairs)                        //iterate over the pais
                        {
                            String[] entry = pair.split("=");                   //split the pairs to get key and value
                            map.put(entry[0].trim(), entry[1].trim());          //add them to the hashmap
                        }

                        fileList.add(map);
                    }

                }catch (JSONException e) {
                    Log.d("error", e.getMessage());
                }


        if(fileList.size()!=0) {
            ListView lv = (ListView) findViewById(R.id.list);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                    TextView file_Id = (TextView) view.findViewById(R.id.ws_Id);
                    TextView file_Ip = (TextView) view.findViewById(R.id.ws_ip);
                    TextView file_title = (TextView) view.findViewById(R.id.ws_title);

                    String fileId = file_Id.getText().toString();
                    String fileip = file_Ip.getText().toString();
                    String fileTitle = file_title.getText().toString();


                    Intent objIndent = new Intent(getApplicationContext(),SharedReadFileActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("file_Id", fileId);
                    extras.putString("file_ip", fileip);
                    extras.putString("file_title", fileTitle);
                    objIndent.putExtras(extras);
                    Log.d("efter intent", "tjo");
                    startActivity(objIndent);


                }});
            ListAdapter adapter = new SimpleAdapter( SharedWorkspace.this,fileList, R.layout.view_ws_share, new String[] { "id","ip","title"}, new int[] {R.id.ws_Id, R.id.ws_ip, R.id.ws_title});
            lv.setAdapter(adapter);
        }
        else{
            Toast.makeText(getApplicationContext(), "No files!", Toast.LENGTH_SHORT).show();
        }



            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null) {
                    if (!s.isClosed()) {
                        try {
                            s.close();
                        } catch (Exception e) {
                            Log.d("Error closing socket:", e.getMessage());
                        }
                    }
                    s = null;
                }
                else{
                    Toast.makeText(getApplicationContext(), "Networking problems", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

    }
