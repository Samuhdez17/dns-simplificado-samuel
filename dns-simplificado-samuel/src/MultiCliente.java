import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import excepciones.*;

public class MultiCliente implements Runnable {
    private final Socket cliente;
    private final HashMap<String, ArrayList<Registro>> registros;
    private static String rutaArchivo = "";
    private final int numCliente;

    public MultiCliente(Socket cliente, HashMap<String, ArrayList<Registro>> registros, String rutaArchivo) {
        this.cliente = cliente;
        this.registros = registros;
        this.rutaArchivo = rutaArchivo;
        this.numCliente = (int) (Math.random() * 1000);
    }

    @Override
    public void run() {
        try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true)
        ) {
            String mensaje;
            do {
                mensaje = entrada.readLine();
                if (mensaje == null) break;

                System.out.println("Cliente " + numCliente + ": " + mensaje);

                if (mensaje.equalsIgnoreCase("exit")) {
                    salida.println("Cerrando conexion...");
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

                } catch (
                        ComandoIncorrectoEx | ResultadoNoEncontrado |
                        ComandoLookupErroneo | ComandoListErroneo |
                        ComandoRegisterIncorrecto e
                ) {
                    salida.println(e.getMessage());
                }

            } while (!mensaje.equalsIgnoreCase("exit"));

        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente");

        } finally {
            try {
                cliente.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar cliente");
            }

            Main.eliminarCliente();
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

    private static void guardarRegistro(String[] registro) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo, true));
        bw.write(registro[0] + " " + registro[1] + " " + registro[2] + "\n");
        bw.close();
    }

    private static void agregarADiccionario(HashMap<String, ArrayList<Registro>> registros, String[] contenidoRegistro) {
        registros.putIfAbsent(contenidoRegistro[0], new ArrayList<>());

        ArrayList<Registro> registrosDelMismoDominio = registros.get(contenidoRegistro[0]);
        registrosDelMismoDominio.add(new Registro(contenidoRegistro));
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
}
