package com.saturdaycoder.easydoubanfm;
import android.app.*;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.*;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;

public class LoginActivity extends Activity {

	private EditText editEmail;
	private EditText editPasswd;
	private Button buttonLogin;
	private TextView textRegister;
	//private Button buttonLogout;
	
	private Intent intent;
	private Bundle bundle;
	private String email;
	private String passwd;
	
	private boolean loggedIn;
	
	
	private static String registerUrl;
	
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
		//intent = this.getIntent();
		//bundle = intent.getExtras();
		email = Preference.getAccountEmail(LoginActivity.this);
		if (email != null) {
			editEmail.setText(email);
		}
		//loggedIn = bundle.getBoolean("login");
		
		//registerUrl = "http://www.douban.com/j/app/register?app_name=radio_android&version=" 
		//			+ Preference.getClientVersion(this);
		registerUrl = "http://www.douban.com/accounts/register";
		
		textRegister = (TextView) findViewById(R.id.textRegister);
		textRegister.setText(android.text.Html.fromHtml("<a href=\"" + registerUrl + "\">去豆瓣网站注册账号</a>"));
		textRegister.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
		
		mServiceConn = new ServiceConnection(){
        	public void onServiceConnected(ComponentName className, IBinder service) {
        		mDoubanFm = (IDoubanFmService)((DoubanFmService.LocalBinder)service).getService();
        	}
        	public void onServiceDisconnected(ComponentName className) {
        		mDoubanFm = null;
        	}
        };
        bindService(new Intent(LoginActivity.this, DoubanFmService.class), 
        		mServiceConn, BIND_AUTO_CREATE);
		
		buttonLogin.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(0);
				/*try {
					Thread.sleep(1000);
				} catch (Exception e) {
					
				}*/
				email = editEmail.getText().toString();
				passwd = editPasswd.getText().toString();
				Debugger.debug("pressed login");
				//if (mDoubanFm.login(email, passwd)) {
					
				LoginTask loginTask = new LoginTask();
				loginTask.execute(email, passwd);
				//} else {
				//	dismissDialog(0);
					
				//}
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

    
    private class LoginTask extends AsyncTask<String, Integer, Boolean> {
    	
    	@Override
    	protected void onCancelled () {
    		Debugger.info("GetPictureTask is cancelled");
    		
    	}
    	@Override
    	protected Boolean doInBackground(String... params) {
    		if (params.length < 2 || mDoubanFm == null) {
    			Debugger.error("service not bind, can not log in");
    			return false;
    		}
    		boolean succ = false;
    		try {
    			succ = mDoubanFm.login(params[0], params[1]);
    			Debugger.warn("failed logging in through API");
    			return succ;
    			//return false;
    		} catch (Exception e) {
    			e.printStackTrace();
    			Debugger.debug("failed logging in: " + e.toString());
    			return false;
    		}
    		
    	}
    	@Override
    	protected void onProgressUpdate(Integer... progress) {
    		
        }
    	@Override
        protected void onPostExecute(Boolean result) {
    		try {
    			dismissDialog(0);
    		} catch (Exception e) {
    			
    		}
    		if (result) {
    			
				Debugger.debug("login succ");
				Preference.setAccountEmail(LoginActivity.this, email);
				Preference.setAccountPasswd(LoginActivity.this, passwd);
				Preference.setLogin(LoginActivity.this, true);
				popNotify(getResources().getString(R.string.notify_login_succ));
				LoginActivity.this.setResult(RESULT_OK, intent);
				LoginActivity.this.finish();
    		} else {
    			Debugger.debug("login failed");
    			Preference.setAccountEmail(LoginActivity.this, email);
    			popNotify(getResources().getString(R.string.notify_login_fail));
    		}
        }
    }
}
