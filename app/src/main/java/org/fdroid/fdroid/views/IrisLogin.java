package org.fdroid.fdroid.views;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.fdroid.fdroid.R;

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
    void setupControls()
    {
        username = (EditText)findViewById(R.id.editText_user);
        password = (EditText)findViewById(R.id.editText_password);
        login_button = (Button)findViewById(R.id.button_login);

        login_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        // Todo: Here we could attempt an authentication of the User/Pass before saving them to the perferences
                        if ( username.getText().length() < 1 || password.getText().length() < 1)
                            Toast.makeText(IrisLogin.this,"Failure! Username or Password are emtpy", Toast.LENGTH_SHORT).show();
                        else
                        {
                            Toast.makeText(IrisLogin.this,"Success. Username & Password are saved.", Toast.LENGTH_SHORT).show();

                            finish();

                        }
                    }
                }
        );
    }
}
