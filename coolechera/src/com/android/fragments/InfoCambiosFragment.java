package com.android.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.business.object.DataBaseBO;
import com.android.coolechera.R;
import com.android.data.ItemDrawerLayout;
import com.android.data.Referencias;
import com.android.util.Constantes;
import com.android.util.ProductosAdapter;



/**
	 * Informe de la lista de productos que han sido pedidos hasta el momento.
	 */
	public class InfoCambiosFragment extends Fragment {
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
		private ListView listViewProductosPedidos;

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static InfoCambiosFragment newInstance(int sectionNumber) {
			InfoCambiosFragment fragment = new InfoCambiosFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public InfoCambiosFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_info_cambios,container, false);
			listViewProductosPedidos = (ListView) rootView.findViewById(R.id.listaProductosCambio);
			
			icon = R.drawable.ic_av_shuffle_red;
			
			//cargar referencia y agregar captura de evento de busqueda.
			search=(SearchView) rootView.findViewById(R.id.searchViewBuscarProductoCambio);
			search.setOnQueryTextListener(searchListener);			
			
			return rootView;
		}
		
		
		@Override
		public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
			//cargar la lista inicial de productos. con string vacio para mostrar todos los productos.
			cargarListaProductos("");
			super.onViewCreated(view, savedInstanceState);
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
				
				//mostrarAlertDialog para decidir si eliminar o modificar el producto
				mostrarAlertDialogElegirAccion(position);
			}
		};

		
		/**
		 * metodo que permite cargar la lista con los datos obtenidos en la busqueda.
		 * @param text
		 */
		public void cargarListaProductos(String text) {
			
			//lista de productos (referencias) cargados.
			listaReferencias = DataBaseBO.buscarReferenciasParaCambio(text, this.getActivity().getBaseContext());
			items = new ArrayList<ItemDrawerLayout>();
			
			for (Referencias producto : listaReferencias) {
				ItemDrawerLayout item = new ItemDrawerLayout();
				item.icono = icon;
				item.titulo = producto.descripcion;
				item.subTitulo = "Codigo: " + producto.codigo + " -Precio: $" + producto.precio + " - IVA: " + producto.iva + "%\nCantidad: " + producto.cantidad;
				this.items.add(item);
			}
			//cargar el adapter en el listView
			adapter = new ProductosAdapter(getActivity(), items);
			listViewProductosPedidos.setAdapter(adapter);	
			listViewProductosPedidos.setOnItemClickListener(listenerListView);
		}
	
		
		/**
		 * Metodo que inserta un producto en la tabla de detalle virtual.
		 * Tabla creada durante el ciclo de vida de la activity VentasActivity.java
		 * @param referencia
		 * @param cantidad 
		 */
		protected void actualizarProductoEnDetalleCambios(Referencias referencia, String cantidad) {
			//cargar NumeroDoc desde el SharedPreference
			SharedPreferences prefNumDoc =  this.getActivity().getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
			String numeroDoc = prefNumDoc.getString("numeroDoc", "ND");
			//verificar que el numero es leido correctamente
			if(!numeroDoc.equals("ND")){
				//se intenta la insercion en la base de datos, tabla virtual de detalle.
				boolean insertado = DataBaseBO.actualizarProductoEnDetalleCambios(referencia, cantidad, numeroDoc, getActivity().getBaseContext());
				if(insertado){
					Toast.makeText(getActivity().getBaseContext(), "Producto actualizado OK!", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getActivity().getBaseContext(), "Error actualizando!\nPor favor intente de nuevo", Toast.LENGTH_LONG).show();
				}
			}
			else {
				Toast.makeText(getActivity().getBaseContext(), "Error Cargando Base de datos NUMDOC!\nPor favor Inicie Dia", Toast.LENGTH_LONG).show();
			}
		}
		
		
		/**
		 * dialog mostrado al seleccionar un producto, para definir si lo desea
		 * eliminar o modificar.
		 * @param referencia
		 */
		protected void dialogModificarDetalle(final Referencias referencia){
			
			final Dialog dialog = new Dialog(getActivity(), R.drawable.logo_coolechera);
			
			//asignar el layout
			dialog.setContentView(R.layout.dialog_venta_producto);
			//definir el titulo del dialog
			dialog.setTitle("Actualizar Referencia");
			//salir solo con boton salir
			dialog.setCancelable(false);
			
			//ocultar el radioGroup para elegeir venta o cambio radioGroupVenta
			RadioGroup radioGrupo = (RadioGroup) dialog.findViewById(R.id.radioGroupVenta);
			radioGrupo.setVisibility(View.GONE);
			
			
			//cargar referencias  view del dialog y setiar los textos a mostrar.
			TextView tituloDialog = (TextView) dialog.findViewById(R.id.textViewDialogDescripcion);
			tituloDialog.setText(referencia.descripcion.trim());
			
			TextView precioDialog = (TextView) dialog.findViewById(R.id.textViewDialogPrecio);
			precioDialog.setText("$" + referencia.precio);
			
			TextView ivaDialog = (TextView) dialog.findViewById(R.id.textViewDialogIva);
			ivaDialog.setText(referencia.iva + "%");
			
			//referencia al EditText para capturar la cantidad ingresada
			final EditText editTextCantidad = (EditText) dialog.findViewById(R.id.editTextCantidadDialog);
			
			
			//captura evento de cancelar dialog.
			Button salirDialog = (Button) dialog.findViewById(R.id.buttonSalirDialog);
			salirDialog.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.cancel();					
				}
			});
			
			//captura evento de cancelar dialog.
			Button aceptarDialog = (Button) dialog.findViewById(R.id.buttonAceptarDialog);
			aceptarDialog.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String cantidad = editTextCantidad.getText().toString().trim();
					
					//validar cantidad.
					if(cantidad.length() > 0 && isNumber(cantidad)){
						// si el la cantidad es valida, se inserta en la tabla detallevirtual
						actualizarProductoEnDetalleCambios(referencia, cantidad);
						//mostrar la nueva lista actualizada
						cargarListaProductos("");
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
		 * informar al usuario para que determine la accion a ejecutar, 
		 * eliminar o modificar el producto..
		 */
		protected void mostrarAlertDialogElegirAccion(final int position) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
			builder.setMessage("Puede modificar o eliminar la referencia del actual pedido").
			setTitle("Modificar o eliminar el pedido?").
			setCancelable(false).
			setIcon(R.drawable.ic_action_about_red).setPositiveButton("Modificar", new Dialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Referencias referencia = listaReferencias.get(position);	
					//mostrar dialog para modificar un detalle
					dialogModificarDetalle(referencia);
					
				}
			}).setNegativeButton("Eliminar", new Dialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					//mostrar otro alert para confirmar si desea eliminar el producto.
					mostrarAlertDialogEliminarProducto(position);
					
				}
			}).setNeutralButton("Cancelar", new Dialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		}
		
		
		protected void mostrarAlertDialogEliminarProducto(final int position) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
			builder.setMessage("Se elimina la referencia del pedido actual.").
			setTitle("Eliminar el producto del pedido actual?").
			setCancelable(false).
			setIcon(R.drawable.ic_alerts_and_states_warning_red).
			setPositiveButton("Eliminar", new Dialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Referencias referencia = listaReferencias.get(position);	
					//eliminar el producto.
					eliminarReferenciaDeDetalleCambios(referencia, getActivity().getBaseContext());
					dialog.cancel();
				}
			}).setNegativeButton("Cancelar", new Dialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();									
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		}

		
		/**
		 * metodo que permite eliminar un producto de la tabla de detalle virtual.
		 * @param referencia
		 * @param baseContext
		 * @return
		 */
		protected void eliminarReferenciaDeDetalleCambios(Referencias referencia, Context context) {			

			//cargar NumeroDoc desde el SharedPreference
			SharedPreferences prefNumDoc =  this.getActivity().getSharedPreferences(Constantes.NUMERO_DOC, Context.MODE_PRIVATE);
			String numeroDoc = prefNumDoc.getString("numeroDoc", "ND");
			//verificar que el numero es leido correctamente
			if(!numeroDoc.equals("ND")){
				//se intenta eliminar el producto.
				boolean eliminado = DataBaseBO.eliminarProductoDeDetalleCambios(referencia, numeroDoc, context);
				if(eliminado){
					//mostrar los productos.
					 cargarListaProductos("");
					Toast.makeText(getActivity().getBaseContext(), "Producto eliminado OK!", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getActivity().getBaseContext(), "Error eliminando!\nPor favor intente de nuevo", Toast.LENGTH_LONG).show();
				}
			}
			else {
				Toast.makeText(getActivity().getBaseContext(), "Error Cargando Base de datos NUMDOC!\nPor favor Inicie Dia", Toast.LENGTH_LONG).show();
			}			
		}
		
		
		
	}//final de la clase