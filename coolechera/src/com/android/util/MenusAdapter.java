package com.android.util;

import java.util.ArrayList;

import com.android.coolechera.R;
import com.android.data.ItemDrawerLayout;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenusAdapter extends BaseAdapter {


	private Activity activity;

	private ArrayList<ItemDrawerLayout> arrayItems;



	/**
	 * constructor
	 * @param activity
	 * @param arrayItems
	 */
	public MenusAdapter(Activity activity,ArrayList<ItemDrawerLayout> arrayItems) {
		super();
		this.activity = activity;
		this.arrayItems = arrayItems;
	}	


	@Override
	public int getCount() {
		return arrayItems.size();
	}

	@Override
	public Object getItem(int position) {
		return arrayItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Row view;
		LayoutInflater inflater = activity.getLayoutInflater();

		if(convertView == null) {
			view = new Row();
			ItemDrawerLayout item = arrayItems.get(position);
			convertView = inflater.inflate(R.layout.items_opciones, null);
			
			view.tituloItem = (TextView) convertView.findViewById(R.id.textViewTitulo);
			view.tituloItem.setText(item.titulo);
			
			view.subtitulo = (TextView) convertView.findViewById(R.id.textViewSubTitulo);
			view.subtitulo.setText(item.subTitulo);
			
			view.icono = (ImageView) convertView.findViewById(R.id.imageViewIconoSincronizacion);
			view.icono.setImageResource(item.icono);
			
			convertView.setTag(view);
		}
		else {
			view = (Row) convertView.getTag();
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
