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


public class RegisterUserActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Register");

        setContentView(R.layout.activity_register_user);

        final EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        final EditText editTextFullName = (EditText) findViewById(R.id.editTextFullName);

        TextWatcher tW = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable s) {
                Button b = (Button) findViewById(R.id.buttonSave);
                if (User.isEmailAddress(editTextEmail.getText().toString()) && !editTextFullName.getText().toString().isEmpty()){
                    b.setEnabled(true);
                }
                else b.setEnabled(false);
            }
        };
        editTextEmail.addTextChangedListener(tW);
        editTextFullName.addTextChangedListener(tW);
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
        final EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        final EditText editTextFullName = (EditText) findViewById(R.id.editTextFullName);
        EditText editTextKeywords = (EditText) findViewById(R.id.editTextTags);



        TextWatcher tW = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable s) {
                Button b = (Button) findViewById(R.id.buttonSave);
                if (User.isEmailAddress(editTextEmail.getText().toString()) && !editTextFullName.getText().toString().isEmpty()){
                    b.setEnabled(true);
                }
                else b.setEnabled(false);
            }
        };
        editTextEmail.addTextChangedListener(tW);
        editTextFullName.addTextChangedListener(tW);


        if (!User.isEmailAddress(editTextEmail.getText().toString())){
            findViewById(R.id.textViewFormat).setVisibility(View.VISIBLE);
        }
        else {

            //user.email = editTextEmail.getText().toString();
            //user.fullName = editTextFullName.getText().toString();
            //repo.insert(user);

            user.setUser(editTextEmail.getText().toString(), editTextFullName.getText().toString(), editTextKeywords.getText().toString());

            Toast.makeText(this, "Account successfully created", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
