package com.android.fragments;

import java.util.ArrayList;

import com.android.business.object.DataBaseBO;
import com.android.coolechera.R;
import com.android.data.ItemDrawerLayout;
import com.android.data.Referencias;
import com.android.util.Constantes;
import com.android.util.ProductosAdapter;

import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;


/**
	 * A placeholder fragment containing a simple view.
	 */
	public class ReferenciasFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		
		/**
		 * adapter personalizado, para mostrar un menu de opciones en tipo listView
		 */
		private ProductosAdapter adapter;
		
		/**
		 * iconos
		 */
		private int icon;	
		
		
		/**
		 * referencia a SearchView ubicado en el layout
		 */
		private SearchView search;
		
		/**
		 * lista que contiene las opciones
		 */
		private ArrayList<ItemDrawerLayout> items;
		
		/**
		 * lista de productos encontrados
		 */
		ArrayList<Referencias> listaReferencias;
		
		
		/**
		 * referencia al listView que mostrara la informacion
		 */
		private ListView listViewProductos;
		
		/**
		 * usado para determinar si un producto es insertado para cambio.
		 */
		private RadioButton cambioRadioButton;

		
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static ReferenciasFragment newInstance(int sectionNumber) {
			ReferenciasFragment fragment = new ReferenciasFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public ReferenciasFragment() {
		}

		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_referencias,container, false);
			listViewProductos = (ListView) rootView.findViewById(R.id.listaProductos);
			icon = R.drawable.ic_social_send_now_red;
			
			//cargar referencia y agregar captura de evento de busqueda.
			search=(SearchView) rootView.findViewById(R.id.searchViewBuscarProducto);
			search.setOnQueryTextListener(searchListener);			
			
			return rootView;
		}
		
		
		/**
		 * handler para controlar el evento de busqueda del searchView
		 */ 
		private OnQueryTextListener searchListener = new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String text) {
				//cargar los productos encontrados.
				cargarListaProductos(text.trim());				
				//buscar el cliente, se envia el parametro sin espacios.
				search.clearFocus();
				search.setFocusable(false);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String text) {
				return false;
			}
		};
		
		
		/**
		 *  Handler para capturar el item seleccionado por el usuario.
		 */
		private OnItemClickListener listenerListView = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//mostrar dialog para insertar un detalle
				dialogInsertarProductoEnDetalle(position);				 
			}
		};

		
		/**
		 * metodo que permite cargar la lista con los datos obtenidos en la busqueda de productos para ofrecer.
		 * @param text
		 */
		protected void cargarListaProductos(String text) {
			
			//lista de productos (referencias) cargados.
			listaReferencias = DataBaseBO.buscarReferencias(text, this.getActivity().getBaseContext());
			items = new ArrayList<ItemDrawerLayout>();
			
			for (Referencias producto : listaReferencias) {
				ItemDrawerLayout item = new ItemDrawerLayout();
				item.icono = icon;
				item.titulo = producto.descripcion;
				item.subTitulo = "Codigo: " + producto.codigo + " -Precio: $" + producto.precio + " - IVA: " + producto.iva + "%";
				this.items.add(item);
			}
			//cargar el adapter en el listView
			adapter = new ProductosAdapter(getActivity(), items);
			listViewProductos.setAdapter(adapter);	
			listViewProductos.setOnItemClickListener(listenerListView);
		}		
		
		
		
		
		/**
		 * Metodo que inserta un producto en la tabla de detalle virtual.
		 * Tabla creada durante el ciclo de vida de la activity VentasActivity.java
		 * @param referencia
		 * @param cantidad 
		 */
		protected void insertarProductoEnDetalleVirtual(Referencias referencia, String cantidad) {
			//cargar NumeroDoc desde el SharedPreference
			SharedPreferences prefNumDoc =  this.getActivity().getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
			String numeroDoc = prefNumDoc.getString("numeroDoc", "ND");
			String bodega = prefNumDoc.getString("bodega", "NB");
			//verificar que el numero es leido correctamente
			if(!numeroDoc.equals("ND") && !bodega.equals("NB")){
				//se intenta la insercion en la base de datos, tabla virtual de detalle.
				boolean insertado = DataBaseBO.insertarProductoEnDetalleVirtual(referencia, cantidad, numeroDoc, bodega, getActivity().getBaseContext());
				if(insertado){
					Toast.makeText(getActivity().getBaseContext(), "Producto insertado OK!", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getActivity().getBaseContext(), "Error insertando!\nVerificar que el producto no haya sido ingresado antes", Toast.LENGTH_LONG).show();
				}
			}
			else {
				Toast.makeText(getActivity().getBaseContext(), "Error Cargando Base de datos NUMDOC!\nPor favor Inicie Dia", Toast.LENGTH_LONG).show();
			}
		}
		
		
		/**
		 * Dialog insertar producto, se elige la opcion de cantidad y venta o cambio de un producto.
		 * @param referencia
		 */
		protected void dialogInsertarProductoEnDetalle(final int position){
			
			final Referencias referencia = listaReferencias.get(position);
			final Dialog dialog = new Dialog(getActivity(), R.drawable.logo_coolechera);
			
			//asignar el layout
			dialog.setContentView(R.layout.dialog_venta_producto);
			//definir el titulo del dialog
			dialog.setTitle("Insertar Referencia");
			//salir solo con boton salir
			dialog.setCancelable(false);
			
			//cargar referencias  view del dialog y setiar los textos a mostrar.
			TextView tituloDialog = (TextView) dialog.findViewById(R.id.textViewDialogDescripcion);
			tituloDialog.setText(referencia.descripcion.trim());
			
			TextView precioDialog = (TextView) dialog.findViewById(R.id.textViewDialogPrecio);
			precioDialog.setText("$" + referencia.precio);
			
			TextView ivaDialog = (TextView) dialog.findViewById(R.id.textViewDialogIva);
			ivaDialog.setText(referencia.iva + "%");
			
			//referencia al EditText para capturar la cantidad ingresada
			final EditText editTextCantidad = (EditText) dialog.findViewById(R.id.editTextCantidadDialog);
			
			//referencias para identificar productos en cambio
			cambioRadioButton = (RadioButton) dialog.findViewById(R.id.radioButtonCambio);
			RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radioGroupVenta);
			radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					switch (checkedId) {
					case R.id.radioButtonCambio:
						mostrarAlertCambio("Producto Registrado como Cambio!");						
						break;
					
					case R.id.radioButtonVenta:
						mostrarAlertCambio("Producto Registrado como Venta!");						
						break;

					default:
						break;
					}					
				}
			});
			
			//captura evento de cancelar dialog.
			Button salirDialog = (Button) dialog.findViewById(R.id.buttonSalirDialog);
			salirDialog.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.cancel();					
				}
			});
			
			//captura evento de aceptar.
			Button aceptarDialog = (Button) dialog.findViewById(R.id.buttonAceptarDialog);
			aceptarDialog.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String cantidad = editTextCantidad.getText().toString().trim();
					
					//validar cantidad.
					if(cantidad.length() > 0 && isNumber(cantidad)){
						
						//verificar si se inserta producto como un cambio.
						boolean cambio = cambioRadioButton.isChecked();
						if(cambio){
							//Actualizar el tipo a cambio. tipo = 2.
							referencia.tipo = Constantes.TIPO_CAMBIO;
							insertarProductoEnCambio(referencia, cantidad);	
						}
						else {
							//Actualizar el tipo a venta. tipo = 1.
							referencia.tipo = Constantes.TIPO_VENTA;
							//calcular el monto de iva generado por la compra de esta referencia.
							referencia.montoIva = referencia.precio * (referencia.iva / 100.0);
							//sumar el iva al precio unitario y redondear el valor.
							referencia.precio = Math.round(referencia.precio + referencia.montoIva);
							// si el la cantidad es valida, se inserta en la tabla detallevirtual
							insertarProductoEnDetalleVirtual(referencia, cantidad);
						}
						
						//cerrar el dialog.
						dialog.cancel();
					}
					else {
						Toast.makeText(getActivity().getBaseContext(), "Cantidad no valida!.\nRecuerde ingresar una cantidad mayor a cero.", Toast.LENGTH_LONG).show();
					}					
				}
			});
			
			//mostrar dialog
			dialog.show();			
		}	


		
		
		
		
		/**
		 * insertar un producto que es para cambio. se inserta en la tabla temporal para cambios.
		 * @param referencia
		 * @param cantidad
		 */
		protected void insertarProductoEnCambio(Referencias referencia, String cantidad) {

			//cargar NumeroDoc desde el SharedPreference
			SharedPreferences prefNumDoc =  this.getActivity().getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
			String numeroDoc = prefNumDoc.getString("numeroDoc", "ND");
			String bodega = prefNumDoc.getString("bodega", "NB");
			//verificar que el numero es leido correctamente
			if(!numeroDoc.equals("ND") && !bodega.equals("NB")){
				//se intenta la insercion en la base de datos, tabla virtual de detalle.
				boolean insertado = DataBaseBO.insertarProductoEnCambio(referencia, cantidad, numeroDoc, bodega, getActivity().getBaseContext());
				if(insertado){
					Toast.makeText(getActivity().getBaseContext(), "Producto insertado para cambio OK!", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getActivity().getBaseContext(), "Error insertando!\nVerificar que el producto no haya sido ingresado para cambio antes", Toast.LENGTH_LONG).show();
				}
			}
			else {
				Toast.makeText(getActivity().getBaseContext(), "Error Cargando Base de datos NUMDOC!\nPor favor Inicie Dia", Toast.LENGTH_LONG).show();
			}		
		}

		
		
		/**
		 * metodo para validar que un String ingresado por un usuario es un numero entero positivo valido.
		 * @param cantidad, valor ingresado para analizar
		 * @return true si es un numero entero positivo valido, false en caso contrario. (0 - 'cero'  no es un entero positivo)
		 */
		protected boolean isNumber(String cantidad) {
			try {
				int number = Integer.parseInt(cantidad);
				if(!(number > 0)){
					return false;
				}
				return true;
			} catch (NumberFormatException e) {
				return false;
			}			
		}

		
		
		/**
		 * alert informatico para cuando se define un producto para cambio.
		 * @param mensaje
		 */
		public void mostrarAlertCambio(final String mensaje){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(mensaje);
			builder.setPositiveButton("OK", new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}


		
	}//final de la clase