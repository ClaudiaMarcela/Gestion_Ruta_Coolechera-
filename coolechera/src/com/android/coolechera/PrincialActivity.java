package com.android.coolechera;

import com.android.fragments.DiaTrabajoFragment;
import com.android.fragments.SincronizacionFragment;
import com.android.gestionruta.LocationGPS;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

public class PrincialActivity extends ActionBarActivity implements	NavigationDrawerFragment.NavigationDrawerCallbacks {

	
	
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	
	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_princial);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();		

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
	
		/*Iniciar servicio Google Api Cliente para captura de coordenadas*/
		LocationGPS location = LocationGPS.getInstance(PrincialActivity.this);
		location.iniciarGoogleApiClient();
		location.conectToGoogleApiClient();
	}


	
	/**
	 * permite cambiar el fragment segun la opcion seleccionada por el usuario
	 */
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();		
		
		switch (position) {
		case 0:
			//mostrar el fragment que contiene las opciones de dia de trabajo
			fragmentManager.beginTransaction().replace(R.id.container,DiaTrabajoFragment.newInstance(position + 1)).commit();						
			break;
			
		case 1:
			//mostrar el fragment que contiene las opciones relacionadas con la sincronizacion de datos a la web
			fragmentManager.beginTransaction().replace(R.id.container,SincronizacionFragment.newInstance(position + 1)).commit();						
			break;

		default:
			break;
		}
	}

	
	
	/**
	 * permite actualizar el titulo a la opcion elegida por el usuario.
	 * @param number
	 */
	public void onSectionAttached(int number) {
		String[] titulos = getResources().getStringArray(R.array.navigation_options);
		mTitle = titulos[number-1];
	}

	
	
	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			/* 	Only show items in the action bar relevant to this screen
			 	if the drawer is not showing. Otherwise, let the drawer
		 		decide what to show in the action bar.
			 */
			getMenuInflater().inflate(R.menu.princial, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/* 	Handle action bar item clicks here. The action bar will
		 	automatically handle clicks on the Home/Up button, so long
		 	as you specify a parent activity in AndroidManifest.xml.
		*/
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
