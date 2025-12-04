import excepciones.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Main {
    private final static String RUTA_TXT = "src/dominios";
    private final static int PUERTO = 5000;

    public static void main(String[] args) {
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

        ServerSocket servidor = null;
        Socket cliente = null;
        String mensaje = "";
        PrintWriter salida = null;

        do {
            try {
                servidor = new ServerSocket(PUERTO);
                System.out.println("Servidor iniciado en el puerto " + PUERTO);
                cliente = servidor.accept();
                System.out.println("Cliente conectado desde: " + cliente.getInetAddress());

                BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                salida = new PrintWriter(cliente.getOutputStream(), true);

                do {
                    mensaje = entrada.readLine();
                    System.out.println("Cliente: " + mensaje);
                    if (mensaje.equalsIgnoreCase("exit") || mensaje.equalsIgnoreCase("exit -f")) {
                        System.out.println("Cerrando conexion...");
                        cliente.close();
                        servidor.close();
                        break;
                    }

                    try {
                        String[] comando = mensaje.split(" ");

                        if (comando[0].equalsIgnoreCase("lookup"))
                            comandoLookup(comando, registros, salida);

                        else if (comando[0].equalsIgnoreCase("list"))
                            comandoList(comando, registros, salida);

                        else if (comando[0].equalsIgnoreCase("register"))
                            comandoRegister(comando, registros, salida);

                        else if (comando[0].equalsIgnoreCase("help"))
                            mostrarComandos(salida);
                        else
                            throw new ComandoIncorrectoEx();

                    } catch (ComandoIncorrectoEx | ResultadoNoEncontrado | ComandoLookupErroneo | ComandoListErroneo | ComandoRegisterIncorrecto e) {
                        salida.println(e.getMessage());
                    }

                } while (!mensaje.equalsIgnoreCase("exit") && !mensaje.equalsIgnoreCase("exit -f"));

            } catch (IOException e) {
                if (salida != null)
                    salida.println("500 Error en el servidor");

                throw new RuntimeException(e);
            }
        } while (!mensaje.equalsIgnoreCase("exit -f"));

        System.out.println("Cerrando servidor...");
        try {
            if (servidor != null) servidor.close();
        }  catch (IOException e) {
            System.err.println("Error al cerrar servidor");
        }
    }

    private static void comandoLookup(String[] comando, HashMap<String, ArrayList<Registro>> registros, PrintWriter salida) {
        if (comando.length != 3)
            throw new ComandoLookupErroneo();

        ArrayList<Registro> registro = registros.get(comando[2]);
        if (registro == null)
            throw new ComandoLookupErroneo();

        int contador = 0;
        for (Registro r : registro) {
            if (comando[1].equalsIgnoreCase(r.getTipo()))
                salida.println("200 " + r.getValor());
            else
                contador++;
        }

        if (contador == registro.size())
            throw new ResultadoNoEncontrado();
    }

    private static void comandoList(String[] comando, HashMap<String, ArrayList<Registro>> registros, PrintWriter salida) {
        if (comando.length > 1)
            throw new ComandoListErroneo();

        else {
            salida.println("150 Inicio listado");

            List<String> clavesOrdenadas = new ArrayList<>(registros.keySet());
            Collections.sort(clavesOrdenadas);

            for (String clave : clavesOrdenadas) {
                ArrayList<Registro> registro = registros.get(clave);
                Collections.sort(registro);

                for (Registro r : registro)
                    salida.println(r);
            }

            salida.println("226 Fin listado");
        }
    }

    private static void comandoRegister(String[] comando, HashMap<String, ArrayList<Registro>> registros, PrintWriter salida) throws IOException {
        if (comando.length != 4)
            throw new ComandoRegisterIncorrecto();

        if (
                comando[2].equalsIgnoreCase("A") ||
                        comando[2].equalsIgnoreCase("MX") ||
                        comando[2].equalsIgnoreCase("CNAME")
        ) {
            String[] registro = { comando[1], comando[2], comando[3] };
            guardarRegistro(registro);
            agregarADiccionario(registros, registro);
            salida.println("200 Registro añadido");

        } else throw new ComandoRegisterIncorrecto();
    }

    private static void mostrarComandos(PrintWriter salida) {
        String mensajeSalida = """
                200 COMANDOS DEL DNS:
                150 Inicio listado
                LOOKUP <TIPO> <DOMINIO>
                El tipo puede ser:
                - A: Para obtener el valor del dominio
                - MX: Para obtener el mail exchanger
                - CNAME: Para obtener el alias
                
                LIST
                Para listar los dominios guardados
                
                REGISTER <DOMINIO> <TIPO> <VALOR>
                - DOMINIO: Nombre del dominio a registrar
                - TIPO: Tipo del valor del dominio
                - VALOR: Valor del dominio a registrar
                
                EXIT
                Para salir del servidor DNS
                226 Fin listado
                """;

        salida.println(mensajeSalida);
    }

    private static void guardarRegistro(String[] registro) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_TXT, true));
        bw.write(registro[0] + " " + registro[1] + " " + registro[2] + "\n");
        bw.close();
    }

    private static void agregarADiccionario(HashMap<String, ArrayList<Registro>> registros, String[] contenidoRegistro) {
        registros.putIfAbsent(contenidoRegistro[0], new ArrayList<>());

        ArrayList<Registro> registrosDelMismoDominio = registros.get(contenidoRegistro[0]);
        registrosDelMismoDominio.add(new Registro(contenidoRegistro));
    }
}
