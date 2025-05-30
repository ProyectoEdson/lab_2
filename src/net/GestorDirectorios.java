package net;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GestorDirectorios {

    // Atributos privados encapsulados en esta clase
    private Path rutaDirectorioGrupo;
    private String nombreArchivoEntrada;

    public GestorDirectorios(Path rutaDirectorioGrupo, String nombreArchivoEntrada) {
        this.rutaDirectorioGrupo = rutaDirectorioGrupo;
        this.nombreArchivoEntrada = nombreArchivoEntrada;
    }


    public boolean CrearYVerificarDirectorios() {
        try {
            Files.createDirectories(rutaDirectorioGrupo);

            File archivoEquipos = new File(rutaDirectorioGrupo.toFile(), nombreArchivoEntrada);
            if (archivoEquipos.createNewFile()) {
                System.out.println("Archivo '" + nombreArchivoEntrada + "' vacío creado en " + rutaDirectorioGrupo + ". Por favor, reemplácelo con el archivo CSV proporcionado que contiene 120 registros.");
            } else {
                System.out.println("Archivo '" + nombreArchivoEntrada + "' ya existe en " + rutaDirectorioGrupo + ". Asegúrese de que contiene los 120 registros (formato CSV).");
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error al crear o verificar directorios/archivos: " + e.getMessage());
            return false;
        }
    }
}