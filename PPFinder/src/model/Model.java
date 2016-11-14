/*
 *  Role Game Character Creator
 *  Programa para crear fichas de personajes de rol en varios
 *  sistemas: Mundo de Tinieblas, NSD20, Malefic Time: Plenilunio,
 *  ...
 *  Permite además, guardar la ficha, editarla y enviarla por
 *  correo electrónico al director de partida.
 */
package model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.swing.JTextArea;

/**
 *
 * Los mensajes del Modelo seguiran la estructura: "Model -- nombre_de_funcion
 * -- mensaje"
 *
 */
public class Model {

    private static final int n_cli = 150;

    private Vector<Cliente> v_cli;
    private Servidor servidor;
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
    public Model(JTextArea consola) throws UnknownHostException {
        this.consola = consola; ///< Esto nos servirá para manejar la consola
        out = new PrintStream(new TextAreaOutputStream(consola));
        v_cli = new Vector<>();
        System.setOut(out);
        System.setErr(out);
        CreaServer();
        CreaClientes();
    }

    /******************************************************************
     * Función que declara el server con un máximo de clientes indicado
     ******************************************************************/
    public void CreaServer() {

        servidor = new Servidor(n_cli,vecinos,port,out, iteraciones); // Inicialmente que el servidor acepte únicamente 20 clientes (EN FASE DE PRUEBAS)
        System.out.println("Model -- CreaServer -- Servidor creado pero NO inicializado");

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

                Cliente cli_aux = new Cliente(ip.getHostAddress(), port, vecinos, out, iteraciones);
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
     * Función que pone en funcionamiento al servidor
    * ***************************************************************
     */
    public void IniciaServer() {

        servidor.start();
        System.out.println("Model -- CreaServer -- Servidor creado pero NO inicializado");

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
