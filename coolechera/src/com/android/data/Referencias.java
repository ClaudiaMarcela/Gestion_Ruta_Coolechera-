/**
 * 
 */
package com.android.data;

/**
 * 
 * Objeto referencia, equivale a un producto ofrecido por coolechera.
 * @author JICZ
 *
 */
public class Referencias {
	
	/**
	 * ¨codigo de producto
	 */
	public String codigo;
	
	/**
	 * descripcion del producto
	 */
	public String descripcion;
	
	/**
	 * linea a la que pertenece el producto
	 */
	public String linea;
	
	/**
	 * precio del producto
	 */
	public double precio;
	
	/**
	 * iva cobrado al producto
	 */
	public double iva;
	
	/**
	 * factor del producto
	 */
	public double factor;
	
	/**
	 * unidad de venta.
	 */
	public int unidadVenta;
	
	/**
	 * promocion a la que aplica el producto.
	 */
	public int promocion;
	
	/**
	 * cantidad pedida, default cero
	 */
	public int cantidad = 0;
	
	/**
	 * tipo de pedido, 1 venta, 2 cambio.
	 */
	public int tipo = 1;
	
	/**
	 * identificador de un producto para cambio.
	 */
	public boolean cambio = false;
	
	
	/**
	 * monto del iva generado por la compra de un producto.
	 */
	public double montoIva = 0;

}
