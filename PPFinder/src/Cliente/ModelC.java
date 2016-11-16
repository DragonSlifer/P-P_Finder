/*
 *  Role Game Character Creator
 *  Programa para crear fichas de personajes de rol en varios
 *  sistemas: Mundo de Tinieblas, NSD20, Malefic Time: Plenilunio,
 *  ...
 *  Permite además, guardar la ficha, editarla y enviarla por
 *  correo electrónico al director de partida.
 */
package Cliente;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.swing.JTextArea;

/**
 *
 * Los mensajes del Modelo seguiran la estructura: "ModelC -- nombre_de_funcion
 -- mensaje"
 *
 */
public class ModelC {

    private static final int n_cli = 20;

    private Vector<ClienteThread> v_cli;
    private InetAddress ip; // IP LOCAL AL ORDENADOR
    private int port = 6000; // CAMBIAR ESTO PORQUE NO SÉ QUE PUERTO ES EL DEL CLIENTE (PUERTO DIFERENTE PARA CADA CLIENTE?)
    private JTextArea consola;
    private int vecinos = 10;
    private int iteraciones = 5;
    PrintStream out;

    /**
     * ****************************************************************
     * Constructor que crea el servidor y los clientes pero no inicializa
     *
     * @param consola
     * @throws java.net.UnknownHostException 
     ***************************************************************
     */
    public ModelC(JTextArea consola) throws UnknownHostException {
        this.consola = consola; ///< Esto nos servirá para manejar la consola
        out = new PrintStream(new TextAreaOutputStream(consola));
        v_cli = new Vector<>();
        System.setOut(out);
        System.setErr(out);
        CreaClientes();
    }


    /******************************************************************
     * Función que declara los clientes pero no los inicializa
     *
     * @throws java.net.UnknownHostException
     ******************************************************************/
    public void CreaClientes() throws UnknownHostException {

        try {

            ip = InetAddress.getLocalHost();

            for (int i = 0; i < n_cli; i++) {

                ClienteThread cli_aux = new ClienteThread(ip.getHostAddress(), port, vecinos, out, iteraciones);
                v_cli.add(cli_aux);
            }

            System.out.println("Model -- CreaClientes -- Clientes creados pero NO inicializados");

        } catch (UnknownHostException e) {
            System.out.println("Model -- CreaClientes -- Error al obtener la IP local"); // puede ser que el ordenador no esté conectado y no tenga IP asignada (?)
            System.err.println("Cliente -- CreaClientes -- " + e.getMessage());
            System.exit(1);
        }

    }

    /**
     * ****************************************************************
     * Función que pone en funcionamiento a todos los clientes
    * ***************************************************************
     */
    public void iniciaClientes() {

        for (int i = 0; i < n_cli; i++) {

            v_cli.elementAt(i).start();

        }

        System.out.println("Model -- CreaClientes -- Clientes inicializados");

    }
    /**
     * Esta clase nos servirá para usarla de consola.
     * Todos los System.out.println que se hagan saldran
     * por el jtextfield correspondiente.
     */
    private static class TextAreaOutputStream extends OutputStream {

        private JTextArea textControl;

        public TextAreaOutputStream(JTextArea consola) {
            textControl = consola;
        }

        @Override
        public void write(int i) throws IOException {
             textControl.append( String.valueOf( ( char )i ) );
        }
    }

}
