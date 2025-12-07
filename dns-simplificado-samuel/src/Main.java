import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private final static String RUTA_TXT = "src/dominios";
    private final static int PUERTO = 5000;
    private static int numClientes = 0;
    private final static int CLIENTES_MAX = 5;

    public static void main(String[] args) {
        HashMap<String, ArrayList<Registro>> registros = leerFicheroRegistros();

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();

                synchronized (Main.class) {
                    if (numClientes >= CLIENTES_MAX) {
                        PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true);

                        salida.println("500 Cliente rechazado: maximo alcanzado");
                        cliente.close();
                        continue;
                    }

                    agregarCliente();
                }

                System.out.println("Cliente conectado desde: " + cliente.getInetAddress());
                Thread hiloCliente = new Thread(new MultiCliente(cliente, registros, RUTA_TXT));
                hiloCliente.start();
            }

        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    private static HashMap<String, ArrayList<Registro>> leerFicheroRegistros() {
        File ficheroDominios = new File(RUTA_TXT);
        HashMap<String, ArrayList<Registro>> registros = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ficheroDominios))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] contenidoRegistro = linea.split(" ");
                agregarADiccionario(registros, contenidoRegistro);
            }

        } catch (IOException e) {
            System.err.println("Error al leer el fichero");
            throw new RuntimeException(e);
        }
        return registros;
    }

    private static void agregarADiccionario(HashMap<String, ArrayList<Registro>> registros, String[] contenidoRegistro) {
        registros.putIfAbsent(contenidoRegistro[0], new ArrayList<>());

        ArrayList<Registro> registrosDelMismoDominio = registros.get(contenidoRegistro[0]);
        registrosDelMismoDominio.add(new Registro(contenidoRegistro));
    }

    public static synchronized void agregarCliente() {
        numClientes++;
    }

    public static synchronized void eliminarCliente() {
        numClientes--;
    }
}
