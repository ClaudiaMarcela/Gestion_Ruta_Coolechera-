package co.com.uniquindio.database;

import java.io.File;



import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;






public class DataBaseBO {
    
    
    
    
    public static File DirApp() {
	
	File SDCardRoot = Environment.getExternalStorageDirectory();
	File dirApp = new File(SDCardRoot.getPath() + "/" + "A_ING_SW_3");
	
	if(!dirApp.isDirectory())
	    dirApp.mkdirs();
	
	return dirApp;
    }
    
    
    
    
    
    public static void guardarPosicionActual(double latitud, double longitud, String fecha, Context context ) {
	
	File filePedido = new File(DirApp(), DataBaseHelper.DATABASE_NAME);
	String temp = filePedido.getPath();
	SQLiteDatabase db = null;
	DataBaseHelper mOpenHelperDataBase = new DataBaseHelper(context, temp);
	db = mOpenHelperDataBase.getWritableDatabase();
	
	ContentValues values = new ContentValues();
	values.put("latitud", latitud);
	values.put("longitud", longitud);
	values.put("direccion", direccion);
	values.put("fecha", fecha);
	db.close();
	closeHelperDataBase(mOpenHelperDataBase);
	
    }
    
    
    
    
    /**
     * cerrar el asistente de conexion a la base de datos.
     * @param db
     */
    private static void closeHelperDataBase(DataBaseHelper db) {
	if(db != null){
	    db.close();
	}		
    }
    
    
    
    
    
    
    
    
}//final de la clase