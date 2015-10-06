package com.android.fragments;




import java.util.ArrayList;

import com.android.business.object.DataBaseBO;
import com.android.coolechera.R;
import com.android.data.ItemDrawerLayout;
import com.android.data.PedidoSincronizado;
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
public  class PedidosSincronizadosFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/*Views que seran usados para mostrar la informacion*/
	private  TextView viewVisitasSincronizado = null;
	private  TextView viewPedidosSincronizado = null;
	private  TextView viewValorSincronizado = null;
	private  ListView listViewPedidosSinc = null;


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
	ArrayList<PedidoSincronizado> listaPedidosSinc;

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
	public static PedidosSincronizadosFragment newInstance(int sectionNumber) {
		PedidosSincronizadosFragment fragment = new PedidosSincronizadosFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public PedidosSincronizadosFragment() {
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_pedido_sincronizado,container, false);	

		//definicion del icono
		icon = R.drawable.ic_social_send_now_red;

		//cargar instancias de las vistas
		viewVisitasSincronizado = (TextView) rootView.findViewById(R.id.textViewVisitasSincronizado);
		viewPedidosSincronizado = (TextView) rootView.findViewById(R.id.textViewPedidosSincronizado);
		viewValorSincronizado = (TextView) rootView.findViewById(R.id.textViewValorSincronizado);
		listViewPedidosSinc = (ListView) rootView.findViewById(R.id.listaPedidosSincronizados);

		return rootView;
	}	




	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

		//resumen de las estadisticas actuales.
		ResumenEstadisticas resumen = DataBaseBO.obtenerResumenEstadisticas(getActivity().getBaseContext());

		//setiar informacion del vendedor
		viewVisitasSincronizado.setText("" + resumen.visitas);
		viewPedidosSincronizado.setText("" + resumen.pedidosSincronizados);
		viewValorSincronizado.setText("$" + resumen.valorSincronizado);	

		//cargar la lista de pedidos no sincronizados
		cargarListaPedidosSincronizados();
		super.onViewCreated(view, savedInstanceState);
	}



	/**
	 * cargar la lista de pedidos no sincronizados.
	 */
	private void cargarListaPedidosSincronizados() {

		//lista de productos (referencias) cargados.
		listaPedidosSinc = DataBaseBO.PedidosSincronizados(this.getActivity().getBaseContext());
		items = new ArrayList<ItemDrawerLayout>();

		for (PedidoSincronizado pedidoSincronizado : listaPedidosSinc) {
			ItemDrawerLayout item = new ItemDrawerLayout();
			item.icono = icon;
			item.titulo = pedidoSincronizado.numeroDoc;
			item.subTitulo = pedidoSincronizado.cliente + "\nValor: $" + pedidoSincronizado.monto + " - Hora Pedido: " + pedidoSincronizado.hora + 
							"\nMotivo: " + pedidoSincronizado.motivo;
			this.items.add(item);
		}
		//cargar el adapter en el listView
		adapter = new ProductosAdapter(getActivity(), items);
		listViewPedidosSinc.setAdapter(adapter);	
		//		listViewPedidosNoSinc.setOnItemClickListener(listenerListView);	
	}

}//final de la clase.