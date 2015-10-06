package com.android.conexion.constantes;

public class Const {

	public final static String TITULO;
	public final static String URL_SYNC;
	public final static String URL_DOWNLOAD_NEW_VERSION;
	public final static String URL_DOWNLOAD_CATALOGO;



	private final static int PRUEBAS    = 1;
	private final static int PRODUCCION = 2;



	/**
	 * IMPORTANTE: Indica que aplicacion se va a generar
	 **/
	private final static int APLICACION = PRODUCCION; 

	static {

		switch (APLICACION) {

		case PRUEBAS:

			TITULO = " - DEMO. UNIQUINDIO";
			URL_SYNC = "http://66.33.97.175/SyncCoolecheraPruebas/";
			URL_DOWNLOAD_NEW_VERSION = "http://64.239.115.11/CoolecheraPruebas/coolechera.apk";
			URL_DOWNLOAD_CATALOGO = "http://127.0.0.1/catalogo/catalogo.zip";

			break;

		case PRODUCCION:			

			TITULO = " - DEMO. UNIQUINDIO";
			URL_SYNC = "http://66.33.97.175/SyncCoolecheraPruebas/";
			URL_DOWNLOAD_NEW_VERSION = "http://64.239.115.11/CoolecheraPruebas/coolechera.apk";
			URL_DOWNLOAD_CATALOGO = "http://127.0.0.1/catalogo/catalogo.zip";

			break;

		default:
			TITULO = " - Sin Definir";
			URL_SYNC = "Sin_Definir";
			URL_DOWNLOAD_NEW_VERSION = "Sin_Definir";
			URL_DOWNLOAD_CATALOGO = "Sin_Definir";

			break;
		}
	}	



	public final static int LOGIN                = 1;
	public final static int LOGOUT               = 2;	
	public final static int DOWNLOAD_DATA_BASE   = 3;
	public final static int ENVIAR_PEDIDO        = 4;
	public final static int DOWNLOAD_VERSION_APP = 5;
	public final static int STATUS_SENAL         = 6;
	public final static int DOWNLOAD_MESSAGE     = 7;
	public final static int DOWNLOAD_CATALOGO    = 8;
	public final static int TERMINAR_LABORES     = 9;		

}
