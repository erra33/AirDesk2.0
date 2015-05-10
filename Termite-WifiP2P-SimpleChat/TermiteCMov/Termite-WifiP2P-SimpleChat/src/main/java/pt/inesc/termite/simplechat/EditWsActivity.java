package pt.inesc.termite.simplechat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class EditWsActivity extends ActionBarActivity {
    private int _Ws_Id=0;
    ArrayList<String> inviteList = new ArrayList<String>();
    InviteListAdapter adapter;
    long bytesAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ws);

        setTitle("Edit Workspace");

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        bytesAvailable = (long)stat.getBlockSizeLong() * (long)stat.getAvailableBlocksLong();
        long mbAvailable = bytesAvailable / (1024*1024);
        String freeStorage = Long.toString(mbAvailable);
        TextView sizeLabel = (TextView) findViewById(R.id.textViewSizeLabel);
        sizeLabel.setText("Max " + freeStorage + " MB");


        Intent intent = getIntent();
        _Ws_Id = intent.getIntExtra("ws_Id", 0);
        WorkspaceRepo repo = new WorkspaceRepo(this);
        Workspace ws = repo.getWorkspaceById(_Ws_Id);
        final EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        CheckBox checkBoxPublic = (CheckBox) findViewById(R.id.checkBoxPublic);
        editTextTitle.setText(ws.title);
        final EditText editTextSizeLimit = (EditText) findViewById(R.id.editTextSizeLimit);
        editTextSizeLimit.setText(Integer.toString(ws.sizeLimit));

        TextWatcher tW = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable s) {
                Button b = (Button) findViewById(R.id.saveBtn);
                if (!editTextSizeLimit.getText().toString().isEmpty() && !editTextTitle.getText().toString().isEmpty()){
                    b.setEnabled(true);
                }
                else b.setEnabled(false);
            }
        };
        editTextSizeLimit.addTextChangedListener(tW);
        editTextTitle.addTextChangedListener(tW);

//        boolean publicWs;
//        if(ws.publicWs != 0){publicWs = true;}
//        else{publicWs = false;}
//        checkBoxPublic.setChecked(publicWs);
        InviteRepo repoInvite = new InviteRepo(this);
        inviteList =  repoInvite.getInviteListByWsId(_Ws_Id);
        if(inviteList.size()!=0) {
            setInviteList();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_ws, menu);
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

    public void saveWs(View view) {
        WorkspaceRepo repo = new WorkspaceRepo(this);
        Workspace ws = repo.getWorkspaceById(_Ws_Id);

        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        ws.title = editTextTitle.getText().toString();
        EditText editTextSizeLimit = (EditText) findViewById(R.id.editTextSizeLimit);
        long sizeLimit = Long.parseLong(editTextSizeLimit.getText().toString());

        if (sizeLimit <= bytesAvailable) {
            ws.sizeLimit = (int) sizeLimit;
            repo.update(ws);


            //Insert list
            if (inviteList.size() > 0) {
                InviteRepo repoInvite = new InviteRepo(this);
                repoInvite.insert(inviteList, _Ws_Id);
            }

            //add to Keword list
//        ws.publicWs = publicWs;
            KeywordsRepo repoKeyword = new KeywordsRepo(this);
            EditText editTextKeyword = (EditText) findViewById(R.id.editTextKeyWords);
            String[] Keywords = editTextKeyword.getText().toString().split(" ");
            repoKeyword.insert(Keywords, _Ws_Id);

            Toast.makeText(this, "Workspace updated", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MyWorkspacesActivity.class);
            startActivity(intent);
        }
    }

    // Called when the user clicks the add invite button
    public void addInvite(View view) {
        EditText editTextInvite = (EditText) findViewById(R.id.editTextInvite);
        String invite = editTextInvite.getText().toString();
        if (!User.isEmailAddress(invite)){
            findViewById(R.id.textViewFormat).setVisibility(View.VISIBLE);
            return;
        }
        else{
            findViewById(R.id.textViewFormat).setVisibility(View.INVISIBLE);
        }
        if (invite.length() > 0) {
            inviteList.add(invite);
            if(inviteList.size()==1) {
                setInviteList();
            }
            adapter.notifyDataSetChanged();
        }
    }

    // Called when the user clicks the cancel button
    public void cancel(View view) {
        Intent intent = new Intent(this, MyWorkspacesActivity.class);
        startActivity(intent);
    }

    public void setInviteList() {
            ListView lv = (ListView) findViewById(R.id.listViewInvite);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                }});
            adapter = new InviteListAdapter(inviteList, this);
    //        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, inviteList);
            lv.setAdapter(adapter);
    }


}
