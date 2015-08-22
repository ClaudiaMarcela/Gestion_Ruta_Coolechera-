package co.com.ingsoftware3;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.com.uniquindio.database.DataBaseBO;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends Activity {
    
    
    
    /**
     * atributo define los proveedores de servicio de ubicacion
     */
    private LocationManager locationManager;
    
    /**
     * atributo para definir las acciones a realizar al recibir cada nueva actualización de la posición.
     */
    private LocationListener locationListener;
    
    
    public static ProgressDialog progressDialogIniciando;
    
    
    /**
     * control del tiempo de captura de la coordenada.
     */
    private Handler timer = new Handler();
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
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
    
    
    public void onClickCapturarCoordenada(View view){
	
	Toast.makeText(getBaseContext(), "Inicia captura por un maximo de 90 segundos!", Toast.LENGTH_LONG).show();
	progressDialogIniciando = ProgressDialog.show(MainActivity.this, "", "Capturando...", true);
	progressDialogIniciando.setCancelable(false);
	progressDialogIniciando.show();
	
	//inicia el timer de maximo dos minutos para forzar el cierre despues de pasar los dos minutos
	timer.postDelayed(forzarCierre, 1000 * 30 * 3); // 90 segundos (1.5 minutos) antes de ejecutarse el cierre. (tiempo en milisegundos)
	
	//iniciar gps
	gps();
    }
    
    
    
    
    /**
     * metodo para verificar que el servicio de gps este activado. en caso de no estarlo
     * lleva a la ventana de configuracion para activarlo
     */
    public void gps() {
	
	locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	
	boolean activado=false; 
	
	if (locationManager!=null) {
	    
	    activado =locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	if (activado==false) {
	    
	    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	    builder.setMessage("EL GPS ESTA DESACTIVADO, DEBE ACTIVARLO PARA CONTINUAR.")	
	    .setCancelable(false)
	    .setPositiveButton("Configuracion", new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int id) {
		    
		    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		    startActivity(settingsIntent); 
		    dialog.cancel();
		    System.exit(0);
		}
	    });
	    
	    AlertDialog alert = builder.create();
	    alert.show();
	    
	}else{
	    Log.i("formServiciosUbicacion", "inicia la captura de la posicion");
	    //leer la posicion de actual desde el gps
	    leerPosicion();			
	}
    }
    
    /**
     * metodo, implemtacion de la interface LocationListener, para la lectura de las coordenadas y de sus metodos asosciados.
     */
    private void leerPosicion() {
	locationListener  = new LocationListener() {
	    
	    public void onLocationChanged(Location location) {
		timer.removeCallbacks(forzarCierre); // detener el timer.
		Log.i("locationListener", "se ha detectado location changed inicia insercion en base de datos.");
		guardarPosicionBD(location);				
	    }
	    
	    public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	    }
	    
	    public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	    }
	    
	    public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub			
	    } 
	};
	
	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, locationListener);
    }
    
    
    
    
    
    /**
     * hilo que se ejecutara despues de los dos minutos definidos en el timer.
     */
    private Runnable forzarCierre = new Runnable() {
	
	public void run() {
	    forzarCierreDeCaptura();			
	}
    };
    
    
    
    /**
     * fuerza el cierre del proceso de captura de coordenadas, 
     * e informar al usuario para que cambie de ubicacion e intente de nuevo la captura.
     */
    protected void forzarCierreDeCaptura() {
	runOnUiThread(new Runnable() {
	    
	    public void run() {
		String mensaje = "" +
			"No se logra establecer comunicacion con el satelite GPS.\nPor favor cambie a una ubicacion " +
			"despejada o intente de nuevo mas tarde.";
		
		progressDialogIniciando.cancel(); //cerrar progress actual
		//definir un alert para informar y terminar activity
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Fallo Captura de Coordenada!");
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(mensaje);
		builder.setCancelable(false);
		builder.setPositiveButton("Salir", new Dialog.OnClickListener() {
		    
		    public void onClick(DialogInterface dialog, int arg1) {
			dialog.dismiss();
			timer.removeCallbacks(forzarCierre); // limpiar timer.
			finish(); // terminar activity.						
		    }
		});	
		AlertDialog alert = builder.create();
		alert.show();
	    }
	});		
    }
    
    
    
    public static String obtenerFechaActual() 
    {
	Date date = new Date(); 
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	return sdf.format(date);
    }
    
    
    /**
     * metodo que guarda la informacion capturada por gps en la base de datos temp.db, tabla Ubicacion
     * @param location
     */
    protected void guardarPosicionBD(Location location) {
	
	//obtener fecha actual
	String fecha = obtenerFechaActual();
	
	//verificar que hay datos para insertar. capturar las coordenadas.
	if(location != null) {
	    
	    double latitud = location.getLatitude();
	    double longitud = location.getLongitude();
	    
	    //insertar en la base de datos.			
	    DataBaseBO.guardarPosicionActual(latitud, longitud, fecha, MainActivity.this);
	}
	else {
	    //si no hay datos, guardar ceros y la fecha
	    DataBaseBO.guardarPosicionActual(0, 0, fecha, MainActivity.this);
	}
	Log.i("locationListener", "se cierra la captura de nuevas corrdenadas");
	locationManager.removeUpdates(locationListener);
	MostrarMensaje(MainActivity.this, "CAPTURA DE COORDENADA FINALIZADA");
    }
    
    
    
    
    
    /**
     * mostrar mensaje al usuario de coordenada capturada, y lista para enviar al sync
     * cuando el usuario presione el boton aceptar.
     * @param context
     * @param formServiciosUbicacionActivity Sincronizador
     * @param mensaje
     */
    public void MostrarMensaje(final Context context, String mensaje) {
	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	builder.setMessage(mensaje)	
	.setCancelable(false)
	.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
	    
	    public void onClick(DialogInterface dialog, int id) {					
		dialog.cancel();
		//enviar coordenada al sincronizador
		MainActivity.this.finish();
	    }		
	});
	
	AlertDialog alert = builder.create();
	alert.show();
    }
}



