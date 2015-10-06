/**
 * 
 */
package com.android.data;

/**
 * 
 * Representa un encabezado de un pedido.
 * @author JICZ
 *
 */
public class Encabezado {
	
	/**
	 * codigo de cliente al que se le hace la venta.
	 */
	public String codigo;
	
	/**
	 * numeroDoc del pedido
	 */
	public String numeroDoc;
	
	/**
	 * fecha actual del sistema
	 */
	public String fechaTrans;
	
	/**
	 * siempre es j
	 */
	public String tipoTrans = "J";
	
	/**
	 * monto total de la venta
	 */
	public String montoFact;
	
	/**
	 * siempre cero, no se aplican descuentos por ahora.
	 */
	public double desc1 = 0;
	
	/**
	 * monto en pesos del iva.
	 */
	public String iva;
	
	/**
	 * codigo vendedor
	 */
	public String usuario;
	
	/**
	 * siempre cero
	 */
	public int sincronizado = 0;
	
	/**
	 * codigo vendedor
	 */
	public String telefono;
	
	/**
	 * siempre cero.
	 */
	public int entregado = 0;
	
	/**
	 * simpre cero.
	 */
	public int sincronizadoEntrega = 0;
	
	/**
	 * siempre cero.
	 */
	public int formaPago = 0;
	
	/**
	 * hora en que termina el pedido.
	 */
	public String horaFinal;
	
	/**
	 * fecha en que finaliza el pedido
	 */
	public String fechaReal;
	
	/***
	 * sumatoria de la cantidad de productos en el detalle de este encabezado
	 */
	public String litros;
	
	/**
	 * Sumatoria de precio * cantidad de todos aquellos productos
	 * que su codigo inicia por 7 o 9
	 */
	public String montoFact2;
	
	/**
	 * Iva generado por montofact2
	 */
	public String iva2;
	
	/**
	 * Cantidad de productos comprados donde su codigo inicia por
	 * 7 o por 9.
	 */
	public String listros2B;
	
	/**
	 * fecha y hora en que se inicio el pedido
	 */
	public String inicioPedido;
	
	/**
	 * fecha y hora en que se termina el pedido.
	 */
	public String finalPedido;
	
	/**
	 * bodega del vendedor que realiza la venta
	 */
	public String bodega;
	
	/**
	 * fecha entrega del pedido.
	 */
	public String fechaEntrega;
	
	/**
	 * version de la aplicacion.
	 */
	public String version;
}
