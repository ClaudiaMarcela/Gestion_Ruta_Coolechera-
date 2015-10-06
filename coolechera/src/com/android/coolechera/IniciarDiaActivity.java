package com.android.coolechera;




import java.io.File;


import com.android.business.object.DataBaseBO;
import com.android.conexion.Sincronizador;
import com.android.conexion.Sync;
import com.android.conexion.constantes.Const;
import com.android.data.Login;
import com.android.data.Vendedor;
import com.android.util.Constantes;
import com.android.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 * Esta activity no se guarda en la pila de activitis del ciclo de vida, esto con el fin de optimizar; 
 * debido a que esta actividad no es necesario conservar su informacion.
 */
public class IniciarDiaActivity extends Activity implements Sincronizador{

	//***DEFINICION DE CONSTANTES USADAS EN LA CLASE*/
	private static final int DIA_STATUS = 1;
	private static final int DIA_DONE_OK = 2;
	private static final int DIA_DONE_ERROR = 3;
	private static final int LUNES_JUEVES = 12; //POS 0
	private static final int MARTES_VIERNES = 22; // POS 1
	private static final int MIERCOLES_SABADO = 32; // POS 2


	/**
	 * determina la frecuencia de rutero a descargar.
	 * se define lunes-jueves por defecto.
	 */
	private int frecuencia_ruta = LUNES_JUEVES;

	// UI referencias
	private View iniciandoDiaStatus;
	private View iniciandoDiaDoneOK;
	private View iniciandoDiaDoneError;
	private TextView iniciandoDiaStatusMessage;
	private TextView iniciandoDiaDoneErrorMessage;
	private ImageButton buttonDone;
	private ImageButton buttonDoneError;
	
	//progress dialog nueva version
	ProgressDialog progressDialog;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.iniciar_dia_activity);

		//cargar referencias de UI
		iniciandoDiaStatus = findViewById(R.id.iniciandoDia_status);
		iniciandoDiaDoneOK = findViewById(R.id.iniciando_done_ok);
		iniciandoDiaDoneError = findViewById(R.id.iniciando_done_error);	
		iniciandoDiaStatusMessage = (TextView)findViewById(R.id.iniciando_message_done);
		iniciandoDiaDoneErrorMessage = (TextView)findViewById(R.id.iniciando_message_done_error);

		//definicion de eventos de botones
		buttonDone = (ImageButton) findViewById(R.id.imageButton_iniciar_dia_ok);
		buttonDone.setOnClickListener(listenerIniciarDiaOK);	

		buttonDoneError = (ImageButton) findViewById(R.id.imageButton_iniciar_dia_error);
		buttonDoneError.setOnClickListener(listenerIniciarDiaError);

		mostrarDialogElegirFrecuenciaRuta();		
	}


	/**
	 * handler para evento onclik del boton terminar dia de forma correcta.
	 */
	private OnClickListener listenerIniciarDiaOK = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			finish();			
		}
	};

	/**
	 * handler para evento onclik del boton terminar dia con error
	 * se intenta hacer una nueva conexion. se reinicia la actividad.
	 */
	private OnClickListener listenerIniciarDiaError = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(IniciarDiaActivity.this, IniciarDiaActivity.class);
			finish();
			startActivity(intent);
		}
	};



	/**
	 * Iniciar el proceso de conexion a sincronizador, muestra el pantallazo con el 
	 * progress de espera de respuesta al sincronizador.
	 */
	public void iniciarDia() {

		boolean existe = DataBaseBO.existeDataBase();
		//verificar si existe la base de datos para comprobar si hay pedidos. 
		if(existe){
			//verificar si hay pedidos por enviar, se debe enviar pedidos antes de iniciar dia.
			boolean hayPedidosPorEnviar = DataBaseBO.verificarPedidosPorEnviar(getBaseContext());
			if(hayPedidosPorEnviar){
				//lanzar activity para enviar los pedidos pendientes.
				Intent i = new Intent(IniciarDiaActivity.this, EnviarPedidoActivity.class); 
				boolean confirmation = true;
				i.putExtra(Constantes.CONFIRMATION, confirmation);
				startActivity(i);
				finish();
			}
		}		
		//limpiar textos basura en mensaje de error
		iniciandoDiaDoneErrorMessage.setText("");

		// Mostrar progress spinner, e iniciar una tarea en background 
		// realizar el intento de inicio de dia.
		iniciandoDiaStatusMessage.setText(R.string.iniciando_progress_dia);
		showProgress(DIA_STATUS);	

		//se realiza conexion al sincronizador para descargar database.db
		descargarBaseDatosDelSincronizador();
	}

	/**
	 * metodo para conexion al sincronizador, permite descargar la database.db y temp.db en 
	 * la carpeta coolechera de la memoria del telefono; para que el usuario 
	 * pueda actualizar informacion de rutas o para iniciar un dia de trabajo.
	 */
	private void descargarBaseDatosDelSincronizador() {

		String user = "";		

		//verificar que se puede establecer una conexion a internet. para poder conectar al sincronizador
		if (isOnline()) {

			//verificar que exista la base de datos para leer desde la main DataBase.db
			boolean existe = DataBaseBO.existeDataBase();
			//si existe = true, hacer lectura local (en DataBase.db) sino, hacer lectura de SharedPreferences
			if (existe) {
				Login login = DataBaseBO.login();
				if(login == null){
					//obtener el usuario desde el sharedPreference llamado login
					SharedPreferences persistLogin = getSharedPreferences("login", Context.MODE_PRIVATE);
					user = persistLogin.getString("user", "nouser");

				} else {
					user = login.user;
				}
			} else {
				//obtener el usuario desde el sharedPreference llamado login
				SharedPreferences persistLogin = getSharedPreferences("login", Context.MODE_PRIVATE);
				user = persistLogin.getString("user", "nouser");
			}

			//conexion al sincronizador para descargar base de datos.
			Sync sync = new Sync(this, Const.DOWNLOAD_DATA_BASE);
			sync.usuario = user;
			sync.frecuencia_ruta = frecuencia_ruta;
			sync.start();
		}
		else {
			//informar que no hay conexion a internet
			iniciandoDiaDoneErrorMessage.setText(getString(R.string.notNetwork));
			showProgress(DIA_DONE_ERROR);
		}
	}



	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress(final int show) {

		//se define que view mostrar.
		iniciandoDiaStatus.setVisibility((show==DIA_STATUS)? View.VISIBLE : View.GONE);
		iniciandoDiaDoneOK.setVisibility((show==DIA_DONE_OK)? View.VISIBLE : View.GONE);
		iniciandoDiaDoneError.setVisibility((show==DIA_DONE_ERROR)? View.VISIBLE : View.GONE);
	}


	/**
	 * metodo para verificar si el movil tiene conexion a internet para poder hacer el login del vendendor
	 * @return true, si hay acceso, false en caso contrario.
	 */
	private boolean isOnline() {	
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();

		//verificar que existe una conexion usable a internet
		if (networkInfo != null && networkInfo.isConnectedOrConnecting() && networkInfo.isConnected()){
			return true;
		}
		return false; //0001
	}



	/**
	 * metodo para ocultar el teclado
	 * @param editText
	 */
	public void ocultarTeclado(EditText editText) {
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}


	/**
	 * metodo para mostrar un dialog al usuario, para que seleccione la frecuencia de ruta con 
	 * la que iniciara dia.
	 */
	private void mostrarDialogElegirFrecuenciaRuta(){

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Seleccione Frecuencia Rutero");
		builder.setCancelable(false);
		builder.setSingleChoiceItems(R.array.frecuencia_ruta, -1, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//definicion de la frecuencia de rutero
				if(which == 1){
					frecuencia_ruta = MARTES_VIERNES;
				}
				if(which == 2){
					frecuencia_ruta = MIERCOLES_SABADO;
				}				
				//se inicia el proceso.
				iniciarDia();
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	
	/**
	 * metodo para cargar la version actual de la aplicacion.
	 * @return
	 */
	public String ObtenerVersion() {
		String versionApp;
		try {
			//obtener la version actual de la aplicacion.
			versionApp = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionApp = "0.0";
			Log.e("FormMenuEstadistica", "ObtenerVersion: " + e.getMessage(), e);
		}
		return versionApp;
	}
	
	
	/**
	 * metodo que permite actualizar a una nueva version si se detecta una version mayor.
	 */
	public void ActualizarVersionApp() {

		//obtener version de la tabla descargada.
		final String versionSvr = DataBaseBO.ObtenerVersionApp();
		//obtener version del dispositivo.
		String versionApp = ObtenerVersion();

		if (versionSvr != null && versionApp != null) {

			float versionServer = com.android.util.Util.ToFloat(versionSvr.replace(".", ""));
			float versionLocal = com.android.util.Util.ToFloat(versionApp.replace(".", ""));

			if (versionLocal < versionServer) {

				AlertDialog.Builder builder = new AlertDialog.Builder(IniciarDiaActivity.this);
				builder.setMessage("Hay una nueva version de la aplicacion: " + versionSvr)	
				.setCancelable(false)
				.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						progressDialog = ProgressDialog.show(IniciarDiaActivity.this, "", "Descargando Version " + versionSvr + "...", true);
						progressDialog.show();
						Sync sync = new Sync(IniciarDiaActivity.this, Const.DOWNLOAD_VERSION_APP);
						sync.start();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();

			} else {
				Vendedor v = DataBaseBO.obtenerVendedor();
				String mensaje = v.nombre + "\nCC: " + v.cedula + "\n";
				mensaje += getString(R.string.inicio_dia_terminar);
				iniciandoDiaStatusMessage.setText(mensaje);
				//mostral pantalla de descarga correcta.
				showProgress(DIA_DONE_OK);
			}
		}
	}





	/**
	 * captura respuesta del sincronizador
	 * @param ok
	 * @param respuestaServer
	 * @param msg
	 * @param codeRequest
	 */
	@Override
	public void RespSync(final boolean ok, final String respuestaServer, String msg, final int codeRequest) {

		//ejecutar la siguiente accion en el hilo principal, debido a cambio de View es necesario hacerlo.
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				//verificar que el codigo de respuesta coincida con el enviado, (descargar base de datos)
				if(codeRequest == Const.DOWNLOAD_DATA_BASE) {
					//verificar que la base de datos ha sido descargada correctamente
					if(ok){
						ActualizarVersionApp();						
					}
					else {
						//mostrar pantalla de error
						showProgress(DIA_DONE_ERROR);
						iniciandoDiaDoneErrorMessage.setText(respuestaServer);
					}
				}
				//codigo de respuesta de nueva version descargada.
				else if(codeRequest == Const.DOWNLOAD_VERSION_APP) {
					if(ok){

						File fileApp = new File(Util.DirApp(), Constantes.fileNameApk);

						if (fileApp.exists()) {
							Uri uri = Uri.fromFile(fileApp);
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(uri, "application/vnd.android.package-archive");
							startActivityForResult(intent, Constantes.RESP_ACTUALIZAR_VERSION);
						} else {
							Util.MostrarAlertDialog(IniciarDiaActivity.this, "No se pudo actualizar la version.");
						}
					}
					else {
						//mostrar pantalla de error
						showProgress(DIA_DONE_ERROR);
						iniciandoDiaDoneErrorMessage.setText(respuestaServer);
					}										
				}
			}
		});			
	}
	
}//final de la clase
