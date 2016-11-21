/*
 *  Role Game Character Creator
 *  Programa para crear fichas de personajes de rol en varios
 *  sistemas: Mundo de Tinieblas, NSD20, Malefic Time: Plenilunio,
 *  ...
 *  Permite además, guardar la ficha, editarla y enviarla por
 *  correo electrónico al director de partida.
 */
package Servidor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import javax.swing.JTextArea;

/**
 *
 * Los mensajes del ClienteThread seguiran la estructura: "ModelS -- nombre_de_funcion
 -- mensaje"
 *
 */
public class ModelS 
{
    private static final int n_cli = 1000;

    private ServidorThread servidor;
    private final int port = 6000; // CAMBIAR ESTO PORQUE NO SÉ QUE PUERTO ES EL DEL CLIENTE (PUERTO DIFERENTE PARA CADA CLIENTE?)
    private final JTextArea consola;
    private final int vecinos = 10;
    private final int iteraciones = 2;
    PrintStream out;

    /**
     * ****************************************************************
     * Constructor que crea el servidor y los clientes pero no inicializa
     *
     * @param consola
     * @throws java.net.UnknownHostException 
     ***************************************************************
     */
    public ModelS(JTextArea consola) throws UnknownHostException 
    {
        this.consola = consola; ///< Esto nos servirá para manejar la consola
        out = new PrintStream(new TextAreaOutputStream(consola));
        System.setOut(out);
        System.setErr(out);
        CreaServer();
    }

    /******************************************************************
     * Función que declara el server con un máximo de clientes indicado
     ******************************************************************/
    public void CreaServer() 
    {
        servidor = new ServidorThread(n_cli,vecinos,port,out, iteraciones); // Inicialmente que el servidor acepte únicamente 20 clientes (EN FASE DE PRUEBAS)
        System.out.println("Model -- CreaServer -- Servidor creado pero NO inicializado");
    }

    /**
     * ****************************************************************
     * Función que pone en funcionamiento al servidor
    * ***************************************************************
     */
    public void IniciaServer() 
    {
        servidor.start();
        System.out.println("Model -- CreaServer -- Servidor creado pero NO inicializado");
    }
    
    /**
     * Esta clase nos servirá para usarla de consola.
     * Todos los System.out.println que se hagan saldran
     * por el jtextfield correspondiente.
     */
    private static class TextAreaOutputStream extends OutputStream 
    {
        private final JTextArea textControl;

        public TextAreaOutputStream(JTextArea consola) 
        {
            textControl = consola;
        }

        @Override
        public void write(int i) throws IOException 
        {
             textControl.append( String.valueOf( ( char )i ) );
        }
    }
}
