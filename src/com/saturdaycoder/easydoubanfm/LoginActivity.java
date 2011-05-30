package com.saturdaycoder.easydoubanfm;
import android.app.*;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.*;
import android.content.res.Resources;
import android.os.*;
import android.view.*;

public class LoginActivity extends Activity {

	private EditText editEmail;
	private EditText editPasswd;
	private Button buttonLogin;
	//private Button buttonLogout;
	
	private Intent intent;
	private Bundle bundle;
	private String email;
	private String passwd;
	
	private boolean loggedIn;
	
	IDoubanFmService mDoubanFm;
	ServiceConnection mServiceConn;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginactivity);
		editEmail = (EditText)findViewById(R.id.editEmail);
		editPasswd = (EditText)findViewById(R.id.editPasswd);
		buttonLogin = (Button)findViewById(R.id.buttonLogin);
		//buttonLogout = (Button)findViewById(R.id.buttonLogout);
		intent = this.getIntent();
		bundle = intent.getExtras();
		//loggedIn = bundle.getBoolean("login");
		

		
		mServiceConn = new ServiceConnection(){
        	public void onServiceConnected(ComponentName className, IBinder service) {
        		mDoubanFm = (IDoubanFmService)((DoubanFmService.LocalBinder)service).getService();
        	}
        	public void onServiceDisconnected(ComponentName className) {
        		mDoubanFm = null;
        	}
        };
        bindService(new Intent(LoginActivity.this, DoubanFmService.class), 
        		mServiceConn, 0);
		
		buttonLogin.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(0);
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					
				}
				email = editEmail.getText().toString();
				passwd = editPasswd.getText().toString();
				Debugger.debug("pressed login");
				if (mDoubanFm.login(email, passwd)) {
					dismissDialog(0);
					Debugger.debug("pressed login succ");
					Preference.setAccountEmail(LoginActivity.this, email);
					Preference.setAccountPasswd(LoginActivity.this, passwd);
					Preference.setLogin(LoginActivity.this, true);
					LoginActivity.this.setResult(RESULT_OK, intent);
					LoginActivity.this.finish();
				} else {
					dismissDialog(0);
					popNotify(getResources().getString(R.string.notify_login_fail));
				}
			}
		});

	}

	
    private void popNotify(String msg)
    {
        Toast.makeText(LoginActivity.this, msg,
                Toast.LENGTH_LONG).show();
    }
    


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("登录");
                dialog.setMessage("请稍候。。。");
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                return dialog;
            }
            default:
            	break;
        }
        return null;
    }

}
