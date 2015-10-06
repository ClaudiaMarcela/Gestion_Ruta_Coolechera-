package com.android.conexion;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpStatus;

import com.android.business.object.DataBaseBO;
import com.android.conexion.constantes.Const;
import com.android.data.Vendedor;
import com.android.util.Constantes;
import com.android.util.Util;

import android.util.Log;


public class Sync extends Thread {

	private final static String TAG = "Conexion.Sync";

	boolean ok; // Indica si la Respuesta del Servidor fue OK o ERROR
	int codeRequest; // Indica el tipo de codeRequest a Sincronizar (Login,
	// DowloadDataBase, ....)
	Sincronizador sincronizador; // Clase que se encarga de procesar la
	// Respuesta del Servidor cuando finaliza el
	// proceso de Sincronizacion.

	// Usuario usuario;
	public String usuario;
	public String clave;
	public String version;
	public String imei;
	public String consecutivo;
	public int frecuencia_ruta;

	public int timeout = 2 * 60 * 1000; // 2 Minutos de Timeout para descargar y
	// enviar informacion!
	public int timeoutOne = 60 * 1000; // 1 Minuto de Timeout para Iniciar
	// Sesion con el Servidor y Terminar
	// labores
	public String cadenaEnviar = "";

	/**
	 * Guarda el mensaje de la ejecucion de la sincrionizacion Puede ser un
	 * mensaje de Ok o de Error
	 **/
	String mensaje;

	/**
	 * Guarda la respuesta del servidor, Puede ser un mensaje de Ok o de Error
	 **/
	String respuestaServer = "";

	/**
	 * Contructor de la clase. Sincronizador es una interfaz que deben
	 * implementar las Actividades que sincronizan datos
	 * 
	 * Cuando se termina la sincronizacion, se llama el metodo RespSync de la
	 * actividad que invoco el Sync con el estatus de la Sincronizacion
	 **/
	public Sync(Sincronizador sincronizador, int codeRequest) {
		this.sincronizador = sincronizador;
		this.codeRequest = codeRequest;
	}

	public void run() {

		switch (codeRequest) {

		case Const.LOGIN:
			LogIn();
			break;

		case Const.DOWNLOAD_DATA_BASE:
			DownloadDataBase();
			break;

		case Const.ENVIAR_PEDIDO:
			EnviarPedido();
			break;

		case Const.DOWNLOAD_VERSION_APP:
			DownloadVersionApp();
			break;

		case Const.STATUS_SENAL:
			senalStatus();
			break;

		case Const.DOWNLOAD_MESSAGE:
			downloadMessages();
			break;

		case Const.TERMINAR_LABORES:
			TerminarLabores();
			break;
		}
	}

	// ////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////
	public void LogIn() {
		boolean ok = false;
		respuestaServer = "";
		HttpURLConnection urlConnection = null;

		try {
			String strURL = Const.URL_SYNC + "login.aspx?un=" + this.usuario + "&pw=" + this.clave;
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
			urlConnection.setConnectTimeout(timeoutOne);
			urlConnection.setReadTimeout(timeoutOne);

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

		sincronizador.RespSync(ok, respuestaServer, mensaje, codeRequest);
	}

	
	
	
	
	public String MensajeHttpError(int statusCode, String pagina) {
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

	// ////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////

	public void DownloadDataBase() {

		boolean ok = false;
		InputStream inputStream = null;
		FileOutputStream fileOutput = null;
		HttpURLConnection urlConnection = null;

		try {
			/************************************
			 * Carga la Configuracion del Vendedor.
			 ************************************/
			//			Vendedor config = DataBaseBO.obtenerVendedor();


			String urlDataBase = Const.URL_SYNC + "creardb.aspx?un=" + this.usuario + "&ruta=" + frecuencia_ruta;

			Log.i("DownloadDataBase", "URLDataBase = " + urlDataBase);

			URL url = new URL(urlDataBase);
			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Cache-Control", "no-cache");
			urlConnection.setRequestProperty("Pragma", "no-cache");
			urlConnection.setRequestProperty("Expires", "-1");
			urlConnection.setDoInput(true);

			/**
			 * SE DEFINE EL TIEMPO DE ESPERA, MAXIMO ESPERA 2 MINUTOS
			 **/
			urlConnection.setConnectTimeout(timeout);
			urlConnection.setReadTimeout(timeout);

			urlConnection.connect();
			inputStream = urlConnection.getInputStream();
			String contentDisposition = urlConnection
					.getHeaderField("Content-Disposition");

			if (contentDisposition != null) { // Viene Archivo Adjunto

				/**
				 * Se obtiene la ruta del SD Card, para guardar la Base de
				 * Datos. Y se crea el Archivo de la BD
				 **/
				String fileName = "Temporal.zip";
				File file = new File(Util.DirApp(), fileName);

				if (file.exists())
					file.delete();

				if (file.createNewFile()) {

					fileOutput = new FileOutputStream(file);

					long downloadedSize = 0;
					int bufferLength = 0;
					byte[] buffer = new byte[1024];

					/**
					 * SE LEE LA INFORMACION DEL BUFFER Y SE ESCRIBE EL
					 * CONTENIDO EN EL ARCHIVO DE SALIDA
					 **/
					while ((bufferLength = inputStream.read(buffer)) > 0) {

						fileOutput.write(buffer, 0, bufferLength);
						downloadedSize += bufferLength;
					}

					fileOutput.flush();
					fileOutput.close();
					inputStream.close();

					long content_length = Util.ToLong(urlConnection.getHeaderField("content-length"));

					if (content_length == 0) {
						mensaje = "No hay conexion, por favor intente de nuevo";
					} 
					else if (content_length != downloadedSize) { 
						// La longitud de descarga no es igual al Content Length del Archivo
						mensaje = "No se pudo descargar la base de datos, por favor intente de nuevo";

					} else {

						Descomprimir(file);
						ok = true;
						mensaje = "Descargo correctamente la Base de Datos";
					}

				} else {

					mensaje = "No se pudo crear el archivo de la Base de Datos";
				}

			} else { // No hay archivo adjunto, se procesa el Mensaje de
				// respuesta del Servidor

				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {

					respuestaServer += line;
				}

				if (respuestaServer.equals(""))
					mensaje = "No se pudo descargar la base de datos, por favor intente de nuevo";
				else
					mensaje = respuestaServer;
			}
		} catch (Exception e) {

			String motivo = e.getMessage();
			Log.e(TAG, "downloadDataBase -> " + motivo, e);

			if (motivo != null && motivo.startsWith("http://"))
				motivo = "Pagina no Encontrada: CrearDB.aspx";

			mensaje = "No se pudo descargar la Base de Datos";

			if (motivo != null)
				mensaje += "\n\nMotivo: " + motivo;

		} finally {

			try {

				if (fileOutput != null)
					fileOutput.close();

				if (inputStream != null)
					inputStream.close();

			} catch (IOException e) {
			}

			if (urlConnection != null)
				urlConnection.disconnect();
		}

		sincronizador.RespSync(ok, mensaje, mensaje, codeRequest);
	}

	
	
	

	public void Descomprimir(File fileZip) {

		try {

			if (fileZip.exists()) {

				String nameFile = fileZip.getName().replace(".zip", "");
				File fileZipAux = new File(Util.DirApp(), nameFile);

				if (!fileZipAux.exists())
					fileZipAux.delete();

				FileInputStream fin = new FileInputStream(fileZip);
				ZipInputStream zin = new ZipInputStream(fin);

				ZipEntry ze = null;

				while ((ze = zin.getNextEntry()) != null) {

					Log.v("Descomprimir", "Unzipping " + ze.getName());

					if (ze.isDirectory()) {

						dirChecker(ze.getName());

					} else {

						String pathFile = Util.DirApp() + "/" + ze.getName();
						File file = new File(pathFile);
						FileOutputStream fout = new FileOutputStream(file);

						// long bytes = 0;
						int bufferLength = 0;
						byte[] buffer = new byte[1024];

						while ((bufferLength = zin.read(buffer)) > 0) {

							fout.write(buffer, 0, bufferLength);
							// bytes += bufferLength;
						}

						zin.closeEntry();
						fout.flush();
						fout.close();
					}
				}
				zin.close();
			}

		} catch (Exception e) {

			Log.e("Descomprimir", e.getMessage(), e);
		}
	}

	
	
	public void EnviarPedido() {

		ok = false;
		String msg = "";

		Vendedor usuario = DataBaseBO.obtenerVendedor();

		if (usuario == null) {

			respuestaServer = mensaje = "No se pudo cargar la Informacion del Usuario. Por favor Inicie Dia";
			sincronizador.RespSync(ok, respuestaServer, mensaje, codeRequest);
			return;
		}

		if (ComprimirArchivo()) {

			File zipPedido = new File(Util.DirApp(), "Temp.zip");

			if (!zipPedido.exists()) {

				Log.i("EnviarPedido", "El archivo Temp.zip no Existe");
				sincronizador.RespSync(ok, "", "El archivo Temp.zip no Existe",	codeRequest);
				return;
			}

			DataOutputStream dos = null;
			HttpURLConnection conexion = null;
			BufferedReader bufferedReader = null;
			FileInputStream fileInputStream = null;

			byte[] buffer;
			int maxBufferSize = 1 * 1024 * 1024;
			int bytesRead, bytesAvailable, bufferSize;

			try {				

				if (usuario != null) {

					String urlUpLoad = Const.URL_SYNC + "RegistrarPedido.aspx?un=" + usuario.codigo + "&ext=zip&termino=0&fechaLabor=" + usuario.fechaLabores + "&co=" + usuario.consecutivo;
					Log.i("EnviarPedido", "URL Enviar Pedido = " + urlUpLoad);

					URL url = new URL(urlUpLoad);
					conexion = (HttpURLConnection) url.openConnection();

					conexion.setDoInput(true); // Permite Entradas
					conexion.setDoOutput(true); // Permite Salidas
					conexion.setUseCaches(false); // No usar cache

					/**
					 * SE ESTABLECEN LOS HEADERS
					 **/
					conexion.setRequestMethod("POST");
					conexion.setRequestProperty("Cache-Control", "no-cache");
					conexion.setRequestProperty("Pragma", "no-cache");
					conexion.setRequestProperty("Expires", "-1");

					conexion.setRequestProperty("Connection", "Keep-Alive");
					conexion.setRequestProperty("Content-Type","multipart/form-data; boundary=---------------------------4664151417711");

					/**
					 * SE DEFINE EL TIEMPO DE ESPERA, MAXIMO ESPERA 2 MINUTOS
					 **/
					conexion.setConnectTimeout(timeout);
					conexion.setReadTimeout(timeout);

					/**
					 * SE CREA EL BUFFER PARA ENVIAR LA INFORMACION DEL ARCHIVO
					 **/
					fileInputStream = new FileInputStream(zipPedido);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					buffer = new byte[bufferSize];

					/**
					 * ABRE LA CONEXION DEL FLUJO DE SALIDA. LEE Y ESCRIBE LA
					 * INFORMACION DEL ARCHIVO EN EL BUFFER Y ENVIA LA
					 * INFORMACION AL SERVIDOR.
					 **/
					dos = new DataOutputStream(conexion.getOutputStream());
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					while (bytesRead > 0) {
						dos.write(buffer, 0, bufferSize);
						bytesAvailable = fileInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);
					}

					dos.flush();
					Log.i("EnviarPedido", "Enviando informacion del Archivo");

					/**
					 * LEE LA RESPUESTA DEL SERVIDOR
					 **/
					bufferedReader = new BufferedReader(new InputStreamReader(
							conexion.getInputStream()));
					String line;

					respuestaServer = "";
					while ((line = bufferedReader.readLine()) != null) {

						respuestaServer += line;
					}

					if (respuestaServer.startsWith("listo")) {

						ok = true;
						DataBaseBO.BorrarInfoTemp();

					} else {

						if (respuestaServer.equals(""))
							msg = "Sin respuesta del servidor";
						else
							msg = respuestaServer;
					}

					Log.i("EnviarPedido", "respuesta: " + respuestaServer);

				}

			} catch (Exception ex) {

				String motivo = ex.getMessage();

				if (motivo != null && motivo.startsWith("http://"))
					motivo = "Pagina no Encontrada: Registrar Informacion";

				msg = "No se pudo Registrar Informacion";

				if (motivo != null)
					msg += "\n\nMotivo: " + motivo;

				Log.e(TAG, "enviarPedido -> " + msg, ex);

			} finally {

				try {

					if (bufferedReader != null)
						bufferedReader.close();

					if (fileInputStream != null)
						fileInputStream.close();

					if (dos != null)
						dos.close();

					if (conexion != null)
						conexion.disconnect();

				} catch (Exception e) {

					Log.e("FileUpLoad",
							"Error cerrando conexion: " + e.getMessage(), e);
				}
			}

		} else {

			msg = "Error comprimiendo la Base de datos Pedido";
			Log.e("FileUpLoad", msg);
		}

		if (respuestaServer.equals(""))
			respuestaServer = "error, Sin respuesta del servidor";

		sincronizador.RespSync(ok, respuestaServer, msg, codeRequest);
	}

	
	
	
	public void unzip(File fileZip) {

		try {

			if (fileZip.exists()) {

				String nameFile = fileZip.getName().replace(".zip", "");
				File dirCatalogo = new File(Util.DirApp(), nameFile);

				if (!dirCatalogo.exists())
					dirCatalogo.mkdir();

				FileInputStream fin = new FileInputStream(fileZip);
				ZipInputStream zin = new ZipInputStream(fin);

				ZipEntry ze = null;

				while ((ze = zin.getNextEntry()) != null) {

					Log.v("Decompress", "Unzipping " + ze.getName());

					if (ze.isDirectory()) {

						dirChecker(ze.getName());

					} else {

						String name = ze.getName();
						File imagefile = new File(Util.DirApp() + "/" + name);
						FileOutputStream fout = new FileOutputStream(imagefile);

//						long bytes = 0;
						int bufferLength = 0;
						byte[] buffer = new byte[1024];

						while ((bufferLength = zin.read(buffer)) > 0) {

							fout.write(buffer, 0, bufferLength);
//							bytes += bufferLength;
						}

						zin.closeEntry();
						fout.flush();
						fout.close();

					}
				}
				zin.close();
			}

		} catch (Exception e) {

			Log.e("Decompress", "unzip", e);
		}
	}

	public void DownloadVersionApp() {

		boolean ok = false;
		InputStream inputStream = null;
		FileOutputStream fileOutput = null;

		try {

			URL url = new URL(Const.URL_DOWNLOAD_NEW_VERSION);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			Log.i(TAG, "downloadVersionApp -> URL App = "+ Const.URL_DOWNLOAD_NEW_VERSION);

			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Cache-Control", "no-cache");
			urlConnection.setRequestProperty("Pragma", "no-cache");
			urlConnection.setRequestProperty("Expires", "-1");
			urlConnection.setDoInput(true);

			/**
			 * SE DEFINE EL TIEMPO DE ESPERA, MAXIMO ESPERA 2 MINUTOS
			 **/
			urlConnection.setConnectTimeout(timeout);
			urlConnection.setReadTimeout(timeout);

			urlConnection.connect();
			inputStream = urlConnection.getInputStream();

			File file = new File(Util.DirApp(), Constantes.fileNameApk);

			if (file.exists())
				file.delete();

			if (file.createNewFile()) {

				fileOutput = new FileOutputStream(file);

				long downloadedSize = 0;
				int bufferLength = 0;
				byte[] buffer = new byte[1024];

				/**
				 * SE LEE LA INFORMACION DEL BUFFER Y SE ESCRIBE EL CONTENIDO EN
				 * EL ARCHIVO DE SALIDA
				 **/
				while ((bufferLength = inputStream.read(buffer)) > 0) {

					fileOutput.write(buffer, 0, bufferLength);
					downloadedSize += bufferLength;
				}

				fileOutput.flush();
				fileOutput.close();
				inputStream.close();

				long content_length = Util.ToLong(urlConnection
						.getHeaderField("content-length"));

				if (content_length == 0) {

					ok = false;
					mensaje = "Error de conexion, por favor intente de nuevo";

				} else if (content_length != downloadedSize) { // La longitud de
					// descarga no
					// es igual al
					// Content
					// Length del
					// Archivo

					ok = false;
					mensaje = "Error descargando la nueva version, por favor intente de nuevo";

				} else {

					ok = true;
					mensaje = "Descargo correctamente la Nueva Version";
				}

			} else {

				mensaje = "Error Creando el Archivo de la Nueva Version";
				ok = false;
			}

		} catch (Exception e) {

			mensaje = "Error Descargando la Nueva version de la Aplicacion\n";
			mensaje += "Detalle Error: " + e.getMessage();
			Log.e("Sync DownloadVersionApp", e.getMessage(), e);
			ok = false;

		} finally {

			try {

				if (fileOutput != null)
					fileOutput.close();

				if (inputStream != null)
					inputStream.close();

			} catch (IOException e) {
			}
		}

		sincronizador.RespSync(ok, mensaje, mensaje, codeRequest);
	}

	private void dirChecker(String dir) {

		File f = new File(Util.DirApp().getPath() + "/" + dir);

		if (!f.isDirectory()) {

			f.mkdirs();
		}
	}

	public boolean ComprimirArchivo() {

		File zipPedido = new File(Util.DirApp(), "Temp.zip");

		if (zipPedido.exists())
			zipPedido.delete();

		FileOutputStream out = null;
		GZIPOutputStream gZipOut = null;
		FileInputStream fileInputStream = null;

		try {

			File dbFile = new File(Util.DirApp(), "Temp.db");

			if (dbFile.exists()) {

				fileInputStream = new FileInputStream(dbFile);

				int lenFile = fileInputStream.available();
				byte[] buffer = new byte[fileInputStream.available()];

				int byteRead = fileInputStream.read(buffer);

				if (byteRead == lenFile) {

					out = new FileOutputStream(zipPedido);
					gZipOut = new GZIPOutputStream(out);
					gZipOut.write(buffer);
					return true;
				}

				if (zipPedido.exists())
					zipPedido.delete();
			}

			return false;

		} catch (Exception e) {

			if (zipPedido.exists())
				zipPedido.delete();

			Log.e("ComprimirArchivo", e.getMessage(), e);
			return false;

		} finally {

			try {

				if (gZipOut != null)
					gZipOut.close();

				if (out != null)
					out.close();

				if (fileInputStream != null)
					fileInputStream.close();

			} catch (Exception e) {

				Log.e("ComprimirArchivo", e.getMessage(), e);
			}
		}
	}

	public void senalStatus() {
		boolean ok = false;
		respuestaServer = "";
		HttpURLConnection urlConnection = null;

		try {
			// Se hace es ajuste para el nuevo sincronizador
			String strURL = Const.URL_SYNC + "/TestConectividad.asp";
			Log.i("senalStatus", "URL senalStatus: " + strURL);

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
			urlConnection.setConnectTimeout(timeoutOne);
			urlConnection.setReadTimeout(timeoutOne);

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

			} else if (statusCode == -1) {

				mensaje = "No se pudo verificar conectividad";

			} else {

				mensaje = MensajeHttpError(statusCode, "TestConectividad.asp");
			}

		} catch (Exception e) {

			String motivo = e.getMessage();
			Log.e(TAG, "logIn -> " + motivo, e);

			if (motivo != null && motivo.startsWith("http://"))
				motivo = "Pagina no Encontrada: TestConectividad.asp";

			mensaje = "No se pudo verificar conectividad";

			if (motivo != null)
				mensaje += "\n\nMotivo: " + motivo;

		} finally {

			if (urlConnection != null)
				urlConnection.disconnect();
		}

		sincronizador.RespSync(ok, respuestaServer, mensaje, codeRequest);
	}

	public void downloadMessages() {
		boolean ok = false;
		respuestaServer = "";
		HttpURLConnection urlConnection = null;

		try {
			// Se hace es ajuste para el nuevo sincronizador
			String strURL = Const.URL_SYNC + "/mensajes/mensajes2.asp?usuario="
					+ this.usuario + "&consecutivo=" + this.consecutivo;
			Log.i("downloadMessages", "URL downloadMessages: " + strURL);

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
			urlConnection.setConnectTimeout(timeoutOne);
			urlConnection.setReadTimeout(timeoutOne);

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

			} else if (statusCode == -1) {

				mensaje = "No se pudo descargar mensajes Pagina No Encontrada: mensajes.asp";

			} else {

				mensaje = MensajeHttpError(statusCode, "mensajes.aspx");
			}

		} catch (Exception e) {

			String motivo = e.getMessage();
			Log.e(TAG, "logIn -> " + motivo, e);

			if (motivo != null && motivo.startsWith("http://"))
				motivo = "Pagina no Encontrada: mensajes.asp";

			mensaje = "No se pudo descargar mensajes";

			if (motivo != null)
				mensaje += "\n\nMotivo: " + motivo;

		} finally {

			if (urlConnection != null)
				urlConnection.disconnect();
		}

		sincronizador.RespSync(ok, respuestaServer, mensaje, codeRequest);
	}

	public void DescargaCatalogo() {

		boolean ok = false;
		InputStream inputStream = null;
		FileOutputStream fileOutput = null;
		HttpURLConnection urlConnection = null;

		try {

			/******************************************
			 * Se descarga el archivo .zip del catalogo
			 ******************************************/
			String urlcatalogo = Const.URL_DOWNLOAD_CATALOGO;

			Log.i("Descarga Catalogo", "URLCatalogo = " + urlcatalogo);

			URL url = new URL(urlcatalogo);
			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setRequestMethod("GET");
			urlConnection.setDoInput(true);

			urlConnection.connect();
			inputStream = urlConnection.getInputStream();
			/**
			 * Se obtiene la ruta del SD Card, para guardar la Base de Datos. Y
			 * se crea el Archivo de la BD
			 **/
			String fileName = "catalogo.zip"; // Nombre del archivo
			File file = new File(Util.DirApp(), fileName);

			if (file.exists())
				file.delete();

			if (file.createNewFile()) {

				fileOutput = new FileOutputStream(file);
				long downloadedSize = 0;
				int bufferLength = 0;
				byte[] buffer = new byte[1024];

				/**
				 * SE LEE LA INFORMACION DEL BUFFER Y SE ESCRIBE EL CONTENIDO EN
				 * EL ARCHIVO DE SALIDA
				 **/
				while ((bufferLength = inputStream.read(buffer)) > 0) {

					fileOutput.write(buffer, 0, bufferLength);
					downloadedSize += bufferLength;
				}

				fileOutput.flush();
				fileOutput.close();
				inputStream.close();

				long content_length = Util.ToLong(urlConnection
						.getHeaderField("content-length"));

				if (content_length == 0) {

					mensaje = "No hay conexion, por favor intente de nuevo";

				} else if (content_length != downloadedSize) { // La longitud de
					// descarga no
					// es igual al
					// Content
					// Length del
					// Archivo

					mensaje = "No se pudo descargar el catalogo bien, por favor intente de nuevo";

				} else {

					Crearcarpetacatalogo();
					Descomprimircatalogo(file);

					ok = true;
					mensaje = "Descargo correctamente el catalogo";
				}

			} else {

				mensaje = "No se pudo crear el catalogo";
			}

		} catch (Exception e) {

			mensaje = "No se pudo descargar el catalogo\n\n";
			mensaje += "Motivo: " + e.getMessage();

			Log.e("Sync Descargar Catalogo", e.getMessage(), e);

		} finally {

			try {

				if (fileOutput != null)
					fileOutput.close();

				if (inputStream != null)
					inputStream.close();

			} catch (IOException e) {
			}

			if (urlConnection != null)
				urlConnection.disconnect();
		}
		sincronizador.RespSync(ok, mensaje, mensaje, codeRequest);
	}

	public void Crearcarpetacatalogo() {
		// TODO Auto-generated method stub
//		File sdCard; 
		File directory;

		try {

//			sdCard = Environment.getExternalStorageDirectory();
			directory = new File(Util.DirApp().getPath() + "/Imgs/productos");
			directory.mkdirs();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void Descomprimircatalogo(File fileZip) {
		try {

			if (fileZip.exists()) { // Si el archivo existe entra, de lo
				// contrario no hace nada

				String nameFile = fileZip.getName().replace(".zip", "");
				File fileZipAux = new File(Util.DirAppCatalogo(), nameFile);

				if (!fileZipAux.exists())
					fileZipAux.delete();

				FileInputStream fin = new FileInputStream(fileZip);
				ZipInputStream zin = new ZipInputStream(fin);

				ZipEntry ze = null;

				while ((ze = zin.getNextEntry()) != null) {

					Log.v("Descomprimir", "Unzipping " + ze.getName());

					if (ze.isDirectory()) {

						dirChecker(ze.getName());

					} else {

						String pathFile = Util.DirApp().getPath()
								+ "/Imgs/productos" + "/"
								+ ze.getName().replace("catalogo/", "");
						File file = new File(pathFile);
						FileOutputStream fout = new FileOutputStream(file);

						// long bytes = 0;
						int bufferLength = 0;
						byte[] buffer = new byte[1024];

						while ((bufferLength = zin.read(buffer)) > 0) {

							fout.write(buffer, 0, bufferLength);
							// bytes += bufferLength;
						}

						zin.closeEntry();
						fout.flush();
						fout.close();
					}
				}
				zin.close();
			}

		} catch (Exception e) {

			Log.e("Descomprimir", e.getMessage(), e);
		}
	}

	
	
	public void TerminarLabores() {

		ok = false;
		mensaje = "";
		HttpURLConnection urlConnection = null;

		/************************************
		 * Carga la Configuracion del Usuario.
		 ************************************/
		Vendedor vendedor = DataBaseBO.obtenerVendedor();

		if (vendedor != null) {

			try {

				SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd@HH_mm_ss", Locale.getDefault());
				String fechaMovil = dateFormat.format(new Date());

				String strURL = Const.URL_SYNC + "TerminarLabores.aspx?un="	+ vendedor.codigo + "&fe=" + fechaMovil + "&i=" + imei;
				Log.i(TAG, "URL TerminarLabores = " + strURL);

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
				urlConnection.setConnectTimeout(timeoutOne);
				urlConnection.setReadTimeout(timeoutOne);

				BufferedReader reader = new BufferedReader( new InputStreamReader(urlConnection.getInputStream()));
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
//						DataBaseBO.ActulizarTerminoLabores();
						mensaje = "Terminar Labores Satisfactorio";

					} else {

						mensaje = respuestaServer;
					}

				} else if (statusCode == -1) {

					mensaje = "No se pudo Terminar Labores. Pagina No Encontrada: TerminarLabores.aspx";

				} else {

					mensaje = MensajeHttpError(statusCode, "TerminarLabores.aspx");
				}

			} catch (Exception e) {

				String motivo = e.getMessage();
				Log.e(TAG, "terminarLabores -> " + motivo, e);

				if (motivo != null && motivo.startsWith("http://"))
					motivo = "Pagina no Encontrada: TerminarLabores.aspx";

				mensaje = "No se pudo Terminar Labores.";

				if (motivo != null)
					mensaje += "\n\nMotivo: " + motivo;

			}

		} else {

			Log.i(TAG,
					"TerminarLabores: Falta establecer la configuracion del Usuario");
			mensaje = "Por favor, primero ingrese la configuracion del usuario";
		}

		sincronizador.RespSync(ok, respuestaServer, mensaje, codeRequest);
	}




	public String mensajeHttpError(int statusCode, String pagina) {

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
