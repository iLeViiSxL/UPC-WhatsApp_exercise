package edu.upc.whatsapp;

import edu.upc.whatsapp.comms.RPC;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

import edu.upc.whatsapp.service.PushService;
import entity.User;
import entity.UserInfo;

public class c_RegistrationActivity extends Activity implements View.OnClickListener {

  _GlobalState globalState;
  ProgressDialog progressDialog;
  User user;
  OperationPerformer operationPerformer;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    globalState = (_GlobalState)getApplication();
    setContentView(R.layout.c_registration);
    ((Button) findViewById(R.id.editregistrationButton)).setOnClickListener(this);
  }

  public void onClick(View arg0) {
    if (arg0 == findViewById(R.id.editregistrationButton)) {

      EditText loginTextView = (EditText) findViewById(R.id.editTextLogin_regis);
      EditText passwordTextView = (EditText) findViewById(R.id.editTextPassword_regis);
      EditText nameTextView = (EditText) findViewById(R.id.editTextName_regis);
      EditText surnameTextView = (EditText) findViewById(R.id.editTextSurname_regis);
      EditText mailTextView = (EditText) findViewById(R.id.editTextMail_regis);

      user = new User();
      user.setLogin(loginTextView.getText().toString());
      user.setPassword(passwordTextView.getText().toString());
      user.setEmail(mailTextView.getText().toString());

      UserInfo userInfo = new UserInfo();
      userInfo.setName(nameTextView.getText().toString());
      userInfo.setSurname(surnameTextView.getText().toString());
      user.setUserInfo(userInfo);

      progressDialog = ProgressDialog.show(this, "RegistrationActivity", "Registering for service...");
      // if there's still a running thread doing something, we don't create a new one
      if (operationPerformer == null) {
        operationPerformer = new OperationPerformer();
        operationPerformer.start();
      }
    }
  }

  private class OperationPerformer extends Thread {

    @Override
    public void run() {
      Message msg = handler.obtainMessage();
      Bundle b = new Bundle();

      UserInfo userInfoRPC = RPC.registration(user);

      b.putSerializable("userInfo",userInfoRPC);
      msg.setData(b);
      handler.sendMessage(msg);
    }
  }

  Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {

      operationPerformer = null;
      progressDialog.dismiss();

      UserInfo userInfo = (UserInfo) msg.getData().getSerializable("userInfo");

      if (userInfo.getId() >= 0) {
        toastShow("Registration successful");

        globalState.my_user = userInfo;

        finish();
      }
      else if (userInfo.getId() == -1) {
        toastShow("Registration unsuccessful,\nlogin already used by another user");
      }
      else if (userInfo.getId() == -2) {
        toastShow("Not registered, connection problem due to: " + userInfo.getName());
        System.out.println("--------------------------------------------------");
        System.out.println("error!!!");
        System.out.println(userInfo.getName());
        System.out.println("--------------------------------------------------");
      }
    }
  };

  private void toastShow(String text) {
    Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
    toast.setGravity(0, 0, 200);
    toast.show();
  }
}
