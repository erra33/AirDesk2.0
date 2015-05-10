package pt.inesc.termite.simplechat;

import android.app.Application;


public class GlobalVariable extends Application{

    boolean bound = true;

    public void setBound(Boolean value) {
        bound = value;

    }

    public boolean getBound() {
        return bound;
    }
}
