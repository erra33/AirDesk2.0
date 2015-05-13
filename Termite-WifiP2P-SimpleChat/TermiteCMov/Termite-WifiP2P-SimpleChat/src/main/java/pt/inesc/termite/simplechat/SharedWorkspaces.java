package pt.inesc.termite.simplechat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;


public class SharedWorkspaces extends ActionBarActivity implements SimWifiP2pManager.GroupInfoListener {


    User user;
    ArrayList<String> peersArray=new ArrayList<String>();
    ArrayList<HashMap<String,String>> wsList = new ArrayList<HashMap<String, String>>();

    TextView ws_Id;
    TextView ws_ip;
    TextView ws_title;


    GlobalVariable bound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_workspaces);


        MainActivity.mManager.requestGroupInfo(MainActivity.mChannel, (SimWifiP2pManager.GroupInfoListener) this);

        user = new User(this);
        user.getUser();

    }


    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            peersArray.add(device.getVirtIp());
            Log.d("peerArray", peersArray+"");
            new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device.getVirtIp() + "");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shared_workspaces, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
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
                json.put("user", user.email);
                json.put("ip", params[0]);
                json.put("command", "getWs");

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
                JSONArray JsonList = json.getJSONArray("WsLists");
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

                    wsList.add(map);
                }

            }catch (JSONException e) {
                Log.d("error", e.getMessage());
            }


            if(wsList.size()!=0) {
                ListView lv = (ListView) findViewById(R.id.list);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                        ws_Id = (TextView) view.findViewById(R.id.ws_Id);
                        ws_ip = (TextView) view.findViewById(R.id.ws_ip);
                        ws_title = (TextView) view.findViewById(R.id.ws_title);
                        String wsId = ws_Id.getText().toString();
                        String wsip = ws_ip.getText().toString();
                        String wstitle = ws_title.getText().toString();

                        Log.d("innan intent", "tjo");

                        Intent objIndent = new Intent(getApplicationContext(),SharedWorkspace.class);
                        Bundle extras = new Bundle();
                        extras.putString("ws_Id", wsId);
                        extras.putString("ws_ip", wsip);
                        extras.putString("ws_title", wstitle);
                        objIndent.putExtras(extras);
                        Log.d("efter intent", "tjo");
                        startActivity(objIndent);
                    }});
                ListAdapter adapter = new SimpleAdapter(SharedWorkspaces.this,wsList, R.layout.view_ws_share, new String[] { "id","ip","title"}, new int[] {R.id.ws_Id,R.id.ws_ip ,R.id.ws_title});
                lv.setAdapter(adapter);
            }
            else{
                Toast.makeText(getApplicationContext(), "No workspaces!", Toast.LENGTH_SHORT).show();
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
