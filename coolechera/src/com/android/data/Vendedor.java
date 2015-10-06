package com.android.data;

/**
 * clase para identificar un vendor que hace uso de la aplicacion.
 * @author JICZ
 *
 */
public class Vendedor {	
	
	/**
	 * codigo del vendedor
	 */
	public String codigo;
	
	/**
	 * nombre del vendedor
	 */
	public String nombre;
	
	/**
	 * identificacion del vendedor
	 */
	public String cedula;
	
	/**
	 * zona de vendedor
	 */
	public String zona;	
	
	/**
	 * bodega asignada al vendedor
	 */
	public String bodega;
	
	/**
	 * consecutivo de ventas del vendedor.
	 * usado para generar numeros de documento. (codigos de encabezados)
	 */
	public int consecutivo;	
	
	
	/**
	 * fecha del dia en que se empieza un dia laboral
	 */
	public String fechaLabores;
	
	/**
	 * version de la aplicacion.
	 */
	public String version;
}
