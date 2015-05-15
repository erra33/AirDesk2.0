package pt.inesc.termite.simplechat;

import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.ArrayList;


public class AddWsActivity extends ActionBarActivity {

    ArrayList<String> InviteList = new ArrayList<String>();
    int publicWs;
   long bytesAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("New Workspace");

        setContentView(R.layout.activity_add_ws);

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        bytesAvailable = (long)stat.getBlockSizeLong() * (long)stat.getAvailableBlocksLong();
        long mbAvailable = bytesAvailable / (1024*1024);
        String freeStorage = Long.toString(mbAvailable);
        TextView sizeLabel = (TextView) findViewById(R.id.textViewSizeLabel);
        sizeLabel.setText("Max " + freeStorage + " MB");

        final EditText keyWords = (EditText) findViewById(R.id.editTextKeyWords);
        CheckBox CheckboxPublic = (CheckBox) findViewById(R.id.checkBoxPublic);

        // add onCheckedListener on checkbox
        // when user clicks on this checkbox, this is the handler.
        CheckboxPublic.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // checkbox status is changed from uncheck to checked.
                if (!isChecked) {
                    publicWs = 0;
                    keyWords.setVisibility(View.INVISIBLE);
                } else {
                    publicWs = 1;
                    keyWords.setVisibility(View.VISIBLE);
                }
            }
        });


        final EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        final EditText editTextSizeLimit = (EditText) findViewById(R.id.editTextSizeLimit);
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

        return super.onOptionsItemSelected(item);
    }

    // Called when the user clicks the save button
    public void saveWs(View view) {

        Workspace ws = new Workspace();

        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        ws.title = editTextTitle.getText().toString();
        EditText editTextSizeLimit = (EditText) findViewById(R.id.editTextSizeLimit);
        long sizeLimit = Long.parseLong(editTextSizeLimit.getText().toString());

        if(sizeLimit <= bytesAvailable) {
            WorkspaceRepo repoWs = new WorkspaceRepo(this);
            ws.publicWs = publicWs;
            ws.sizeLimit = (int) sizeLimit;
            int wsID = repoWs.insert(ws);


            //add to Invite list
            InviteRepo repoInvite = new InviteRepo(this);
            repoInvite.insert(InviteList, wsID,"",1);

            //add to Keword list
            KeywordsRepo repoKeyword = new KeywordsRepo(this);
            EditText editTextKeyword = (EditText) findViewById(R.id.editTextKeyWords);
            String[] Keywords = editTextKeyword.getText().toString().split(" ");
            repoKeyword.insert(Keywords, wsID);

            Toast.makeText(this, "Workspace added", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MyWorkspacesActivity.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Size of workspace too big..", Toast.LENGTH_SHORT).show();
        }
    }

    // Called when the user clicks the cancel button
    public void cancel(View view) {
        Intent intent = new Intent(this, MyWorkspacesActivity.class);
        startActivity(intent);
    }

    public void addInvite(View view) {
        EditText editTextInvite = (EditText) findViewById(R.id.editTextInvite);
        String Invite = editTextInvite.getText().toString();
        if (!User.isEmailAddress(Invite)){
            findViewById(R.id.textViewFormat).setVisibility(View.VISIBLE);
            return;
        }
        else{
            findViewById(R.id.textViewFormat).setVisibility(View.INVISIBLE);
        }

        //add Invite email
        InviteList.add(Invite);
        editTextInvite.setText("");

        ListView lv = (ListView) findViewById(R.id.listViewInvites);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, InviteList);
        lv.setAdapter(adapter);

    }
}
