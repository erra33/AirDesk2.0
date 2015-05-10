package pt.inesc.termite.simplechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;

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

public class MainActivity extends ActionBarActivity implements
		PeerListListener, GroupInfoListener {

    public static final String TAG = "simplechat";

    private SimWifiP2pManager mManager = null;
    private Channel mChannel = null;
    private Messenger mService = null;
	private SimWifiP2pSocketServer mSrvSocket = null;
	private ReceiveCommTask mComm = null;
	private SimWifiP2pSocket mCliSocket = null;
    private ArrayList<String> peersArrey;

    private static boolean mHasItRun = false;
    GlobalVariable bound = null;

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
        Toast.makeText(getApplicationContext(),bound.getBound()+"",Toast.LENGTH_SHORT).show();


        // initialize the UI
        setContentView(R.layout.activity_main);


        //Check if it's any user reg
        User user = new User(this);
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
            new IncommingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);

            mHasItRun = true;
        }




	}

    // Called when the user clicks the myWs button
    public void startMyWs(View view) {
        Intent intent = new Intent(this, MyWorkspacesActivity.class);
        startActivity(intent);
    }

    // Called when the user clicks the sharedWs button
    public void startSharedWs(View view) {
        Intent intent = new Intent(this, SharedWorkspaces.class);
        startActivity(intent);
    }

    // Called when the user clicks the sharedWs button
    public void connect(View view) {
        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, peersArrey.get(0) + "");
    }

    // Called when the user clicks the sharedWs button
    public void disconnect(View view) {
        if (mCliSocket != null) {
            try {
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCliSocket = null;
    }


    public void send(View view) {
        Toast.makeText(getApplicationContext(), "Send ",Toast.LENGTH_SHORT).show();
        try {
            mCliSocket.getOutputStream().write( ("hello\n").getBytes());
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

	public class IncommingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

			try {
				mSrvSocket = new SimWifiP2pSocketServer(
						Integer.parseInt(getString(R.string.port)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted()) {
				try {
					SimWifiP2pSocket sock = mSrvSocket.accept();
					if (mCliSocket != null && mCliSocket.isClosed()) {
						mCliSocket = null;
					}
					if (mCliSocket != null) {
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
			return null;
		}

		@Override
		protected void onProgressUpdate(SimWifiP2pSocket... values) {
			mCliSocket = values[0];
			mComm = new ReceiveCommTask();

            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
		}
	}


	public class OutgoingCommTask extends AsyncTask<String, Void, String> {


		@Override
		protected String doInBackground(String... params) {
			try {
				mCliSocket = new SimWifiP2pSocket(params[0],
						Integer.parseInt(getString(R.string.port)));
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
            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mCliSocket);

		}
	}


	public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, String, String> {
		SimWifiP2pSocket s;

        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "connected ",Toast.LENGTH_SHORT).show();
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
                    if (st.charAt(0) == 'h'){
                        try {
                            mCliSocket.getOutputStream().write( ("1hello\n").getBytes());
                            mCliSocket.getOutputStream().flush();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(),e.getMessage() +" error",Toast.LENGTH_SHORT).show();
                        }
                    }

                }
			} catch (IOException e) {
				Log.d("Error reading socket:", e.getMessage());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
            Toast.makeText(getApplicationContext(), "!" + values[0] + "!",Toast.LENGTH_SHORT).show();
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
