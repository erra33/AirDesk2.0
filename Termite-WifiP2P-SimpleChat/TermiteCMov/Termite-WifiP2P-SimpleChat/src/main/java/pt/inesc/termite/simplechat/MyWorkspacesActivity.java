package pt.inesc.termite.simplechat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


public class MyWorkspacesActivity extends ActionBarActivity {

    TextView ws_Id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_workspaces);
            WorkspaceRepo repo = new WorkspaceRepo(this);

            ArrayList<HashMap<String, String>> wsList = repo.getWorkspaceList();


            if(wsList.size()!=0) {
                ListView lv = (ListView) findViewById(R.id.list);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                        ws_Id = (TextView) view.findViewById(R.id.file_Id);
                        String wsId = ws_Id.getText().toString();
                        Intent objIndent = new Intent(getApplicationContext(),MyWorkspaceActivity.class);
                        objIndent.putExtra("ws_Id", Integer.parseInt(wsId));
                        startActivity(objIndent);
                    }});
                ListAdapter adapter = new SimpleAdapter( MyWorkspacesActivity.this,wsList, R.layout.view_file_entry, new String[] { "id","title"}, new int[] {R.id.file_Id, R.id.file_title});
                lv.setAdapter(adapter);
            }
            else{
                Toast.makeText(this, "No workspaces!", Toast.LENGTH_SHORT).show();
            }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_workspaces, menu);
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

        if (id == R.id.action_add) {
            Intent intent = new Intent(this,AddWsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
