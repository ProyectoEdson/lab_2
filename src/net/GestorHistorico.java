package net;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class GestorHistorico {

    // Atributos privados encapsulados en esta clase
    private final Path rutaDirectorioBase; // Lab2
    private final Path rutaDirectorioGrupo; // Lab2/Grupo_06
    private String nombreDirectorioHistorico;
    private Path rutaDirectorioHistorico;
    private String nombreArchivoOrigen;

    public GestorHistorico(Path rutaDirectorioBase, String nombreDirectorioHistorico, String nombreArchivoOrigen, Path rutaDirectorioGrupo) {
        this.rutaDirectorioBase = rutaDirectorioBase;
        this.rutaDirectorioGrupo = rutaDirectorioGrupo;
        this.nombreDirectorioHistorico = nombreDirectorioHistorico;
        this.rutaDirectorioHistorico = Paths.get(rutaDirectorioBase.toString(), nombreDirectorioHistorico);
        this.nombreArchivoOrigen = nombreArchivoOrigen;
    }

    /**
     * Procesa el archivado del reporte de ganancias netas en la carpeta histórica.
     * Incluye la creación del directorio histórico, manejo de duplicados y listado de archivos.
     */
    public void ArchivarReporte() {
        System.out.println("\n--- Procesando el Histórico de Reportes ---");

        // 1. Crear directorio Historico dentro de Lab2
        boolean directorioHistoricoCreado = CrearDirectorioHistorico();
        if (!directorioHistoricoCreado) {
            System.err.println("No se pudo crear la carpeta '" + nombreDirectorioHistorico + "'. No se puede archivar el reporte.");
            return;
        }
        System.out.println("Carpeta '" + nombreDirectorioHistorico + "' creada o ya existe en: " + rutaDirectorioHistorico);

        // 2. Generar nombre de archivo histórico y rutas
        String nombreArchivoHistorico = GenerarNombreArchivoHistorico();
        Path rutaArchivoOrigen = Paths.get(rutaDirectorioGrupo.toString(), nombreArchivoOrigen);
        Path rutaArchivoDestino = Paths.get(rutaDirectorioHistorico.toString(), nombreArchivoHistorico);

        // Verificar si el archivo de origen existe antes de intentar copiar
        if (!Files.exists(rutaArchivoOrigen)) {
            System.err.println("Error en la manipulación de archivos: El archivo de origen '" + nombreArchivoOrigen + "' no se encontró en " + rutaArchivoOrigen);
            return;
        }

        // 3. Manejar archivo existente y copiar el nuevo
        try {
            boolean archivoManejado = ManejarYCopiarArchivo(rutaArchivoOrigen, rutaArchivoDestino);
            if (archivoManejado) {
                System.out.println("El nuevo archivo histórico generado es: " + nombreArchivoHistorico);
            } else {
                System.err.println("No se pudo completar la operación de copia del archivo histórico.");
            }
        } catch (IOException e) {
            System.err.println("Error en la manipulación de archivos al copiar/renombrar: " + e.getMessage());
        }

        // 4. Listar todos los archivos en la carpeta Historico
        System.out.println("\n--- Listado de Archivos en la Carpeta '" + nombreDirectorioHistorico + "' ---");
        ListarArchivos();
    }

    /**
     * Crea el directorio Historico si no existe.
     * @return true si el directorio existe o fue creado exitosamente, false de lo contrario.
     */
    private boolean CrearDirectorioHistorico() {
        try {
            Files.createDirectories(rutaDirectorioHistorico);
            return true;
        } catch (IOException e) {
            System.err.println("Error al crear el directorio Historico: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera el nombre del archivo histórico basado en el mes y año actual.
     * Formato: Historico_MM-YYYY.csv
     * @return El nombre del archivo generado.
     */
    private String GenerarNombreArchivoHistorico() {
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("MM-yyyy");
        return "Historico_" + fechaActual.format(formateador) + ".csv";
    }

    /**
     * Maneja la existencia del archivo histórico de destino (lo renombra si es duplicado)
     * y luego copia el archivo de origen.
     * @param rutaArchivoOrigen La ruta al archivo de origen (Reporte_Ganancias_Netas.csv).
     * @param rutaArchivoDestino La ruta al archivo histórico que se intenta crear.
     * @return true si el archivo fue copiado exitosamente, false de lo contrario.
     * @throws IOException Si ocurre un error de I/O durante las operaciones de archivo.
     */
    private boolean ManejarYCopiarArchivo(Path rutaArchivoOrigen, Path rutaArchivoDestino) throws IOException {
        File archivoDestino = rutaArchivoDestino.toFile();

        if (archivoDestino.exists()) {
            System.out.println("El archivo histórico '" + archivoDestino.getName() + "' ya existe.");
            // Renombrar el archivo existente
            String nombreArchivoDuplicado = archivoDestino.getName().replace(".csv", "_duplicado.csv");
            File archivoDuplicado = new File(archivoDestino.getParent(), nombreArchivoDuplicado);

            boolean renombrado = archivoDestino.renameTo(archivoDuplicado);
            if (renombrado) {
                System.out.println("Archivo existente renombrado a: " + archivoDuplicado.getName());
            } else {
                System.err.println("Error: No se pudo renombrar el archivo existente '" + archivoDestino.getName() + "'.");
                // Continuar intentando copiar, pero podría fallar o sobrescribir si el renombramiento falló
            }
        } else {
            System.out.println("El archivo histórico '" + archivoDestino.getName() + "' no existe. Copiando...");
        }

        // Copiar el nuevo archivo
        Files.copy(rutaArchivoOrigen, rutaArchivoDestino, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Archivo '" + nombreArchivoOrigen + "' copiado exitosamente como '" + rutaArchivoDestino.getFileName() + "'.");
        return true;
    }

    /**
     * Lista todos los archivos en el directorio Historico, mostrando su nombre, tamaño y fecha de última modificación.
     */
    private void ListarArchivos() {
        if (!Files.exists(rutaDirectorioHistorico)) {
            System.out.println("La carpeta '" + nombreDirectorioHistorico + "' no existe.");
            return;
        }

        try (Stream<Path> archivos = Files.list(rutaDirectorioHistorico)) {
            archivos.forEach(archivo -> {
                try {
                    BasicFileAttributes atributos = Files.readAttributes(archivo, BasicFileAttributes.class);
                    System.out.printf("- Nombre: %s, Tamaño: %d bytes, Última Modificación: %s%n",
                            archivo.getFileName(), atributos.size(), atributos.lastModifiedTime());
                } catch (IOException e) {
                    System.err.println("Error al obtener atributos del archivo " + archivo.getFileName() + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("Error al listar archivos en la carpeta Historico: " + e.getMessage());
        }
    }
}