package org.fdroid.fdroid.views;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.R;
import org.fdroid.fdroid.UpdateService;

public class IrisLogin extends AppCompatActivity {

    private static EditText username;
    private static EditText password;
    private static TextView attempt;
    private static Button login_button;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iris_login);

        setupControls();

    }
    boolean UserofPassChanged()
    {
        username = (EditText)findViewById(R.id.editText_user);
        password = (EditText)findViewById(R.id.editText_password);

        String curUser = Preferences.get().getPrefUsername();
        String curPass = Preferences.get().getPrefPassword();

        if ( username.getText().toString().equals(curUser) &&
                password.getText().toString().equals(curPass)   )
        {
            return false;
        }

        return true;
    }
    void setupControls()
    {
        username = (EditText)findViewById(R.id.editText_user);
        password = (EditText)findViewById(R.id.editText_password);
        login_button = (Button)findViewById(R.id.button_login);

        String curUser = Preferences.get().getPrefUsername();
        String curPass = Preferences.get().getPrefPassword();

        username.setText(curUser);
        password.setText(curPass);

        if ( !curUser.isEmpty() && !curPass.isEmpty() )
        {
            login_button.setEnabled(false);

             TextWatcher textWatcher = new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (UserofPassChanged())
                        login_button.setEnabled(true);
                    else
                        login_button.setEnabled(false);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            };

            username.addTextChangedListener(textWatcher);
            password.addTextChangedListener(textWatcher);

        }

        login_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String user = username.getText().toString();
                        String pass = password.getText().toString();
                        // Todo: Here we could attempt an authentication of the User/Pass before saving them to the perferences
                        if ( username.getText().length() < 1 || password.getText().length() < 1)
                            Toast.makeText(IrisLogin.this,"Failure! Username or Password are emtpy", Toast.LENGTH_SHORT).show();
                        else
                        {
                            Toast.makeText(IrisLogin.this,"Success. Username & Password are saved.", Toast.LENGTH_SHORT).show();
                            // save use/pass in the preferences

                            Preferences.get().setPrefUsername(user);
                            Preferences.get().setPrefPassword(pass);

                            UpdateService.updateNow(getBaseContext());

                            // trigger an update
                            finish();

                        }
                    }
                }
        );
    }
}
