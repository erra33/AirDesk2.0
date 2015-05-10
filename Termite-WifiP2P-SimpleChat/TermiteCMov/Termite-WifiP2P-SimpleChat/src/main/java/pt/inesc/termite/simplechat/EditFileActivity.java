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


public class EditFileActivity extends ActionBarActivity {

    private int _File_Id=0;
    private String originalTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Edit File");

        setContentView(R.layout.activity_add_file);

        Intent intent = getIntent();
        _File_Id = intent.getIntExtra("file_Id", 0);
        FileRepo repo = new FileRepo(this);
        File file = repo.getFileById(_File_Id);
        final EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
        editTextContent.setText(file.content);
        editTextTitle.setText(file.title);

        originalTitle = file.title;

        ((Button) findViewById(R.id.saveBtn)).setEnabled(true); //Ok to save without making any edits
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

        FileRepo repo = new FileRepo(this);
        File file = repo.getFileById(_File_Id);

        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
        file.title = editTextTitle.getText().toString();
        file.content = editTextContent.getText().toString();

        if (!file.title.equals(originalTitle) && repo.existsAFileWithTitleInWorkspace(file.title,file.ws)){
            Toast.makeText(this, "File with the same name already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        WorkspaceRepo wsRepo = new WorkspaceRepo(this);
        Workspace workspace = wsRepo.getWorkspaceById(file.ws);
        int oldSize = file.size;
        file.setSize();
        if(workspace.sizeLimit-repo.getFileSizes(file.ws)+oldSize >= file.size) {
            repo.update(file);
            Toast.makeText(this, "File updated", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MyWorkspaceActivity.class);
            intent.putExtra("ws_Id",file.ws);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "File is too big.", Toast.LENGTH_SHORT).show();
        }


    }

    // Called when the user clicks the cancel button
    public void cancel(View view) {
        Intent intent = new Intent(this, ReadFileActivity.class);
        intent.putExtra("file_Id",_File_Id);
        startActivity(intent);
    }
}
