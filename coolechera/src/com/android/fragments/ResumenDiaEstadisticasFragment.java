package com.android.fragments;




import com.android.business.object.DataBaseBO;
import com.android.coolechera.R;
import com.android.data.ResumenEstadisticas;
import com.android.data.Vendedor;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public  class ResumenDiaEstadisticasFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/*Views que seran usados para mostrar la informacion*/
	private  TextView viewInfoVendedor = null;
	private  TextView viewCedulaVendedor = null;
	private  TextView viewFechaLabores = null;
	private  TextView viewZona = null;
	private  TextView viewCodigo = null;
	private  TextView viewVisitas = null;
	private  TextView viewTotalPedidos= null;
	private  TextView viewPedidosSincronizados= null;
	private  TextView viewPedidosNoSincronizados= null;
	private  TextView viewEfectividad= null;
	private  TextView viewTotalVentas= null;
	private  TextView viewNoCompras= null;
	private  TextView viewNoComprasSincronizados= null;
	private  TextView viewNoComprasNoSincronizados= null;



	/**
	 * contenedor principal
	 */
	View rootView;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static ResumenDiaEstadisticasFragment newInstance(int sectionNumber) {
		ResumenDiaEstadisticasFragment fragment = new ResumenDiaEstadisticasFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public ResumenDiaEstadisticasFragment() {
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_resumen_recorrido,container, false);			

		//cargar instancias de las vistas
		viewInfoVendedor = (TextView) rootView.findViewById(R.id.textViewInfoVendedor);
		viewCedulaVendedor = (TextView) rootView.findViewById(R.id.textViewCedulaVendedor);
		viewCodigo = (TextView) rootView.findViewById(R.id.textViewCodigoVendedor);
		viewFechaLabores = (TextView) rootView.findViewById(R.id.textViewFechaLabores);
		viewZona = (TextView) rootView.findViewById(R.id.textViewZona);

		//instancias de views donde se muestra el resumen
		viewVisitas = (TextView) rootView.findViewById(R.id.textViewVisitas);
		viewTotalPedidos = (TextView) rootView.findViewById(R.id.textViewTotalPedidos);
		viewPedidosSincronizados = (TextView) rootView.findViewById(R.id.textViewPedidosSincronizados);
		viewPedidosNoSincronizados = (TextView) rootView.findViewById(R.id.textViewPedidosNoSincronizados);
		viewEfectividad = (TextView) rootView.findViewById(R.id.textViewEfectividad);
		viewTotalVentas = (TextView) rootView.findViewById(R.id.textViewTotalVentas);
		viewNoCompras = (TextView) rootView.findViewById(R.id.textViewNoCompras);
		viewNoComprasSincronizados = (TextView) rootView.findViewById(R.id.textViewNoComprasSincronizados);
		viewNoComprasNoSincronizados = (TextView) rootView.findViewById(R.id.textViewNoComprasNoSincronizados);

		return rootView;
	}	




	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		Vendedor vendedor = DataBaseBO.obtenerVendedor();

		//calcula el resumen de estadisticas.
		ResumenEstadisticas resumen = DataBaseBO.obtenerResumenEstadisticas(getActivity().getBaseContext());

		//setiar informacion del vendedor
		viewInfoVendedor.setText(vendedor.nombre.trim());
		viewCedulaVendedor.setText(vendedor.cedula.trim());
		viewCodigo.setText(vendedor.codigo.trim());
		viewFechaLabores.setText(vendedor.fechaLabores);
		viewZona.setText(vendedor.zona.trim());

		//setiar informacion del resumen
		viewVisitas.setText("" + resumen.visitas);
		viewTotalPedidos.setText("" + (int)resumen.totalPedidos);
		viewPedidosSincronizados.setText("" + resumen.pedidosSincronizados);
		viewPedidosNoSincronizados.setText("" + resumen.pedidosNoSincronizados);
		viewEfectividad.setText(resumen.efectividad + "%");
		viewTotalVentas.setText("$" + resumen.totalVentas);
		viewNoCompras.setText("" + resumen.noCompras);
		viewNoComprasSincronizados.setText("" + resumen.noComprasSinc);
		viewNoComprasNoSincronizados.setText("" + resumen.noComprasNoSinc);

		super.onViewCreated(view, savedInstanceState);
	}





}//final de la clase.