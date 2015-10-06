/**
 * 
 */
package com.android.conexion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.android.conexion.constantes.Const;
import com.android.data.DataSync;

/**
 * @author JICZ
 *
 */
public class LoginSync {
	
	/**
	 * tag de identificacion
	 */
	private final static String TAG = "LoginSync";
	
	/**
	 * usuario
	 */
	public String user;
	
	/**
	 * contraseña
	 */
	public String password;
	
	/**
	 * codigo de respuesta.
	 */
	public int codeRequest;

	
	
	
	/**
	 * constructor de la clase
	 * @param user
	 * @param password
	 */
	public LoginSync(String user, String password, int codeRequest) {
		this.user = user;
		this.password = password;
		this.codeRequest = codeRequest;
	}
	





	/**
	 * metodo para loguear un vendedor atraves del sincronizador.
	 * se requiere acceso a internet
	 * @return
	 */
	public  DataSync logIn() {
		boolean ok = false;
		String mensaje = "";
		String respuestaServer = "";
		HttpURLConnection urlConnection = null;

		try {
			String strURL = Const.URL_SYNC + "login.aspx?un=" + this.user + "&pw=" + this.password;
			Log.i("LogIn", "URL Login: " + strURL);

			URL url = new URL(strURL);
			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Cache-Control", "no-cache");
			urlConnection.setRequestProperty("Pragma", "no-cache");
			urlConnection.setRequestProperty("Expires", "-1");
			urlConnection.setDoInput(true);

			/**
			 * SE DEFINE EL TIEMPO DE ESPERA, MAXIMO ESPERA 1 MINUTO
			 **/
			urlConnection.setConnectTimeout(60*1000);
			urlConnection.setReadTimeout(2*60*100);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			int statusCode = urlConnection.getResponseCode();

			if (statusCode == HttpURLConnection.HTTP_OK) {
				/*****************************************************************
				 * Si la Solicitud al Servidor fue Satisfactoria, lee la
				 * Respuesta
				 *****************************************************************/
				String line = null;
				while ((line = reader.readLine()) != null) {
					respuestaServer += line;
				}

				if (respuestaServer.startsWith("ok")) {
					ok = true;
					mensaje = "Login realizado con exito";

				} else {

					mensaje = "No se pudo Iniciar Sesion.\n" + respuestaServer;
				}

			} else if (statusCode == -1) {

				mensaje = "No se pudo Iniciar Sesion. Pagina No Encontrada: LogIn.aspx";

			} else {

				mensaje = MensajeHttpError(statusCode, "LogIn.aspx");
			}

		} catch (Exception e) {

			String motivo = e.getMessage();
			Log.e(TAG, "logIn -> " + motivo, e);

			if (motivo != null && motivo.startsWith("http://"))
				motivo = "Pagina no Encontrada: LogIn.aspx";

			mensaje = "No se pudo Iniciar Sesion";

			if (motivo != null)
				mensaje += "\n\nMotivo: " + motivo;

		} finally {

			if (urlConnection != null)
				urlConnection.disconnect();
		}
		
		DataSync data = new DataSync();
		data.ok = ok;
		data.mensaje = mensaje;
		data.respuestaServer = respuestaServer;
		data.codeRequest = codeRequest;
		return data;
	}
	

	


	/**
	 * captura de mensajes de error por fallas  de acceso a internet.
	 * @param statusCode
	 * @param pagina
	 * @return
	 */
	private String MensajeHttpError(int statusCode, String pagina) {
		switch (statusCode) {

		case HttpStatus.SC_NOT_IMPLEMENTED:
			return "Conexion no Disponible: No Implementado.";

		case HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED:
			return "Conexion no Disponible: Version No Soportada.";

		case HttpStatus.SC_INTERNAL_SERVER_ERROR:
			return "Conexion no Disponible: Error Interno.";

		case HttpStatus.SC_GATEWAY_TIMEOUT:
			return "Conexion no Disponible: Tiempo de Conexion Excedido.";

		case HttpStatus.SC_BAD_GATEWAY:
			return "Conexion no Disponible: Mala Conexion.";

		case HttpStatus.SC_NOT_FOUND:
			return "Pagina No Encontrada: " + pagina + ".";

		default:
			return "Conexion no Disponible. Http Error Code: " + statusCode
					+ ".";
		}
	}
}
