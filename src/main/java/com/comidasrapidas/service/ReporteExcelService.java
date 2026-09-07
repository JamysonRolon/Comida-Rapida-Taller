package com.comidasrapidas.service;

import com.comidasrapidas.model.Cliente;
import com.comidasrapidas.model.Venta;
import com.comidasrapidas.util.Formato;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ReporteExcelService {

    public byte[] generarReporteExcel(String tituloPeriodo, List<Venta> listaVentas) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        exportarReporte(tituloPeriodo, listaVentas, salida);
        return salida.toByteArray();
    }

    public void exportarReporte(String tituloPeriodo, List<Venta> listaVentas, OutputStream salida) throws IOException {
        try (Workbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("Ventas");

            // Estilos
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            fuenteTitulo.setColor(IndexedColors.DARK_BLUE.getIndex());

            CellStyle estiloTitulo = libro.createCellStyle();
            estiloTitulo.setFont(fuenteTitulo);

            Font fuenteEncabezado = libro.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());

            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(fuenteEncabezado);
            estiloEncabezado.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);
            estiloEncabezado.setBorderBottom(BorderStyle.THIN);

            CellStyle estiloMoneda = libro.createCellStyle();
            DataFormat formatoDatos = libro.createDataFormat();
            estiloMoneda.setDataFormat(formatoDatos.getFormat("$#,##0.00"));
            estiloMoneda.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle estiloCentrado = libro.createCellStyle();
            estiloCentrado.setAlignment(HorizontalAlignment.CENTER);

            Font fuenteTotal = libro.createFont();
            fuenteTotal.setBold(true);

            CellStyle estiloFilaTotal = libro.createCellStyle();
            estiloFilaTotal.setFont(fuenteTotal);
            estiloFilaTotal.setBorderTop(BorderStyle.DOUBLE);

            CellStyle estiloTotalMoneda = libro.createCellStyle();
            estiloTotalMoneda.setFont(fuenteTotal);
            estiloTotalMoneda.setDataFormat(formatoDatos.getFormat("$#,##0.00"));
            estiloTotalMoneda.setAlignment(HorizontalAlignment.RIGHT);
            estiloTotalMoneda.setBorderTop(BorderStyle.DOUBLE);

            // Fila de título
            Row filaTitulo = hoja.createRow(0);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("REPORTE DE VENTAS - " + tituloPeriodo.toUpperCase());
            celdaTitulo.setCellStyle(estiloTitulo);

            // Encabezados
            String[] columnas = {
                "N.º Venta", "Fecha y Hora", "Tipo Comprobante", "Cliente",
                "Tipo Doc.", "N.º Documento", "Atendido Por", "Total Venta"
            };

            Row filaEncabezados = hoja.createRow(2);
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = filaEncabezados.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            int indiceFila = 3;
            double sumaTotal = 0.0;

            for (Venta venta : listaVentas) {
                Row fila = hoja.createRow(indiceFila++);
                Cliente cliente = venta.getCliente();
                boolean esFacturaElectronica = (cliente != null);

                Cell celdaId = fila.createCell(0);
                celdaId.setCellValue(venta.getId());
                celdaId.setCellStyle(estiloCentrado);

                Cell celdaFecha = fila.createCell(1);
                celdaFecha.setCellValue(Formato.fecha(venta.getFechaHora()));

                Cell celdaTipo = fila.createCell(2);
                celdaTipo.setCellValue(esFacturaElectronica ? "Factura Electrónica" : "Tiquete POS");
                celdaTipo.setCellStyle(estiloCentrado);

                Cell celdaCliente = fila.createCell(3);
                celdaCliente.setCellValue(cliente != null ? (cliente.getNombre() + " " + cliente.getApellido()).trim() : "Consumidor Final");

                Cell celdaTipoDoc = fila.createCell(4);
                celdaTipoDoc.setCellValue(cliente != null ? cliente.getTipoDocumento() : "N/A");
                celdaTipoDoc.setCellStyle(estiloCentrado);

                Cell celdaNumDoc = fila.createCell(5);
                celdaNumDoc.setCellValue(cliente != null ? cliente.getNumeroDocumento() : "N/A");

                Cell celdaEmpleado = fila.createCell(6);
                celdaEmpleado.setCellValue(venta.getUsuario().getNombre());

                Cell celdaTotal = fila.createCell(7);
                double total = venta.getTotal().doubleValue();
                celdaTotal.setCellValue(total);
                celdaTotal.setCellStyle(estiloMoneda);

                sumaTotal += total;
            }

            // Fila de resumen total
            Row filaResumen = hoja.createRow(indiceFila);
            Cell celdaEtiquetaTotal = filaResumen.createCell(6);
            celdaEtiquetaTotal.setCellValue("TOTAL GENERAL:");
            celdaEtiquetaTotal.setCellStyle(estiloFilaTotal);

            Cell celdaGranTotal = filaResumen.createCell(7);
            celdaGranTotal.setCellValue(sumaTotal);
            celdaGranTotal.setCellStyle(estiloTotalMoneda);

            // Ajustar ancho de columnas
            for (int i = 0; i < columnas.length; i++) {
                hoja.autoSizeColumn(i);
                hoja.setColumnWidth(i, Math.max(hoja.getColumnWidth(i) + 1200, 3200));
            }

            libro.write(salida);
        }
    }
}
