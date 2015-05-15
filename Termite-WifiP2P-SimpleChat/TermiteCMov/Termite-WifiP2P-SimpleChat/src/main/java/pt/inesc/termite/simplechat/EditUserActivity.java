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


public class EditUserActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        setTitle("Edit");

        Button b = (Button) findViewById(R.id.buttonSave);
        b.setEnabled(true);

        User user = new User(this);


        EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        EditText editTextFullName = (EditText) findViewById(R.id.editTextFullName);
        EditText editTextKeywords = (EditText) findViewById(R.id.editTextTags);

        editTextEmail.setText(user.getUser().email);
        editTextEmail.setEnabled(false);
        editTextFullName.setText(user.getUser().fullName);
        editTextKeywords.setText(user.getUser().keywords);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register_user, menu);
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

    // Called when the user clicks the sharedWs button
    public void insertUser(View view) {

        User user = new User(this);
        EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        EditText editTextFullName = (EditText) findViewById(R.id.editTextFullName);
        EditText editTextKeywords = (EditText) findViewById(R.id.editTextTags);
        if (!User.isEmailAddress(editTextEmail.getText().toString())){
            findViewById(R.id.textViewFormat).setVisibility(View.VISIBLE);
        }
        else {

            //user.email = editTextEmail.getText().toString();
            //user.fullName = editTextFullName.getText().toString();
            //repo.insert(user);

            user.update(editTextEmail.getText().toString(), editTextFullName.getText().toString(), editTextKeywords.getText().toString());

            Toast.makeText(this, "Account successfully edited", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
