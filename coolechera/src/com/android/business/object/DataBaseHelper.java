/**
 * 
 */
package com.android.business.object;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author JICZ
 *
 */
public class DataBaseHelper extends SQLiteOpenHelper {

	/**
	 * version creada en 21/agosto/2014
	 */
	private static final int DATABASE_VERSION = 1;

	
	
	/**
	 * Constructor, solo se requiere definir un contexto para la cual estara disponible la conexion. 
	 * @param context
	 */
	public DataBaseHelper(Context context, String name ) {		
		super(context, name, null, DATABASE_VERSION);
	}

	
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
