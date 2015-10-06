package com.android.fragments;



import com.android.business.object.DataBaseBO;
import com.android.coolechera.R;
import com.android.data.InfoVenta;
import com.android.data.MotivosNoCompra;
import com.android.util.Constantes;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
	 * A placeholder fragment containing a simple view.
	 */
	public  class InfoClienteFragment extends Fragment {
		
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		
		/*Views que seran usados para mostrar la informacion*/
		private  TextView viewRazonSocial = null;
		private  TextView viewRepresentante = null;
		private  TextView viewNIT = null;
		private  TextView viewDireccion = null;
		private  TextView viewBarrio = null;
		private  TextView viewTelefono = null;
		private  TextView viewCodigo = null;
		private  TextView viewValor = null;
		private  TextView viewValorIva = null;
		private  TextView viewValorTotal = null;

		
		/**
		 * atributo que identifica un motivo de no compra
		 */
		private MotivosNoCompra motivoNoCompra;
		
		/**
		 * contenedor principal
		 */
		View rootView;
		
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static InfoClienteFragment newInstance(int sectionNumber) {
			InfoClienteFragment fragment = new InfoClienteFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public InfoClienteFragment() {
		}

		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			rootView = inflater.inflate(R.layout.fragment_info_cliente,container, false);			
			
			//cargar instancias de las vistas
			viewRazonSocial = (TextView) rootView.findViewById(R.id.textViewRazonSocial);
			viewRepresentante = (TextView) rootView.findViewById(R.id.textViewRepresentante);
			viewNIT = (TextView) rootView.findViewById(R.id.textViewNIT);
			viewDireccion = (TextView) rootView.findViewById(R.id.textViewDireccion);
			viewBarrio = (TextView) rootView.findViewById(R.id.textViewBarrio);
			viewTelefono = (TextView) rootView.findViewById(R.id.textViewTelefono);
			viewCodigo = (TextView) rootView.findViewById(R.id.textViewCodigo);	
			//referencias a vistas de estado de venta.
			viewValor = (TextView) rootView.findViewById(R.id.textViewValor);
			viewValorIva = (TextView) rootView.findViewById(R.id.textViewValorIva);
			viewValorTotal = (TextView) rootView.findViewById(R.id.textViewTotal);			

			return rootView;
		}	
		
		
	

		@Override
		public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
			
			/* cargar la informacion del cliente, desde el sharedPreferences.
			 * se usa esta forma para minimizar el maximo de conexiones a la base de datos, ya que
			 * se puede generar errores de insercion de partes criticas como detalle o encabezado por la concurrencia
			 * a la base de datos para leer informacion.*/

			/*como este fragment es una extension de la activity ventas, es necesario cargar el preference de esta forma*/
			SharedPreferences prefCliente =  this.getActivity().getSharedPreferences(Constantes.CLIENTE, Context.MODE_PRIVATE);
			String codigo = prefCliente.getString("CODIGO", "error_sharedPreferences");
			String razon = prefCliente.getString("razon", "error_sharedPreferences");
			String representante = prefCliente.getString("representante", "error_sharedPreferences");
			String direccion = prefCliente.getString("DIRECCION", "error_sharedPreferences");
			String telefono = prefCliente.getString("telefono", "error_sharedPreferences");
			String nit = prefCliente.getString("NIT", "error_sharedPreferences");
			String barrio = prefCliente.getString("BARRIO", "error_sharedPreferences");
			
			//cargar los datos leidos del cliente.
			viewRazonSocial.setText(razon.trim());
			viewRepresentante.setText(representante.trim());
			viewNIT.setText(nit.trim());
			viewDireccion.setText(direccion.trim());
			viewBarrio.setText(barrio.trim());
			viewTelefono.setText(telefono.trim());
			viewCodigo.setText(codigo.trim());
			
			
			/*cargar la informacion de la venta actual consultando la tabla detalleVirtual que estara activa durante todo el ciclo de 
			 * vida de esta activity*/
			InfoVenta venta = new InfoVenta();
			SharedPreferences prefNumDoc =  this.getActivity().getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
			String numerDoc = prefNumDoc.getString("numeroDoc", "ND");
			if(!numerDoc.equals("ND")){
				venta = DataBaseBO.calcularEstadoDeLaVentaActual(numerDoc, this.getActivity().getBaseContext());				

				//ACTUALIZAR LOS DATOS DE LA VENTA
				viewValor.setText("$" + venta.valor.trim());
				viewValorIva.setText("$" + venta.iva.trim());
				viewValorTotal.setText("$" + venta.total.trim());
			}
			else {
				Toast.makeText(this.getActivity().getBaseContext(), "Error inesperado vuelva a realizar el pedido", Toast.LENGTH_LONG).show();
			}		
			
			super.onViewCreated(view, savedInstanceState);
		}
		
		
		/**
		 * metodo que permite mantener actualizado el estado de la venta
		 * en el view de informacion de ventas.
		 */
		public void actualizarEstadoVenta() {

			/*cargar la informacion de la venta actual consultando la tabla detalleVirtual que estara activa durante todo el ciclo de 
			 * vida de esta activity*/
			InfoVenta venta = new InfoVenta();
			SharedPreferences prefNumDoc =  this.getActivity().getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
			String numerDoc = prefNumDoc.getString("numeroDoc", "ND");
			
			if(!numerDoc.equals("ND")){
				venta = DataBaseBO.calcularEstadoDeLaVentaActual(numerDoc, this.getActivity().getBaseContext());
				//ACTUALIZAR LOS DATOS DE LA VENTA
				viewValor.setText("$" + venta.valor.trim());
				viewValorIva.setText("$" + venta.iva.trim());
				viewValorTotal.setText("$" + venta.total.trim());
			}
			else {
				Toast.makeText(this.getActivity().getBaseContext(), "Error inesperado vuelva a realizar el pedido", Toast.LENGTH_LONG).show();
			}
		}
		
		
		/**
		 * metodo para cargar el monto en pesos de la venta actual.
		 * @return
		 */
		public InfoVenta obtenerMontoVenta(){
			/*cargar la informacion de la venta actual consultando la tabla detalleVirtual que estara activa durante todo el ciclo de 
			 * vida de esta activity*/
			InfoVenta venta = new InfoVenta();
			SharedPreferences prefNumDoc =  this.getActivity().getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
			String numerDoc = prefNumDoc.getString("numeroDoc", "ND");
			if(!numerDoc.equals("ND")){
				venta = DataBaseBO.calcularEstadoDeLaVentaActual(numerDoc, this.getActivity().getBaseContext());
			}
			else {
				Toast.makeText(this.getActivity().getBaseContext(), "Error inesperado vuelva a intentar finalizar el pedido.", Toast.LENGTH_LONG).show();
			}
			return venta;
		}
		
		/**
		 * metodo para cargar el monto2 en pesos de la venta actual.
		 * refrerencias que inician con 7 0 9
		 * @return
		 */
		public InfoVenta obtenerMonto2Venta(){
			/*cargar la informacion de la venta actual consultando la tabla detalleVirtual que estara activa durante todo el ciclo de 
			 * vida de esta activity*/
			InfoVenta venta = new InfoVenta();
			SharedPreferences prefNumDoc =  this.getActivity().getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
			String numerDoc = prefNumDoc.getString("numeroDoc", "ND");
			if(!numerDoc.equals("ND")){
				venta = DataBaseBO.calcularEstado2DeVentaActual(numerDoc, this.getActivity().getBaseContext());
			}
			else {
				Toast.makeText(this.getActivity().getBaseContext(), "Error inesperado vuelva a intentar finalizar el pedido.", Toast.LENGTH_LONG).show();
			}
			return venta;
		}		
		
		
		
	

		/**
		 * @return the motivoNoCompra
		 */
		public MotivosNoCompra getMotivoNoCompra() {
			/*retornar siempre motivo con codigo 1, ya que es definido para pedidos. */
			motivoNoCompra = new MotivosNoCompra();
			motivoNoCompra.codigo = 1;
			return motivoNoCompra;
		}
		
		
		
		
		

	
	}//final de la clase.