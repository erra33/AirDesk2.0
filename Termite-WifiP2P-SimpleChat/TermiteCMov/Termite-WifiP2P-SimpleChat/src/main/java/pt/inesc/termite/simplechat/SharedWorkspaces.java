package pt.inesc.termite.simplechat;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class SharedWorkspaces extends ActionBarActivity {


    TextView ws_Id;
    GlobalVariable bound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_workspaces);

        bound = (GlobalVariable) getApplicationContext();
        bound.setBound(true);
        Toast.makeText(getApplicationContext(),bound.getBound()+"",Toast.LENGTH_SHORT).show();

//        mManager.requestGroupInfo(mChannel, (GroupInfoListener) savedInstanceState);



/*//        UserRepo userRepo = new UserRepo(this);
//        User user = userRepo.getUser();


        User user = new User(this);
        user.getUser();


        //network get list of workspaces
        NetworkHandlerRequest nwHdl = new NetworkHandlerRequest();
        ArrayList<HashMap<String, String>> wsList = nwHdl.getListWs(user.email);


        if(wsList.size()!=0) {
            ListView lv = (ListView) findViewById(R.id.list);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                    ws_Id = (TextView) view.findViewById(R.id.file_Id);
                    String wsId = ws_Id.getText().toString();
                    Intent objIndent = new Intent(getApplicationContext(),SharedWorkspace.class);
                    objIndent.putExtra("ws_Id", Integer.parseInt(wsId));
                    startActivity(objIndent);
                }});
            ListAdapter adapter = new SimpleAdapter( SharedWorkspaces.this,wsList, R.layout.view_file_entry, new String[] { "id","title"}, new int[] {R.id.file_Id, R.id.file_title});
            lv.setAdapter(adapter);
        }
        else{
            Toast.makeText(this, "No workspaces!", Toast.LENGTH_SHORT).show();
        }*/
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
}
