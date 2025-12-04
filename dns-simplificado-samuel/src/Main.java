package Ejercicios.E009DNS;

import Ejercicios.E009DNS.excepciones.ComandoIncorrectoEx;
import Ejercicios.E009DNS.excepciones.ComandoListErroneo;
import Ejercicios.E009DNS.excepciones.ComandoLookupErroneo;
import Ejercicios.E009DNS.excepciones.ResultadoNoEncontrado;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Main {
    private final static String RUTA_TXT = "dominios";
    private final static int PUERTO = 5000;

    public static void main(String[] args) {
        File ficheroDominios = new File(RUTA_TXT);
        HashMap<String, ArrayList<Registro>> registros = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ficheroDominios))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] contenidoRegistro = linea.split(" ");

                registros.putIfAbsent(contenidoRegistro[0], new ArrayList<>());

                ArrayList<Registro> registrosDelMismoDominio = registros.get(contenidoRegistro[0]);
                registrosDelMismoDominio.add(new Registro(contenidoRegistro));
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

                        if (comando[0].equalsIgnoreCase("lookup")) {
                            if (comando.length != 3)
                                throw new ComandoLookupErroneo();

                            ArrayList<Registro> registro = registros.get(comando[2]);
                            if (registro == null)
                                throw new ResultadoNoEncontrado();

                            int contador = 0;
                            for (Registro r : registro) {
                                if (comando[1].equalsIgnoreCase(r.getTipo()))
                                    salida.println("200 " + r.getValor());
                                else
                                    contador++;
                            }

                            if (contador == registro.size())
                                throw new ResultadoNoEncontrado();

                        } else if (comando[0].equalsIgnoreCase("list")) {
                            if (comando.length > 1)
                                throw new ComandoListErroneo();

                            else {
                                salida.println("150 Inicio listado");

                                for (String clave : registros.keySet()) {
                                    ArrayList<Registro> registro = registros.get(clave);
                                    Collections.sort(registro);

                                    for (Registro r : registro)
                                        salida.println(r);
                                }

                                salida.println("226 Fin listado");
                            }
                        } else
                            throw new ComandoIncorrectoEx();

                    } catch (ComandoIncorrectoEx | ResultadoNoEncontrado | ComandoLookupErroneo | ComandoListErroneo e) {
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
}
