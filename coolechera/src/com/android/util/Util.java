package com.android.util;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector; 




import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;






public class Util {	



	public static void MostrarAlert(Context context, String mensaje) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(mensaje)
		.setCancelable(false)
		.setPositiveButton("Aceptar",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {

				dialog.cancel();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}





	public static int ToInt(String value) {

		try {

			return Integer.parseInt(value);

		} catch (NumberFormatException e) {

			return 0;
		}
	}





	public static long ToLong(String value) {

		try {

			return Long.parseLong(value);

		} catch (NumberFormatException e) {

			return 0L;
		}
	}





	public static float ToFloat(String value) {

		try {

			return Float.parseFloat(value);

		} catch (NumberFormatException e) {

			return 0F;
		}
	}






	public static String Redondear(String numero, int cantDec) {

		int tamNumero = 0;
		double numRedondear;
		int cantAfterPunto;

		if (numero.indexOf(".") == -1) {

			return numero;
		}

		tamNumero = numero.length();
		cantAfterPunto = tamNumero - (numero.indexOf(".") + 1);

		if (cantAfterPunto <= cantDec)
			return numero;

		String numeroSumar = "0.";

		for (int i = 0; i < cantDec; i++) {

			numeroSumar = numeroSumar.concat("0");
		}

		numeroSumar = numeroSumar.concat("5");

		numRedondear = Double.parseDouble(numero);

		numRedondear = numRedondear + Double.parseDouble(numeroSumar);

		numero = String.valueOf(numRedondear);

		tamNumero = numero.length();
		cantAfterPunto = tamNumero - (numero.indexOf(".") + 1);

		if (cantAfterPunto <= cantDec)
			return numero;
		else {

			if (cantDec == 0)
				numero = numero.substring(0, numero.indexOf("."));
			else
				numero = numero.substring(0,
						(numero.indexOf(".") + 1 + cantDec));

			return numero;
		}
	}







	public static String Pasar_Entero(String numero) {
		String valoruno;
		String valordos;
		int esta = numero.indexOf("e");
		String Numero = numero;

		if (esta == -1) {
			Numero = numero;
		} else {
			// Si el valor de numero sin e es de una sola cifra
			if (esta == 1) {
				valoruno = "" + numero.charAt(0); // 1
			} else {
				valoruno = numero.substring(0, esta - 1); // 1.5
			}

			valordos = numero.substring(esta + 2, numero.length()); // 06
			int esta1 = valoruno.indexOf(".");
			int contador = 1;

			if (esta1 == -1) {
				float a = Float.parseFloat(valoruno);
				int b = Integer.parseInt(valordos);

				for (int i = 0; i < b; i++) {
					contador = contador * 10;
				}
				int resultado = (int) (a * contador);
				Numero = Integer.toString(resultado);
			} else {
				float a = Float.parseFloat(valoruno);
				int b = Integer.parseInt(valordos);

				for (int i = 0; i < b; i++) {
					contador = contador * 10;
				}
				int resultado = (int) (a * contador);
				Numero = Integer.toString(resultado);
			}
		}
		return Numero;
	}








	public static String SepararMiles(String numero) {

		String cantidad;
		String cantidadAux1;
		String cantidadAux2;
		boolean tieneMenos;

		int posPunto;
		int i;

		cantidad = "";
		cantidadAux1 = "";
		cantidadAux2 = "";

		tieneMenos = false;
		if (numero.indexOf("-") != -1) {

			String aux;
			tieneMenos = true;
			aux = numero.substring(0, numero.indexOf("-"));
			aux = aux
					+ numero.substring(numero.indexOf("-") + 1, numero.length());
			numero = aux;
		}

		if (numero.indexOf(".") == -1) {

			if (numero.length() > 3) {

				cantidad = ColocarComas(numero, numero.length());

			} else {

				if (tieneMenos)
					numero = "$-" + numero;
				else
					numero = "$" + numero;

				return numero;
			}

		} else {

			posPunto = numero.indexOf(".");

			for (i = 0; i < posPunto; i++) {

				cantidadAux1 = cantidadAux1 + numero.charAt(i);
			}

			for (i = posPunto; i < numero.length(); i++) {

				cantidadAux2 = cantidadAux2 + numero.charAt(i);
			}

			if (cantidadAux1.length() > 3) {

				cantidad = ColocarComas(cantidadAux1, posPunto);
				cantidad = cantidad + cantidadAux2;

			} else {

				if (tieneMenos)
					numero = "$-" + numero;
				else
					numero = "$" + numero;

				return numero;
			}
		}

		if (tieneMenos)
			cantidad = "$-" + cantidad;
		else
			cantidad = "$" + cantidad;

		return cantidad;
	}








	private static String ColocarComas(String numero, int pos) {

		String cantidad;
		Vector<String> cantidadAux;
		String cantidadAux1;
		int i;
		int cont;

		cantidadAux = new Vector<String>();
		cantidad = "";
		cantidadAux1 = "";
		cont = 0;

		for (i = (pos - 1); i >= 0; i--) {

			if (cont == 3) {

				cantidadAux1 = "," + cantidadAux1;
				cantidadAux.addElement(cantidadAux1);
				cantidadAux1 = "";
				cont = 0;
			}

			cantidadAux1 = numero.charAt(i) + cantidadAux1;
			cont++;
		}

		cantidad = cantidadAux1;

		for (i = cantidadAux.size() - 1; i >= 0; i--) {

			cantidad = cantidad + cantidadAux.elementAt(i).toString();
		}

		return cantidad;
	}






	public static String ObtenerHora() {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int hh = calendar.get(Calendar.HOUR_OF_DAY);
		String hora = hh < 10 ? "0" + hh : "" + hh;

		int mm = calendar.get(Calendar.MINUTE);
		String minutos = mm < 10 ? "0" + mm : "" + mm;

		int ss = calendar.get(Calendar.SECOND);
		String segundos = ss < 10 ? "0" + ss : "" + ss;

		return hora + ":" + minutos + ":" + segundos;
	}





	public static String lpad(String cadena, int tamano, String caracter) {

		int i;
		int tamano1;

		tamano1 = cadena.length();

		if (tamano1 > tamano)
			cadena = cadena.substring(0, tamano);

		tamano1 = cadena.length();

		for (i = tamano1; i < tamano; i++)
			cadena = caracter + cadena;

		return cadena;
	}

	public static File DirApp() {

		File SDCardRoot = Environment.getExternalStorageDirectory();
		File dirApp = new File(SDCardRoot.getPath() + "/" + Constantes.nameDirApp);

		if (!dirApp.isDirectory())
			dirApp.mkdirs();

		return dirApp;
	}




	public static File DirAppCatalogo() {

		File SDCardRoot = Environment.getExternalStorageDirectory();
		File dirApp = new File(SDCardRoot.getPath() + "/Imgs/productos"
				+ Constantes.nameDirApp);

		if (!dirApp.isDirectory())
			dirApp.mkdirs();

		return dirApp;
	}




	public static void MostrarAlertDialog(Context context, String mensaje) {

		AlertDialog alertDialog;

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false).setPositiveButton("Aceptar",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {

				dialog.cancel();
			}
		});

		alertDialog = builder.create();
		alertDialog.setMessage(mensaje);
		alertDialog.show();
	}





	public static String DateToStringMM_DD_YYYY(Date date) {
		Calendar calendario;
		String fechaRetorna = "";

		calendario = Calendar.getInstance();
		calendario.setTime(date);

		if ((calendario.get(Calendar.MONTH) + 1) < 10)
			fechaRetorna += "0" + (calendario.get(Calendar.MONTH) + 1);
		else
			fechaRetorna += String.valueOf(calendario.get(Calendar.MONTH) + 1);

		fechaRetorna += "/";

		if (calendario.get(Calendar.DAY_OF_MONTH) < 10)
			fechaRetorna += "0" + calendario.get(Calendar.DAY_OF_MONTH);
		else
			fechaRetorna += String.valueOf(calendario
					.get(Calendar.DAY_OF_MONTH));

		fechaRetorna += "/";

		fechaRetorna += String.valueOf(calendario.get(Calendar.YEAR));

		return fechaRetorna;
	}







	public static String QuitarE(String numero) {

		int posE;
		int cantMover;
		int posAux;
		int cantCeros;
		int posPunto;
		String cantMoverString;
		String cantidad;
		String cantidadAux1, cantidadAux2;

		cantMoverString = "";
		cantidad = "";

		if (!(numero.indexOf("E") != -1)) {

			return numero;
		} else {

			posE = numero.indexOf("E");
			posE++;

			while (posE < numero.length()) {

				cantMoverString = cantMoverString + numero.charAt(posE);
				posE++;
			}

			cantMover = Integer.parseInt(cantMoverString);

			posE = numero.indexOf("E");
			posAux = 0;
			posPunto = 0;

			while (posAux < posE) {

				if (numero.charAt(posAux) != '.') {

					cantidad = cantidad + numero.charAt(posAux);
				} else {

					posPunto = posAux;
				}
				posAux++;
			}

			if (cantidad.length() < (cantMover + posPunto)) {

				cantCeros = cantMover - cantidad.length() + posPunto;

				for (int i = 0; i < cantCeros; i++) {

					cantidad = cantidad + "0";
				}
			} else {

				cantidadAux1 = cantidad.substring(0, (cantMover + posPunto));
				cantidadAux2 = cantidad.substring((cantMover + posPunto),
						cantidad.length());

				if (!cantidadAux2.equals("")) {

					cantidad = cantidadAux1 + "." + cantidadAux2;
				} else {

					cantidad = cantidadAux1;
				}
			}
		}

		return cantidad;
	}





	public static String ObtenerFechaActual() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		return sdf.format(date);
	}





	public static String FechaActual(String format) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		return sdf.format(date);
	}







	public static boolean BorrarDataBase() {
		File dbFile = new File(Util.DirApp(), "DataBase.db");

		try {

			boolean borro = false;

			if (dbFile.exists())
				borro = dbFile.delete();

			return borro;

		} catch (Exception e) {

			return false;
		}
	}





	// ////////////////////////////////////////////////////////
	// /////////// Manejo de Fechas para cartera /////////////
	// ////////////////////////////////////////////////////////

	public static Date aDate(String strFecha) {
		SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
		Date fecha = null;

		try {
			fecha = formatoDelTexto.parse(strFecha);
		} catch (java.text.ParseException ex) {
			ex.printStackTrace();
		}
		Log.i("aDate", "" + fecha);
		return fecha;
	}

	public static String FormatoFecha(String fecha) {
		String year = fecha.substring(0, 4);
		String month = fecha.substring(4, 6);
		String day = fecha.substring(6);

		fecha = day + "/" + month + "/" + year;

		return fecha;
	}

	public static String Formato_Fecha(String fecha) {
		String year = fecha.substring(0, 4);
		String month = fecha.substring(4, 6);
		String day = fecha.substring(6);

		fecha = year + "/" + month + "/" + day;

		return fecha;
	}

	public static String Formato_Fecha_Orden(String fecha) {
		String day = fecha.substring(0, 2);
		String month = fecha.substring(3, 5);
		String year = fecha.substring(6);

		fecha = year + "/" + month + "/" + day;

		return fecha;
	}

	public static int fechasDiferenciaEnDias(String fechaFinal,
			String fechaActual) {
		int yearAc = Integer.parseInt(fechaActual.substring(0, 4));
		int monthAc = Integer.parseInt(fechaActual.substring(5, 7)) - 1;
		int dayAc = Integer.parseInt(fechaActual.substring(8));
		int yearVenc = Integer.parseInt(fechaFinal.substring(0, 4));
		int monthVenc = Integer.parseInt(fechaFinal.substring(5, 7)) - 1;
		int dayVenc = Integer.parseInt(fechaFinal.substring(8));

		GregorianCalendar t1 = new GregorianCalendar(yearAc, monthAc, dayAc);
		GregorianCalendar t2 = new GregorianCalendar(yearVenc, monthVenc,
				dayVenc);

		// int dias = t1.get(Calendar.DAY_OF_YEAR) -
		// t2.get(Calendar.DAY_OF_YEAR);

		long dif = t1.getTimeInMillis() - t2.getTimeInMillis();
		int dias = (int) (dif / (1000 * 60 * 60 * 24));

		return dias;
	}

	public static String ObtenerFechaId() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault());
		return sdf.format(date);
	}

	public static String Quitar_Cero(String numero) {
		int limite = numero.indexOf(".");

		if (limite == -1) {

			return numero;

		} else {

			String numerosincero = numero.substring(0, limite);

			return numerosincero;

		}
	}

	// ////////////////////////////////////////////////////////////
	// //////////// Obtener la fecha del dispositivo //////////////
	public static String DatePhone() {
		Calendar cal = new GregorianCalendar();
		Date date = cal.getTime();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		String formatteDate = df.format(date);
		return formatteDate;
	}

	public static String DateToday() {
		Calendar cal = new GregorianCalendar();
		Date date = cal.getTime();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
		String formatteDate = df.format(date);
		return formatteDate;
	}

	public static String DateToStringYYYY_MM_DD(Date date) {

		Calendar calendario;
		String fechaRetorna = "";

		calendario = Calendar.getInstance();
		calendario.setTime(date);

		fechaRetorna += String.valueOf(calendario.get(Calendar.YEAR));
		fechaRetorna += "-";

		if ((calendario.get(Calendar.MONTH) + 1) < 10)
			fechaRetorna += "0" + (calendario.get(Calendar.MONTH) + 1);
		else
			fechaRetorna += String.valueOf(calendario.get(Calendar.MONTH) + 1);

		fechaRetorna += "-";

		if (calendario.get(Calendar.DAY_OF_MONTH) < 10)
			fechaRetorna += "0" + calendario.get(Calendar.DAY_OF_MONTH);
		else
			fechaRetorna += String.valueOf(calendario
					.get(Calendar.DAY_OF_MONTH));

		return fechaRetorna;
	}

	public static boolean mkDir(String dirName) {

		try {

			File dirApp = new File(DirApp() + "/" + dirName);

			if (dirApp.isDirectory())
				return true;
			else
				return dirApp.mkdirs();

		} catch (Exception e) {

			Log.e("mkDir", e.getMessage(), e);
			return false;
		}
	}

	//	public static Drawable ResizedImage(Drawable imgOriginal, int newWidth, int newHeight) {
	//
	//		Matrix matrix;
	//		Bitmap resizedBitmap = null;
	//		Bitmap bitmapOriginal = null;
	//
	//		try {
	//
	//			// bitmapOriginal = BitmapDrawable(resizedBitmap);
	//			bitmapOriginal = ((BitmapDrawable) imgOriginal).getBitmap();
	//
	//			int width = bitmapOriginal.getWidth();
	//			int height = bitmapOriginal.getHeight();
	//
	//			if (width == newWidth && height == newHeight) {
	//
	//				return new BitmapDrawable(bitmapOriginal);
	//			}
	//
	//			// Reescala el Ancho y el Alto de la Imagen
	//			float scaleWidth = ((float) newWidth) / width;
	//			float scaleHeight = ((float) newHeight) / height;
	//
	//			matrix = new Matrix();
	//			matrix.postScale(scaleWidth, scaleHeight);
	//
	//			// Crea la Imagen con el nuevo Tamano
	//			resizedBitmap = Bitmap.createBitmap(bitmapOriginal, 0, 0, width,
	//					height, matrix, true);
	//			// mensaje = "Imagen escalada correctamente";
	//			return new BitmapDrawable(resizedBitmap);
	//
	//		} catch (Exception e) {
	//
	//			// mensaje = "Error escalando la Imagen: " + e.toString();
	//			return null;
	//
	//		} finally {
	//
	//			matrix = null;
	//			resizedBitmap = null;
	//			bitmapOriginal = null;
	//			System.gc();
	//		}
	//	}

	public static String SepararPalabrasTextView(String cadena, int caracteres) {

		StringBuffer buffer = new StringBuffer();
		String mainString = cadena;

		if (mainString.equals("")) {

			buffer.append(mainString);
		}

		while (!mainString.equals("")) {

			if (mainString.length() <= caracteres) {

				if (!buffer.toString().equals(""))
					buffer.append("<br />");

				buffer.append(mainString);
				mainString = "";

			} else {

				String tempString;

				if (mainString.length() >= caracteres) {

					if (mainString.lastIndexOf(' ') == -1) {

						if (!buffer.toString().equals(""))
							buffer.append("<br />");

						buffer.append(mainString);

						mainString = "";
						continue;

					} else {

						tempString = mainString.substring(0, caracteres);
					}

				} else {

					tempString = mainString;
				}

				if (tempString.lastIndexOf(' ') == -1) {

					if (!buffer.toString().equals(""))
						buffer.append("<br />");

					buffer.append(tempString);

					if (!tempString.equals(mainString)) {

						mainString = mainString.substring(caracteres + 1);

					} else {

						mainString = "";
					}

				} else {

					if (!buffer.toString().equals(""))
						buffer.append("<br />");

					buffer.append(tempString.substring(0,
							tempString.lastIndexOf(' ')));

					mainString = mainString.substring(tempString
							.lastIndexOf(' ') + 1);
				}
			}
		}

		return buffer.toString();
	}

	public static String quitarCeros(String dato) {

		if (dato.startsWith("0000000000"))
			dato = dato.substring(10, dato.length());
		else if (dato.startsWith("000000000"))
			dato = dato.substring(9, dato.length());
		else if (dato.startsWith("00000000"))
			dato = dato.substring(8, dato.length());
		else if (dato.startsWith("0000000"))
			dato = dato.substring(7, dato.length());
		else if (dato.startsWith("000000"))
			dato = dato.substring(6, dato.length());
		else if (dato.startsWith("00000"))
			dato = dato.substring(5, dato.length());
		else if (dato.startsWith("0000"))
			dato = dato.substring(4, dato.length());
		else if (dato.startsWith("000"))
			dato = dato.substring(3, dato.length());
		else if (dato.startsWith("00"))
			dato = dato.substring(2, dato.length());
		else if (dato.startsWith("0"))
			dato = dato.substring(1, dato.length());

		return dato;

	}

	public static void MostrarAlertDialog(Context context, String mensaje,
			int tipoIcono) {

		AlertDialog alertDialog;

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false).setPositiveButton("Aceptar",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {

				dialog.cancel();
			}
		});

		alertDialog = builder.create();
		alertDialog.setMessage(mensaje);

		if (tipoIcono == 1) {

			alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
			alertDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
			alertDialog.setTitle("Atencion");
			alertDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
					android.R.drawable.ic_dialog_alert);

		}

		if (tipoIcono == 2) {

			alertDialog.setIcon(android.R.drawable.ic_dialog_info);
			alertDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
			alertDialog.setTitle("Mensaje");
			alertDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
					android.R.drawable.ic_dialog_info);

		}

		alertDialog.show();
	}

	public static String[] split(String original, String separator) {

		Vector<String> nodes = new Vector<String>();

		// Parse nodes into vector
		int index = original.indexOf(separator);
		while (index >= 0) {
			nodes.addElement(original.substring(0, index));
			original = original.substring(index + separator.length());
			index = original.indexOf(separator);
		}
		// Get the last node
		nodes.addElement(original);

		// Create splitted string array
		String[] result = new String[nodes.size()];
		if (nodes.size() > 0) {
			for (int loop = 0; loop < nodes.size(); loop++)
				result[loop] = (String) nodes.elementAt(loop);
		}
		return result;
	}

	public static boolean checkSDCard() {

		boolean check = false;

		try {

			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {

				check = true;

			} else {

				check = false;

			}

		} catch (Exception e) {

			check = false;

		}

		return check;

	}

	public static String getDecimalFormat(float numero) {

		DecimalFormat myFormatter = new DecimalFormat("0");

		String output = myFormatter.format(numero);

		return output;
	}

	public static String quitaEspacios(String texto) {
		java.util.StringTokenizer tokens = new java.util.StringTokenizer(texto);
		texto = "";
		while (tokens.hasMoreTokens()) {
			texto += " " + tokens.nextToken();
		}
		texto = texto.toString();
		texto = texto.trim();
		return texto;
	}

	public static String getCadenaEspacio(int longitud) {

		String espacio = "";

		for (int i = 0; i < longitud; i++) {

			espacio = espacio + " ";

		}

		return espacio;
	}

	public static String ajustarNombre1(String nombre, int l) {

		if (nombre.length() >= l) {

			return nombre.substring(0, l - 1);

		} else {

			return nombre;

		}

	}

	public static void closeTecladoStartActivity(Activity context) {

		context.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

	}

	public static boolean validateDate(String dateStr, String formatStr) {
		if (formatStr == null)
			return false;
		SimpleDateFormat df = new SimpleDateFormat(formatStr,Locale.getDefault());
		Date testDate = null;
		try {
			testDate = df.parse(dateStr);
		} catch (ParseException e) {
			return false;
		}

		if (!df.format(testDate).equals(dateStr))
			return false;
		return true;
	}

	public static String rpad(String cadena, int tamano, String caracter) {

		if (cadena.length() > tamano) {
			cadena = cadena.substring(0, tamano);
		}

		int tamano1 = cadena.length();

		for (int i = tamano1; i < tamano; i++) {
			cadena = cadena + caracter;
		}

		return cadena;
	}

	public static String CentrarLinea(String linea, int numSpace) {

		int space, longitud;
		String centrado;
		centrado = "";

		if (linea.length() > numSpace) {

			linea = linea.substring(0, numSpace);
		}

		longitud = linea.length() / 2;

		if (longitud % 2 != 0) {

			longitud++;
		}

		space = numSpace / 2;
		space = space - longitud;

		for (int i = 0; i < space; i++) {

			centrado = centrado + " ";
		}

		centrado = centrado + linea;

		for (int i = centrado.length(); i < numSpace; i++) {

			centrado = centrado + " ";
		}

		return centrado;
	}

	public static void makeDiscoverable(Context context) {
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500);
		context.startActivity(discoverableIntent);
		Log.i("Log", "Discoverable ");
	}

	/**
	 * Metodo encargado de remover los acentos y carateres especiales de una
	 * cadena
	 * 
	 * @param cadena
	 * @return
	 */
	public static String removerAcentos(String cadena) {
		cadena = cadena.replace("Á", "A");
		cadena = cadena.replace("É", "E");
		cadena = cadena.replace("Í", "I");
		cadena = cadena.replace("Ó", "O");
		cadena = cadena.replace("Ú", "U");
		cadena = cadena.replace("Ñ", "N");
		cadena = cadena.replace("à", "a");
		cadena = cadena.replace("è", "e");
		cadena = cadena.replace("ì", "i");
		cadena = cadena.replace("ò", "o");
		cadena = cadena.replace("ù", "u");
		cadena = cadena.replace("ñ", "n");
		return cadena;
	}

	public static String removerCeros(String cadena) {

		return cadena.replaceFirst("^0*", "");

	}

	public static String RedondearFit(String numero, int cantDec) {

		int tamNumero = 0;
		double numRedondear;
		int cantAfterPunto;

		if (numero.indexOf(".") == -1) {

			return numero;
		}

		tamNumero = numero.length();
		cantAfterPunto = tamNumero - (numero.indexOf(".") + 1);

		if (cantAfterPunto <= cantDec) {

			int falta = cantDec - cantAfterPunto;

			if (falta > 0) {

				for (int i = 0; i < falta; i++)
					numero += "0";
			}

			return numero;
		}

		String numeroSumar = "0.";

		for (int i = 0; i < cantDec; i++) {

			numeroSumar = numeroSumar.concat("0");
		}

		numeroSumar = numeroSumar.concat("5");

		numRedondear = Double.parseDouble(numero);

		numRedondear = numRedondear + Double.parseDouble(numeroSumar);

		numero = String.valueOf(numRedondear);

		tamNumero = numero.length();
		cantAfterPunto = tamNumero - (numero.indexOf(".") + 1);

		if (cantAfterPunto <= cantDec) {

			int falta = cantDec - cantAfterPunto;

			if (falta > 0) {

				for (int i = 0; i < falta; i++)
					numero += "0";
			}

			return numero;

		} else {

			if (cantDec == 0)
				numero = numero.substring(0, numero.indexOf("."));
			else
				numero = numero.substring(0,
						(numero.indexOf(".") + 1 + cantDec));

			return numero;
		}
	}

	public static String SepararMilesSin(String numero) {

		String cantidad;
		String cantidadAux1;
		String cantidadAux2;
		boolean tieneMenos;

		int posPunto;
		int i;

		cantidad = "";
		cantidadAux1 = "";
		cantidadAux2 = "";

		tieneMenos = false;
		if (numero.indexOf("-") != -1) {

			String aux;
			tieneMenos = true;
			aux = numero.substring(0, numero.indexOf("-"));
			aux = aux
					+ numero.substring(numero.indexOf("-") + 1, numero.length());
			numero = aux;
		}

		if (numero.indexOf(".") == -1) {

			if (numero.length() > 3) {

				cantidad = ColocarComas(numero, numero.length());

			} else {

				if (tieneMenos)
					numero = "-" + numero;
				else
					numero = "" + numero;

				return numero;
			}

		} else {

			posPunto = numero.indexOf(".");

			for (i = 0; i < posPunto; i++) {

				cantidadAux1 = cantidadAux1 + numero.charAt(i);
			}

			for (i = posPunto; i < numero.length(); i++) {

				cantidadAux2 = cantidadAux2 + numero.charAt(i);
			}

			if (cantidadAux1.length() > 3) {

				cantidad = ColocarComas(cantidadAux1, posPunto);
				cantidad = cantidad + cantidadAux2;

			} else {

				if (tieneMenos)
					numero = "-" + numero;
				else
					numero = "" + numero;

				return numero;
			}
		}

		if (tieneMenos)
			cantidad = "-" + cantidad;
		else
			cantidad = "" + cantidad;

		return cantidad;
	}

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}


	/**
	 * obtener imei serial del dispositivo
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context){
		TelephonyManager mngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); 
		String imei = mngr.getDeviceId();
		return imei;
	}




}
