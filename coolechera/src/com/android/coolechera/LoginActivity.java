package com.android.coolechera;



import com.android.business.object.DataBaseBO;
import com.android.conexion.LoginSync;
import com.android.conexion.constantes.Const;
import com.android.data.DataSync;
import com.android.data.Login;
import com.android.util.Util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	
	

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_USER = "Usuario";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String user;
	private String password;

	// UI references.
	private EditText userView;
	private EditText passwordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private TextView textViewVersion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		
		// Set up the login form.
		user = getIntent().getStringExtra(EXTRA_USER);
		userView = (EditText) findViewById(R.id.user);
		userView.setText(user);

		passwordView = (EditText) findViewById(R.id.password);

		passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		textViewVersion = (TextView) findViewById(R.id.textViewVersion);
		
		findViewById(R.id.sign_in_button).setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
		//cargar informacion de version.
		boolean existe = DataBaseBO.existeDataBase();
		if(existe){
//			version = DataBaseBO.obtenerVersion(getBaseContext());
			try {
				String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
				textViewVersion.setText("Version instalada app: " + version);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_exit) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}	

		// Reset errors.
		userView.setError(null);
		passwordView.setError(null);

		// Store values at the time of the login attempt.
		user = userView.getText().toString();
		password = passwordView.getText().toString();
		
		ocultarTeclado(passwordView);

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(password)) {
			passwordView.setError(getString(R.string.error_field_required));
			focusView = passwordView;
			cancel = true;
		} else if (password.length() < 2) {
			passwordView.setError(getString(R.string.error_invalid_password));
			focusView = passwordView;
			cancel = true;
		}

		// Check for a valid user address.
		if (TextUtils.isEmpty(user)) {
			userView.setError(getString(R.string.error_field_required));
			focusView = userView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});
		} 
		else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {

			//objeto que contiene los datos obtenidos de la base de datos
			Login login = null;
			
			//contiene la respuesta ofrecida por el servidor
			DataSync dataSync = null;	
			
			try {			

				//verificar que exista la base de datos para logeo desde la main DataBase.db
				boolean existe = DataBaseBO.existeDataBase();

				//si existe = true, hacer logeo local (en DataBase.db) sino, hacer logeo al SYNC
				if(existe) {
					login = DataBaseBO.login();
					//si es null o el usuario no coincide hacer logeo al sync
					if(login == null || !login.user.equals(user)){
						dataSync = loginServer();						
					}
				}
				else {
					dataSync = loginServer();
				}
				
				// mejorar efecto de carga de login
				Thread.sleep(1500);				
				
				//verificar logeo web.
				if(dataSync != null){
					
					if(dataSync.ok){
						return true;
					}
					else {
						throw new InterruptedException(dataSync.mensaje);
					}
				}
				//verificar logeo local
				else if(login != null){
					return login.user.equals(user) && login.password.equals(password);					
				}


			} catch (InterruptedException e) {
				return false;
			}		
			
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			
			String usuario = userView.getText().toString().trim();
			String pasword = passwordView.getText().toString().trim();
			showProgress(false);

			if (success) {
				
				//verificar si esiste database
				boolean existe = DataBaseBO.existeDataBase();
				//si existe actualizar el usuario anterior por el nuevo.
				if(existe){
					DataBaseBO.cambiarUsuario(getBaseContext(),usuario, pasword );
				}				
				//persistir el usuario y la contraseña en sharedPreference llamado login, solo visible para la aplicacion
				SharedPreferences  persistLogin = getSharedPreferences("login", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = persistLogin.edit();
				editor.putString("user", usuario);
				editor.putString("password", pasword);
				editor.commit();
				
				//limpiar cajas de texto de login.
				passwordView.setText("");
				userView.setText("");
				Intent i = new Intent(getBaseContext(), PrincialActivity.class);
				startActivity(i);
			} else {
				userView.setError(getString(R.string.error_incorrect_password_or_user));
				userView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}


	/**
	 * metodo para hacer el login atraves del sincronizador, se requiere servicio de conexion 
	 * a internet habilitado.
	 * @param usuario
	 * @param password
	 * @return dataSync, que contiene la respuesta ofrecida por el servidor
	 */
	public DataSync loginServer() {
		DataSync dataSync = null;
		//verificar conexion a internet
		if (isOnline()) {
			LoginSync loginSyn = new LoginSync(this.user, this.password, Const.LOGIN);
			dataSync = loginSyn.logIn();

		} else {
			Util.MostrarAlertDialog(this, "No hay conexion a la red.\n\nNo se puede iniciar Sesion con el Servidor debido a que no hay conexion a Internet.",android.R.drawable.ic_dialog_alert);
		}
		return dataSync;
	}	
	

	/**
	 * metodo para verificar si el movil tiene conexion a internet para poder hacer el login del vendendor
	 * @return true, si hay acceso, false en caso contrario.
	 */
	public boolean isOnline() {	
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnectedOrConnecting())
			return true;

		return true; //0001
	}
	
	
	
	/**
	 * metodo para ocultar el teclado
	 * @param editText
	 */
	private void ocultarTeclado(EditText editText) {
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
	

	
	

	
	
	
}//final de la clase
