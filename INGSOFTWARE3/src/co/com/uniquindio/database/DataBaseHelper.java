/**
 * 
 */
package co.com.uniquindio.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * Clase de prueba para administracion de la DataBase.db descargada desde el sincronizador, 
 * permite un control mas optimizado de la conexion y las trasnsacciones que se hacen a la base de datos.
 * basado en el ejemplo ofrecido por android en sdk\samples\android-10  -> NotePadProvider.java
 * @author JICZ
 *
 */
public class DataBaseHelper extends SQLiteOpenHelper {

		
	/**
	 * version creada en 05/agosto/2014
	 */
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "ING_SW_3.db";
	
	
	private String sqlCreate = "" +
			"CREATE TABLE [coordenadas] (" +
			"  [latitud] FLOAT NOT NULL ON CONFLICT ROLLBACK, " +
			"  [longitud] FLOAT NOT NULL, " +
			"  [direccion] VARCHAR(50) NOT NULL, " +
			"  [fecha] DATE);";
	
	
	/**
	 * Constructor, solo se requiere definir un contexto para la cual estara disponible la conexion. 
	 * @param context
	 */
	public DataBaseHelper(Context context, String name) {		
		super(context, name, null, DATABASE_VERSION);
	}
	
	

	@Override
	public void onCreate(SQLiteDatabase db) {
	    db.execSQL(sqlCreate);
	}

	
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}	

}
