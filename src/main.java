
import net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class main {

    public static void main(String[] args) {
        // Asegurar formato numérico consistente (ej. punto para decimales)
        Locale.setDefault(Locale.US);

        System.out.println("--- Inicio del Procesamiento de Reportes de Soluciones de Software ---");

        // Definiciones de rutas y nombres de archivos usados por las clases
        final String nombreDirectorioBase = "Lab2";
        final String nombreDirectorioGrupo = "Grupo_06"; // <--- ¡CAMBIA ESTO!
        final String directorioUsuario = System.getProperty("user.home");

        final Path rutaBase = Paths.get(directorioUsuario, nombreDirectorioBase);
        final Path rutaGrupo = Paths.get(rutaBase.toString(), nombreDirectorioGrupo);

        final String nombreArchivoEntradaEquipos = "equipos.csv";
        final String nombreArchivoSalidaGananciasBrutas = "Reporte_Ganancias_Brutas.csv";
        final String nombreArchivoSalidaGananciasNetas = "Reporte_Ganancias_Netas.csv";
        final String nombreDirectorioHistorico = "Historico";

        // 1. Gestionar Directorios
        GestorDirectorios gestorDirectorios = new GestorDirectorios(rutaGrupo, nombreArchivoEntradaEquipos);
        if (!gestorDirectorios.CrearYVerificarDirectorios()) {
            System.err.println("No se pudieron crear o verificar los directorios necesarios. Terminando el programa.");
            return;
        }
        System.out.println("Directorios verificados/creados correctamente en: " + rutaGrupo);

        // 2. Procesar Ganancia Bruta
        ProcesadorGananciaBruta procesadorBruta = new ProcesadorGananciaBruta(
                rutaGrupo, nombreArchivoEntradaEquipos, nombreArchivoSalidaGananciasBrutas
        );
        procesadorBruta.ProcesarDatosVentas();

        // 3. Procesar Ganancia Neta
        ProcesadorGananciaNeta procesadorNeta = new ProcesadorGananciaNeta(
                rutaGrupo, nombreArchivoSalidaGananciasBrutas, nombreArchivoSalidaGananciasNetas
        );
        procesadorNeta.GenerarReporteGananciaNeta();

        // 4. Gestionar Histórico
        GestorHistorico gestorHistorico = new GestorHistorico(
                rutaGrupo, nombreDirectorioHistorico, nombreArchivoSalidaGananciasNetas
        );
        gestorHistorico.ArchivarReporte();

        System.out.println("\n--- Procesamiento Finalizado ---");
    }
}