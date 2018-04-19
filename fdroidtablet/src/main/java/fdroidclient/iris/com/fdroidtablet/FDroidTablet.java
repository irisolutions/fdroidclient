package fdroidclient.iris.com.fdroidtablet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.fdroid.fdroid.Preferences;

public class FDroidTablet extends AppCompatActivity {
    public static final String TABLET = "tablet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Preferences.get().setPrefDeviceType(TABLET);

//        Intent intent = new Intent(this, FDroid.class);
//        startActivity(intent);
    }
}
