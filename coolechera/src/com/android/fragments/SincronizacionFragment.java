package com.android.fragments;

import java.util.ArrayList;

import com.android.business.object.DataBaseBO;
import com.android.conexion.Sincronizador;
import com.android.conexion.Sync;
import com.android.conexion.constantes.Const;
import com.android.coolechera.EnviarPedidoActivity;
import com.android.coolechera.IniciarDiaActivity;
import com.android.coolechera.PrincialActivity;
import com.android.coolechera.R;
import com.android.data.ItemDrawerLayout;
import com.android.util.Constantes;
import com.android.util.MenusAdapter;
import com.android.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


/**
 * A placeholder fragment containing a simple view.
 */
public class SincronizacionFragment extends Fragment implements Sincronizador {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * adapter personalizado, para mostrar un menu de opciones en tipo listView
	 */
	private MenusAdapter adapter;

	/**
	 * iconos
	 */
	private TypedArray icons;

	/**
	 * titulos de cada opcion del listview
	 */
	private String[] titulos;

	/**
	 * subtitulos de cada opcion del listview
	 */
	private String[] subTitulos;

	/**
	 * lista que contiene las opciones
	 */
	private ArrayList<ItemDrawerLayout> items;

	private ListView listaOpcionesSincronizacion;

	static int position;
	
	
	/**
	 * progressdialog para terminar dia.
	 */
	private ProgressDialog progress;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static SincronizacionFragment newInstance(int sectionNumber) {
		SincronizacionFragment fragment = new SincronizacionFragment();
		Bundle args = new Bundle();
		position = sectionNumber;
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public SincronizacionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_sincronizacion, container, false);
		listaOpcionesSincronizacion =  (ListView) rootView.findViewById(R.id.listaOpcionesSincronizacion);

		//cargar listView para mostrar el menu de opciones disponibles en este fragment.
		//crear el adapter personalizado para insertarlo en el listView
		icons = getResources().obtainTypedArray(R.array.sincronizacion_icons);
		titulos = getResources().getStringArray(R.array.sincronizacion_options);
		subTitulos = getResources().getStringArray(R.array.sincronizacion_subOptions);
		items = new ArrayList<ItemDrawerLayout>();

		//cargar items.
		for (int i = 0; i < titulos.length; i++) {
			ItemDrawerLayout item = new ItemDrawerLayout();
			item.titulo = titulos[i];
			item.subTitulo = subTitulos[i];
			item.icono = this.icons.getResourceId(i, -1);
			this.items.add(item);
		}			
		adapter = new MenusAdapter(this.getActivity(), items);
		listaOpcionesSincronizacion.setAdapter(adapter);
		listaOpcionesSincronizacion.setOnItemClickListener(clickedHandlerListaOpciones);
		return rootView;
	}


	// Handler para capturar el item seleccionado por el usuario.
	private OnItemClickListener clickedHandlerListaOpciones = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			switch (position) {
			case 0: // enviar pedidos
				enviarInformacion();

				break;				

			case 1: // iniciar dia
				iniciarDia(1);
				break;

			case 2: // terminar dia
				mostrarDialogTerminarLabores();
				break;

			default:
				break;
			}			
		}
	};


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((PrincialActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));			
	}




	/**
	 * mostrar Dialog para confirmacion de terminar dia.
	 */
	protected void mostrarDialogTerminarLabores() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("¿Desea Terminar Dia de Trabajo?");
		builder.setMessage("No se podran realizar mas pedidos el dia de hoy.\nLa aplicacion se cerrará.");
		builder.setPositiveButton("Aceptar", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				terminarDia();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("Cancelar", new Dialog.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		} );
		AlertDialog alert = builder.create();
		alert.show();
	}


	/**
	 * metodo que envia al sincronizador la solicitud para terminar labores.
	 */
	protected void terminarDia() {		
		progress = new ProgressDialog(getActivity());
		progress.setTitle("Terminando dia");
		progress.setMessage("Finalizando ...");
		progress.show();
		String imei = Util.getIMEI(getActivity().getBaseContext());
		Sync sync = new Sync(this, Const.TERMINAR_LABORES);
		sync.imei = imei;
		sync.start();
	}

	
	
	//esperar respuesta del sincronizador.
	@Override
	public void RespSync(final boolean ok, final String respuestaServer, String msg, final int codeRequest) {

		//ejecutar la siguiente accion en el hilo principal, debido a cambio de View es necesario hacerlo.
		getActivity().runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				//cerrar el progress dialog y mostrar respuesta servidor.
				progress.dismiss();
				//verificar que el codigo de respuesta coincida con el enviado, (descargar base de datos)
				if(codeRequest == Const.TERMINAR_LABORES){
					//verificar que la base de datos ha sido descargada correctamente
					if(ok){
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("Terminó labores correctamente.");
						builder.setPositiveButton("Salir", new Dialog.OnClickListener() {					
							@Override
							public void onClick(DialogInterface dialog, int which) {
								icons.recycle();
								//update fechatermino en database novedades para confirmar que termino labores.
								DataBaseBO.confirmarFechaTerminoLabores(getActivity().getBaseContext());
								getActivity().finish();
							}
						});
						AlertDialog alert = builder.create();
						alert.show();
					}
					else {
						//mostrar pantalla de error
						Toast.makeText(getActivity(), "Falló terminar dia.\n" + respuestaServer, Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	/**
	 * metodo para lanzar activity de enviar pedidos o enviar informacion
	 */
	protected void enviarInformacion() {		
		Intent i = new Intent(getActivity().getBaseContext(), EnviarPedidoActivity.class);
		boolean confirmation = false;
		i.putExtra(Constantes.CONFIRMATION, confirmation);
		startActivity(i);		
	}

	/**
	 * metodo para descargar las bases de datos 
	 * @param position 
	 */
	protected void iniciarDia(int position) {
		Intent i = new Intent(getActivity().getBaseContext(), IniciarDiaActivity.class);
		startActivity(i);			
	}


}//final clase.