/**
 * 
 */
package com.android.data;

/**
 * Representa un detalle de un pedido
 * @author JICZ
 *
 */
public class Detalle {
	
	/**
	 * numero de doc del pedido
	 */
	public String NumDoc;
	
	/**
	 * fecha actual del pedido
	 */
	public String Fecha;
	
	/**
	 * codigo de producto
	 */
	public String CodigoRef;
	
	/**
	 * precio unitario
	 */
	public String Precio;
	
	/**
	 * iva porcentual
	 */
	public String TarifaIva;
	
	/**
	 * siempre es cero
	 */
	public String DescuentoRenglon = "0";
	
	/**
	 * cantidad comprada
	 */
	public String Cantidad;	
	
	/**
	 * siempre cero
	 */
	public String Sincronizado = "0";
	
	/**
	 * siempre cero
	 */
	public String CantidadEntregada = "0";
	
	/**
	 * siempre cero
	 */
	public String SincronizadoEntrega = "0";
	
	/**
	 * fecha actual mas 2 dias (puede cambiar).
	 */
	public String FechaEntrega;
	
	/**
	 * siempre 1
	 */
	public String Tipo = "1";
	
	/**
	 * fecha real del dispositivo
	 */
	public String Fechareal;
	
	/**
	 * consecutivo de detalle
	 */
	public String item;
	
	/**
	 * bodega asignada
	 */
	public String bodega;

}
