package com.android.gestionruta;


import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.android.business.object.DataBaseBO;
import com.android.data.Tiempo;
import com.android.util.Util;
import com.google.android.gms.location.FusedLocationProviderApi;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author JICZ
 *
 */
public class LocationReceiver extends IntentService {
	
	/**
	 * Tiempo para evitar dobles inserciones.
	 */
	private long mLastClickTime = 0;
	
	/**
	 * consecutivo
	 */
	private long consecutivo = 0;
	
	/**
	 * TAG para logs e identificacion de clase en el resto de la aplicacion.
	 */
	protected static final String TAG = "location-Receiver-coolechera";


	/**
	 * Representa la localizacion geografica actualizada.
	 */
	private Location currentLocation;
	
	/**
	 * Representa la localizacion geografica anterior.
	 */
	private Location previousLocation;


	/**
	 * 
	 */
	public LocationReceiver() {
		super("LocationReceiver");
	}





	@Override
	protected void onHandleIntent(Intent intent) {
		
		final Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
		/*Validar que la captura no este nula para insertar en BD*/
		if(location != null){
			/*Impedir que se inserten datos continuos, evitar duplicidad*/
			if (SystemClock.elapsedRealtime() - mLastClickTime < 10000) {
				return;
			}
			mLastClickTime = SystemClock.elapsedRealtime();

			Log.i(TAG, "onHandleIntent generado en segundo plano, captura de coordenada");
			//captura de location.
			
			
			
			Tiempo tiempo = cargarTiemposCoordenada();

			/*Validar si la captura corresponde al horario laboral del vendedor*/
			boolean horarioValido = validarVentanaHoraria(tiempo);

			if(!horarioValido){
				LocationGPS.stopLocationUpdates();
				return;
			}

			/*Usado para calcular distancia entre los dos puntos usando WSG84*/
			float distanciaPuntoAnterior = 0f;

			if(previousLocation != null){
				distanciaPuntoAnterior = currentLocation.distanceTo(previousLocation);	
				previousLocation = currentLocation;
			}
			guardarDatosCoordenada(location, tiempo, distanciaPuntoAnterior);
		}
	}
	
	

	/**
	 * Permite verificar si la ventana horaria esta en el rango permitido de labores de los asesores.
	 * @param tiempo
	 * @return
	 */
	private boolean validarVentanaHoraria(Tiempo tiempo) {
		return DataBaseBO.validarVentanaHoraria(tiempo, this);
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
		tiempo.lastUpdateTime = Util.FechaActual("yyyy-MM-dd HH:mm:ss.SSS");
		return tiempo;
	}
	
	
	/**
	 * Guardar los datos de la coordenada capturada.
	 * @param lastUpdateTime 
	 * @param currentLocation
	 * @param distanciaPuntoAnterior 
	 */
	private void guardarDatosCoordenada(Location location, Tiempo tiempo, float distanciaPuntoAnterior) {
		Log.i(TAG, "guardar Datos Coordenada. termina ciclo de captura e inicia de nuevo.");
		Toast.makeText(this, "guardando coordenada", Toast.LENGTH_SHORT).show();
		if(location != null){
			double latitud = location.getLatitude();
			double longitud = location.getLongitude();
			consecutivo++;
			String vendedor = "803";
			DataBaseBO.insertarCoordendaSeguimiento(latitud,longitud,tiempo,consecutivo,vendedor, this);
		}
	}
}
