package Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

public class ClienteThread extends Thread
{
    private final INFO info;              ///< Esta clase guardará la información del cliente en cuestion
    private Vector<INFO> ubi;
    int puerto, vecinos, id, iteraciones;
    String ip;
    Socket socket;
    DatagramSocket socketUDP;
    PrintStream out;
    double latitud = 0, longitud = 0;
    long time_start, time_end, time_total, tiempo_acumulado = 0, tiempo_medio;

    /******************************************************************
     * Constructor de la clase cliente al cual se le pasa el puerto
     * y la direccion ip del servidor. Ademas se le pasa la salida de
     * consola.
     *
     * @param ip         Ip del servidor
     * @param puerto     Puerto del Servidor
     * @param vecinos
     * @param out        Salida de consola
     * @param iteraciones
     ****************************************************************/
    public ClienteThread(String ip, int puerto, int vecinos, PrintStream out, int iteraciones) 
    {
        info = new INFO();
        this.puerto = puerto;
        this.ip = ip;
        this.vecinos = vecinos;
        this.iteraciones = iteraciones;
        System.setOut(out);
        System.setErr(out);
        actualizarCoordenadas();
    }
    
    /******************************************************************
    * Envia una petición de conexión al servidor 
    * ****************************************************************/
    private void peticionConexion()
    {
        try 
        {
            socket = new Socket(ip, puerto);
        } 
        
        catch (IOException ex) 
        {
            Logger.getLogger(ClienteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /******************************************************************
    * Espera a que el servidor le envie la peticion de inicio de
    * comunicación
    * ****************************************************************/
    private void establecerConexion()
    {
        String respuesta;
        PrintStream p;
        BufferedReader b;
        int index;
        
        try 
        {
            p = new PrintStream(socket.getOutputStream());
            b = new BufferedReader ( new InputStreamReader ( socket.getInputStream() ) );
            
            respuesta = b.readLine();
            
            index = respuesta.indexOf('s');
            
            id = Integer.parseInt(respuesta.substring(0, index));
            
            vecinos = Integer.parseInt(respuesta.substring(5 + index, respuesta.length()));
            
            p.close();
            b.close();
            socket.close();
            
            System.out.println("CLIENTE ----> El cliente " + id + " inicia comunicacion");
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(ClienteThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    /******************************************************************
    * El cliente envia al servidor sus coordenadas
    * ****************************************************************/
    private void enviarCoordenadas() throws UnknownHostException
    {
        String mensaje;
        DatagramPacket paquete;
        InetAddress address;
        byte[] mensaje_bytes;
        
        try 
        {
            socketUDP = new DatagramSocket();
            ///< Esto manda la localización
            //address = InetAddress.getByName("localhost");
            address = InetAddress.getByName("37.133.216.11");
            DecimalFormat decimales = new DecimalFormat("0.0000");
            mensaje = id + "->" + decimales.format(latitud) + " / " + decimales.format(longitud);
            mensaje_bytes = mensaje.getBytes();
            paquete = new DatagramPacket(mensaje_bytes, mensaje.length(), address, puerto);

            socketUDP.send(paquete);                ///< Envio paquete
            System.out.println("CLIENTE ----> El cliente " + id + " envia sus coordenadas");
        }
        catch (IOException e) 
        {
            System.err.println("Cliente -- enviarCoordenadas -- "  + e.getMessage());
            System.exit(1);
        }
    }
    
    private void recibirCoordenadas()
    {
        byte[] mensaje_bytes;
        DatagramPacket servPaquete, paquete;
        int contador = 0;
        InetAddress address;
        String mensaje;
        Vector<InetAddress> vec1 = new Vector<InetAddress>();
        
        while(contador < vecinos - 1)
        {
            try 
            {
                mensaje_bytes = new byte[256];
                servPaquete = new DatagramPacket(mensaje_bytes, 256);
                socketUDP.receive(servPaquete);
                contador++;
                mensaje = new String(mensaje_bytes).trim();
                
                System.out.println("CLIENTE ----> El cliente " + id + " recibe las coordenadas " + mensaje);
                
                /*address = servPaquete.getAddress();
                mensaje = id + "-> recibido";
                mensaje_bytes = mensaje.getBytes();
                paquete = new DatagramPacket(mensaje_bytes, mensaje.length(), address, puerto);
                
                socketUDP.send(paquete);*/
                vec1.add(servPaquete.getAddress());
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(ClienteThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for(int i = 0; i < vec1.size(); i++)
        {
            try 
            {
                address = vec1.get(i);
                mensaje = id + "-> recibido";
                mensaje_bytes = mensaje.getBytes();
                paquete = new DatagramPacket(mensaje_bytes, mensaje.length(), address, puerto);
                
                socketUDP.send(paquete);
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(ClienteThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /******************************************************************
    * El cliente esperara a recibir la confirmación de todos sus vecinos
    * ****************************************************************/
    private void recibirConfirmaciones()
    {
        byte[] mensaje_bytes;
        DatagramPacket servPaquete;
        boolean fin = false;
        int contador = 0;
        String mensaje;
        boolean to = false;
        
        try
        {
            while(contador != vecinos - 1)
            {
                socketUDP.setSoTimeout(20000);
                mensaje_bytes = new byte[256];
                servPaquete = new DatagramPacket(mensaje_bytes, 256);
                try
                {
                socketUDP.receive(servPaquete);
                } catch (SocketTimeoutException e) 
                {
                        System.err.println(e.getMessage());
                    }
                 // Lo formateamos
                mensaje = new String(mensaje_bytes).trim();

                /*if(mensaje.contains("fin"))
                {     
                    fin = true;
                }*/

                contador++;
            }
        }
        catch (IOException e) 
        {
            if (!to) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
        
        System.out.print("CLIENTE ----> El cliente " + id + " ha recibido " + (contador) + " confirmaciones\n");
    }
    
    /******************************************************************
    * Se encarga de crear los nuevos valor de las coordenadas en cada
    * iteracion
    * ****************************************************************/
    private void actualizarCoordenadas()
    {
        Random rnd = new Random();
        
        longitud = rnd.nextDouble() * 180 + 0;
        latitud = rnd.nextDouble() * 180 + 0;
    }
    
    /******************************************************************
    * Se encarga de enviar el tiempo medio de ejecución del cliente al 
    * servidor
    * ****************************************************************/
    private void enviarTiempos()
    {
        byte[] mensaje_bytes;
        DatagramPacket paquete;
        InetAddress address;
        String mensaje;
        
        tiempo_medio = tiempo_acumulado / iteraciones;
        
        try 
        {
            //address = InetAddress.getByName("localhost");
            address = InetAddress.getByName("37.133.216.11");
            mensaje = id + "->" + tiempo_medio;
            
            mensaje_bytes = mensaje.getBytes();
            paquete = new DatagramPacket(mensaje_bytes, mensaje.length(), address, puerto);

            socketUDP.send(paquete);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(ClienteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run()
    {       
        try 
        {
            peticionConexion(); // Envio mediante TCP inicial y///< En caso de que la Informacion haya sido actualizada, la reenvi de actualizacion
            establecerConexion(); //Esta funcion esperara el mensaje del servidor confirmando el inciio de la comunicación
            
            for(int i = 0; i < iteraciones; i++)
            {
                time_start = System.currentTimeMillis();
                enviarCoordenadas(); // Envio mediante UDP de la localizacion
                recibirCoordenadas(); // Recibimos las coordenadas del resto de vecinos
                recibirConfirmaciones(); //El cliente espera hasta recibir la confirmación de todos sus vecinos
                time_end = System.currentTimeMillis();
                time_total = (time_end - time_start); //Tiempo total en segundos
                tiempo_acumulado += time_total;
                actualizarCoordenadas(); //Aqui se calcularian las nuevas coordenadas
                
                try 
                {
                    sleep(25000);
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(ClienteThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            enviarTiempos();
            
            socket.close();
            socketUDP.close();
        } 
        catch (UnknownHostException ex) 
        {
            Logger.getLogger(ClienteThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClienteThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
