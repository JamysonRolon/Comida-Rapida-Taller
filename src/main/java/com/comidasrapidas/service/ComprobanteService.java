package com.comidasrapidas.service;

import com.comidasrapidas.model.Cliente;
import com.comidasrapidas.model.DetalleVenta;
import com.comidasrapidas.model.Venta;
import com.comidasrapidas.util.Formato;
import org.springframework.stereotype.Service;

@Service
public class ComprobanteService {
    public String generar(Venta venta) {
        StringBuilder texto = new StringBuilder("COMPROBANTE INTERNO DE VENTA\n");
        texto.append("Venta n.º ").append(venta.getId()).append("\n");
        texto.append("Fecha: ").append(Formato.fecha(venta.getFechaHora())).append("\n");
        texto.append("Atendió: ").append(venta.getUsuario().getNombre()).append("\n");
        texto.append(cliente(venta.getCliente())).append("\n\n");
        venta.getDetalles().forEach(d -> texto.append(linea(d)));
        texto.append("\nTOTAL: ").append(Formato.dinero(venta.getTotal()));
        texto.append("\nPago: efectivo\n\nComprobante interno. No es factura electrónica.\n");
        return texto.toString();
    }

    private String cliente(Cliente cliente) {
        if (cliente == null) {
            return "Cliente: consumidor sin registro";
        }
        return "Cliente: " + cliente.getNombre() + " " + cliente.getApellido()
                + "\nDocumento: " + cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento();
    }

    private String linea(DetalleVenta detalle) {
        return detalle.getProducto().getNombre() + "\n  " + detalle.getCantidad()
                + " × " + Formato.dinero(detalle.getPrecioUnitario())
                + " = " + Formato.dinero(detalle.getSubtotal()) + "\n";
    }
}
