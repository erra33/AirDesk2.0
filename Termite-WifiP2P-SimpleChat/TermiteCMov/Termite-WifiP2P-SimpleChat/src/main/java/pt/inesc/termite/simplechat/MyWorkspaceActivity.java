package pt.inesc.termite.simplechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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


public class MyWorkspaceActivity extends ActionBarActivity{

    private int _Ws_Id=0;
    TextView file_Id;
    WorkspaceRepo wsRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_workspace);

        Intent intent = getIntent();
        _Ws_Id =intent.getIntExtra("ws_Id", 0);
        wsRepo = new WorkspaceRepo(this);
        Workspace ws = wsRepo.getWorkspaceById(_Ws_Id);
        setTitle(ws.title);

        FileRepo repo = new FileRepo(this);

        ArrayList<HashMap<String, String>> fileList =  repo.getFileList(_Ws_Id);


        if(fileList.size()!=0) {
            ListView lv = (ListView) findViewById(R.id.list);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                    file_Id = (TextView) view.findViewById(R.id.file_Id);
                    String fileId = file_Id.getText().toString();
                    Intent objIndent = new Intent(getApplicationContext(),ReadFileActivity.class);
                    objIndent.putExtra("file_Id", Integer.parseInt( fileId));
                    startActivity(objIndent);
                }});
            ListAdapter adapter = new SimpleAdapter( MyWorkspaceActivity.this,fileList, R.layout.view_file_entry, new String[] { "id","title"}, new int[] {R.id.file_Id, R.id.file_title});
            lv.setAdapter(adapter);
        }
        else{
            Toast.makeText(this, "No files!", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_workspace, menu);
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
            Intent intent = new Intent(this,AddFileActivity.class);
            intent.putExtra("ws_Id",_Ws_Id);
            startActivity(intent);
        }
        else if (id == R.id.action_delete) {

            //popup window

            wsRepo.delete(_Ws_Id);
            Intent intent = new Intent(this, MyWorkspacesActivity.class);
            startActivity(intent);
        }

       else if (id == R.id.action_edit) {
            Intent intent = new Intent(this,EditWsActivity.class);
            intent.putExtra("ws_Id",_Ws_Id);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }
}
