package com.android.business.object;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.data.Cliente;
import com.android.data.Encabezado;
import com.android.data.InfoVenta;
import com.android.data.Login;
import com.android.data.MotivosNoCompra;
import com.android.data.PedidoNoSincronizado;
import com.android.data.PedidoSincronizado;
import com.android.data.Referencias;
import com.android.data.ResumenEstadisticas;
import com.android.data.Tiempo;
import com.android.data.Vendedor;
import com.android.util.Util;



/**
 * clase controladora de conexiones a las bases de datos. 
 * se apoya en la estructura recomendada por android. hace uso de la clase SqliteOpenHelper.
 * @author JICZ
 *
 */
public class DataBaseBO {




	/**
	 * cerrar conexiones de forma segura.
	 * si existe transaccion en proceso.
	 * @param db
	 */
	public static void closeDataBase(SQLiteDatabase db) {
		if (db != null) {
			if (db.inTransaction()) {
				db.endTransaction();
			}
			db.close();
		}
	}



	/**
	 * metodo para identificar si el archivo de base de datos existe, se requiere conocer si existe para 
	 * determinar si se puede leer desde el movil o se necesita conexion al sincronizador.
	 * @return true, si existe, false en caso contrario.
	 */
	public static boolean existeDataBase() {
		File dbFile = new File(Util.DirApp(), "DataBase.db");
		return dbFile.exists();
	}
	
	
	/**
	 * metodo para verificar la version que se descargo en la tabla despues de iniciar dia.
	 * @return
	 */
	public static String ObtenerVersionApp() {
		String version = "";
		SQLiteDatabase db = null;

		try {
			File dbFile = new File(Util.DirApp(), "DataBase.db");
			db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);

			String query = "SELECT version FROM Version";
			Cursor cursor = db.rawQuery(query, null);

			if (cursor.moveToFirst()) {
				version = cursor.getString(cursor.getColumnIndex("version"));
			}
			Log.i("ObtenerVersionApp", "version = " + version);
			if (cursor != null)
				cursor.close();

		} catch (Exception e) {
			Log.e("verificar nueva version", "ObtenerVersionApp: " + e.getMessage());
		} finally {
			if (db != null)
				db.close();
		}
		return version;
	}



	/**	
	 * Metodo para cargar la informacion del vendedor desde la base de datos principal descargada en el movil.
	 * @return vendedor.
	 */
	public static Vendedor obtenerVendedor() {

		SQLiteDatabase db = null;
		Vendedor v = null;	

		try {			

			//abrir base de datos en modo de solo lectura.
			File dbFile = new File(Util.DirApp(), "DataBase.db");
			db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);			

			//columnas a leer.
			String[] columns = new String[] {"Codigo", "Nombre", "zona", "Consecutivo", "cedula", "bodega", "fechaLabores"};

			//generar la consulta
			Cursor cursor = db.query("Vendedor", columns, null, null, null, null, null);

			//verificar datos obtenidos en el cursor
			if(cursor.moveToFirst()) {
				do {					
					v = new Vendedor();
					v.codigo = cursor.getString(cursor.getColumnIndex("Codigo"));
					v.nombre = cursor.getString(cursor.getColumnIndex("Nombre"));
					v.zona = cursor.getString(cursor.getColumnIndex("zona"));
					v.consecutivo = cursor.getInt(cursor.getColumnIndex("Consecutivo"));
					v.cedula = cursor.getString(cursor.getColumnIndex("cedula"));
					v.bodega = cursor.getString(cursor.getColumnIndex("bodega"));
					v.fechaLabores = cursor.getString(cursor.getColumnIndex("fechaLabores"));

				} while (cursor.moveToNext());				
			}

			//cerrar cursor si esta abierto
			if(!cursor.isClosed()){
				cursor.close();
			}

			//obtener version de la applicacion.
			//columnas a leer.
			columns = new String[] {"Version"};
			//generar la consulta
			cursor = db.query("version", columns, null, null, null, null, null);
			//verificar datos obtenidos en el cursor
			if(cursor.moveToFirst()) {
				do {					
					v.version = cursor.getString(cursor.getColumnIndex("version"));
				} while (cursor.moveToNext());				
			}
			//cerrar cursor si esta abierto
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			v = null;

		} finally {
			if (db != null)
				db.close();
		}	
		return v;
	}


	/**
	 * metodo para borrar la informacion de la base de datos temporal.
	 * solo se borra cuando se termina de enviar informacion al sincronizador.
	 * @return
	 */
	public static boolean BorrarInfoTemp() {
		SQLiteDatabase dbTemp = null;

		try {
			File dbFile = new File(Util.DirApp(), "Temp.db");

			if (dbFile.exists()) {

				//abrir conexcion en modo escritura
				dbTemp = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);

				//cargar todas las tablas que estan en la main, es decir (DataBase.db)
				Vector<String> tableNames = new Vector<String>();
				String query = "SELECT tbl_name FROM sqlite_master";
				Cursor cursor = dbTemp.rawQuery(query, null);

				if (cursor.moveToFirst()) {
					do {

						String tableName = cursor.getString(cursor.getColumnIndex("tbl_name"));

						if (tableName.equals("android_metadata"))
							continue;

						tableNames.addElement(tableName);
					} while (cursor.moveToNext());
				}

				if (cursor != null)
					cursor.close();

				//borrar todos los datos que esten en la base de datos temp.db
				for (String tableName : tableNames) {
					query = "DELETE FROM " + tableName;
					dbTemp.execSQL(query);
				}
			}
			return true;
		} catch (Exception e) {
			Log.e("", "BorrarInfoTemp: " + e.getMessage(), e);
			return false;
		} finally {
			if (dbTemp != null)
				dbTemp.close();
		}
	}



	/**
	 * metodo para obtener el usuario y contrase�a registrados anteriormente.
	 * @return Login
	 */
	public static Login login() {
		SQLiteDatabase db = null;
		Login login = null;

		try {
			//abrir base de datos en modo de solo lectura.
			File dbFile = new File(Util.DirApp(), "DataBase.db");
			db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);	

			//columnas a leer.
			String[] columns = new String[] {"usuario", "password"};
			//generar la consulta
			Cursor cursor = db.query("Usuario", columns, null, null, null, null, null);

			//verificar datos obtenidos en el cursor
			if(cursor.moveToFirst()) {
				do {					
					login = new Login();
					login.user = cursor.getString(cursor.getColumnIndex("usuario"));
					login.password = cursor.getString(cursor.getColumnIndex("password"));
				} while (cursor.moveToNext());				
			}

			//cerrar cursor si esta abierto
			if(!cursor.isClosed()){
				cursor.close();
			}			
		} catch (SQLException e) {
			db.close();
			login = null;
			Log.e("Error Login Usuario DataBase.db", "Login user error: " + e.getMessage(), e);
			return login;
		} finally {
			if (db != null)
				db.close();
		}	
		return login;
	}



	/**
	 * metodo para actualizar el usuario que se ha logueado.
	 * @param context 
	 * @param pasword 
	 * @param usuario 
	 */
	public static void cambiarUsuario(Context context, String usuario, String pasword) {
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//guardar valores a insertar
			ContentValues values = new ContentValues();
			values.put("usuario", usuario);
			values.put("password", pasword);

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			//inicia transaccion
			db.beginTransaction();

			//actualizar todas las filas, (solo debe contener una fila)
			int update = db.update("Usuario", values, null, null);
			values.clear();
			if(update < 1){				
				throw new Exception("Error actualizando Usuario en DataBase.db");
			}			
			//confirmar transaccion.
			db.setTransactionSuccessful();
			Log.i("Usuario update", "Usuario actualizado a: " + usuario);

		} catch (Exception e) {
			Log.e("Usuario update", "Error actualizando usuario en bd: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
	}




	/**
	 * metodo que permite saber si hay pedidos por enviar, o cualquier novedad registrada sin enviar.
	 * se hace un conteo de las filas de la tabla [04Encabezado] de temp.db.
	 * donde el campo sincronizado sea igual a cero. Esto significa que los sincronizados
	 * con valor cero aun no han sido enviados.
	 * @param context 
	 * @return true si hay pedidos sin enviar, false en caso contrario.
	 */
	public static boolean verificarPedidosPorEnviar(Context context) {
		boolean existen = false;
		//referencia del manejador de las base de datos.
		DataBaseHelper helperTemp = null;
		SQLiteDatabase temp = null;
		String dataBaseTemp = "";
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "Temp.db");

			//verificar que existe la base de datos temp.db.
			if(fileTemp.exists()){
				dataBaseTemp = fileTemp.getPath();
			}
			else {
				return existen;
			}

			//instancia de la conexion a la bd DataBase.db
			helperTemp = new DataBaseHelper(context, dataBaseTemp);

			//abrir conexion con DataBase.db solo lectura y ejecutar consulta.
			temp = helperTemp.getReadableDatabase();
			String query = "SELECT COUNT(n.[Sincronizado]) AS sinEnviar FROM [04NovedadesCompras] n WHERE n.[Sincronizado] = 0;";
			Cursor cursor = temp.rawQuery(query, null);

			//hacer el conteo para confirmar si hay pedidos por enviar.
			if(cursor.moveToFirst()){
				do {
					int count = cursor.getInt(cursor.getColumnIndex("sinEnviar"));
					if(count > 0){
						existen = true;
					}
				} while (cursor.moveToNext());
			}
			if(!cursor.isClosed()){
				cursor.close();
			}			
		} catch (Exception e) {
			Log.e("Pedidos sin enviar", "No se realizo el conteo Error: " + e.getMessage());
		} finally {
			closeDataBase(temp);
			//cerrar conexiones			
			if(helperTemp != null){
				helperTemp.close();
			}			
		}
		return existen;
	}



	/**
	 * retorna una lista de clientes encontrados en la consulta.
	 * llamado desde BuscarClienteActivity.java
	 * @param text
	 * @param baseContext
	 * @return listaClientes, lista que contiene todos los clientes cargados desde la base de datos.
	 */
	public static ArrayList<Cliente> buscarClientes(String text, Context context) {

		//crear la lista nula
		ArrayList<Cliente> listaClientes = new ArrayList<Cliente>();

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();
			String query = "SELECT c.[CODIGO], c.[razon], c.[representante], c.[DIRECCION], c.[telefono], c.[NIT], c.[VENDEDOR], c.[BARRIO] " +
					"FROM Clientes c " +
					"WHERE c.[CODIGO] LIKE('%" + text + "%') OR c.[razon] LIKE ('%" + text + "%') " +
					"ORDER BY c.[razon], c.[CODIGO], c.[representante];";

			//ejecutar consulta.
			Cursor cursor = db.rawQuery(query, null);

			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					Cliente cliente = new Cliente();
					cliente.codigo = cursor.getString(cursor.getColumnIndex("CODIGO"));
					cliente.razonSocial = cursor.getString(cursor.getColumnIndex("razon"));
					cliente.representante = cursor.getString(cursor.getColumnIndex("representante"));
					cliente.direccion = cursor.getString(cursor.getColumnIndex("DIRECCION"));
					cliente.telefono = cursor.getString(cursor.getColumnIndex("telefono"));
					cliente.NIT = cursor.getString(cursor.getColumnIndex("NIT"));
					cliente.vendedor = cursor.getString(cursor.getColumnIndex("VENDEDOR"));
					cliente.barrio = cursor.getString(cursor.getColumnIndex("BARRIO"));

					//agregar el cliente a la lista
					listaClientes.add(cliente);					
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Buscar Clientes", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return listaClientes;
	}



	/**
	 * metodo para consultar la fecha de labores de un vendedor
	 * usado para determinar si un vendedor no ha iniciado dia, y asi no permitirle 
	 * realizar pedidos hasta que sincronize las fechas.
	 * @param context
	 * @return
	 */
	public static String obtenerFechaLaboresVendedor(Context context) {
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		String fechaLabores = "";

		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();
			String[] columns = new String[]{"fechaLabores"};
			//ejecutar consulta a la tabla vendedor.
			Cursor cursor = db.query("Vendedor", columns, null, null, null, null, null);

			if(cursor.moveToFirst()){
				do {
					fechaLabores = cursor.getString(cursor.getColumnIndex("fechaLabores"));
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}			
		}catch (Exception e) {
			Log.e("Cargar fecha labores", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return fechaLabores;
	}



	/**
	 * metodo que permite buscar un cliente, conociendo su codigo.
	 * @param codCliente
	 * @param baseContext
	 * @return cliente, el cliente que ha sido encontrado, null si no ha sido encontrado.
	 */
	public static Cliente buscarCliente(String codCliente, Context context) {
		Cliente cliente = null;
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//generar la consulta para obtener el cliente.
			String[] columns = new String[]{"CODIGO","razon","representante","DIRECCION","telefono","NIT","VENDEDOR","BARRIO"};
			String selection = "CODIGO = ?";
			String[] selectionArgs = new String[]{codCliente};
			Cursor cursor = db.query("Clientes", columns, selection, selectionArgs, null, null, null);

			//lectura de datos
			if(cursor.moveToFirst()){
				do {
					cliente = new Cliente();
					cliente.codigo = cursor.getString(cursor.getColumnIndex("CODIGO"));
					cliente.razonSocial = cursor.getString(cursor.getColumnIndex("razon"));
					cliente.representante = cursor.getString(cursor.getColumnIndex("representante"));
					cliente.direccion = cursor.getString(cursor.getColumnIndex("DIRECCION"));
					cliente.telefono = cursor.getString(cursor.getColumnIndex("telefono"));
					cliente.NIT = cursor.getString(cursor.getColumnIndex("NIT"));
					cliente.vendedor = cursor.getString(cursor.getColumnIndex("VENDEDOR"));
					cliente.barrio = cursor.getString(cursor.getColumnIndex("BARRIO"));					
				} while (cursor.moveToNext());
			}
			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Buscar cliente", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return cliente;
	}



	/**
	 * Metodo que retorna una lista de productos segun su parametro de busqueda.
	 * @param text
	 * @param context
	 * @return lista de productos. 
	 */
	public static ArrayList<Referencias> buscarReferencias(String text,	Context context) {

		//crear la lista nula
		ArrayList<Referencias> listaReferencias = new ArrayList<Referencias>();

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();
			String query = "" +
					"SELECT r.[CODIGO], r.[descripcion], r.[precio], r.[IVA], r.[linea], r.[FACTOR], r.[UNIDADVENTA], r.[PROMOCION] " +
					"FROM [Referencias] r " +
					"WHERE r.[CODIGO] LIKE('%" + text + "%') OR r.[descripcion] LIKE('%" + text + "%') " +
					"ORDER BY r.[CODIGO], r.[descripcion]; ";

			//ejecutar consulta.
			Cursor cursor = db.rawQuery(query, null);

			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					Referencias referencia = new Referencias();
					referencia.codigo = cursor.getString(cursor.getColumnIndex("CODIGO"));
					referencia.descripcion = cursor.getString(cursor.getColumnIndex("descripcion"));
					referencia.precio = cursor.getDouble(cursor.getColumnIndex("precio"));
					referencia.iva = cursor.getDouble(cursor.getColumnIndex("IVA"));
					referencia.linea = cursor.getString(cursor.getColumnIndex("linea"));
					referencia.factor = cursor.getDouble(cursor.getColumnIndex("FACTOR"));
					referencia.unidadVenta = cursor.getInt(cursor.getColumnIndex("UNIDADVENTA"));
					referencia.promocion = cursor.getInt(cursor.getColumnIndex("PROMOCION"));					

					//agregar el cliente a la lista
					listaReferencias.add(referencia);					
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Buscar Clientes", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return listaReferencias;	
	}



	/**
	 * metodo que permite crear una tabla de detalle virtual que sera usada durante el comienzo y finalizacion
	 * de un pedido para insertar en ella todos los productos pedidos.
	 * cuando sea confirmado el pedido, se transfieren los datos virtuales a la tabla de detalle que debe ser enviada
	 * al sincronizador.
	 * Se hace de esta forma con el fin de evitar concurrencia a la base de datos real. y por comodidad
	 * a la hora de hacer modificaciones a un pedido en particualar.
	 * 
	 * Cada vez que se agrege un nuevo campo desde el sincronizador, debe ser insertado de forma manual en este metodo.
	 * @param baseContext
	 * @return true, si la tabla es creada, false en caso contrario.
	 */
	public static boolean crearTablaDetalleVirtual(Context context) {

		boolean tabla = false;
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//consulta para crear una tabla virtual exactamente igual a la original ([04Detalle]).
			String query = "" +
					"CREATE TABLE IF NOT EXISTS [main].[DETALLEVIRTUAL] (" +
					"       NumDoc varchar(20) default NT," +
					"       Fecha datetime," +
					"       CodigoRef varchar(6) UNIQUE," +
					"       Precio float, TarifaIva float," +
					"       DescuentoRenglon float," +
					"       Cantidad int," +
					"       Sincronizado int," +
					"       CantidadEntregada int," +
					"       SincronizadoEntrega int," +
					"       FechaEntrega datetime," +
					"       Tipo int," +
					"       Fechareal datetime," +
					"       item INTEGER PRIMARY KEY AUTOINCREMENT," +
					"       bodega varchar(2)," +
					"		iva float " +
					"); ";
					

			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();			

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			//inicia transaccion
			db.beginTransaction();

			//ejecutar la consulta.
			db.execSQL(query);

			//confirmar transaccion.
			db.setTransactionSuccessful();
			Log.i("Crear DetalleVirtual", "DetalleVirtual OK!");
			tabla = true;

		} catch (Exception e) {
			Log.e("Crear DetalleVirtual", "Error creando Tabla: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return tabla;
	}
	
	
	
	
	
	/**
	 * metodo que permite crear una tabla de cambios detalle virtual que sera usada durante el comienzo y finalizacion
	 * de un pedido para insertar en ella todos los productos pedidos.
	 * cuando sea confirmado el pedido, se transfieren los datos virtuales a la tabla de detalle que debe ser enviada
	 * al sincronizador.
	 * Se hace de esta forma con el fin de evitar concurrencia a la base de datos real. y por comodidad
	 * a la hora de hacer modificaciones a un pedido en particualar.
	 * 
	 * Cada vez que se agrege un nuevo campo desde el sincronizador, debe ser insertado de forma manual en este metodo.
	 * @param baseContext
	 * @return true, si la tabla es creada, false en caso contrario.
	 */
	public static boolean crearTablaDetalleCambiosVirtual(Context context) {

		boolean tabla = false;
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//consulta para crear una tabla virtual exactamente igual a la original ([04Detalle]).
			String query = "" +
					"CREATE TABLE IF NOT EXISTS [main].[DETALLECAMBIOS] (" +
					"       NumDoc varchar(20) default NT," +
					"       Fecha datetime," +
					"       CodigoRef varchar(6) UNIQUE," +
					"       Precio float, TarifaIva float," +
					"       DescuentoRenglon float," +
					"       Cantidad int," +
					"       Sincronizado int," +
					"       CantidadEntregada int," +
					"       SincronizadoEntrega int," +
					"       FechaEntrega datetime," +
					"       Tipo int," +
					"       Fechareal datetime," +
					"       item INTEGER PRIMARY KEY AUTOINCREMENT," +
					"       bodega varchar(2)," +
					"		iva float " +
					");";

			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();			

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			//inicia transaccion
			db.beginTransaction();

			//ejecutar la consulta.
			db.execSQL(query);

			//confirmar transaccion.
			db.setTransactionSuccessful();
			Log.i("Crear DetalleCambiosVirtual", "DetalleCambiosVirtual OK!");
			tabla = true;

		} catch (Exception e) {
			Log.e("Crear DetalleVirtual", "Error creando Tabla: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return tabla;
	}



	/**
	 * metodo que borra la tabla detallevirtual.
	 * @param context
	 */
	public static void eliminarDetalleVirtual(Context context) {

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//consulta para eliminar la tabla virtual.
			String query = "DROP TABLE IF EXISTS DETALLEVIRTUAL;";

			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();			

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			//inicia transaccion
			db.beginTransaction();

			//ejecutar la consulta.
			db.execSQL(query);

			//confirmar transaccion.
			db.setTransactionSuccessful();
			Log.i("Eliminar DetalleVirtual", "DetalleVirtual borrado OK!");

		} catch (Exception e) {
			Log.e("Eliminar DetalleVirtual", "Error borrando Tabla: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
	}
	
	
	/**
	 * metodo que borra la tabla detalleCambiovirtual.
	 * @param context
	 */
	public static void eliminarDetalleCambioVirtual(Context context) {

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//consulta para eliminar la tabla virtual.
			String query = "DROP TABLE IF EXISTS DETALLECAMBIOS;";

			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();			

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			//inicia transaccion
			db.beginTransaction();

			//ejecutar la consulta.
			db.execSQL(query);

			//confirmar transaccion.
			db.setTransactionSuccessful();
			Log.i("Eliminar DetalleCambiosVirtual", "DetalleCambiosVirtual borrado OK!");

		} catch (Exception e) {
			Log.e("Eliminar DetalleCambiosVirtual", "Error borrando Tabla: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
	}



	/**
	 * metodo que permite generar un numeroDoc que no este repetido en la tabla de encabezado ni en la tabla novedades.
	 * @param vendedor 
	 * @param consecutivo 
	 * @param context
	 * @return
	 */
	public static String generarNumeroDoc(Vendedor vendedor, int consecutivo, Context context) {

		String numeroDoc= "";
		String codVendedor = "";

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//cargar fecha actual del movil.
			Calendar calendar = new GregorianCalendar();
			Date date = calendar.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHH", Locale.getDefault());
			String fechaActual = sdf.format(date);

			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();			

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion con DataBase.db
			db = helperDataBase.getReadableDatabase();

			//ejecutar consulta.
			String[] columns = new String[]{"Codigo"};			
			Cursor cursor = db.query("Vendedor", columns, null, null, null, null, null);

			if(cursor.moveToFirst()){
				do {
					codVendedor = cursor.getString(cursor.getColumnIndex("Codigo"));
				} while (cursor.moveToNext());
			}
			//cerrar el cursor
			if(!cursor.isClosed()){
				cursor.close();
			}

			//primera definicion del numeroDoc
			numeroDoc = codVendedor + fechaActual + consecutivo;

			//verificar numerodoc valido
			if(numeroDoc.equals("") || codVendedor.equals("") ){
				throw new Exception("fallo lectura de base de datos");
			}

			//ciclo para obtener un numeroDoc no repetido.			
			boolean condition = true;

			do {
				int cont = Integer.MIN_VALUE;

				//consulta para verifiar que el numeroDoc generado no existe en ninguna tabla.(encabezado o novedad)
				String query = "SELECT SUM(x.[conta]) AS cont " +
						"FROM " +
						"    (SELECT  COUNT(n.[Numerodoc]) AS conta " +
						"             FROM [04NovedadesCompras] n " +
						"             WHERE n.[Numerodoc] = '" + numeroDoc + "' " +
						"UNION " +
						"      SELECT  COUNT(e.[NumeroDoc]) AS conta " +
						"              FROM [04Encabezado] e " +
						"              WHERE e.[NumeroDoc] = '" + numeroDoc + "') x;";

				cursor = db.rawQuery(query, null);

				if(cursor.moveToFirst()){
					do {
						cont = cursor.getInt(cursor.getColumnIndex("cont"));
					} while (cursor.moveToNext());
				}
				//verificar que no existe el numeroDoc
				if(cont == 0 ){
					condition = false;
				}
				else {
					consecutivo += 1;
					numeroDoc = codVendedor + fechaActual + consecutivo;					
				}
				//cerrar cursor
				if(!cursor.isClosed()){
					cursor.close();
				}
				vendedor.consecutivo = consecutivo;
			} while (condition);			
		} catch (Exception e) {
			Log.e("Cargar consecutivo", "Error cargando consecutivo: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return numeroDoc;
	}



	/**
	 * metodo que permite insertar un detalle a la tabla detalleviutual. 
	 * @param referencia
	 * @param cantidad
	 * @param numeroDoc
	 * @param bodega
	 * @param baseContext
	 * @return
	 */
	public static boolean insertarProductoEnDetalleVirtual(	Referencias referencia, String cantidad, String numeroDoc, String bodega, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;

		try {

			//cargar fecha actual del movil.
			Calendar calendar = new GregorianCalendar();
			//fecha actual
			Date date = calendar.getTime();

			//se suman un dias, como maximo para generar fecha de entrega
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_YEAR, 1);	// el 1 es el numero de dias a sumar a la fecha actual		
			Date dateEntrega = calendar.getTime();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			String fechaActual = sdf.format(date);
			String fechaEntrega = sdf.format(dateEntrega);
			

			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//cargar valores para insertar en la tabla
			ContentValues values = new ContentValues();
			values.put("NumDoc", numeroDoc);
			values.put("Fecha", fechaActual);
			values.put("CodigoRef", referencia.codigo); //constraint unique
			values.put("Precio", referencia.precio);
			values.put("TarifaIva", (int)referencia.montoIva); // tomar la parte entera del monto de iva.
			values.put("DescuentoRenglon", 0);
			values.put("Cantidad", cantidad);
			values.put("Sincronizado", 0);
			values.put("CantidadEntregada", 0);
			values.put("SincronizadoEntrega", 0);
			values.put("FechaEntrega", fechaEntrega);
			values.put("Tipo", referencia.tipo);
			values.put("Fechareal", fechaActual);
			values.putNull("item"); // autoincremental
			values.put("bodega", bodega);
			values.put("iva", referencia.iva);

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			long rows = db.insertOrThrow("DETALLEVIRTUAL", "item", values);  //item se envia vacio, para generar el autoincremental

			if(rows == -1){
				throw new SQLException("posible referencia repetida");
			}
			// confirmar transaccion.
			db.setTransactionSuccessful();
			insertado = true;
			Log.i("Insertar Referencia DetalleVirtual", "DetalleVirtual insertado OK!");

		} catch (SQLException e) {
			Log.e("Insertar Referencia DetalleVirtual", "Error insertando: " + e.getMessage());
			insertado = false;
		} finally {
			closeDataBase(db);
			// cerrar conexiones
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;
	}



	/**
	 * metodo que calcula el estado de venta actual, para informar al cliente
	 * del monto en pesos del pedido actual.
	 * Modificado por calculo erroneo de iva, cambiar por:  {@link calcularEstadoDeLaVentaActual}
	 * @param numerDoc
	 * @param baseContext
	 * @return
	 */
	@Deprecated
	public static InfoVenta calcularEstadoDeVentaActual(String numerDoc, Context context) {

		InfoVenta venta = null;
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//generar la consulta para obtener el cliente.
			String query = "SELECT COALESCE(SUM(v.[valor]),0) AS valor, COALESCE(SUM(v.[iva]),0) as iva, COALESCE(SUM(v.[total]),0) as total," +
					"(SELECT SUM(x.[Cantidad]) FROM [DETALLEVIRTUAL] x ) as litros " +
					"FROM " +
					"(SELECT m.[monto] as valor, (m.[monto] * m.[iva]) as iva, (m.[monto] + (m.[monto] * m.[iva])) AS total " +
					"	FROM (SELECT SUM(d.[Precio] * d.[Cantidad]) as monto, (d.[iva]/100) as iva " +
					"     	FROM [DETALLEVIRTUAL] d GROUP BY d.[iva]) m) v";
			Cursor cursor = db.rawQuery(query, null);

			//lectura de datos
			if(cursor.moveToFirst()){
				do {
					venta = new InfoVenta();
					venta.valor = cursor.getString(cursor.getColumnIndex("valor"));
					venta.iva = cursor.getString(cursor.getColumnIndex("iva"));
					venta.total = cursor.getString(cursor.getColumnIndex("total"));
					venta.litros = cursor.getString(cursor.getColumnIndex("litros"));
				} while (cursor.moveToNext());
			}
			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Buscar cliente", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return venta;
	}
	
	
		
	/**
	 * metodo que calcula el estado de venta actual, para informar al cliente
	 * del monto en pesos del pedido actual.
	 * Metodo actualizado de: {@link calcularEstadoDeVentaActual}
	 * @param numerDoc
	 * @param baseContext
	 * @return
	 */
	public static InfoVenta calcularEstadoDeLaVentaActual(String numerDoc, Context context) {

		InfoVenta venta = null;
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//generar la consulta para obtener el cliente.
			String query = 
					"SELECT COALESCE((DV.[Precio] - DV.[TarifaIva]) * DV.[Cantidad],0) AS valor, " +
					"       COALESCE(SUM(DV.[TarifaIva] * DV.[Cantidad]),0) AS iva,        " +
					"       COALESCE(SUM(DV.[Precio] * DV.[Cantidad]),0) AS total, " +
					"       COALESCE(SUM(DV.[Cantidad]),0) AS litros      " +
					"FROM [DETALLEVIRTUAL] DV";
			
			Cursor cursor = db.rawQuery(query, null);

			//lectura de datos
			if(cursor.moveToFirst()){
				do {
					venta = new InfoVenta();
					venta.valor = cursor.getString(cursor.getColumnIndex("valor"));
					venta.iva = cursor.getString(cursor.getColumnIndex("iva"));
					venta.total = cursor.getString(cursor.getColumnIndex("total"));
					venta.litros = cursor.getString(cursor.getColumnIndex("litros"));
				} while (cursor.moveToNext());
			}
			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Buscar cliente", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return venta;
	}



	/**
	 * metodo que permite conocer todos los productos que han sido pedidos
	 * hasta un momento determinado
	 * @param text
	 * @param baseContext
	 * @return
	 */
	public static ArrayList<Referencias> buscarReferenciasPedidas(String text,Context context) {

		//crear la lista nula
		ArrayList<Referencias> listaReferencias = new ArrayList<Referencias>();

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			String query = "";
			//se generan dos consultas, una para mostrar todos los productos y la otra solo por el criterio de busqueda.
			if(text.equals("")){
				query = "" +
						"SELECT r.[CODIGO], r.[descripcion], r.[precio], r.[IVA], r.[linea], r.[FACTOR], r.[UNIDADVENTA], r.[PROMOCION]," +
						"       v.[Cantidad] " +
						"FROM [Referencias] r " +
						"INNER JOIN [DETALLEVIRTUAL] v " +
						"      ON r.[CODIGO] = v.[CodigoRef] " +
						"ORDER BY r.[CODIGO], r.[descripcion]; ";
			}
			else {
				query = "" +
						"SELECT r.[CODIGO], r.[descripcion], r.[precio], r.[IVA], r.[linea], r.[FACTOR], r.[UNIDADVENTA], r.[PROMOCION]," +
						"       v.[Cantidad] " +
						"FROM [Referencias] r " +
						"INNER JOIN [DETALLEVIRTUAL] v " +
						"      ON r.[CODIGO] = v.[CodigoRef] " +
						"WHERE r.[CODIGO] LIKE('%" + text + "%') OR r.[descripcion] LIKE('%" + text + "%')" +
						"ORDER BY r.[CODIGO], r.[descripcion]; ";
			}

			//ejecutar consulta.
			Cursor cursor = db.rawQuery(query, null);

			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					Referencias referencia = new Referencias();
					referencia.codigo = cursor.getString(cursor.getColumnIndex("CODIGO"));
					referencia.descripcion = cursor.getString(cursor.getColumnIndex("descripcion"));
					referencia.precio = cursor.getDouble(cursor.getColumnIndex("precio"));
					referencia.iva = cursor.getDouble(cursor.getColumnIndex("IVA"));
					referencia.linea = cursor.getString(cursor.getColumnIndex("linea"));
					referencia.factor = cursor.getDouble(cursor.getColumnIndex("FACTOR"));
					referencia.unidadVenta = cursor.getInt(cursor.getColumnIndex("UNIDADVENTA"));
					referencia.promocion = cursor.getInt(cursor.getColumnIndex("PROMOCION"));
					referencia.cantidad = cursor.getInt(cursor.getColumnIndex("Cantidad"));
					
					//agregar el cliente a la lista
					listaReferencias.add(referencia);					
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Buscar Productos pedidos", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return listaReferencias;
	}
	
	
	/**
	 * metodo que permite conocer todos los productos que han sido recbidos para cambio
	 * hasta un momento determinado
	 * @param text
	 * @param baseContext
	 * @return
	 */
	public static ArrayList<Referencias> buscarReferenciasParaCambio(String text,Context context) {

		//crear la lista nula
		ArrayList<Referencias> listaReferencias = new ArrayList<Referencias>();

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			String query = "";
			//se generan dos consultas, una para mostrar todos los productos y la otra solo por el criterio de busqueda.
			if(text.equals("")){
				query = "" +
						"SELECT r.[CODIGO], r.[descripcion], r.[precio], r.[IVA], r.[linea], r.[FACTOR], r.[UNIDADVENTA], r.[PROMOCION]," +
						"       v.[Cantidad] " +
						"FROM [Referencias] r " +
						"INNER JOIN [DETALLECAMBIOS] v " +
						"      ON r.[CODIGO] = v.[CodigoRef] " +
						"ORDER BY r.[CODIGO], r.[descripcion]; ";
			}
			else {
				query = "" +
						"SELECT r.[CODIGO], r.[descripcion], r.[precio], r.[IVA], r.[linea], r.[FACTOR], r.[UNIDADVENTA], r.[PROMOCION]," +
						"       v.[Cantidad] " +
						"FROM [Referencias] r " +
						"INNER JOIN [DETALLECAMBIOS] v " +
						"      ON r.[CODIGO] = v.[CodigoRef] " +
						"WHERE r.[CODIGO] LIKE('%" + text + "%') OR r.[descripcion] LIKE('%" + text + "%')" +
						"ORDER BY r.[CODIGO], r.[descripcion]; ";
			}

			//ejecutar consulta.
			Cursor cursor = db.rawQuery(query, null);

			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					Referencias referencia = new Referencias();
					referencia.codigo = cursor.getString(cursor.getColumnIndex("CODIGO"));
					referencia.descripcion = cursor.getString(cursor.getColumnIndex("descripcion"));
					referencia.precio = cursor.getDouble(cursor.getColumnIndex("precio"));
					referencia.iva = cursor.getDouble(cursor.getColumnIndex("IVA"));
					referencia.linea = cursor.getString(cursor.getColumnIndex("linea"));
					referencia.factor = cursor.getDouble(cursor.getColumnIndex("FACTOR"));
					referencia.unidadVenta = cursor.getInt(cursor.getColumnIndex("UNIDADVENTA"));
					referencia.promocion = cursor.getInt(cursor.getColumnIndex("PROMOCION"));
					referencia.cantidad = cursor.getInt(cursor.getColumnIndex("Cantidad"));

					//agregar el cliente a la lista
					listaReferencias.add(referencia);					
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Buscar Productos pedidos", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return listaReferencias;
	}



	/**
	 * metodo que permite actualizar un producto en la tabla detalle
	 * @param referencia
	 * @param cantidad
	 * @param numeroDoc
	 * @param bodega
	 * @param baseContext
	 * @return
	 */
	public static boolean actualizarProductoEnDetalleVirtual(Referencias referencia, String cantidad, String numeroDoc, Context context) {


		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;

		try {

			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//cargar valores para insertar en la tabla
			ContentValues values = new ContentValues();
			values.put("Cantidad", cantidad);

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			String whereClause = "NumDoc=? AND CodigoRef=?";
			String[] whereArgs = new String[]{numeroDoc,referencia.codigo};
			long rows = db.update("DETALLEVIRTUAL", values, whereClause, whereArgs);

			if(rows == -1){
				throw new SQLException("posible referencia repetida");
			}
			values.clear();
			// confirmar transaccion.
			db.setTransactionSuccessful();
			insertado = true;
			Log.i("Actualizar Referencia DetalleVirtual", "DetalleVirtual actualizado OK!");

		} catch (SQLException e) {
			Log.e("Actualizar Referencia DetalleVirtual", "Error Actualizando: " + e.getMessage());
			insertado = false;
		} finally {
			closeDataBase(db);
			// cerrar conexiones
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;

	}
	
	
	/**
	 * metodo que permite actualizar un producto en la tabla detalle
	 * @param referencia
	 * @param cantidad
	 * @param numeroDoc
	 * @param bodega
	 * @param baseContext
	 * @return
	 */
	public static boolean actualizarProductoEnDetalleCambios(Referencias referencia, String cantidad, String numeroDoc, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;

		try {

			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//cargar valores para insertar en la tabla
			ContentValues values = new ContentValues();
			values.put("Cantidad", cantidad);

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			String whereClause = "NumDoc=? AND CodigoRef=?";
			String[] whereArgs = new String[]{numeroDoc,referencia.codigo};
			long rows = db.update("DETALLECAMBIOS", values, whereClause, whereArgs);

			if(rows == -1){
				throw new SQLException("posible referencia repetida");
			}
			values.clear();
			// confirmar transaccion.
			db.setTransactionSuccessful();
			insertado = true;
			Log.i("Actualizar Referencia DetalleVirtual", "DetalleVirtual actualizado OK!");

		} catch (SQLException e) {
			Log.e("Actualizar Referencia DetalleVirtual", "Error Actualizando: " + e.getMessage());
			insertado = false;
		} finally {
			closeDataBase(db);
			// cerrar conexiones
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;
	}



	/**
	 * eliminar un producto de la tabla detallevirtual. metodo llamado cuando un usuario
	 * elimina un producto que ha sido insertado antes al detalle del pedido.
	 * @param referencia
	 * @param numeroDoc 
	 * @param context
	 * @return
	 */
	public static boolean eliminarProductoDeDetalleVirtual(Referencias referencia, String numeroDoc, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean eliminado = false;

		try {

			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			String whereClause = "CodigoRef = ? AND NumDoc = ?";
			String[] whereArgs = new String[]{referencia.codigo, numeroDoc};
			long rows = db.delete("DETALLEVIRTUAL", whereClause, whereArgs);

			if(rows <= 0){
				throw new SQLException("no se borro ningun pedido");
			}
			// confirmar transaccion.
			db.setTransactionSuccessful();
			eliminado = true;
			Log.i("Eliminar Referencia DetalleVirtual", "DetalleVirtual eliminado referencia OK!");

		} catch (SQLException e) {
			Log.e("Eliminar Referencia DetalleVirtual", "Error eliminando: " + e.getMessage());
			eliminado = false;
		} finally {
			closeDataBase(db);
			// cerrar conexiones
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return eliminado;


	}
	
	
	
	/**
	 * eliminar un producto de la tabla detalleCambios. metodo llamado cuando un usuario
	 * elimina un producto que ha sido insertado antes al detalle del pedido.
	 * @param referencia
	 * @param numeroDoc 
	 * @param context
	 * @return
	 */
	public static boolean eliminarProductoDeDetalleCambios(Referencias referencia, String numeroDoc, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean eliminado = false;

		try {

			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			String whereClause = "CodigoRef = ? AND NumDoc = ?";
			String[] whereArgs = new String[]{referencia.codigo, numeroDoc};
			long rows = db.delete("DETALLECAMBIOS", whereClause, whereArgs);

			if(rows <= 0){
				throw new SQLException("no se borro ningun pedido");
			}
			// confirmar transaccion.
			db.setTransactionSuccessful();
			eliminado = true;
			Log.i("Eliminar Referencia DetalleVirtual", "DetalleVirtual eliminado referencia OK!");

		} catch (SQLException e) {
			Log.e("Eliminar Referencia DetalleVirtual", "Error eliminando: " + e.getMessage());
			eliminado = false;
		} finally {
			closeDataBase(db);
			// cerrar conexiones
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return eliminado;


	}



	/**
	 * permite conocer el estado de venta de productos que inician por 7 0 9
	 * @param numerDoc
	 * @param context
	 * @return
	 */
	public static InfoVenta calcularEstado2DeVentaActual(String numerDoc, Context context) {


		InfoVenta venta = null;
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//generar la consulta para obtener el cliente.
			String query = "" +
					"SELECT COALESCE(t.[valor],0) AS valor, COALESCE(t.[iva],0) AS iva, COALESCE(t.[total],0) AS total, COALESCE(t.[litros], 0) AS litros " +
					"FROM (SELECT SUM(m.[monto]) AS valor, SUM((m.[monto] * m.[iva])) AS iva,  SUM((m.[monto] + m.[monto] * m.[iva])) AS total, SUM(m.[litros]) AS litros " +
					"             FROM    (SELECT SUM(d.[Precio] * d.[Cantidad]) AS monto, (d.[TarifaIva]/100) AS iva, SUM(d.[Cantidad]) AS litros  " +
					"                     FROM [DETALLEVIRTUAL] d " +
					"                     WHERE d.[CodigoRef] LIKE('7%') OR d.[CodigoRef] LIKE('9%') " +
					"                     GROUP BY d.[TarifaIva]) m) t";

			Cursor cursor = db.rawQuery(query, null);			
			//lectura de datos
			if(cursor.moveToFirst()){
				do {
					venta = new InfoVenta();
					venta.valor = cursor.getString(cursor.getColumnIndex("valor"));
					venta.iva = cursor.getString(cursor.getColumnIndex("iva"));
					venta.total = cursor.getString(cursor.getColumnIndex("total"));
					venta.litros = cursor.getString(cursor.getColumnIndex("litros"));
				} while (cursor.moveToNext());
			}
			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Buscar cliente", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return venta;


	}



	/**
	 * metodo que inserta el detalleVirtual en la tabla detalle original de la base de datos DataBase.db.
	 * se realiza una copia exacta de los datos de la tabla virtual a la tabla original.
	 * @param numeroDoc 
	 * @param context 
	 * @return true si el proceso de insercion termina correctamente, false en caso contrario.
	 */
	public static boolean insertarDetalleDataBase(String numeroDoc, Context context) {
		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;

		try {				
			// cargar rutas de bases de datos.
			File fileDataBase = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileDataBase.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			//consulta para realizar la copia de tablas.
			String query = "" +
					"INSERT INTO [04Detalle] (Numdoc, Fecha, CodigoRef, Precio, TarifaIva, DescuentoRenglon, Cantidad," +
					"                         Sincronizado, CantidadEntregada, SincronizadoEntrega, FechaEntrega, Tipo," +
					"                         Fechareal, item, bodega, iva) " +
					"SELECT d.[NumDoc], d.[Fecha], d.[CodigoRef], d.[Precio], d.[TarifaIva], d.[DescuentoRenglon], d.[Cantidad]," +
					"       d.[Sincronizado], d.[CantidadEntregada], d.[SincronizadoEntrega], d.[FechaEntrega], d.[Tipo]," +
					"       d.[Fechareal], d.[item], d.[bodega], d.[iva] " +
					"FROM [DETALLEVIRTUAL] d " +
					"WHERE d.[NumDoc] = '"+ numeroDoc +"'" +
					"UNION " +
					"SELECT d.[NumDoc], d.[Fecha], d.[CodigoRef], d.[Precio], d.[TarifaIva], d.[DescuentoRenglon], d.[Cantidad]," +
					"       d.[Sincronizado], d.[CantidadEntregada], d.[SincronizadoEntrega], d.[FechaEntrega], d.[Tipo]," +
					"       d.[Fechareal], d.[item], d.[bodega], d.[iva] " +
					"FROM [DETALLECAMBIOS] d " +
					"WHERE d.[NumDoc] = '"+ numeroDoc +"'";

			// inicia transaccion para la DataBase.db
			db.beginTransaction();			
			//ejecutar insercion
			db.execSQL(query);			
			// confirmar transaccion.
			db.setTransactionSuccessful();			
			//confirmar la insercion
			insertado = true;
			Log.i("DataBaseBO Copy DETALLEVIRTUAL", "Detalle copiado en DataBase ok!!!!!");
		} catch (SQLException e) {
			Log.e("DataBaseBO Copy DETALLEVIRTUAL", "Error copiando tabla detalle: " + e.getMessage());
			insertado = false;
		} finally {
			// cerrar conexiones con la database.
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;	
	}

	/**
	 * Metodo para insertar el detalleViertual en la temp.db.
	 * @param numeroDoc
	 * @param context
	 * @return
	 */
	public static boolean insertarDetalleTemp(String numeroDoc, Context context) {
		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;

		try {				
			// cargar rutas de bases de datos.
			File fileDataBase = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileDataBase.getPath();

			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "Temp.db");
			String DBtemp = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			String attach = "ATTACH '" + DBtemp + "' AS 'db2'; ";
			String detach = "DETACH 'db2';";
			//adjuntar temp para agregar el detalle
			db.execSQL(attach);

			//consulta para realizar la copia de tablas.
			String query = "" +
					"INSERT INTO db2.[04Detalle] (Numdoc, Fecha, CodigoRef, Precio, TarifaIva, DescuentoRenglon, Cantidad," +
					"                         Sincronizado, CantidadEntregada, SincronizadoEntrega, FechaEntrega, Tipo," +
					"                         Fechareal, item, bodega, iva) " +
					"SELECT d.[NumDoc], d.[Fecha], d.[CodigoRef], d.[Precio], d.[TarifaIva], d.[DescuentoRenglon], d.[Cantidad]," +
					"       d.[Sincronizado], d.[CantidadEntregada], d.[SincronizadoEntrega], d.[FechaEntrega], d.[Tipo]," +
					"       d.[Fechareal], d.[item], d.[bodega], d.[iva] " +
					"FROM [DETALLEVIRTUAL] d " +
					"WHERE d.[NumDoc] = '"+ numeroDoc +"'" +
					"UNION " +
					"SELECT d.[NumDoc], d.[Fecha], d.[CodigoRef], d.[Precio], d.[TarifaIva], d.[DescuentoRenglon], d.[Cantidad]," +
					"       d.[Sincronizado], d.[CantidadEntregada], d.[SincronizadoEntrega], d.[FechaEntrega], d.[Tipo]," +
					"       d.[Fechareal], d.[item], d.[bodega], d.[iva] " +
					"FROM [DETALLECAMBIOS] d " +
					"WHERE d.[NumDoc] = '"+ numeroDoc +"'";

			//ejecutar insercion
			db.execSQL(query);	

			/* generar un retardo para esperar insercion en bd temp. ya que no se puede usar transacciones 
			 * en este modo de copiado de tablas desde diferentes bases de datos.
			 * es importante dejar este retardo para que se eviten fallos en la insercion a la temp.db. 
			 * este retardo es muy minimo y no representa ningun efecto para el usuario.*/
			for (int i = 0; i < 100000000; i++) {
				; // no hace nada.
			}

			//soltar la temp
			db.execSQL(detach);

			//confirmar la insercion			
			insertado = true;
			for (int i = 0; i < 100000000; i++) {
				; // no hace nada.
			}
			Log.i("DataBaseBO Copy DETALLEVIRTUAL", "Detalle copiado en Temp.db ok!!!!!");
		} catch (SQLException e) {
			Log.e("DataBaseBO Copy DETALLEVIRTUAL", "Error copiando tabla detalle: " + e.getMessage());
			insertado = false;
		} finally {
			// cerrar conexiones con la database.
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;	
	}


	/**
	 * metodo que permite conocer si hay algun detalle insertado en la tabla virtual.
	 * esto con el fin de validar si se intenta terminar un pedido al cual no se le han 
	 * insertado productos.
	 * @return true, si hay detalles, false en caso contrario.
	 */
	public static boolean verificarSiHayDetalle(String numeroDoc,Context context) {
		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean hayDetalle = false;
		try {
			// cargar rutas de bases de datos.
			File fileDataBase = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileDataBase.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getReadableDatabase();  

			String query = "" +
					"SELECT COALESCE(SUM(c.conteo),0) AS conteo " +
					"FROM ( " +
					"    SELECT COUNT(d.[NumDoc]) AS conteo FROM [DETALLEVIRTUAL] d WHERE d.[NumDoc] = '" + numeroDoc + "' " +
					"    UNION " +
					"    SELECT COUNT(d.[NumDoc]) AS conteo FROM [DETALLECAMBIOS] d WHERE d.[NumDoc] = '" + numeroDoc + "' " +
					") c;";

			Cursor cursor = db.rawQuery(query, null);

			if(cursor.moveToFirst()){
				do {
					int conteo = cursor.getInt(cursor.getColumnIndex("conteo"));
					if(conteo > 0){
						hayDetalle = true;
					}
				} while (cursor.moveToNext());
			}

			if(!cursor.isClosed()){
				cursor.close();
			}
		}  catch (SQLException e) {
			Log.e("Consulta si hay detalle", "Error consultando tabla detalle: " + e.getMessage());
			hayDetalle = false;
		} finally {
			// cerrar conexiones con la database.
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return hayDetalle;
	}


	/**
	 * metodo que inserta un encabezado en la database.db
	 * @param encabezado
	 * @param baseContext
	 * @return
	 */
	public static boolean insertarEncabezadoDataBase(Encabezado encabezado,	Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;
		ContentValues values = null;

		try {			
			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//cargar valores para insertar en la tabla			
			values = cargarValuesEncabezado(encabezado);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			long rows = db.insertOrThrow("[04Encabezado]", null, values);			
			if(rows == -1){
				throw new SQLException("error insertando encabezado");
			}
			// confirmar transaccion.
			db.setTransactionSuccessful();
			insertado = true;
			Log.i("Insertar Encabezado", "Encabezado insertado en database OK!");
			for (int i = 0; i < 10000000; i++) {
				;
			}

		} catch (SQLException e) {
			Log.e("Insertar Encabezado", "Error insertando: " + e.getMessage());
			insertado = false;
		} finally {
			//limpiar memoria.
			if(values != null)
				values.clear();
			// cerrar conexiones
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;	
	}

	/**
	 * metodo para insertar el encabezado en temp.db
	 * @param encabezado
	 * @param baseContext
	 * @return
	 */
	public static boolean insertarEncabezadoTemp(Encabezado encabezado, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;
		ContentValues values = null;

		try {			
			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "Temp.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//cargar valores para insertar en la tabla			
			values = cargarValuesEncabezado(encabezado);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			long rows = db.insertOrThrow("[04Encabezado]", null, values);			
			if(rows == -1){
				throw new SQLException("error insertando encabezado");
			}
			// confirmar transaccion.
			db.setTransactionSuccessful();
			insertado = true;
			Log.i("Insertar Encabezado", "Encabezado insertado en temp.db OK!");
			for (int i = 0; i < 10000000; i++) {
				;
			}

		} catch (SQLException e) {
			Log.e("Insertar Encabezado", "Error insertando encambezado en temp.db: " + e.getMessage());
			insertado = false;
		} finally {
			//limpiar memoria.
			if(values != null)
				values.clear();
			// cerrar conexiones
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;	
	}



	/**
	 * Metodo que permite cargar el contenido a ser insertado en el encabezado.
	 * valido para insertar en temp.db y database.db
	 */
	private static ContentValues cargarValuesEncabezado(Encabezado encabezado) {

		//cargar fecha actual del movil.
		Calendar calendar = new GregorianCalendar();
		//fecha actual
		Date date = calendar.getTime();			
		calendar.setTime(date);			
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		String fechaReal = sdf.format(date);		

		//valores a insertar.
		ContentValues values = new ContentValues();

		values.put("Codigo", encabezado.codigo);
		values.put("NumeroDoc", encabezado.numeroDoc);
		values.put("FechaTrans", encabezado.fechaTrans); 
		values.put("TipoTrans", encabezado.tipoTrans);
		values.put("MontoFact", encabezado.montoFact);
		values.put("Desc1", encabezado.desc1);
		values.put("Iva", encabezado.iva);
		values.put("Usuario", encabezado.usuario);
		values.put("Sincronizado", encabezado.sincronizado);
		values.put("Telefono", encabezado.telefono);
		values.put("Entregado", encabezado.entregado);
		values.put("SincronizadoEntrega", encabezado.sincronizadoEntrega);
		values.put("Formapago", encabezado.formaPago);
		values.put("HoraFinal", encabezado.horaFinal); 
		values.put("Fechareal", fechaReal);
		values.put("Litros", encabezado.litros);
		values.put("Montofact2", encabezado.montoFact2);
		values.put("Iva2", encabezado.iva2);
		values.put("LITROS2B", encabezado.listros2B);
		values.put("inicioPedido", encabezado.inicioPedido);
		values.put("finalPedido", encabezado.finalPedido);
		values.put("bodega", encabezado.bodega);
		values.put("FechaEntrega", encabezado.fechaEntrega);

		return values;
	}



	/**
	 * metodo para cargar los motivos de no compra.
	 * @param listaMotivosNoCompra
	 * @param context
	 */
	public static void cargarMotivosNoCompra(List<MotivosNoCompra> listaMotivosNoCompra, Context context) {

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//generar la consulta para obtener el cliente.
			String[] columns = new String[]{"codigo", "motivo"};
			String orderBy = "codigo";
			Cursor cursor = db.query("MotivosCompras", columns, null, null, null, null, orderBy);			
			//lectura de datos
			if(cursor.moveToFirst()){
				do {
					MotivosNoCompra motivo = new MotivosNoCompra();
					motivo.codigo = cursor.getInt(cursor.getColumnIndex("codigo"));
					motivo.motivo = cursor.getString(cursor.getColumnIndex("motivo"));
					listaMotivosNoCompra.add(motivo);
				} while (cursor.moveToNext());
			}
			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Motivos no compra", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}	
	}



	/**
	 * metodo para insertar una novedad en la database.db
	 * @param motivo
	 * @param encabezado
	 * @param baseContext
	 * @return
	 */
	public static boolean insertarNovedadDataBase(MotivosNoCompra motivo, Encabezado encabezado, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;
		ContentValues values = null;

		try {			
			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//cargar valores para insertar en la tabla			
			values = cargarValuesNovedad(encabezado, motivo);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			long rows = db.insertOrThrow("[04NovedadesCompras]", null, values);			
			if(rows == -1){
				throw new SQLException("error insertando novedad");
			}
			// confirmar transaccion.
			db.setTransactionSuccessful();
			insertado = true;
			Log.i("Insertar novedad", "Novedad insertada en database OK!");

		} catch (SQLException e) {
			Log.e("Insertar novedad", "Error insertando: " + e.getMessage());
			insertado = false;
		} finally {
			//limpiar memoria.
			if(values != null)
				values.clear();
			// cerrar conexiones
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;		
	}



	/**
	 * metodo para insertar novedad en la temp.db
	 * @param motivo
	 * @param encabezado
	 * @param baseContext
	 * @return
	 */
	public static boolean insertarNovedadTemp(MotivosNoCompra motivo, Encabezado encabezado, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;
		ContentValues values = null;

		try {			
			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "Temp.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//cargar valores para insertar en la tabla			
			values = cargarValuesNovedad(encabezado, motivo);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			long rows = db.insertOrThrow("[04NovedadesCompras]", null, values);			
			if(rows == -1){
				throw new SQLException("error insertando novedad");
			}
			// confirmar transaccion.
			db.setTransactionSuccessful();
			insertado = true;
			Log.i("Insertar novedad", "Novedad insertada en temp.db OK!");

		} catch (SQLException e) {
			Log.e("Insertar novedad", "Error insertando: " + e.getMessage());
			insertado = false;
		} finally {
			//limpiar memoria.
			if(values != null)
				values.clear();
			// cerrar conexiones
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;	
	}



	/**
	 * metodo para cargar los campos de la tabla novedadescompras.
	 * @param encabezado
	 * @param motivo
	 * @return
	 */
	private static ContentValues cargarValuesNovedad(Encabezado encabezado, MotivosNoCompra motivo) {

		//valores a insertar.
		ContentValues values = new ContentValues();

		values.put("CodigoCliente", encabezado.codigo);
		values.put("Fecha", encabezado.finalPedido);
		values.put("Motivo", motivo.codigo); 
		values.put("Vendedor", encabezado.usuario);
		values.put("Telefono", encabezado.telefono);
		values.put("Sincronizado", encabezado.sincronizado);
		values.put("Valor", encabezado.montoFact);
		values.put("Litros", encabezado.litros);
		values.put("Numerodoc", encabezado.numeroDoc);
		values.put("valor2", encabezado.montoFact2);
		values.put("litros2", encabezado.listros2B);
		values.put("longitud", 0);
		values.put("latitud",  0);
		values.put("sincronizadomapas", 0); 
		values.put("barcode", 0);
		values.put("bodega", encabezado.bodega);
		values.put("VERSION", encabezado.version);		
		return values;	
	}




	/**
	 * confirmar la correcta insercion de todos los datos en database.db
	 * @param numeroDoc
	 * @param context
	 * @return
	 */
	public static boolean confirmarPedidoTerminadoDataBase(String numeroDoc, Context context) {
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		int contador = Integer.MIN_VALUE;
		boolean insertado = false;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//generar la consulta para verificar que el numeroDoc existe en las tres tablas.
			String query = "SELECT  COUNT(e.[Numerodoc]) AS insertados FROM [04Encabezado] e " +
					"INNER JOIN [04Detalle] d " +
					"      ON e.[NumeroDoc] = d.[NumDoc]      " +
					"INNER JOIN [04NovedadesCompras] n " +
					"      ON e.[NumeroDoc] = n.[Numerodoc] " +
					"WHERE e.[Numerodoc] = '" + numeroDoc + "'";

			Cursor cursor = db.rawQuery(query, null);			
			//lectura de datos
			if(cursor.moveToFirst()){
				do {
					contador  = cursor.getInt(cursor.getColumnIndex("insertados"));
				} while (cursor.moveToNext());
			}
			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
			//verificar la insercion en todas las tablas.
			if(!(contador > 0)){
				insertado = false;
				throw new Exception("no insertado en todas las tablas");
			}
			insertado = true;
		} catch (Exception e) {
			Log.e("Conteo de Inserciones", "No se realizo la consulta en dataBase.db: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}	
		return insertado;
	}



	/**
	 * confirmar pedido insertado en todas las tablas de temp.db
	 * @param numeroDoc
	 * @param baseContext
	 * @return
	 */
	public static boolean confirmarPedidoTerminadoTemp(String numeroDoc, Context context) {
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		int contador = Integer.MIN_VALUE;
		boolean insertado = false;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "Temp.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//generar la consulta para verificar que el numeroDoc existe en las tres tablas.
			String query = "SELECT  COUNT(e.[Numerodoc]) AS insertados FROM [04Encabezado] e " +
					"INNER JOIN [04Detalle] d " +
					"      ON e.[NumeroDoc] = d.[NumDoc]      " +
					"INNER JOIN [04NovedadesCompras] n " +
					"      ON e.[NumeroDoc] = n.[Numerodoc] " +
					"WHERE e.[Numerodoc] = '" + numeroDoc + "'";

			Cursor cursor = db.rawQuery(query, null);			
			//lectura de datos
			if(cursor.moveToFirst()){
				do {
					contador  = cursor.getInt(cursor.getColumnIndex("insertados"));
				} while (cursor.moveToNext());
			}
			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
			//verificar la insercion en todas las tablas.
			if(!(contador > 0)){
				insertado = false;
				throw new Exception("no insertado en todas las tablas");
			}
			insertado = true;
		} catch (Exception e) {
			Log.e("Conteo de Inserciones", "No se realizo la consulta en temp.db: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}	
		return insertado;
	}



	/**
	 * metodo para actualizar el consecutivo en la base de datos.
	 * @param consecutivo
	 * @param context
	 */
	public static void actualizarConsecutivo(int consecutivo, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		ContentValues values = null;

		try {			
			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// valor a actualizar
			values = new ContentValues();
			values.put("Consecutivo", consecutivo);

			// inicia transaccion
			db.beginTransaction();

			db.update("Vendedor", values, null, null);		

			// confirmar transaccion.
			db.setTransactionSuccessful();
			Log.i("Actualizar consecutivo", "consecutivo actualizado OK!");

		} catch (SQLException e) {
			Log.e("Actualizar consecutivo", "Error actualizando: " + e.getMessage());
		} finally {
			//limpiar memoria.
			if(values != null)
				values.clear();
			// cerrar conexiones
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}	
	}



	/**
	 * Metodo para borrar el detalle. de posibles pedidos sin terminar. en DataBase.db
	 * @param numeroDoc
	 * @param baseContext
	 */
	public static void borrarPedidoSinTerminarDataBase(String numeroDoc, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		ContentValues values = null;

		try {			
			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();		

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();			

			// inicia transaccion
			db.beginTransaction();

			db.delete("[04Detalle]", "Numdoc=?", new String[]{numeroDoc});		

			// confirmar transaccion.
			db.setTransactionSuccessful();
			Log.i("borrar basura", "basura borrada de DataBase OK!");			

		} catch (SQLException e) {
			Log.e("borrar basura", "Error limpiando basura: " + e.getMessage());
		} finally {
			//limpiar memoria.
			if(values != null)
				values.clear();
			// cerrar conexiones
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
	}



	/**
	 * limpiar posibles datos pedidos basuras de temp.db
	 * @param numeroDoc
	 * @param context
	 */
	public static void borrarPedidoSinTerminarTemp(String numeroDoc, Context context) {
		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		ContentValues values = null;

		try {			
			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "Temp.db");
			String dataBase = fileTemp.getPath();		

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);			

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();			

			// inicia transaccion
			db.beginTransaction();

			db.delete("[04Detalle]", "Numdoc=?", new String[]{numeroDoc});		

			// confirmar transaccion.
			db.setTransactionSuccessful();
			Log.i("Borrar basura", "basura borrada de DataBase OK!");


		} catch (SQLException e) {
			Log.e("Borrar basura", "Error limpiando basura: " + e.getMessage());
		} finally {
			//limpiar memoria.
			if(values != null)
				values.clear();
			// cerrar conexiones
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}	
	}



	/**
	 * metodo que retorna la lista de clientes por visitar.
	 * Esta informacion se carga de la tabla rutero, ordenada numericamente segun su columna orden.
	 * despues de definir una novedad para un cliente visitado en el rutero, este se quita de la lista.
	 * esta comparacion se hace con la tabla novedad, ya que ahi se guardan todos los eventos realizados a los clientes.
	 * @param baseContext
	 * @return
	 */
	public static ArrayList<Cliente> listarClientesRutero(Context context) {

		//crear la lista nula
		ArrayList<Cliente> listaClientes = new ArrayList<Cliente>();

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();
			String query = "" +
					"SELECT  r.[codigoCliente] AS codigoCliente, c.[razon] AS razon, c.[representante] AS representante, c.[DIRECCION] AS direccion," +
					"COALESCE(c.[telefono], 'No registra') AS telefono, c.[NIT] AS nit, c.[VENDEDOR] AS vendedor, c.[BARRIO] AS barrio " +
					"FROM (SELECT CAST(r.[ORDEN] AS NUMERIC) AS orden, CodigoCliente AS codigoCliente   FROM Rutero r) r " +
					"INNER JOIN Clientes c " +
					"      ON c.[CODIGO] = r.[codigoCliente]      " +
					"WHERE c.[CODIGO] NOT IN (SELECT n.[CodigoCliente] FROM [04NovedadesCompras] n) " +
					"ORDER BY r.[orden];";

			//ejecutar consulta.
			Cursor cursor = db.rawQuery(query, null);

			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					Cliente cliente = new Cliente();
					cliente.codigo = cursor.getString(cursor.getColumnIndex("codigoCliente"));
					cliente.razonSocial = cursor.getString(cursor.getColumnIndex("razon"));
					cliente.representante = cursor.getString(cursor.getColumnIndex("representante"));
					cliente.direccion = cursor.getString(cursor.getColumnIndex("direccion"));
					cliente.telefono = cursor.getString(cursor.getColumnIndex("telefono"));
					cliente.NIT = cursor.getString(cursor.getColumnIndex("nit"));
					cliente.vendedor = cursor.getString(cursor.getColumnIndex("vendedor"));
					cliente.barrio = cursor.getString(cursor.getColumnIndex("barrio"));

					//agregar el cliente a la lista
					listaClientes.add(cliente);					
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Listar Clientes Rutero", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return listaClientes;	
	}



	/**
	 * obtener la version de la aplicacion.
	 * @param context
	 * @return
	 */
	public static String obtenerVersion(Context context) {

		String version  = "Sin datos";

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//ejecutar consulta.
			String[] columns = new String[]{"version"};
			Cursor cursor = db.query("Version", columns, null, null, null, null, null);


			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					version = cursor.getString(cursor.getColumnIndex("version"));
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Leer version", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return version;		
	}



	/**
	 * metodo que permite conocer el resumen de estadisticas del dia.
	 * @param context 
	 * @return
	 */
	public static ResumenEstadisticas obtenerResumenEstadisticas(Context context) {

		ResumenEstadisticas resumen = null;
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//ejecutar consulta.
			String query = "" +
					"SELECT COUNT(n.[Numerodoc]) AS visitas, " +
					"       ns.[no_sincronizados] AS no_sincronizados, " +
					"       ns.[monto] AS valor_no_sinc,		" +
					"       s.[sincronizados] AS sincronizados, " +
					"       s.[monto] AS valor_sinc, " +
					"       p.[pedidos] AS pedidos, " +
					"       v.[valor] AS valor," +
					"       nc.[no_compras] AS no_compras," +
					"       ncs.[no_compras_sinc] AS no_compras_sinc,       " +
					"       ncns.[no_compras_nsinc] AS no_compras_no_sinc " +
					"FROM [04NovedadesCompras] n 		" +
					"     INNER JOIN 			" +
					"           (SELECT " +
					"                   COUNT(n.[Sincronizado]) AS no_sincronizados, " +
					"                   COALESCE(SUM(n.[Valor]),0) AS monto " +
					"           FROM [04NovedadesCompras] n 			 " +
					"           WHERE n.[Sincronizado] = 0 AND n.[Motivo] = 1) ns     " +
					"     INNER JOIN 			" +
					"           (SELECT " +
					"                   COUNT(n.[Sincronizado]) AS sincronizados, " +
					"                   COALESCE(SUM(n.[Valor]),0) AS monto FROM [04NovedadesCompras] n 			" +
					"           WHERE n.[Sincronizado] = 1 AND n.[Motivo] = 1) s     " +
					"     INNER JOIN 			" +
					"           (SELECT " +
					"                   COUNT(n.[Motivo]) AS pedidos " +
					"           FROM [04NovedadesCompras] n 			 " +
					"           WHERE n.[Motivo] = 1) p         " +
					"     INNER JOIN 			" +
					"           (SELECT SUM(n.[Valor]) AS valor FROM [04NovedadesCompras] n) v           " +
					"     INNER JOIN 			" +
					"           (SELECT COUNT(n.[Motivo]) AS no_compras FROM [04NovedadesCompras] n WHERE n.[Motivo] <> 1) nc     " +
					"     INNER JOIN 			" +
					"           (SELECT COUNT(n.[Sincronizado]) AS no_compras_sinc FROM [04NovedadesCompras] n " +
					"           WHERE n.[Motivo] <> 1 AND n.[Sincronizado] = 1) ncs     " +
					"     INNER JOIN 			" +
					"           (SELECT COUNT(n.[Sincronizado]) AS no_compras_nsinc FROM [04NovedadesCompras] n " +
					"           WHERE n.[Motivo] <> 1 AND n.[Sincronizado] = 0) ncns;";

			Cursor cursor = db.rawQuery(query, null);

			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					resumen = new ResumenEstadisticas();
					resumen.visitas = cursor.getInt(cursor.getColumnIndex("visitas"));
					resumen.totalPedidos = cursor.getDouble(cursor.getColumnIndex("pedidos"));
					resumen.pedidosSincronizados = cursor.getInt(cursor.getColumnIndex("sincronizados"));
					resumen.pedidosNoSincronizados = cursor.getInt(cursor.getColumnIndex("no_sincronizados"));
					resumen.totalVentas = cursor.getInt(cursor.getColumnIndex("valor"));
					resumen.valorNoSincronizado = cursor.getDouble(cursor.getColumnIndex("valor_no_sinc"));
					resumen.noCompras = cursor.getInt(cursor.getColumnIndex("no_compras"));
					resumen.noComprasSinc = cursor.getInt(cursor.getColumnIndex("no_compras_sinc"));
					resumen.noComprasNoSinc = cursor.getInt(cursor.getColumnIndex("no_compras_no_sinc"));
					resumen.valorSincronizado = cursor.getDouble(cursor.getColumnIndex("valor_sinc"));
					//ajuste a dos decimales de la efectividad
					double efectividad = (resumen.totalPedidos / resumen.visitas) * 100;					
					resumen.efectividad = Math.rint(efectividad * 100)/100;
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Leer resumen estadisticas", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
		return resumen;			
	}



	/**
	 * metodo que retorna la lista de pedidos no sincronizados, y que seran mostrados 
	 * en una lista al usuario.
	 * @param context
	 * @return
	 */
	public static ArrayList<PedidoNoSincronizado> PedidosNoSincronizados(Context context) {

		ArrayList<PedidoNoSincronizado> listaNoSinc = new ArrayList<PedidoNoSincronizado>();

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//ejecutar consulta.
			String query = "" +
					"SELECT n.[Numerodoc] AS numDoc, n.[Valor] AS monto,SUBSTR(n.[Fecha],11) AS hora, c.[razon] AS cliente, " +
					"m.[motivo] AS motivo " +
					"FROM  [04NovedadesCompras] n " +
					"INNER JOIN [Clientes] c " +
					"      ON n.[CodigoCliente] = c.[CODIGO] " +
					"INNER JOIN [MotivosCompras] m " +
					"      ON m.[codigo]  = n.[Motivo]           " +
					"WHERE n.[Sincronizado] = 0 AND n.[Motivo] = 1 " +
					"ORDER BY n.[Fecha]";

			Cursor cursor = db.rawQuery(query, null);

			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					PedidoNoSincronizado pedido = new PedidoNoSincronizado();
					pedido.numeroDoc = cursor.getString(cursor.getColumnIndex("numDoc"));
					pedido.monto = cursor.getString(cursor.getColumnIndex("monto"));
					pedido.hora = cursor.getString(cursor.getColumnIndex("hora"));
					pedido.cliente = cursor.getString(cursor.getColumnIndex("cliente"));
					pedido.motivo = cursor.getString(cursor.getColumnIndex("motivo"));
					listaNoSinc.add(pedido);
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Listar pedidos no sincronizados", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}		
		return listaNoSinc;
	}



	/**
	 * metodo que retorna una lista de pedidos que ya fueron sincronizados
	 * @param context
	 * @return
	 */
	public static ArrayList<PedidoSincronizado> PedidosSincronizados(Context context) {

		ArrayList<PedidoSincronizado> listaSinc = new ArrayList<PedidoSincronizado>();

		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//ejecutar consulta.
			String query = "" +
					"SELECT n.[Numerodoc] AS numDoc, n.[Valor] AS monto,SUBSTR(n.[Fecha],11) AS hora, c.[razon] AS cliente, " +
					"m.[motivo] AS motivo " +
					"FROM  [04NovedadesCompras] n " +
					"INNER JOIN [Clientes] c " +
					"      ON n.[CodigoCliente] = c.[CODIGO] " +
					"INNER JOIN [MotivosCompras] m " +
					"      ON m.[codigo]  = n.[Motivo]           " +
					"WHERE n.[Sincronizado] = 1 AND n.[Motivo] = 1 " +
					"ORDER BY n.[Fecha]";

			Cursor cursor = db.rawQuery(query, null);

			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					PedidoSincronizado pedido = new PedidoSincronizado();
					pedido.numeroDoc = cursor.getString(cursor.getColumnIndex("numDoc"));
					pedido.monto = cursor.getString(cursor.getColumnIndex("monto"));
					pedido.hora = cursor.getString(cursor.getColumnIndex("hora"));
					pedido.cliente = cursor.getString(cursor.getColumnIndex("cliente"));
					pedido.motivo = cursor.getString(cursor.getColumnIndex("motivo"));
					listaSinc.add(pedido);
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Listar pedidos no sincronizados", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}		
		return listaSinc;
	}



	/**
	 * metodo que permite que despues de enviar pedidos, se cambie su estado a sincronizados.
	 * esto se hace a todas las novedades de la tabla novedadescompra.
	 * @param baseContext
	 */
	public static void actualizarEstadoASincronizado(Context context) {
		//referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("Sincronizado", 1); // cambiar estado de sincronizado a 1

			db.beginTransaction();
			db.update("[04NovedadesCompras]", values, null, null);
			db.setTransactionSuccessful();
			Log.i("Actualizar estado novedad sincronizado", "Novedad sincronizada OK!");

		} catch (SQLException e) {
			Log.e("Actualizar estado novedad sincronizado", "Error actualizando estado sincronizado: " + e.getMessage());
		} finally {
			// cerrar conexiones
			closeDataBase(db);			
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
	}



	/**
	 * insertar productos que van para cambio en la lista tabla virtual de cambios.
	 * @param referencia
	 * @param cantidad
	 * @param numeroDoc
	 * @param bodega
	 * @param context
	 * @return
	 */
	public static boolean insertarProductoEnCambio(Referencias referencia, String cantidad, String numeroDoc, String bodega, Context context) {


		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean insertado = false;

		try {

			//cargar fecha actual del movil.
			Calendar calendar = new GregorianCalendar();
			//fecha actual
			Date date = calendar.getTime();

			//se suman un dias, como maximo para generar fecha de entrega
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_YEAR, 1);	// el 1 es el numero de dias a sumar a la fecha actual		
			Date dateEntrega = calendar.getTime();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			String fechaActual = sdf.format(date);
			String fechaEntrega = sdf.format(dateEntrega);

			// cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			// instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//cargar valores para insertar en la tabla
			ContentValues values = new ContentValues();
			values.put("NumDoc", numeroDoc);
			values.put("Fecha", fechaActual);
			values.put("CodigoRef", referencia.codigo); //constraint unique
			values.put("Precio", 0);
			values.put("TarifaIva", 0);
			values.put("DescuentoRenglon", 0);
			values.put("Cantidad", cantidad);
			values.put("Sincronizado", 0);
			values.put("CantidadEntregada", 0);
			values.put("SincronizadoEntrega", 0);
			values.put("FechaEntrega", fechaEntrega);
			values.put("Tipo", referencia.tipo);
			values.put("Fechareal", fechaActual);
			values.putNull("item"); // autoincremental
			values.put("bodega", bodega);
			values.put("iva", referencia.iva);

			// abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			// inicia transaccion
			db.beginTransaction();

			long rows = db.insertOrThrow("DETALLECAMBIOS", "item", values);  //item se envia vacio, para generar el autoincremental

			if(rows == -1){
				throw new SQLException("posible referencia repetida");
			}
			// confirmar transaccion.
			db.setTransactionSuccessful();
			insertado = true;
			Log.i("Insertar Referencia CAMBIO", "DetalleCambio insertado OK!");

		} catch (SQLException e) {
			Log.e("Insertar Referencia CAMBIO", "Error insertando: " + e.getMessage());
			insertado = false;
		} finally {
			closeDataBase(db);
			// cerrar conexiones
			if (helperDataBase != null) {
				helperDataBase.close();
			}
		}
		return insertado;	
	}


	/**
	 * metodo que permite verificar si un usuario ya termino labores, lo que impide que se puedan seguir tomando pedidos.
	 * @param context
	 * @return (String) "OK" si el usuario no ha terminado dia, en caso contrario la fecha en que termino labores
	 */
	public static String verificarSiTerminoLabores(Context context) {
		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		String termino = "OK";
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getReadableDatabase();

			//ejecutar consulta.
			String query = "" +
					"SELECT COALESCE(n.[Fechatermino],'OK') AS fecha_termino FROM [04NovedadesCompras] n " +
					"GROUP BY n.[Fechatermino] " +
					"ORDER BY n.[Fechatermino] DESC " +
					"LIMIT 1";

			Cursor cursor = db.rawQuery(query, null);

			//verificar si existen datos.
			if(cursor.moveToFirst()){
				do {
					termino = cursor.getString(cursor.getColumnIndex("fecha_termino"));					
				} while (cursor.moveToNext());
			}

			//cerrar el cursor.
			if(!cursor.isClosed()){
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("Consultar si termino labores", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}		
		return termino;
	}

	/**
	 * metodo para confirmar un terminar dia, el cual actualiza la columna fechatermino de la tabla novedades
	 * en database.db, de un null a la fecha actual. Para permitir mas pedidos es necesario que el usuario
	 * se comunique con la web para que desde alli le cambien a null este campo, inicie dia de nuevo y pueda realizar
	 * mas pedidos.
	 */
	public static void confirmarFechaTerminoLabores(Context context) {	
		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;

		try {
			//cargar fecha actual del movil.
			Calendar calendar = new GregorianCalendar();
			//fecha actual
			Date date = calendar.getTime();

			//fecha a string
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			String fechaActual = sdf.format(date);
			
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//guardar valores a insertar
			ContentValues values = new ContentValues();
			values.put("Fechatermino", fechaActual);			

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion con DataBase.db
			db = helperDataBase.getWritableDatabase();

			//inicia transaccion
			db.beginTransaction();

			//actualizar todas las filas, (solo debe contener una fila)
			int update = db.update("[04NovedadesCompras]", values, null, null);
			values.clear();
			if(update < 1){				
				throw new Exception("Error actualizando FechaTermino en DataBase.db");
			}			
			//confirmar transaccion.
			db.setTransactionSuccessful();
			Log.i("Novedades fechatermino update", "FechaTermino actualizado a: " + fechaActual);

		} catch (Exception e) {
			Log.e("Novedades fechatermino update", "Error actualizando fechatermino en bd: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
	}



	public static boolean validarVentanaHoraria(Tiempo tiempo, Context context) {
		
		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		boolean validado = false;

		try {
			/*cargar rutas de bases de datos.*/
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();
			
			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);
			
			//abrir conexion con DataBase.db
			db = helperDataBase.getReadableDatabase();
			
			String query = ""
					+ "SELECT v.[anio] AS anio,"
					+ "       v.[mes] AS mes, "
					+ "       v.[dia] AS dia, "
					+ "       v.[horaInicio] AS H_inicio, "
					+ "       v.[minutoInicio] AS M_inicio, "
					+ "       v.[horaFin] AS H_fin, "
					+ "       v.[minutoFin] AS M_fin "
					+ "FROM [VentanaHoraria] v LIMIT 1;";
			
			Cursor cursor = db.rawQuery(query, null);
			
			if(cursor.moveToFirst()){
				do {
					/*validaciones de ventana horaria*/
					int anio = cursor.getInt(cursor.getColumnIndex("anio"));
					if(anio > tiempo.year){
						break;
					}
					
					int mes = cursor.getInt(cursor.getColumnIndex("mes"));
					if(mes != tiempo.Month){
						break;
					}
					
					int dia = cursor.getInt(cursor.getColumnIndex("dia"));
					if(dia != tiempo.dayOfMonth){
						break;
					}
					
					int hInicio = cursor.getInt(cursor.getColumnIndex("H_inicio"));
					int mInicio = cursor.getInt(cursor.getColumnIndex("M_inicio"));
					int hFin = cursor.getInt(cursor.getColumnIndex("H_fin"));
					int mFin = cursor.getInt(cursor.getColumnIndex("M_fin"));
					
					/*Pasar horas de calculo a decimales.*/
					float inicio = ((hInicio * 100f) + mInicio)/100f;
					float actual = ((tiempo.hour * 100f) + tiempo.minute) / 100f;
					float fin =    ((hFin * 100f) + mFin) / 100f;
					
					if(inicio > actual || actual > fin){
						break;
					}
					else {
						validado = true;
						break;
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e("Novedades fechatermino update", "Error actualizando fechatermino en bd: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}
	return validado;
	}



	/**
	 * Insertar coordenada de seguimiento
	 * @param latitud
	 * @param longitud
	 * @param tiempo
	 * @param consecutivo
	 * @param vendedor
	 * @param context
	 */
	public static void insertarCoordendaSeguimiento(double latitud, double longitud, Tiempo tiempo, long consecutivo, String vendedor, Context context) {

		// referencia del manejador de las base de datos.
		DataBaseHelper helperDataBase = null;
		SQLiteDatabase db = null;
		long numero = 1;
		
		try {
			//cargar rutas de bases de datos.
			File fileTemp = new File(Util.DirApp(), "DataBase.db");
			String dataBase = fileTemp.getPath();

			//instancia de la conexion a la bd DataBase.db
			helperDataBase = new DataBaseHelper(context, dataBase);

			//abrir conexion de solo lectura.
			db = helperDataBase.getWritableDatabase();
			
			/*leer el consecutivo actual*/
			String query = "SELECT max(consecutivoMovil) as consecutivo FROM [CoordenadaSeguimiento];";
			Cursor cursor = db.rawQuery(query, null);
			if(cursor.moveToFirst()){
				do {
					numero = cursor.getLong(cursor.getColumnIndex("consecutivo")) + 1;
					break;
				} while (cursor.moveToNext());
			}
			if(!cursor.isClosed()){
			cursor.close();
			}

			ContentValues values = new ContentValues();
			values.put("latitud", latitud);
			values.put("longitud", longitud);
			values.put("fechaMovil", tiempo.lastUpdateTime);
			values.put("hora", tiempo.hour);
			values.put("minuto", tiempo.minute);
			values.put("consecutivoMovil", numero);
			values.put("vendedor", vendedor);
			
			db.insert("CoordenadaSeguimiento", null, values);
			
			
		} catch (Exception e) {
			Log.e("Consultar si termino labores", "No se realizo la consulta: " + e.getMessage());
		} finally {
			closeDataBase(db);
			//cerrar conexiones			
			if(helperDataBase != null){
				helperDataBase.close();
			}			
		}		
	}

}// final de la clase
