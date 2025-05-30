package net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcesadorGananciaBruta {

    // Atributos privados encapsulados en esta clase
    private static final double PorcentajeGananciaElectronica = 0.20; // 20%
    private static final double PorcentajeGananciaAccesorios = 0.10;  // 10%
    private static final double PorcentajeGananciaRedes = 0.15;     // 15%

    private Path rutaDirectorioGrupo;
    private String nombreArchivoEntrada;
    private String nombreArchivoSalida;

    public ProcesadorGananciaBruta(Path rutaDirectorioGrupo, String nombreArchivoEntrada, String nombreArchivoSalida) {
        this.rutaDirectorioGrupo = rutaDirectorioGrupo;
        this.nombreArchivoEntrada = nombreArchivoEntrada;
        this.nombreArchivoSalida = nombreArchivoSalida;
    }


    public void ProcesarDatosVentas() {
        Path rutaArchivoEntrada = Paths.get(rutaDirectorioGrupo.toString(), nombreArchivoEntrada);
        Path rutaArchivoSalida = Paths.get(rutaDirectorioGrupo.toString(), nombreArchivoSalida);

        double totalVentasElectronica = 0;
        double totalGananciaElectronica = 0;
        double totalVentasAccesorios = 0;
        double totalGananciaAccesorios = 0;
        double totalVentasRedes = 0;
        double totalGananciaRedes = 0;

        try (BufferedReader lector = new BufferedReader(new FileReader(rutaArchivoEntrada.toFile()));
             BufferedWriter escritor = new BufferedWriter(new FileWriter(rutaArchivoSalida.toFile()))) {

            escritor.write("ID,Nombre,Categoría,Precio Unitario,Cantidad Vendida,Subtotal Venta,Ganancia\n");

            String linea;
            boolean primeraLinea = true; // Para saltar la fila de cabecera en equipos.csv
            while ((linea = lector.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue; // Saltar la primera línea (cabecera)
                }

                String[] partes = linea.split(","); // Asumiendo valores separados por coma

                String idProducto, nombreProducto, categoriaProducto;
                double precioUnitario;
                int cantidadVendida;

                if (partes.length == 5) { // Formato esperado: id,nombre,categoria,precio,cantidad
                    try {
                        idProducto = partes[0].trim();
                        nombreProducto = partes[1].trim();
                        categoriaProducto = partes[2].trim();
                        precioUnitario = Double.parseDouble(partes[3].trim());
                        cantidadVendida = Integer.parseInt(partes[4].trim());

                        double subtotalVenta = precioUnitario * cantidadVendida;
                        double ganancia = 0;

                        String categoriaMinusculas = categoriaProducto.toLowerCase();

                        switch (categoriaMinusculas) {
                            case "electronica":
                                ganancia = subtotalVenta * PorcentajeGananciaElectronica;
                                totalVentasElectronica += subtotalVenta;
                                totalGananciaElectronica += ganancia;
                                break;
                            case "accesorios":
                                ganancia = subtotalVenta * PorcentajeGananciaAccesorios;
                                totalVentasAccesorios += subtotalVenta;
                                totalGananciaAccesorios += ganancia;
                                break;
                            case "redes":
                                ganancia = subtotalVenta * PorcentajeGananciaRedes;
                                totalVentasRedes += subtotalVenta;
                                totalGananciaRedes += ganancia;
                                break;
                            default:
                                System.out.println("Categoría desconocida encontrada: " + categoriaProducto + " para el producto: " + nombreProducto);
                                // Opcionalmente, asignar una ganancia por defecto o saltar
                                break;
                        }

                        // Escribir detalles del producto en el reporte CSV
                        escritor.write(String.format("%s,%s,%s,%.2f,%d,%.2f,%.2f%n",
                                idProducto, nombreProducto, categoriaProducto, precioUnitario, cantidadVendida, subtotalVenta, ganancia));

                    } catch (NumberFormatException e) {
                        System.err.println("Error de formato numérico en la línea: " + linea + " - " + e.getMessage());
                    }
                } else {
                    if (!linea.trim().isEmpty()){
                        System.err.println("Línea con formato incorrecto (se esperaban 5 campos CSV): " + linea);
                    }
                }
            }

            // Escribir filas de resumen al archivo CSV
            escritor.write(String.format(",,Electronica,,,%.2f,%.2f%n", totalVentasElectronica, totalGananciaElectronica));
            escritor.write(String.format(",,Accesorios,,,%.2f,%.2f%n", totalVentasAccesorios, totalGananciaAccesorios));
            escritor.write(String.format(",,Redes,,,%.2f,%.2f%n", totalVentasRedes, totalGananciaRedes));

            double granTotalVentas = totalVentasElectronica + totalVentasAccesorios + totalVentasRedes;
            double granTotalGanancia = totalGananciaElectronica + totalGananciaAccesorios + totalGananciaRedes;
            escritor.write(String.format(",,TOTALES,,,%.2f,%.2f%n", granTotalVentas, granTotalGanancia));

            // Imprimir resumen en la terminal
            System.out.println("\n--- Resumen de Ventas y Ganancias Brutas por Categoría (Terminal) ---");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("Electrónica:     Subtotal Venta = %.2f\tGanancia = %.2f%n", totalVentasElectronica, totalGananciaElectronica);
            System.out.printf("Accesorios:      Subtotal Venta = %.2f\tGanancia = %.2f%n", totalVentasAccesorios, totalGananciaAccesorios);
            System.out.printf("Redes:           Subtotal Venta = %.2f\tGanancia = %.2f%n", totalVentasRedes, totalGananciaRedes);
            System.out.println("-------------------------------------------------------------");
            System.out.printf("Total General de Ventas:    %.2f%n", granTotalVentas);
            System.out.printf("Total General de Ganancias: %.2f%n", granTotalGanancia);

            System.out.println("\nInforme de ganancias brutas (CSV) generado exitosamente en: " + rutaArchivoSalida);

        } catch (IOException e) {
            System.err.println("Error de I/O al procesar archivos de ganancia bruta: " + e.getMessage());
        }
    }
}