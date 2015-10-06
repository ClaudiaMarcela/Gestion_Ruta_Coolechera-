package com.android.fragments;

import java.util.ArrayList;

import com.android.coolechera.BuscarClienteActivity;
import com.android.coolechera.EstadisticasActivity;
import com.android.coolechera.PrincialActivity;
import com.android.coolechera.R;
import com.android.coolechera.RuteroActivity;
import com.android.data.ItemDrawerLayout;
import com.android.util.MenusAdapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


/**
 * A placeholder fragment containing a simple view.
 */
public class DiaTrabajoFragment extends Fragment {
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
	 * titulos de cada opcion del listview
	 */
	private String[] subTitulos;

	/**
	 * lista que contiene las opciones
	 */
	private ArrayList<ItemDrawerLayout> items;

	private ListView listaOpcionesDiaTrabajo;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static DiaTrabajoFragment newInstance(int sectionNumber) {
		DiaTrabajoFragment fragment = new DiaTrabajoFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public DiaTrabajoFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_dia_trabajo,container, false);
		listaOpcionesDiaTrabajo = (ListView) rootView.findViewById(R.id.listaOpcionesDiaTrabajo);

		//cargar listView para mostrar el menu de opciones disponibles en este fragment.
		//crear el adapter personalizado para insertarlo en el listView
		icons = getResources().obtainTypedArray(R.array.trabajo_icons);
		titulos = getResources().getStringArray(R.array.trabajo_options);
		subTitulos = getResources().getStringArray(R.array.trabajo_subOptions);
		items = new ArrayList<ItemDrawerLayout>();

		//cargar items.
		for (int i = 0; i < titulos.length; i++) {
			ItemDrawerLayout item = new ItemDrawerLayout();
			item.titulo = titulos[i];
			item.subTitulo = subTitulos[i];
			item.icono = this.icons.getResourceId(i, -1);
			this.items.add(item);
		}
		//cargar opciones
		adapter = new MenusAdapter(this.getActivity(), items);
		listaOpcionesDiaTrabajo.setAdapter(adapter);
		listaOpcionesDiaTrabajo.setOnItemClickListener(clickedHandlerListaOpciones);			
		return rootView;
	}


	// Handler para capturar el item seleccionado por el usuario.
	private OnItemClickListener clickedHandlerListaOpciones = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			switch (position) {
			case 0: //buscar clientes

				buscarClientes();
				break;

			case 1:
				iniciarRutero();
				break;

			case 2:
				estadisticas();
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
	 * iniciar activity de estadisticas de comportamiento de ventas.
	 */
	protected void estadisticas() {
		Intent i = new Intent(getActivity().getBaseContext(), EstadisticasActivity.class);
		startActivity(i);			
	}

	/**
	 * inicia el activity de rutero del dia
	 */
	protected void iniciarRutero() {
		Intent i = new Intent(getActivity().getBaseContext(), RuteroActivity.class);
		startActivity(i);
	}

	/**
	 * inicia el activity de buscar clientes.
	 */
	protected void buscarClientes() {
		Intent i = new Intent(getActivity().getBaseContext(), BuscarClienteActivity.class);
		startActivity(i);			
	}
}