package it.digisin.collabroute;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity {

    private static UserHandler User = null;

    private enum ResponseMSG {OK, AUTH_FAILED, USER_NOT_CONFIRMED, EMAIL_NOT_FOUND, CONFIRM_MAIL_ERROR, DATABASE_ERROR, CONN_TIMEDOUT, CONN_REFUSED, CONN_BAD_URL, CONN_GENERIC_IO_ERROR, CONN_GENERIC_ERROR;}

    EditText mailField;
    EditText passField;
    Dialog confirmDialog;
    EditText codeField;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mailField = (EditText) findViewById(R.id.emailLogin);
        passField = (EditText) findViewById(R.id.passwordLogin);

        final Button loginButton = (Button) findViewById(R.id.buttonLogin);
        final Button registrationButton = (Button) findViewById(R.id.buttonSignIn);

        mailField.setText("dummy@dummy.dummy");
        passField.setText("dummy");

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegistration();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void doLogin() {

        final Editable mailEdit = mailField.getText();
        final Editable passEdit = passField.getText();
        String mail = mailEdit.toString();
        String passwd = passEdit.toString();

        if (TextUtils.isEmpty(mailEdit) || TextUtils.isEmpty(passEdit) || !EmailValidator.validate(mail)) {
            Toast.makeText(LoginActivity.this, "Email or Password missing or incorrect", Toast.LENGTH_SHORT).show();
            return;
        }
        if (User == null) {
            User = UserHandler.create(mail, passwd);
        } else {
            User.setEMail(mail);
            User.setPassword(passwd);
        }
        UserLoginHandler login = new UserLoginHandler(LoginActivity.this, User, this); //extend AsyncTask and run with a separate thread
        login.execute("login"); //start the thread

    }

    void checkCredentials(JSONObject response) {
        try {
            String resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case AUTH_FAILED:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.AUTH_FAILED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_REFUSED:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case USER_NOT_CONFIRMED:
                    String codeFromJson = response.getString("code");
                    code = codeFromJson;
                    createConfirmDialog();
                    confirmDialog.show();
                    return;
            }
            User.setName(response.getString("name"));
            User.setToken(response.getString("token"));
            User.setId(response.getInt("id"));
            System.err.println(User.getId() + " " + User.getName() + " " + User.getToken()); //debug
            Intent homeIntent = new Intent(getApplication(), travelListActivity.class);
            startActivity(homeIntent);
            finish(); //TODO Pass userHandler Object to next activity before finish


        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    public void goToRegistration() {
        Intent registrationIntent = new Intent(getApplication(), RegistrationActivity.class);
        startActivity(registrationIntent);
    }

    public void createConfirmDialog() {
        confirmDialog = new Dialog(this);
        confirmDialog.setContentView(R.layout.confirm_dialog);
        confirmDialog.setTitle(this.getString(R.string.dialog_confirm_title));

        final TextView dialogMessage = (TextView) confirmDialog.findViewById(R.id.confirm_text);
        final Button sendCode = (Button) confirmDialog.findViewById(R.id.dialogSedCode);
        final Button checkMail = (Button) confirmDialog.findViewById(R.id.dialogCheckMail);
        codeField = (EditText) confirmDialog.findViewById(R.id.dialogVeriCode);

        String dialogText = String.format(this.getString(R.string.registration_email_sent), User.getEMail());
        dialogMessage.setText(dialogText);

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCode(codeField.getText().toString());
            }
        });

        checkMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMail();
            }
        });
    }

    public void checkCode(String codeWritten) {
        if (!code.equals(codeWritten)) {
            Toast.makeText(LoginActivity.this, this.getString(R.string.registration_wrong_code), Toast.LENGTH_SHORT).show();
            return;
        }
        confirmDialog.dismiss();
        UserLoginHandler login = new UserLoginHandler(LoginActivity.this, User, this);
         login.execute("confirm");//extend AsyncTask and run with a separate thread
    }

    public void checkMail() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage("com.android.email");
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(LoginActivity.this, this.getString(R.string.registration_mail_not_configured), Toast.LENGTH_SHORT).show();
        }
    }

    public void confirmationResponse(JSONObject response) {
        try {
            String resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case CONN_REFUSED:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case EMAIL_NOT_FOUND:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.EMAIL_NOT_FOUND), Toast.LENGTH_SHORT).show();
                    return;
                case OK:
                    User.setId(Integer.parseInt(response.getString("id")));
                    User.setEMail(response.getString("mail"));
                    User.setName(response.getString("name"));
                    Toast.makeText(LoginActivity.this, String.format(UserLoginHandler.errors.get(UserLoginHandler.OK), User.getName()), Toast.LENGTH_SHORT).show();
                    mailField.setText(User.getEMail());
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }
}