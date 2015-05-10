package pt.inesc.termite.simplechat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class AddFileActivity extends ActionBarActivity {

    private int _Ws_Id=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("New File");
        setContentView(R.layout.activity_add_file);
        Intent wsIntent = getIntent();
        _Ws_Id = wsIntent.getIntExtra("ws_Id", 0);

        final EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        TextWatcher tW = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable s) {
                Button b = (Button) findViewById(R.id.saveBtn);
                if (!editTextTitle.getText().toString().isEmpty()){
                    b.setEnabled(true);
                }
                else b.setEnabled(false);
            }
        };

        editTextTitle.addTextChangedListener(tW);

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

        //UserRepo userRepo = new UserRepo(this);
        //User user = userRepo.getUser();

        User user = new User(this);
        user.getUser();


        File file = new File();

        final EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
        file.title = editTextTitle.getText().toString();
        file.content = editTextContent.getText().toString();
        file.author = user.getUser().email;
        file.setSize();

        file.ws = _Ws_Id;
        WorkspaceRepo wsRepo = new WorkspaceRepo(this);
        Workspace workspace = wsRepo.getWorkspaceById(_Ws_Id);

        FileRepo repo = new FileRepo(this);

        if (repo.existsAFileWithTitleInWorkspace(file.title,file.ws)){
            Toast.makeText(this, "File with the same name already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(workspace.sizeLimit-repo.getFileSizes(_Ws_Id) >= file.size) {
            repo.insert(file);
            Toast.makeText(this, "File added", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MyWorkspaceActivity.class);
            intent.putExtra("ws_Id", _Ws_Id);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "File is too big.", Toast.LENGTH_SHORT).show();
        }
    }

    // Called when the user clicks the cancel button
    public void cancel(View view) {
        Intent intent = new Intent(this, MyWorkspaceActivity.class);
        intent.putExtra("ws_Id", _Ws_Id);
        startActivity(intent);
    }
}
