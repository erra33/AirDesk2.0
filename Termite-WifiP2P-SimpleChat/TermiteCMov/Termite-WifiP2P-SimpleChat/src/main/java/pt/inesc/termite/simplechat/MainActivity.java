package pt.inesc.termite.simplechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.GroupInfoListener;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity implements
		PeerListListener, GroupInfoListener {

    public static final String TAG = "simplechat";


    public static SimWifiP2pManager mManager = null;
    public static Channel mChannel = null;
    public static Messenger mService = null;

    public static IncommingCommTask IncomeMsg;
    public static SimWifiP2pSocketServer mSrvSocket = null;

    public SimWifiP2pSocket mCliSocket2 = null;
    public ReceiveCommTaskIncome mCommListener = null;

    public ReceiveCommTask mComm = null;
    public SimWifiP2pSocket mCliSocket = null;

    public static ArrayList<String> peersArrey;

    private static boolean mHasItRun = false;
    GlobalVariable bound = null;

    User user;

	public SimWifiP2pManager getManager() {
		return mManager;
	}
	
	public Channel getChannel() {
		return mChannel;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        peersArrey = new ArrayList<String>();
        bound = (GlobalVariable) getApplicationContext();



        // initialize the UI
        setContentView(R.layout.activity_main);


        //Check if it's any user reg
        user = new User(this);
        user.getUser();

        if (user.email == null) {
            Intent intentRegUser = new Intent(this, RegisterUserActivity.class);
            startActivity(intentRegUser);
        }


        if(!mHasItRun) {

            // initialize the WDSim API
            SimWifiP2pSocketManager.Init(getApplicationContext());

             // register broadcast receiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
            filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
            filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
            filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
            SimWifiP2pBroadcastReceiver receiver = new SimWifiP2pBroadcastReceiver(this);
            registerReceiver(receiver, filter);

            Intent intent = new Intent(getApplication(), SimWifiP2pService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            // spawn the chat server background task
            IncomeMsg = new IncommingCommTask();
            IncomeMsg.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            mHasItRun = true;
        }


        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);


	}

    // Called when the user clicks the myWs button
    public void startMyWs(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
//        Intent intent = new Intent(this, MyWorkspacesActivity.class);
//        startActivity(intent);
    }

    // Called when the user clicks the sharedWs button
    public void startSharedWs(View view) {
        Intent intent = new Intent(this, SharedWorkspaces.class);
        startActivity(intent);
    }

    // Called when the user clicks the connect button
    public void connect(View view) {
        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, peersArrey.get(0) + "");
    }

    // Called when the user clicks the new button
    public void button(View view) {

    }


    // Called when the user clicks the disconnect button
    public void disconnect(View view) {
        if (mCliSocket != null) {
            try {
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCliSocket = null;
        if (mCliSocket2 != null) {
            try {
                mCliSocket2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCliSocket2 = null;
    }


    public void send(View view) {
        Toast.makeText(getApplicationContext(), "Send ",Toast.LENGTH_SHORT).show();
        try {
            mCliSocket.getOutputStream().write( ("2\n").getBytes());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),e.getMessage() +" error",Toast.LENGTH_SHORT).show();
        }

    }

    public void networkGroup(View view) {
        mManager.requestGroupInfo(mChannel, (GroupInfoListener) MainActivity.this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
        }
    };

/*
	 * Listeners associated to WDSim
	 */

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        StringBuilder peersStr = new StringBuilder();

        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            String devstr = "" + device.deviceName + " (" + device.getVirtIp() + ")\n";
            peersStr.append(devstr);
        }

        // display list of devices in range
        new AlertDialog.Builder(this)
                .setTitle("Devices in WiFi Range")
                .setMessage(peersStr.toString())
                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
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
            peersStr.append(devstr);
            peersArrey.add(device.getVirtIp());
        }



        // display list of network members
        new AlertDialog.Builder(this)
                .setTitle("Devices in WiFi Network")
                .setMessage(peersStr.toString())
                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }


	/*
	 *  Classes implementing chat message exchange
	 */


	public class OutgoingCommTask extends AsyncTask<String, Void, String> {


		@Override
		protected String doInBackground(String... params) {
			try {
				mCliSocket = new SimWifiP2pSocket(params[0],
						Integer.parseInt(getString(R.string.port)));

                mCliSocket.getOutputStream().write( ("1\n").getBytes());

			} catch (UnknownHostException e) {
				return "Unknown Host:" + e.getMessage();
			} catch (IOException e) {
				return "IO error:" + e.getMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
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

                Log.d("Error tjo", sockIn + "");


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


    public class IncommingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        Integer.parseInt(getString(R.string.port)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Log.d("innan","tomstreng");
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    Log.d("efter","tomstreng");
                    if (mCliSocket2 != null && mCliSocket2.isClosed()) {
                        mCliSocket2 = null;
                    }
                    if (mCliSocket2 != null) {
                        Log.d(TAG, "Closing accepted socket because mCliSocket still active.");
                        sock.close();
                    } else {
                        publishProgress(sock);
                    }
                } catch (IOException e) {
                    Log.d("Error accepting socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            ;
            return null;
        }

        @Override
        protected void onProgressUpdate(SimWifiP2pSocket... values) {
            mCliSocket2 = values[0];
            mCommListener = new ReceiveCommTaskIncome();
            mCommListener.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket2);
        }
    }

    public class ReceiveCommTaskIncome extends AsyncTask<SimWifiP2pSocket, String, String> {
        SimWifiP2pSocket s;

        @Override
        protected void onPreExecute() {
            Log.d("","");
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

                    Log.d("fåt in json", st+"");

                    JSONObject json = new JSONObject(st);
                    String command = json.getString("command");
                    String ip = json.getString("ip");


                    WorkspaceRepo repoWs =  new WorkspaceRepo(getApplicationContext());

                    if (command.compareToIgnoreCase("getWs") == 0) {
                        String userRemote = json.getString("user");

                        ArrayList<HashMap<String, String>> wsList = repoWs.getWorkspaceListByEmail(userRemote);


                        JSONArray jsonList = new JSONArray();

                        for(int i=0; i < wsList.size() ; i++){
                           jsonList.put(wsList.get(i));
                           Log.d("wslist", wsList.get(i)+"");

                        }
                        json = new JSONObject();

                        try{
                            json.put("ip", ip);
                            json.put("myName", user.email);
                            json.put("myName", this);

                            json.put("WsLists",jsonList);
                        }catch (JSONException e){

                        }

                        try {
                            s.getOutputStream().write((json.toString() + "\n").getBytes());
                            s.getOutputStream().flush();
                            s.getOutputStream().close();

                        } catch (IOException e) {
                        }
                    }
                    else if (command.compareToIgnoreCase("getWsById") == 0) {

                        Log.d("Inne i getwsbyID","pidpfkåsfsdåp");

                        int id = json.getInt("id");

                        Log.d("Inne i getwsbyID",id+"");


                        Workspace ws = repoWs.getWorkspaceById(id);

                        Log.d("Inne i getwsbyID",ws+"");

                        FileRepo repo = new FileRepo(getApplicationContext());
                        ArrayList<HashMap<String, String>> fileList =  repo.getFileList(id);

                        JSONArray jsonList = new JSONArray();

                        for(int i=0; i < fileList.size() ; i++){
                            jsonList.put(fileList.get(i));
                        }
                        json = new JSONObject();
                        json.put("ws_id",ws.ws_ID);
                        json.put("ws_title",ws.title);
                        json.put("ip",ip);
                        json.put("fileList",jsonList);



                        try {
                            s.getOutputStream().write((json.toString() + "\n").getBytes());
                            s.getOutputStream().flush();
                            s.getOutputStream().close();

                        } catch (IOException e) {
                            Log.d("Try send  ", e.getMessage());
                        }

                    }

                    else if (command.compareToIgnoreCase("getFileById") == 0) {

                        Log.d("Inne i getFilebyID","pidpfkåsfsdåp");

                        int id = json.getInt("id");

                        Log.d("Inne i getFilebyID",id+"");


                        FileRepo repoFile = new FileRepo(getApplicationContext());
                        File file = repoFile.getFileById(id);

                        Log.d("Inne i getFilebyID",file+"");

                        json = new JSONObject();
                        json.put("file_id",file.file_ID);
                        json.put("file_title",file.title);
                        json.put("ip",ip);
                        json.put("content",file.content);
                        json.put("author",file.author);
                        json.put("createdAt", file.createdAt);
                        json.put("size",file.size);

                        try {
                            s.getOutputStream().write((json.toString() + "\n").getBytes());
                            s.getOutputStream().flush();
                            s.getOutputStream().close();

                        } catch (IOException e) {
                            Log.d("Try send  ", e.getMessage());
                        }

                    }



                    }
            } catch (IOException e) {
                Log.d("Error reading socket:", e.getMessage());
            } catch (JSONException e) {
                Log.d("Json i server för inkomande msg", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("danielSuger", "Erik Äger");
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