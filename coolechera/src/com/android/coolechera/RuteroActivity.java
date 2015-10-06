package com.android.coolechera;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.android.business.object.DataBaseBO;
import com.android.data.Cliente;
import com.android.data.ItemDrawerLayout;
import com.android.util.ClientesAdapter;
import com.android.util.Constantes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;




/**
 * Esta activity no se guarda en la pila de activities del ciclo de vida, esto con el fin de optimizar; 
 * debido a que esta actividad no es necesario conservar su informacion.
 */
public class RuteroActivity extends Activity{


	//constante para formato de fechas, solo usado por esta clase.
	private static final String FORMATO_FECHA = "dd/MM/yyyy";


	/**
	 * adapter personalizado, para mostrar un menu de opciones en tipo listView
	 */
	private ClientesAdapter adapter;

	/**
	 * iconos
	 */
	private int icono;

	/**
	 * lista que contiene las opciones
	 */
	private ArrayList<ItemDrawerLayout> items;

	/**
	 * lista de clientes encontrados
	 */
	ArrayList<Cliente> listaClientes;


	/**
	 * layout que muestra los clientes encontrados.
	 */
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rutero_activity);

		//icono para mostrar en la lista de clientes encontrados.
		icono = R.drawable.ic_social_person_red;

		//referencia al listView que mostrara los resultados encontrados.
		listView = (ListView) findViewById(R.id.listaClientesRutero);
		listView.setOnItemClickListener(listViewHandler);
		//instancia de la lista de clientes, vacia.
		listaClientes = new ArrayList<Cliente>();
		
		//listar los clientes disponibles en rutero
		listarClientesRutero();
	}






	/**
	 * metodo que permite una conexion a la base de datos para buscar clientes. y guardarlos en la lista.
	 * @param text
	 */
	protected void listarClientesRutero() {

		//lista de clientes cargados.
		listaClientes = DataBaseBO.listarClientesRutero(this.getBaseContext());

		items = new ArrayList<ItemDrawerLayout>();
		for (Cliente cliente : listaClientes) {
			ItemDrawerLayout item = new ItemDrawerLayout();
			//se define siempre el mismo icono
			item.icono = icono;
			item.titulo = cliente.razonSocial;
			item.subTitulo = "\n- " + cliente.representante + "\n" + cliente.direccion + "\nB/: " + cliente.barrio + "\nTel: " + cliente.telefono;
			this.items.add(item);
		}
		//cargar el adapter en el listView
		adapter = new ClientesAdapter(this, items);
		listView.setAdapter(adapter);		
	}


	/**
	 * handler para manejar el evento de seleccion de un cliente por parte del usuario.
	 */
	private OnItemClickListener listViewHandler = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//cargar el codigo de cliente seleccionado.
			String codigoCliente = listaClientes.get(position).codigo;
			//validar fechaLabores con fecha actual.para permitir toma de pedidos.
			boolean fechaLaboresValida = verificarFechaLabores();

			//iniciar la actividad si la fecha de labores es valida.
			if(fechaLaboresValida){
				Intent i = new Intent(getBaseContext(), VentasActivity.class);
				i.putExtra(Constantes.COD_CLIENTE, codigoCliente);
				startActivity(i);
			}
			else {
				// mostrar un alert indicando que no se puede realizar pedidos, debe iniciar dia primero.
				mostrarAlertDialogIniciarDia();
			}
		}
	};


	/**
	 * metodo para validar que la fecha de labores coincida con la fecha actual del telefono, si estan las fechas sincronizadas
	 * se permite la toma de pedidos, en caso contrario se debe solicitar iniciar dia.
	 * @return
	 */
	protected boolean verificarFechaLabores() {

		//fechaLabores del vendedor, cargada desde la base de datos.
		String fechaLabores = DataBaseBO.obtenerFechaLaboresVendedor(getBaseContext());

		//cargar fecha actual del movil.
		Calendar calendar = new GregorianCalendar();
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
		String fechaActual = sdf.format(date);

		//comparar fechas, true si son iguales, false en caso contrario.
		return compararFechas(fechaLabores, fechaActual);
	}




	/**
	 * metodo para determinar si dos fechas son iguales.
	 * @param fechaLabores
	 * @param fechaActual
	 * @return true si son iguales, false en caso contrario.
	 */
	private boolean compararFechas(String fechaLabores, String fechaActual) {
		//se intenta hacer la conversion de fechas para hacer las comparaciones.
		try {
			SimpleDateFormat formateador = new SimpleDateFormat(FORMATO_FECHA, Locale.getDefault());
			Date dFechaActual = formateador.parse(fechaActual);
			Date dFechaLabores = formateador.parse(fechaLabores);
			int compare = dFechaLabores.compareTo(dFechaActual);
			return (compare == 0);			
		} catch (ParseException e) {
			Log.e("Comparando fechas", "Error: " + e.getMessage());
		}		
		return false;
	}	


	/**
	 * mostrar una alerta al usuario para que inicie dia y sincronice las fechas.
	 */
	protected void mostrarAlertDialogIniciarDia() {
		AlertDialog.Builder builder = new AlertDialog.Builder(RuteroActivity.this);

		builder.setMessage("Deben coincidir las fechas de Base de datos y telefono movil").
		setTitle("Desea Iniciar Dia?").
		setCancelable(false).
		setIcon(android.R.drawable.ic_dialog_alert).
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
}//final de la clase
