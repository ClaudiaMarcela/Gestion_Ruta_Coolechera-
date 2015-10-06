/**
 * 
 */
package com.android.gestionruta;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.android.business.object.DataBaseBO;
import com.android.data.Tiempo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * ejemplo: http://www.androidhive.info/2015/02/android-location-api-using-google-play-services/
 * Esta clase permite el uso de:<br>
 * <b>Google Play Services Location APIs.</b> Para aplicaciones que hacen uso de
 * localizacion.<br>
 * Disponible en:
 * <a href="https://developer.android.com/training/location/index.html">https://
 * developer.android.com/training/location/index.html</a> <br>
 * <br>
 * Para el desarrollo del modulo: <b> capturar coordenadas </b><br>
 * Se hace uso del metodo <b>Receiving Location Updates</b> visite: <a href=
 * "https://developer.android.com/training/location/receive-location-updates.html">
 * Receiving Location Updates </a> <br>
 * A traves de:<a href=
 * "https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi">
 * <b>FusedLocationProviderApi</b> </a>
 * 
 * <br>
 * Para mayor informacion sobre el uso de las API visite: <br>
 * <a href="https://developers.google.com/android/guides/setup"> https://
 * developers.google.com/android/guides/setup</a>
 * 
 * <br>
 * <br>
 * Esta clase usa el patron de diseño <b>Singleton</b>, con el fin de minimizar las
 * fallas relacionadas a multiples instancias que podrian provocar un fallo en
 * la integridad de los datos, puesto que multiples instancias de esta clase
 * podrian capturar coordendas al mismo tiempo, provocando multiplicidad de los
 * datos y un evidente desborde de informacion por el hecho de capturar gran
 * cantidad de coordendas.
 * 
 * 
 * @author 1-Claudia Marcela Barrera C. <br>
 *         2-Juan David Amaya. <br>
 *         3-Jorge Castro.
 *
 */
public class LocationGPS implements ConnectionCallbacks, OnConnectionFailedListener {

	/**
	 * TAG para logs e identificacion de clase en el resto de la aplicacion.
	 */
	protected static final String TAG = "location-updates-coolechera";

	/**
	 * tasa en milisegundos en el cual se prefiere recibir actualizaciones de ubicacion.
	 */
	private static final long INTERVAL_RATE = 60000;

	/**
	 * tasa en milisegundos en la que se puede manejar actualizaciones de ubicaci�n.
	 * Limite superior a INTERVAL_RATE. se recomienda la mitad de INTERVAL_RATE.
	 */
	private static final long FASTEST_RATE = INTERVAL_RATE / 2;

	/**
	 * Desplazamiento m�nimo entre cambios de ubicacion. (en metros)
	 */
	private static final int DISPLACEMENT = 10;

	/**
	 * Proporciona el punto de entrada a los servicios de Google Play.
	 */
	private static GoogleApiClient googleApiClient;

	/**
	 * Almacena los parametros de las solicitudes a FusedLocationProviderApi.
	 * La configuracion que se neceita para obtener los datos de localizacion
	 */
	private LocationRequest locationRequest;
	
	/**
	 * Representa la localizacion geografica actualizada.
	 */
	private Location currentLocation;
	
	/**
	 * Representa la localizacion geografica anterior.
	 */
	private Location previousLocation;

	/**
	 * Contexto (Activity) desde donde es llamado el servicio de localizacion.
	 */
	private Context context;
	

	/**
	 * Atributo private que conserva la instancia unica que se va a generar para la captura de coordenadas.
	 */
	private static LocationGPS locationGPS;
	
	/**
	 * inicia el servicio para ejecucion en background con PendingIntent.
	 * mire el metodo {@link #startLocationUpdates()}
	 */
	private Intent intent;
	
	/**
	 * Servicio de segundo plano.
	 */
	private static PendingIntent locationIntent; 

	


	/**
	 * Constructor private para implementacion del patrn Singleton para esta clase.
	 * @param context
	 */
	private LocationGPS(Context context) {
		super();
		this.context = context;
		Log.i(TAG, "Creando instancia de LocationGPS ");
	}
	
	
	/**
	 * Metodo statico para capturar la instancia que genera el patron Singleton
	 * @param context
	 * @return la instancia de LocationGPS para manejo de coordenadas.
	 */
	public static LocationGPS getInstance(Context context){
		if(locationGPS == null){
			locationGPS = new LocationGPS(context);
		}
		return locationGPS;
	}

	
	
	/**
	 * Realizar las validaciones necesarias para iniciar el servicio de captura
	 * de coordenadas de GoogleApiClient.
	 */
	public boolean iniciarGoogleApiClient(){
		Log.i(TAG, "iniciar GoogleApiClient");
		if(locationGPS != null){
			buildGoogleApiClient();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Crea la instancia de la API de google, especificando que vamos 
	 * a usar los servicios de Localizacion con <b>LocationServices.API</b>.
	 */
	private synchronized void buildGoogleApiClient() {
		Log.i(TAG, "Building GoogleApiClient");
		/* se especifca que se usaran los servicios de localizacion ofrecidos por google.*/
		googleApiClient = new GoogleApiClient.Builder(context, this, this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API) 
				.build();
		createLocationRequest();
	}
	
	
	/**
	 * Intentar conectar con el servicio
	 */
	public void conectToGoogleApiClient(){
		if(locationGPS != null && googleApiClient != null){
			if(!googleApiClient.isConnected()){
				googleApiClient.connect();
				Log.i(TAG, " GoogleApiClient Conectado");
			}
			else {
				googleApiClient.reconnect();
				Log.i(TAG, " GoogleApiClient reconnect");
			}
		}
	}
	
	
	/**
	 * Configurar las peticiones que se haran de ubicacion.
	 * se define la precision y el intervalo.
	 */
	private void createLocationRequest() {
		locationRequest = new LocationRequest();

		/* Definir el Intervalo de actualizacion deseado.
		 * Este intervalo es inexacto, no se pueden recibir actualizaciones de localizacion 
		 * si no hay proveedores activados, o no hay conexion a internet.
		 * Tambien se puede recibir actualizaciones mas rapido si hay otra aplicacion haciendo
		 * la misma peticion de actualizacion.
		 * */
		locationRequest.setInterval(INTERVAL_RATE);

		/* Este intervalo es exacto y nunca se recibiran actualizaciones mas rapido que este valor
		 * Se recomienda usar la mitad de INTERVAL_RATE */
		locationRequest.setFastestInterval(FASTEST_RATE);

		/* Se define una prioridad de alta precision para captura de localizacion*/
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
		/*Se define la distancia minima para iniciar una nueva peticion de coordenada.*/
		locationRequest.setSmallestDisplacement(DISPLACEMENT);
	}





	/**
	 * Pedir actualizaciones de ubicacion a FusedLocationApi.
	 * Capturar en el proceso de seguno plano locationIntent.
	 */
	private synchronized void startLocationUpdates() {
		
		/*Crear proceso de segundo plano para capturar coordendas.*/
		if(intent == null){
			//crear el servicio que recibirá en background los cambios de location.
			intent = new Intent(this.context, LocationReceiver.class);
			locationIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		
		LocationServices.FusedLocationApi.requestLocationUpdates( googleApiClient, locationRequest, locationIntent);
		Log.i(TAG, " GoogleApiClient requestLocationUpdates FusedLocationApi ");
	}
	
	
	/**
	 * Detener las actualizaciones de ubicacion a FusedLocationApi. 
	 */
	public static void stopLocationUpdates() {
		Log.i(TAG, " GoogleApiClient Detener servicio");
		LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationIntent);
	}


	/**
	 * Sobrecargado desde interface ConnectionCallbacks,
	 * se llama cuando se conecta a GoogleApiClient.
	 * y se inicia el proceso de captura de ubicacion en modo: Receiving Location Updates
	 */
	@Override
	public void onConnected(Bundle connectionHint) {

        Log.i(TAG, "Connected to GoogleApiClient");

        /*Capturar la ultima ubicacion disponible si es posible.*/
        if (currentLocation == null) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            previousLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            
            /*Cargar informacion del tiempo de captura.*/
            Tiempo tiempo = cargarTiemposCoordenada();

    		/*Validar si la captura corresponde al horario laboral del vendedor*/
    		boolean horarioValido = validarVentanaHoraria(tiempo);

    		if(!horarioValido){
    			Log.i(TAG, "Fuera de ventana horaria -  se detiene el servicio.");
    			stopLocationUpdates();
    			return;
    		}
    		Log.i(TAG, "Ventana horaria - Correcto!");
            guardarDatosCoordenada(currentLocation, tiempo, 0f);
        }
        
        //iniciar modo Receiving Location Updates
        startLocationUpdates();
	}

	


	/**
	 * Sobrecargado desde interface ConnectionCallbacks
	 */
	@Override
	public void onConnectionSuspended(int result) {
		// TODO Auto-generated method stub

	}


	/**
	 * Sobrecargado desde interface OnConnectionFailedListener
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		int errorCode = result.getErrorCode();
		Toast.makeText(context, "Failed - Error: " + errorCode, Toast.LENGTH_SHORT).show();
	}


	
	
	/**
	 * Permite conservar todas las definiciones de tiempo, respecto al momento en que se captura la coordenada
	 * @return
	 */
	private Tiempo cargarTiemposCoordenada() {
		/*Conservar las fechas de la ubicacion capturada.*/
		Calendar fechaActual = Calendar.getInstance(Locale.getDefault());
		Tiempo tiempo = new Tiempo();
		tiempo.year = fechaActual.get(Calendar.YEAR);
		tiempo.Month = fechaActual.get(Calendar.MONTH) + 1;
		tiempo.dayOfMonth = fechaActual.get(Calendar.DAY_OF_MONTH);
		tiempo.hour = fechaActual.get(Calendar.HOUR_OF_DAY);
		tiempo.minute = fechaActual.get(Calendar.MINUTE);
		tiempo.lastUpdateTime = DateFormat.getTimeInstance().format(fechaActual.getTime());
		return tiempo;
	}


	/**
	 * Permite verificar si la ventana horaria esta en el rango permitido de labores de los asesores.
	 * @param tiempo
	 * @return
	 */
	private boolean validarVentanaHoraria(Tiempo tiempo) {
		return DataBaseBO.validarVentanaHoraria(tiempo, context);
	}


	/**
	 * Guardar los datos de la coordenada capturada.
	 * @param lastUpdateTime 
	 * @param currentLocation
	 * @param distanciaPuntoAnterior 
	 */
	private void guardarDatosCoordenada(Location location, Tiempo tiempo, float distanciaPuntoAnterior) {
		Toast.makeText(context, "guardando coordenada", Toast.LENGTH_SHORT).show();
	}
}
