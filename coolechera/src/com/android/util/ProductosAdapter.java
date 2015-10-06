package com.android.util;

import java.util.ArrayList;

import com.android.coolechera.R;
import com.android.data.ItemDrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Adapter que permite mostrar la lista de productos encontradas.
 * Se implementa patron ViewHolder para optimizar el rendimiento a la hora de mostrar
 * los resultados por pantalla.
 * 
 * @author JICZ
 *
 */
@SuppressLint("InflateParams") public class ProductosAdapter extends ArrayAdapter<ItemDrawerLayout> {


	private Activity activity;

	private ArrayList<ItemDrawerLayout> arrayItems;
	
	


	/**
	 * constructor
	 * @param activity
	 * @param arrayItems
	 */
	public ProductosAdapter(Activity activity,ArrayList<ItemDrawerLayout> arrayItems) {
		super(activity, R.layout.items_buscar_productos, arrayItems);
		this.activity = activity;
		this.arrayItems = arrayItems;
	}





	@Override
	public int getCount() {
		return arrayItems.size();
	}

	@Override
	public ItemDrawerLayout getItem(int position) {
		return arrayItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
	/**
	 * getView optimizado para cargar gran numero de opciones encontradas minimizando el consumo de recursos.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if(convertView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			convertView = inflater.inflate(R.layout.items_buscar_productos, null);
						
			//conifurar viewHolder (optimizacion)
			Row rowHolder = new Row();			
			rowHolder.tituloItem = (TextView) convertView.findViewById(R.id.textViewTituloBuscarProducto);			
			rowHolder.subtitulo = (TextView) convertView.findViewById(R.id.textViewSubTituloBuscarProducto);			
			rowHolder.icono = (ImageView) convertView.findViewById(R.id.imageViewIconoBuscarProducto);
			
			convertView.setTag(rowHolder);
		}
		
		//llenar datos
		Row holder = (Row) convertView.getTag();
		ItemDrawerLayout item = arrayItems.get(position);
		if(item != null){
			holder.tituloItem.setText(item.titulo);
			holder.subtitulo.setText(item.subTitulo);
			holder.icono.setImageResource(item.icono);
		}
		
		return convertView;
	}



	/**
	 * representa una fila, (ViewHolder).
	 * @author JICZ
	 *
	 */
	public static class Row {
		TextView tituloItem;
		TextView subtitulo;
		ImageView icono;		
	}

}
