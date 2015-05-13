package pt.inesc.termite.simplechat;

import android.os.Bundle;

public class SharedEditFileActivity extends EditFileActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Edit File");

        setContentView(R.layout.activity_add_file);
    }
}