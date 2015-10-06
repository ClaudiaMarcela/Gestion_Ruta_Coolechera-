/**
 * 
 */
package com.android.data;

/**
 * Crea un objeto para empaquetar la informacion del resumen de estadisticas.
 * @author JICZ
 *
 */
public class ResumenEstadisticas {
	
	/**
	 * numero de visitas
	 */
	public int visitas;
	
	/**
	 * numero total de pedidos realizados
	 */
	public double totalPedidos;
	
	/**
	 * numero de pedidos sincronizados
	 */
	public int pedidosSincronizados;
	
	/**
	 * monto sincronizado
	 */
	public double valorSincronizado;
	
	/**
	 * numero de pedidos que faltan por enviar
	 */
	public int pedidosNoSincronizados;
	
	/**
	 * monto no sincronizado.
	 */
	public double valorNoSincronizado;
	
	/**
	 * efectividad porcentual. se mide diviendo: (totalPedidos / visitas) * 100%
	 */
	public double efectividad;
	
	/**
	 * monto de ventas.
	 */
	public double totalVentas;
	
	/**
	 * cantidad de no compras
	 */
	public int noCompras;
	
	/**
	 * cantidad de no compras que  han sido sincronizadas
	 */
	public int noComprasSinc;
	
	/**
	 * cantidad de no compras que No han sido sincronizadas
	 */
	public int noComprasNoSinc;
}
