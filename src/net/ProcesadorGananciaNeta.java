package net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ProcesadorGananciaNeta {


    // Atributos privados encapsulados en esta clase
    private static final double TASA_IMPUESTO = 0.07;     // 7% de impuesto
    private static final double TASA_COMISION = 0.05; // 5% de comisión

    private Path rutaDirectorioGrupo;
    private String nombreArchivoEntradaBruta;
    private String nombreArchivoSalidaNeta;

    public ProcesadorGananciaNeta(Path rutaDirectorioGrupo, String nombreArchivoEntradaBruta, String nombreArchivoSalidaNeta) {
        this.rutaDirectorioGrupo = rutaDirectorioGrupo;
        this.nombreArchivoEntradaBruta = nombreArchivoEntradaBruta;
        this.nombreArchivoSalidaNeta = nombreArchivoSalidaNeta;
    }

    /**
     * Lee datos del Reporte_Ganancias_Brutas.csv, procesa la ganancia neta (después de impuestos y comisiones)
     * y escribe el reporte en Reporte_Ganancias_Netas.csv.
     */
    public void GenerarReporteGananciaNeta() {
        Path rutaArchivoEntrada = Paths.get(rutaDirectorioGrupo.toString(), nombreArchivoEntradaBruta);
        Path rutaArchivoSalida = Paths.get(rutaDirectorioGrupo.toString(), nombreArchivoSalidaNeta);

        // Mapas para almacenar totales por categoría
        Map<String, Double> categoriaGananciaBruta = new HashMap<>();
        Map<String, Double> categoriaImpuesto = new HashMap<>();
        Map<String, Double> categoriaComision = new HashMap<>();
        Map<String, Double> categoriaGananciaNeta = new HashMap<>();

        try (BufferedReader lector = new BufferedReader(new FileReader(rutaArchivoEntrada.toFile()));
             BufferedWriter escritor = new BufferedWriter(new FileWriter(rutaArchivoSalida.toFile()))) {

            // Escribir cabecera CSV para el nuevo reporte
            escritor.write("ID,Nombre,Categoría,Precio Unitario,Cantidad Vendida,Subtotal Venta,Ganancia Bruta,Impuesto (7%),Comisión (5%),Ganancia Neta\n");

            String linea;
            int numeroLinea = 0; // Para rastrear números de línea e identificar líneas de resumen del archivo de entrada
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (numeroLinea == 1) {
                    continue; // Saltar la línea de cabecera del archivo de entrada
                }

                // Identificar y saltar las líneas de resumen del reporte de ganancias brutas (las 4 últimas)
                if (linea.contains(",,Electronica") || linea.contains(",,Accesorios") || linea.contains(",,Redes") || linea.contains(",,TOTALES")) {
                    continue;
                }

                String[] partes = linea.split(","); // Asumiendo valores separados por coma

                String idProducto, nombreProducto, categoriaProducto;
                double precioUnitario, cantidadVendida, subtotalVenta, gananciaBruta;

                // Formato esperado: ID,Nombre,Categoría,Precio Unitario,Cantidad Vendida,Subtotal Venta,Ganancia
                if (partes.length == 7) {
                    try {
                        idProducto = partes[0].trim();
                        nombreProducto = partes[1].trim();
                        categoriaProducto = partes[2].trim();
                        precioUnitario = Double.parseDouble(partes[3].trim());
                        cantidadVendida = Double.parseDouble(partes[4].trim()); // Leer como double para consistencia
                        subtotalVenta = Double.parseDouble(partes[5].trim());
                        gananciaBruta = Double.parseDouble(partes[6].trim());

                        double impuestoCalculado = gananciaBruta * TASA_IMPUESTO;
                        double comisionCalculada = gananciaBruta * TASA_COMISION;
                        double gananciaNetaCalculada = gananciaBruta - impuestoCalculado - comisionCalculada;

                        // Actualizar totales por categoría
                        String categoriaMinusculas = categoriaProducto.toLowerCase();
                        categoriaGananciaBruta.put(categoriaMinusculas, categoriaGananciaBruta.getOrDefault(categoriaMinusculas, 0.0) + gananciaBruta);
                        categoriaImpuesto.put(categoriaMinusculas, categoriaImpuesto.getOrDefault(categoriaMinusculas, 0.0) + impuestoCalculado);
                        categoriaComision.put(categoriaMinusculas, categoriaComision.getOrDefault(categoriaMinusculas, 0.0) + comisionCalculada);
                        categoriaGananciaNeta.put(categoriaMinusculas, categoriaGananciaNeta.getOrDefault(categoriaMinusculas, 0.0) + gananciaNetaCalculada);

                        // Escribir detalles del producto en el nuevo reporte CSV
                        escritor.write(String.format("%s,%s,%s,%.2f,%.0f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                                idProducto, nombreProducto, categoriaProducto, precioUnitario, cantidadVendida, subtotalVenta, gananciaBruta, impuestoCalculado, comisionCalculada, gananciaNetaCalculada));

                    } catch (NumberFormatException e) {
                        System.err.println("Error de formato numérico en la línea de ganancia bruta: " + linea + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Línea con formato incorrecto en el archivo de ganancias brutas (se esperaban 7 campos CSV): " + linea);
                }
            }

            // Escribir filas de resumen para ganancias netas por categoría
            escritor.write(String.format(",,Electronica,,,,,%.2f,%.2f,%.2f,%.2f%n",
                    categoriaGananciaBruta.getOrDefault("electronica", 0.0),
                    categoriaImpuesto.getOrDefault("electronica", 0.0),
                    categoriaComision.getOrDefault("electronica", 0.0),
                    categoriaGananciaNeta.getOrDefault("electronica", 0.0)));

            escritor.write(String.format(",,Accesorios,,,,,%.2f,%.2f,%.2f,%.2f%n",
                    categoriaGananciaBruta.getOrDefault("accesorios", 0.0),
                    categoriaImpuesto.getOrDefault("accesorios", 0.0),
                    categoriaComision.getOrDefault("accesorios", 0.0),
                    categoriaGananciaNeta.getOrDefault("accesorios", 0.0)));

            escritor.write(String.format(",,Redes,,,,,%.2f,%.2f,%.2f,%.2f%n",
                    categoriaGananciaBruta.getOrDefault("redes", 0.0),
                    categoriaImpuesto.getOrDefault("redes", 0.0),
                    categoriaComision.getOrDefault("redes", 0.0),
                    categoriaGananciaNeta.getOrDefault("redes", 0.0)));

            // Calcular totales generales
            double granTotalGananciaBruta = categoriaGananciaBruta.values().stream().mapToDouble(Double::doubleValue).sum();
            double granTotalImpuesto = categoriaImpuesto.values().stream().mapToDouble(Double::doubleValue).sum();
            double granTotalComision = categoriaComision.values().stream().mapToDouble(Double::doubleValue).sum();
            double granTotalGananciaNeta = categoriaGananciaNeta.values().stream().mapToDouble(Double::doubleValue).sum();

            escritor.write(String.format(",,TOTALES,,,,,%.2f,%.2f,%.2f,%.2f%n",
                    granTotalGananciaBruta, granTotalImpuesto, granTotalComision, granTotalGananciaNeta));

            // Imprimir resumen en la terminal
            System.out.println("\n--- Resumen de Ganancias Netas por Categoría (Terminal) ---");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("Electrónica:     Ganancia Neta = %.2f%n", categoriaGananciaNeta.getOrDefault("electronica", 0.0));
            System.out.printf("Accesorios:      Ganancia Neta = %.2f%n", categoriaGananciaNeta.getOrDefault("accesorios", 0.0));
            System.out.printf("Redes:           Ganancia Neta = %.2f%n", categoriaGananciaNeta.getOrDefault("redes", 0.0));
            System.out.println("-------------------------------------------------------------");
            System.out.printf("Total General de Ganancias Netas: %.2f%n", granTotalGananciaNeta);

            System.out.println("\nInforme de ganancias netas (CSV) generado exitosamente en: " + rutaArchivoSalida);

        } catch (IOException e) {
            System.err.println("Error de I/O al procesar archivos de ganancia neta: " + e.getMessage());
        }
    }
}