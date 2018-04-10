package fdroidclient.iris.com.fdroiddongle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.fdroid.fdroid.Preferences;

public class FDroidDongle extends AppCompatActivity {

    public static final String DONGLE = "dongle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Preferences.get().setPrefDeviceType(DONGLE);
//        Intent intent = new Intent(this, FDroid.class);
//        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
