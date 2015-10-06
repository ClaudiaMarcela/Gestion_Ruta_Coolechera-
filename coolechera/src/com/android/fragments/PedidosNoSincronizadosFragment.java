package com.android.fragments;




import java.util.ArrayList;

import com.android.business.object.DataBaseBO;
import com.android.coolechera.R;
import com.android.data.ItemDrawerLayout;
import com.android.data.PedidoNoSincronizado;
import com.android.data.ResumenEstadisticas;
import com.android.util.ProductosAdapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public  class PedidosNoSincronizadosFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/*Views que seran usados para mostrar la informacion*/
	private  TextView viewVisitasNoSincronizado = null;
	private  TextView viewPedidosNoSincronizado = null;
	private  TextView viewValorNoSincronizado = null;
	private  ListView listViewPedidosNoSinc = null;


	/**
	 * adapter personalizado, para mostrar un menu de opciones en tipo listView
	 */
	private ProductosAdapter adapter;

	/**
	 * lista que contiene las opciones
	 */
	private ArrayList<ItemDrawerLayout> items;

	/**
	 * lista de pedidos no sincronizados encontrados
	 */
	ArrayList<PedidoNoSincronizado> listaPedidosNoSinc;

	/**
	 * icono para mostrar en la lista
	 */
	private int icon;;


	/**
	 * contenedor principal
	 */
	View rootView;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static PedidosNoSincronizadosFragment newInstance(int sectionNumber) {
		PedidosNoSincronizadosFragment fragment = new PedidosNoSincronizadosFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public PedidosNoSincronizadosFragment() {
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_pedido_no_sincronizado,container, false);	

		//definicion del icono
		icon = R.drawable.ic_social_send_now_red;

		//cargar instancias de las vistas
		viewVisitasNoSincronizado = (TextView) rootView.findViewById(R.id.textViewVisitasNoSincronizado);
		viewPedidosNoSincronizado = (TextView) rootView.findViewById(R.id.textViewPedidosNoSincronizado);
		viewValorNoSincronizado = (TextView) rootView.findViewById(R.id.textViewValorNoSincronizado);
		listViewPedidosNoSinc = (ListView) rootView.findViewById(R.id.listaPedidosNoSincronizados);

		return rootView;
	}	




	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

		//resumen de las estadisticas actuales.
		ResumenEstadisticas resumen = DataBaseBO.obtenerResumenEstadisticas(getActivity().getBaseContext());

		//setiar informacion del vendedor
		viewVisitasNoSincronizado.setText("" + resumen.visitas);
		viewPedidosNoSincronizado.setText("" + resumen.pedidosNoSincronizados);
		viewValorNoSincronizado.setText("$" + resumen.valorNoSincronizado);	

		//cargar la lista de pedidos no sincronizados
		cargarListaEPedidosNoSincronizados();
		super.onViewCreated(view, savedInstanceState);
	}



	/**
	 * cargar la lista de pedidos no sincronizados.
	 */
	private void cargarListaEPedidosNoSincronizados() {

		//lista de productos (referencias) cargados.
		listaPedidosNoSinc = DataBaseBO.PedidosNoSincronizados(this.getActivity().getBaseContext());
		items = new ArrayList<ItemDrawerLayout>();

		for (PedidoNoSincronizado pedidoNoSincronizado : listaPedidosNoSinc) {
			ItemDrawerLayout item = new ItemDrawerLayout();
			item.icono = icon;
			item.titulo = pedidoNoSincronizado.numeroDoc;
			item.subTitulo = pedidoNoSincronizado.cliente + "\nValor: $" + pedidoNoSincronizado.monto + " - Hora Pedido: " + 
							pedidoNoSincronizado.hora + "\nMotivo: " + pedidoNoSincronizado.motivo;
			this.items.add(item);
		}
		//cargar el adapter en el listView
		adapter = new ProductosAdapter(getActivity(), items);
		listViewPedidosNoSinc.setAdapter(adapter);	
		//		listViewPedidosNoSinc.setOnItemClickListener(listenerListView);	
	}

}//final de la clase.