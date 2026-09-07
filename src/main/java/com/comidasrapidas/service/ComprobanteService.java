package com.comidasrapidas.service;

import com.comidasrapidas.model.Cliente;
import com.comidasrapidas.model.DetalleVenta;
import com.comidasrapidas.model.Venta;
import com.comidasrapidas.util.Formato;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.OutputStream;
import org.springframework.stereotype.Service;

@Service
public class ComprobanteService {

    public String generar(Venta venta) {
        boolean esFacturaElectronica = venta.getCliente() != null;
        StringBuilder texto = new StringBuilder();
        texto.append(esFacturaElectronica ? "FACTURA ELECTRÓNICA DE VENTA\n" : "DOCUMENTO EQUIVALENTE POS - TIQUETE DE VENTA\n");
        texto.append("========================================\n");
        texto.append("ESTABLECIMIENTO: COMIDAS RÁPIDAS\n");
        texto.append("Venta n.º ").append(venta.getId()).append("\n");
        texto.append("Fecha: ").append(Formato.fecha(venta.getFechaHora())).append("\n");
        texto.append("Atendió: ").append(venta.getUsuario().getNombre()).append("\n");
        texto.append("----------------------------------------\n");
        texto.append(cliente(venta.getCliente())).append("\n");
        texto.append("----------------------------------------\n");
        texto.append("DETALLE DE PRODUCTOS:\n");
        venta.getDetalles().forEach(d -> texto.append(linea(d)));
        texto.append("----------------------------------------\n");
        texto.append("TOTAL A PAGAR: ").append(Formato.dinero(venta.getTotal())).append("\n");
        texto.append("Pago: efectivo\n");
        texto.append("========================================\n");

        if (esFacturaElectronica) {
            texto.append("Factura electrónica emitida al cliente registrado.\n");
        } else {
            texto.append("Tiquete de mostrador / Documento equivalente POS.\n");
        }
        return texto.toString();
    }

    public void generarPdf(Venta venta, OutputStream salida) throws DocumentException {
        boolean esFacturaElectronica = venta.getCliente() != null;
        // Dimensiones tipo recibo / media carta (ancho 300, alto variable o A5/A6)
        Rectangle tamanoTicket = new Rectangle(360, 600);
        Document documento = new Document(tamanoTicket, 20, 20, 25, 25);
        PdfWriter.getInstance(documento, salida);
        documento.open();

        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(20, 50, 90));
        Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
        Font fuenteTexto = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        Font fuenteNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
        Font fuenteTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(20, 100, 40));
        Font fuentePie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

        // Encabezado
        Paragraph titulo = new Paragraph("COMIDAS RÁPIDAS", fuenteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);

        Paragraph tipoComp = new Paragraph(esFacturaElectronica ? "FACTURA ELECTRÓNICA DE VENTA" : "DOCUMENTO EQUIVALENTE POS", fuenteSubtitulo);
        tipoComp.setAlignment(Element.ALIGN_CENTER);
        tipoComp.setSpacingAfter(8);
        documento.add(tipoComp);

        // Metadatos
        documento.add(new Paragraph("Venta N.º: " + venta.getId(), fuenteNegrita));
        documento.add(new Paragraph("Fecha y hora: " + Formato.fecha(venta.getFechaHora()), fuenteTexto));
        documento.add(new Paragraph("Atendido por: " + venta.getUsuario().getNombre(), fuenteTexto));

        // Información de cliente
        Paragraph seccionCliente = new Paragraph();
        seccionCliente.setSpacingBefore(6);
        seccionCliente.setSpacingAfter(8);
        if (esFacturaElectronica) {
            Cliente c = venta.getCliente();
            seccionCliente.add(new Paragraph("Cliente: " + c.getNombre() + " " + c.getApellido(), fuenteNegrita));
            seccionCliente.add(new Paragraph(c.getTipoDocumento() + ": " + c.getNumeroDocumento(), fuenteTexto));
            if (!c.getTelefono().isEmpty()) {
                seccionCliente.add(new Paragraph("Teléfono: " + c.getTelefono(), fuenteTexto));
            }
            if (!c.getCorreo().isEmpty()) {
                seccionCliente.add(new Paragraph("Correo: " + c.getCorreo(), fuenteTexto));
            }
        } else {
            seccionCliente.add(new Paragraph("Cliente: Consumidor Final", fuenteTexto));
        }
        documento.add(seccionCliente);

        // Tabla de ítems
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{45f, 15f, 20f, 20f});
        tabla.setSpacingBefore(5);
        tabla.setSpacingAfter(10);

        Color fondoEncabezado = new Color(230, 235, 245);

        agregarCeldaEncabezado(tabla, "Producto", fuenteNegrita, fondoEncabezado, Element.ALIGN_LEFT);
        agregarCeldaEncabezado(tabla, "Cant.", fuenteNegrita, fondoEncabezado, Element.ALIGN_CENTER);
        agregarCeldaEncabezado(tabla, "P. Unit.", fuenteNegrita, fondoEncabezado, Element.ALIGN_RIGHT);
        agregarCeldaEncabezado(tabla, "Subtotal", fuenteNegrita, fondoEncabezado, Element.ALIGN_RIGHT);

        for (DetalleVenta d : venta.getDetalles()) {
            PdfPCell cProd = new PdfPCell(new Paragraph(d.getProducto().getNombre(), fuenteTexto));
            cProd.setBorder(Rectangle.BOTTOM);
            cProd.setBorderColor(Color.LIGHT_GRAY);
            cProd.setPadding(4);
            tabla.addCell(cProd);

            PdfPCell cCant = new PdfPCell(new Paragraph(String.valueOf(d.getCantidad()), fuenteTexto));
            cCant.setBorder(Rectangle.BOTTOM);
            cCant.setBorderColor(Color.LIGHT_GRAY);
            cCant.setHorizontalAlignment(Element.ALIGN_CENTER);
            cCant.setPadding(4);
            tabla.addCell(cCant);

            PdfPCell cPrecio = new PdfPCell(new Paragraph(Formato.dinero(d.getPrecioUnitario()), fuenteTexto));
            cPrecio.setBorder(Rectangle.BOTTOM);
            cPrecio.setBorderColor(Color.LIGHT_GRAY);
            cPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cPrecio.setPadding(4);
            tabla.addCell(cPrecio);

            PdfPCell cSub = new PdfPCell(new Paragraph(Formato.dinero(d.getSubtotal()), fuenteTexto));
            cSub.setBorder(Rectangle.BOTTOM);
            cSub.setBorderColor(Color.LIGHT_GRAY);
            cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cSub.setPadding(4);
            tabla.addCell(cSub);
        }
        documento.add(tabla);

        // Total
        Paragraph totalP = new Paragraph("TOTAL: " + Formato.dinero(venta.getTotal()), fuenteTotal);
        totalP.setAlignment(Element.ALIGN_RIGHT);
        totalP.setSpacingAfter(4);
        documento.add(totalP);

        Paragraph pagoP = new Paragraph("Forma de pago: Efectivo", fuenteTexto);
        pagoP.setAlignment(Element.ALIGN_RIGHT);
        pagoP.setSpacingAfter(12);
        documento.add(pagoP);

        // Pie de página
        Paragraph pie = new Paragraph(esFacturaElectronica
                ? "Factura Electrónica generada satisfactoriamente para el cliente registrado."
                : "Comprobante de venta rápida POS para consumidor final.", fuentePie);
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);

        documento.close();
    }

    private void agregarCeldaEncabezado(PdfPTable tabla, String texto, Font fuente, Color fondo, int alineacion) {
        PdfPCell celda = new PdfPCell(new Paragraph(texto, fuente));
        celda.setBackgroundColor(fondo);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(Color.GRAY);
        tabla.addCell(celda);
    }

    private String cliente(Cliente cliente) {
        if (cliente == null) {
            return "Cliente: consumidor sin registro (Venta POS)";
        }
        return "Cliente: " + cliente.getNombre() + " " + cliente.getApellido()

                + "\n" + cliente.getTipoDocumento() + ": " + cliente.getNumeroDocumento()
                + (cliente.getTelefono().isEmpty() ? "" : "\nTeléfono: " + cliente.getTelefono())
                + (cliente.getCorreo().isEmpty() ? "" : "\nCorreo: " + cliente.getCorreo());
    }

    private String linea(DetalleVenta detalle) {
        return detalle.getProducto().getNombre() + "\n  " + detalle.getCantidad()
                + " × " + Formato.dinero(detalle.getPrecioUnitario())
                + " = " + Formato.dinero(detalle.getSubtotal()) + "\n";
    }
}

