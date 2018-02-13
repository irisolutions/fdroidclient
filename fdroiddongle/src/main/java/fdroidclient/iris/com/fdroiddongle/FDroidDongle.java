package fdroidclient.iris.com.fdroiddongle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.fdroid.fdroid.FDroid;

public class FDroidDongle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, FDroid.class);
        startActivity(intent);
    }
}
