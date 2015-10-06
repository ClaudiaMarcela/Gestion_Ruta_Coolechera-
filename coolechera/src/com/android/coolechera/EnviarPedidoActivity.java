package com.android.coolechera;



import com.android.business.object.DataBaseBO;
import com.android.conexion.Sincronizador;
import com.android.conexion.Sync;
import com.android.conexion.constantes.Const;
import com.android.util.Constantes;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
public class EnviarPedidoActivity extends Activity implements Sincronizador{

	//***DEFINICION DE CONSTANTES USADAS EN LA CLASE*/
	private static final int ENVIAR_STATUS = 1;
	private static final int ENVIAR_DONE_OK = 2;
	private static final int ENVIAR_DONE_ERROR = 3;
	private static final int ENVIAR_CONFIRMATION = 4;

	// UI referencias
	private View enviarInfoStatus;
	private View enviarInfoDoneOK;
	private View enviarInfoDoneError;
	private View enviarInfoConfirmation;
	private TextView enviarInfoErrorMessage;
	private ImageButton buttonDone;
	private ImageButton buttonConfirmation;
	private ImageButton buttonDoneError;

	/**
	 * atributo para identificar que view mostrar al inicio.
	 */
	private boolean confirmation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enviar_pedido_activity);

		Bundle bundle = getIntent().getExtras();

		/*cargar confirmation, si es true significa que la actividad es lanzada desde IniciarDiaActiviy, en la verificacion 
		 * de hayPedidosPorEnviar, ya que no se puede iniciar dia, sin antes enviar los pedidos*/
		confirmation = bundle.getBoolean(Constantes.CONFIRMATION);

		//cargar referencias de UI
		enviarInfoStatus = findViewById(R.id.enviarInfo_status);
		enviarInfoDoneOK = findViewById(R.id.enviarInfo_done_ok);
		enviarInfoDoneError = findViewById(R.id.enviarInfo_done_error);	
		enviarInfoConfirmation = findViewById(R.id.enviarInfo_confirmation);
		enviarInfoErrorMessage = (TextView)findViewById(R.id.enviarInfo_message_done_error);

		//definicion de eventos de botones
		buttonDone = (ImageButton) findViewById(R.id.imageButton_enviar_ok);
		buttonDone.setOnClickListener(listenerIniciarDiaOK);	

		buttonDoneError = (ImageButton) findViewById(R.id.imageButton_enviar_error);
		buttonDoneError.setOnClickListener(listenerEnviarPedidoError);

		buttonConfirmation = (ImageButton) findViewById(R.id.imageButton_enviar_confirmation);
		buttonConfirmation.setOnClickListener(listenerEnviarPedidoError);

		//se inicia el proceso.
		enviarInfo();
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
	private OnClickListener listenerEnviarPedidoError = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EnviarPedidoActivity.this, EnviarPedidoActivity.class);
			boolean confirmation = false;
			intent.putExtra(Constantes.CONFIRMATION, confirmation);
			finish();
			startActivity(intent);
		}
	};



	/**
	 * Iniciar el proceso de conexion a sincronizador, muestra el pantallazo con el 
	 * progress de espera de respuesta al sincronizador.
	 */
	public void enviarInfo() {

		//limpiar textos basura en mensaje de error
		enviarInfoErrorMessage.setText("");

		// Mostrar progress spinner, e iniciar una tarea en background 
		// realizar el intento de inicio de dia.
		if(confirmation){
			//mostrar el view para solicitar confirmacion de envio
			showProgress(ENVIAR_CONFIRMATION);
		}
		else {
			//mostrar el view normal de envio de info.
			showProgress(ENVIAR_STATUS);
		}


		//se realiza conexion al sincronizador para descargar database.db
		enviarPedidoAlSincronizador();
	}

	/**
	 * enviar pedidos que estan en la base de datos local
	 */
	private void enviarPedidoAlSincronizador() {		

		//verificar que se puede establecer una conexion a internet. para poder conectar al sincronizador
		if (isOnline()) {

			//verificar que exista la base de datos para leer desde la main DataBase.db
			boolean existe = DataBaseBO.existeDataBase();
			//si existe = true, hacer lectura local (en DataBase.db) sino
			if (existe) {
				//conexion al sincronizador para enviar pedido
				Sync sync = new Sync(this, Const.ENVIAR_PEDIDO);
				sync.start();
			} else {
				//informar que no hay usuario en la base de datos
				enviarInfoErrorMessage.setText(getString(R.string.notBD));
				showProgress(ENVIAR_DONE_ERROR);
			}			
		}
		else {
			//informar que no hay conexion a internet
			enviarInfoErrorMessage.setText(getString(R.string.notNetwork));
			showProgress(ENVIAR_DONE_ERROR);
		}
	}



	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress(final int show) {
		//se define que view mostrar.
		enviarInfoStatus.setVisibility((show==ENVIAR_STATUS)? View.VISIBLE : View.GONE);
		enviarInfoConfirmation.setVisibility((show==ENVIAR_CONFIRMATION)? View.VISIBLE : View.GONE);
		enviarInfoDoneOK.setVisibility((show==ENVIAR_DONE_OK)? View.VISIBLE : View.GONE);
		enviarInfoDoneError.setVisibility((show==ENVIAR_DONE_ERROR)? View.VISIBLE : View.GONE);
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
				if(codeRequest == Const.ENVIAR_PEDIDO){
					//verificar que la base de datos ha sido descargada correctamente
					if(ok){
						//actualizar el estado de novedades a estado sincronizados.
						DataBaseBO.actualizarEstadoASincronizado(getBaseContext());

						//mostral pantalla de descarga correcta.
						showProgress(ENVIAR_DONE_OK);
					}
					else {
						//mostrar pantalla de error
						showProgress(ENVIAR_DONE_ERROR);
						enviarInfoErrorMessage.setText(respuestaServer);
					}
				}
			}
		});			
	}	
}//final de la clase
