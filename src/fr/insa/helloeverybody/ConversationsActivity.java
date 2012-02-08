package fr.insa.helloeverybody;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ConversationsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Conversations tab");
        setContentView(textview);
    }
}
