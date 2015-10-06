package com.android.coolechera;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.android.business.object.DataBaseBO;
import com.android.data.Cliente;
import com.android.data.Encabezado;
import com.android.data.InfoVenta;
import com.android.data.MotivosNoCompra;
import com.android.data.Vendedor;
import com.android.fragments.InfoCambiosFragment;
import com.android.fragments.InfoClienteFragment;
import com.android.fragments.InfoPedidoFragment;
import com.android.fragments.ReferenciasFragment;
import com.android.util.Constantes;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * Clase implementada para el control de ventas realizados por un vendedor.
 * @author JICZ
 *
 */
public class VentasActivity extends ActionBarActivity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;


	/**
	 * cliente que sera conservado durante el ciclo de vida de esta activity.
	 * se le registran las ventas a este cliente.
	 */
	private Cliente cliente;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;


	/**
	 * referencia al fragment infoCliente, para permitir actualizaciones
	 * a las vistas de este fragment.
	 */
	private InfoClienteFragment infoCliente;

	/**
	 * referencia al fragment infoPedido, para permitir actualizaciones
	 * a la vistas de este fragment.
	 */
	private InfoPedidoFragment infoPedido;
	
	/**
	 * referencia al fragment infoCambios, para permitir actualizaciones
	 * a la vistas de este fragment.
	 */
	private InfoCambiosFragment infoCambio;

	/**
	 * conserva el consecutivo de encabezado.
	 */
	int consecutivo = Integer.MIN_VALUE;

	/**
	 * progress dialog para mostrar informacion del estado de guardado del pedido.
	 */
	private ProgressDialog progress = null;

	/**
	 * Atributo que conservara el estado de la transaccion. y sera mostrado en pantalla.
	 */
	private static String mensaje = "";

	/**
	 * lista de motivos no compra
	 */
	private List<MotivosNoCompra> listaMotivosNoCompra;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ventas);

		//capturar el codigo de cliente enviado como putExtra
		Bundle bundle = getIntent().getExtras();
		String codCliente = bundle.getString(Constantes.COD_CLIENTE);

		//inicializar motivos de no compra		
		listaMotivosNoCompra = new ArrayList<MotivosNoCompra>();	


		/*cargar el cliente que sera conservado en el ciclo de vida de esta activity. 
		 * fue elegido por el vendedor, desde el rutero o desde buscar cliente.*/
		cliente = DataBaseBO.buscarCliente(codCliente, getBaseContext());
		
		/*verificar si el usuario ya termino labores. si ya termino no se permite la toma de pedidos.
		 * sera necesario informar que debe comunicarse al administrador para habilitar de nuevo. */
		String terminoLabores = DataBaseBO.verificarSiTerminoLabores(getBaseContext());
		
		if(!terminoLabores.equals("OK")){
			mostrarAlertInformaTerminoLabores(terminoLabores);
		}

		/*cargar hora en que se inicia el pedido*/
		Calendar calendar = new GregorianCalendar();
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		String fechaInicioPedido = sdf.format(date);

		//verificar cliente valido y guardar en preferences
		if(cliente != null) {
			//sharedPreference para guardar los datos que seran usados durante esta sesion de venta.
			SharedPreferences prefCliente =  getSharedPreferences(Constantes.CLIENTE, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefCliente.edit();
			editor.putString("CODIGO", cliente.codigo);
			editor.putString("razon", cliente.razonSocial);
			editor.putString("representante", cliente.representante);
			editor.putString("DIRECCION", cliente.direccion);
			editor.putString("telefono", cliente.telefono);
			editor.putString("NIT", cliente.NIT);
			editor.putString("VENDEDOR", cliente.vendedor);
			editor.putString("BARRIO", cliente.barrio);
			editor.commit();
		}

		//cargar el vendedor que esta logueado para este venta.
		Vendedor vendedor = DataBaseBO.obtenerVendedor();

		if(vendedor != null){			

			consecutivo = vendedor.consecutivo;
			//generar numeroDoc para esta sesion de venta.
			String numeroDoc = DataBaseBO.generarNumeroDoc(vendedor, consecutivo, getBaseContext());

			if(!numeroDoc.equals("")){
				//sharedPreference para guardar los datos que seran usados durante esta sesion de venta.
				SharedPreferences prefNumDoc =  getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefNumDoc.edit();
				editor.putInt("consecutivo", vendedor.consecutivo);
				editor.putString("numeroDoc", numeroDoc);
				editor.putString("bodega", vendedor.bodega);
				editor.putString("fechaInicioPedido", fechaInicioPedido);
				editor.putString("version", vendedor.version);
				editor.commit();
			}
			else {
				//informar de fallo en la BD.
				mostrarAlertDialogDetalleNoCreado();
			}					
		}
		else {
			//informar de fallo en la BD.
			mostrarAlertDialogDetalleNoCreado();
		}

		/* CREAR TABLA DETALLEVIRTUAL y DETALLECAMBIOS, permite insertar en esta tabla el detalle del pedido. Cuando se 
		 * confirme el pedido, se inserta cada detalle en la tabla [detalle] normal de la base de datos.
		 */
		boolean tablaCreada = DataBaseBO.crearTablaDetalleVirtual(getBaseContext());
		boolean tablaCambioCreada = DataBaseBO.crearTablaDetalleCambiosVirtual(getBaseContext());
		if(!tablaCreada && !tablaCambioCreada){
			mostrarAlertDialogDetalleNoCreado();
		}

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				//solo actualizar cuando se solicita el fragment infoCliente.
				if(position == 0){
					infoCliente.actualizarEstadoVenta();
				}
				if(position == 2) {
					//mostrar todos los productos pedidos hasta el momento.
					infoPedido.cargarListaProductos("");
				}
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
		}		
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ventas, menu);
		return true;
	}




	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_Terminar:
			//mostrarProgress
			progressDialogGuardandoPedido();
			break;

		case R.id.action_No_Compra:
			//Definir no Compra
			mostrarDialogNoCompra();
			break;


		case R.id.action_Cancelar:
			//cancelar la venta actual, no se registra el pedido.
			mostrarAlertDialogCancelarPedido();
			break;
		}

		return super.onOptionsItemSelected(item);
	}



	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.		
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}	

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).

			switch (position) {
			case 0:
				infoCliente = InfoClienteFragment.newInstance(position + 1);
				return infoCliente;

			case 1:
				return ReferenciasFragment.newInstance(position + 1);

			case 2:
				infoPedido = InfoPedidoFragment.newInstance(position + 1);
				return infoPedido;	

			case 3:
				infoCambio = InfoCambiosFragment.newInstance(position + 1);
				return infoCambio;

			default:
				//si hay un fallo mostrar en blanco, para evitar el crash.
				return PlaceholderFragment.newInstance(position + 1);
			}
		}		


		@Override
		public int getCount() {
			// Show 4 total pages.
			return 4;
		}



		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_tab_1).toUpperCase(l);
			case 1:
				return getString(R.string.title_tab_2).toUpperCase(l);
			case 2:
				return getString(R.string.title_tab_3).toUpperCase(l);
			case 3:
				return getString(R.string.title_tab_4).toUpperCase(l);
			}
			return null;
		}
	}




	/**
	 * impedir salir de ventas con el boton de retroceso.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Toast.makeText(getBaseContext(), "Debe ir a menu y elegir una opcion.", Toast.LENGTH_LONG).show();
			return false;
		default:
			break;
		}
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}




	/**
	 * metodo que cancela la venta actual, no se modifica la base de datos real.
	 */
	private void cancelarVenta() {
		//borrar la tabla detalle para cancelar el pedido actual.
		DataBaseBO.eliminarDetalleVirtual(getBaseContext());
		DataBaseBO.eliminarDetalleCambioVirtual(getBaseContext());
	}


	/**
	 * mostrar una alerta al usuario para que inicie dia y sincronice las fechas.
	 */
	protected void mostrarAlertDialogCancelarPedido() {
		AlertDialog.Builder builder = new AlertDialog.Builder(VentasActivity.this);

		builder.setMessage("No se conserva ningun cambio y el pedido sera eliminado.").
		setTitle("Eliminar Pedido Actual?").
		setCancelable(false).
		setIcon(R.drawable.ic_alerts_and_states_warning_red).
		setPositiveButton("Si", new OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//se cancela la venta y se termina el activity
				cancelarVenta();
				finish();
			}
		}).
		setNegativeButton("No", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//cerrar el dialog 
				dialog.dismiss();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}


	/**
	 * informar al usaurio que es necesario iniciar dia para realizar ventas.
	 */
	protected void mostrarAlertDialogDetalleNoCreado() {
		AlertDialog.Builder builder = new AlertDialog.Builder(VentasActivity.this);

		builder.setMessage("No se logro crear la base de datos local. Es necesario iniciar dia.").
		setTitle("Iniciar Dia?").
		setCancelable(false).
		setIcon(R.drawable.ic_alerts_and_states_warning_red).
		setPositiveButton("Si", new OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//cargar activity de iniciar dia.
				Intent i = new Intent(getBaseContext(), IniciarDiaActivity.class);
				startActivity(i);
			}
		}).
		setNegativeButton("No", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//cerrar el dialog 
				dialog.dismiss();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}




	/**
	 * metodo para confirmar una venta satsfactoria.
	 * Se trasladan los datos de la tabla detalleVirtual y se insertan en el detalle
	 * definitivo, ademas se crea el encabezado y la novedad.
	 */
	private void terminarVenta() {
		/*cargar hora en que se termina el pedido y fecha de entrega*/
		Calendar calendar = new GregorianCalendar();
		Date date = calendar.getTime();

		//se suman un dias, como maximo para generar fecha de entrega
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, 1);	// el 1 es el numero de dias a sumar a la fecha actual	

		// si es un domingo sumar un dia mas.
		if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		Date dateEntrega = calendar.getTime();		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		
		//fecha final y fecha entrega.
		String fechaFinalPedido = sdf.format(date);
		String fechaEntrega = sdf.format(dateEntrega);

		//cargar informacion de cliente.
		SharedPreferences prefCliente =  getSharedPreferences(Constantes.CLIENTE, Context.MODE_PRIVATE);
		String codigo = prefCliente.getString("CODIGO", "000");
		String usuario = prefCliente.getString("VENDEDOR", "000");
		String telefono = usuario; //asi esta definido en la tabla encabezado. puede cambiar en cualquier momento (04/septiembre/2014)

		//cargar informacion del numeroDoc
		SharedPreferences prefNumDoc =  getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
		String numeroDoc = prefNumDoc.getString("numeroDoc", "000");
		String bodega = prefNumDoc.getString("bodega", "000");
		String version = prefNumDoc.getString("version", "000");
		consecutivo = prefNumDoc.getInt("consecutivo", Integer.MIN_VALUE);
		String fechaInicio = prefNumDoc.getString("fechaInicioPedido", "000");

		//cargar infromacion de los montos del pedido.
		InfoVenta venta = infoCliente.obtenerMontoVenta();
		String montoFact = venta.total;
		String montoIva = venta.iva;
		String litros = venta.litros;

		//Cargar montoFact2
		InfoVenta venta2 = infoCliente.obtenerMonto2Venta();


		//generar objeto encabezado
		Encabezado encabezado = new Encabezado();
		encabezado.codigo = codigo;
		encabezado.numeroDoc = numeroDoc;
		encabezado.fechaTrans = fechaFinalPedido;
		encabezado.montoFact = montoFact;
		encabezado.iva = montoIva;
		encabezado.usuario = usuario;
		encabezado.telefono = telefono;
		encabezado.horaFinal = fechaFinalPedido;
		encabezado.fechaReal = fechaFinalPedido;
		encabezado.litros = (litros == null)? "0" : litros;
		encabezado.montoFact2 = venta2.total;
		encabezado.iva2 = venta2.iva;
		encabezado.listros2B = venta2.litros;
		encabezado.inicioPedido = fechaInicio;
		encabezado.finalPedido = fechaFinalPedido;
		encabezado.bodega = bodega;
		encabezado.fechaEntrega = fechaEntrega;
		encabezado.version = version;

		//insertar en bd temp y database.
		boolean finalizado = finalizarPedido(encabezado, consecutivo);	
		if(finalizado){
			mensaje = "Pedido Finalizado Correctamente.";
			puente.sendEmptyMessage(0);
			generarEspera();
			progress.dismiss();
			Looper.myLooper().quit();
			finish();
		}
		else {
			mensaje = "Falló finalizar pedido. Intente de nuevo.";
			puente.sendEmptyMessage(0);
			generarEspera();
			progress.dismiss();
			Looper.myLooper().quit();
		}
	}

	/**
	 * mostrar dialog con las opciones de no compra.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	private void mostrarDialogNoCompra() {
		DataBaseBO.cargarMotivosNoCompra(listaMotivosNoCompra, getBaseContext());
		//remover la opcion de pedido.
		listaMotivosNoCompra.remove(0);
		ArrayList<String> listaMostrar = new ArrayList<String>();
		for (MotivosNoCompra motivo : listaMotivosNoCompra) {
			listaMostrar.add(motivo.motivo);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, listaMostrar);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Motivo No Compra");
		builder.setAdapter(adapter, new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//se incrementa dos, debido a que se elimina la primera opcion que es la de pedido.
				MotivosNoCompra motivo = listaMotivosNoCompra.get(which);
				guardarNoCompra(motivo);				
			}
		});		
		AlertDialog alert = builder.create();
		alert.show();
	}





	/**
	 * guardar novedad de no compra.
	 * @param motivo
	 */
	protected void guardarNoCompra(MotivosNoCompra motivo) {
		/*cargar hora en que se termina el pedido y fecha de entrega*/
		Calendar calendar = new GregorianCalendar();
		Date date = calendar.getTime();

		//se suman un dias, como maximo para generar fecha de entrega
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, 1);	// el 1 es el numero de dias a sumar a la fecha actual		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		//fecha final y fecha entrega.
		String fechaFinalPedido = sdf.format(date);

		//cargar informacion de cliente.
		SharedPreferences prefCliente =  getSharedPreferences(Constantes.CLIENTE, Context.MODE_PRIVATE);
		String codigo = prefCliente.getString("CODIGO", "000");
		String usuario = prefCliente.getString("VENDEDOR", "000");
		String telefono = usuario; //asi esta definido en la tabla encabezado. puede cambiar en cualquier momento (04/septiembre/2014)

		//cargar informacion del numeroDoc
		SharedPreferences prefNumDoc =  getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
		String numeroDoc = prefNumDoc.getString("numeroDoc", "000");
		String bodega = prefNumDoc.getString("bodega", "000");
		String version = prefNumDoc.getString("version", "000");
		consecutivo = prefNumDoc.getInt("consecutivo", Integer.MIN_VALUE);
		String fechaInicio = prefNumDoc.getString("fechaInicioPedido", "000");	


		//generar objeto encabezado
		Encabezado encabezado = new Encabezado();
		encabezado.codigo = codigo;
		encabezado.numeroDoc = numeroDoc;
		encabezado.fechaTrans = fechaFinalPedido;
		encabezado.montoFact = "0";
		encabezado.iva = "0";
		encabezado.usuario = usuario;
		encabezado.telefono = telefono;
		encabezado.horaFinal = fechaFinalPedido;
		encabezado.fechaReal = fechaFinalPedido;
		encabezado.litros = "0";
		encabezado.montoFact2 = "0";
		encabezado.iva2 = "0";
		encabezado.listros2B = "0";
		encabezado.inicioPedido = fechaInicio;
		encabezado.finalPedido = fechaFinalPedido;
		encabezado.bodega = bodega;
		encabezado.fechaEntrega = fechaFinalPedido;
		encabezado.version = version;
		//solo inserta una novedad de no compra.
		boolean insertNovedadDataBase = DataBaseBO.insertarNovedadDataBase(motivo, encabezado, getBaseContext());
		boolean insertNovedadTemp = DataBaseBO.insertarNovedadTemp(motivo, encabezado, getBaseContext());
		if(insertNovedadDataBase && insertNovedadTemp) {
			//actualizar consecutivo
			DataBaseBO.actualizarConsecutivo(consecutivo, getBaseContext());
			//borrar detallevirtual.
			DataBaseBO.eliminarDetalleVirtual(getBaseContext());
			DataBaseBO.eliminarDetalleCambioVirtual(getBaseContext());
			Toast.makeText(getApplication(), "No compra guardada con exito", Toast.LENGTH_LONG).show();
			finish();
		}
		else {
			Toast.makeText(getApplication(), "No se guardo novedad.\nIntente de nuevo.", Toast.LENGTH_LONG).show();
		}		
	}

	/**
	 * metodo que finaliza un pedido, insertando detalle y encabezado.
	 * @param encabezado
	 * @param consecutivo 
	 * @return
	 */
	private boolean finalizarPedido(Encabezado encabezado, int consecutivo) {
		//borrar posibles detalles que esten registrados.
		DataBaseBO.borrarPedidoSinTerminarDataBase(encabezado.numeroDoc, getBaseContext());
		DataBaseBO.borrarPedidoSinTerminarTemp(encabezado.numeroDoc, getBaseContext());
		mensaje = "Limpiando...";
		puente.sendEmptyMessage(0);
		generarEspera();
		// insertar detalle en las bases de datos temp y database		
		boolean detalleDBInsertado = DataBaseBO.insertarDetalleDataBase(encabezado.numeroDoc, getBaseContext());
		mensaje = "Guardando Detalle en data base..";
		puente.sendEmptyMessage(0);
		generarEspera();
		boolean detalleTempInsertado = DataBaseBO.insertarDetalleTemp(encabezado.numeroDoc, getBaseContext());
		mensaje = "Guardando Detalle en temp..";
		puente.sendEmptyMessage(0);
		generarEspera();

		//confirmar para inserar detalle en ambas bases de datos.
		if(detalleDBInsertado && detalleTempInsertado){

			boolean insertEncabezadoDataBase = DataBaseBO.insertarEncabezadoDataBase(encabezado, getBaseContext());
			boolean insertEncabezadoTemp = DataBaseBO.insertarEncabezadoTemp(encabezado, getBaseContext());
			mensaje = "Guardando Encabezado...";
			puente.sendEmptyMessage(0);
			generarEspera();
			//confirmar para insertar novedad
			if(insertEncabezadoDataBase && insertEncabezadoTemp){

				MotivosNoCompra motivo = infoCliente.getMotivoNoCompra();
				boolean insertNovedadDataBase = DataBaseBO.insertarNovedadDataBase(motivo, encabezado, getBaseContext());
				boolean insertNovedadTemp = DataBaseBO.insertarNovedadTemp(motivo, encabezado, getBaseContext());
				mensaje = "Guardando Novedad...";
				puente.sendEmptyMessage(0);
				generarEspera();
				//confirmar insertado definitivo en todoas las tablas.
				if(insertNovedadTemp && insertNovedadDataBase){

					//verificar que todas las tablas se han insertado con el numeroDoc actual.
					boolean pedidoTerminadoDataBase =  DataBaseBO.confirmarPedidoTerminadoDataBase(encabezado.numeroDoc, getBaseContext());
					boolean pedidoTerminadoTemp =  DataBaseBO.confirmarPedidoTerminadoTemp(encabezado.numeroDoc, getBaseContext());
					mensaje = "Validando...";
					puente.sendEmptyMessage(0);
					generarEspera();
					//confirmar para actualizar el consecutivo y borrar la tabla detalleVirtual para un nuevo pedido.
					if(pedidoTerminadoDataBase && pedidoTerminadoTemp){

						//actualizar consecutivo
						DataBaseBO.actualizarConsecutivo(consecutivo, getBaseContext());
						//borrar detallevirtual.
						DataBaseBO.eliminarDetalleVirtual(getBaseContext());
						DataBaseBO.eliminarDetalleCambioVirtual(getBaseContext());
						return true;
					}
				}
			}
		}		
		return false;
	}



	/**
	 * Mostrar progressDialog de insercion en bd.
	 * @param view
	 */
	public void progressDialogGuardandoPedido() {

		//cargar informacion del numeroDoc
		SharedPreferences prefNumDoc =  getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
		final String numeroDoc = prefNumDoc.getString("numeroDoc", "000");

		final Thread t = new Thread(){
			@Override
			public void run(){
				Looper.prepare();
				mensaje = "Por Favor Espere...";
				puente.sendEmptyMessage(0);
				generarEspera();
				//verificar si hay detalles para insertar
				boolean hayDetalle = DataBaseBO.verificarSiHayDetalle(numeroDoc, getBaseContext());
				if(hayDetalle){
					//terminar el pedido actual de forma correcta
					terminarVenta();	
				}
				else {
					mensaje = "No hay Pedidos Para Registrar.";
					puente.sendEmptyMessage(0);
					generarEspera();
					progress.dismiss();
					Looper.myLooper().quit();					
				}
			}
		};
		t.start();
	}

	/**
	 * mostrar espera.
	 */
	private void generarEspera(){
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * handler para mostrar al usuario el proceso de guardado como va ejecuntando.
	 */
	@SuppressLint("HandlerLeak") private Handler puente = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mostrarMensaje();
		}
	};

	/**
	 * metodo para mostrar el progres dialog en sus diferentes tiempos de ejecucion de la impresion.
	 */
	public void mostrarMensaje() {

		if (progress == null) {
			progress = new ProgressDialog(this);
			progress.setTitle("Guardando Pedido");
			progress.setCancelable(false);
		}
		progress.setMessage(mensaje);

		if (!progress.isShowing())
			progress.show();
	}


	@Override
	protected void onDestroy() {
		//eliminar base de datos virtual si la aplicacion se detiene, se debera volver a iniciar el pedido.
		DataBaseBO.eliminarDetalleVirtual(getBaseContext());
		DataBaseBO.eliminarDetalleCambioVirtual(getBaseContext());
		super.onDestroy();
	}
	
	
	/**
	 * alert para informar al usuario que ya termino labores
	 * @param terminoLabores
	 */
	private void mostrarAlertInformaTerminoLabores(String terminoLabores) {

		String mensaje = "" +
				"No puede realizar mas pedidos!\nYa termino dia en la fecha:\n" + terminoLabores.substring(0,19) +"\nPara realizar " +
				"un nuevo pedido se debe comunicar con el administrador para habilitar de nuevo sus labores."; 
		
		AlertDialog.Builder builder = new AlertDialog.Builder(VentasActivity.this);
		builder.setTitle("Usuario Actual Ya Termino Labores!");
		builder.setMessage(mensaje);
		builder.setCancelable(false);
		builder.setPositiveButton("Aceptar", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();				
			}
		});	
		AlertDialog alert = builder.create();
		alert.show();
	}



	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {		
		private static final String ARG_SECTION_NUMBER = "section_number";
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}
		public PlaceholderFragment() {
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_ventas, container, false);
			return rootView;
		}
	}

}//final de la clase.
