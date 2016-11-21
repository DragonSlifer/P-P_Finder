package Servidor;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Thread.sleep;

public class ServidorThread extends Thread
{
    int num_clientes, puerto_server, vecinos, iteraciones;
    ServerSocket socketTCP;
    DatagramSocket socketUDP;
    Vector<Vector<Socket>> vec_clientes = new Vector<Vector<Socket>>(num_clientes);
    Vector<Vector<Long>> vec_tiempos = new Vector<Vector<Long>>(num_clientes);
    PrintStream out;
    
    /******************************************************************
    * Constructor de la clase a la que se le pasa desde el modelo el 
    * numero de clientes con el que se va a trabajar
    *
    * @param num_clientes 
    * @param vecinos
    * @param port
    * @param out
    * @param iteraciones
    ******************************************************************/
    public ServidorThread(int num_clientes, int vecinos, int port, PrintStream out, int iteraciones)
    {
        this.num_clientes = num_clientes;
        puerto_server = port;
        this.out = out;                     ///< Para los mensajes de la consola
        this.vecinos = vecinos;
        this.iteraciones = iteraciones;
        System.setOut(out);
        System.setErr(out);
    }
    
    /******************************************************************
    * Función que se encarga de permanecer a la espera de nuevas conexiones
    * por parte de los clientes hasta que se llega al numero de clientes
    * establecido.
    * 
    * De momento falta establecer que a los 20 segundos se dejara de esperar
    * conexiones
    * ****************************************************************/
    void clientesConexion()
    {
        int clientes_conectados = 0, grupo = 0;
        // En este try catch el servidor se queda en espera hasta que todos los clientes le manda una solicitud de conexion
        try 
        {
            socketTCP = new ServerSocket(puerto_server);
            vec_clientes.add(new Vector<Socket>(vecinos));
            vec_tiempos.add(new Vector<Long>(vecinos));
            System.out.println("SERVIDOR ----> Esperando conexion de clientes...");
            while(clientes_conectados < num_clientes) 
            {
                Socket socket_conexion = socketTCP.accept();

                //System.out.println("SERVIDOR ----> Nuevo cliente conectado" + clientes_conectados);
                vec_clientes.get(grupo).add(socket_conexion);
                clientes_conectados++;

                if(clientes_conectados%vecinos == 0 && clientes_conectados != num_clientes) //Para que haya 10 clientes en el grupo y que no cree un nuevo grupo al meter el ultimo cliente
                {
                    grupo++;
                    vec_clientes.add(new Vector<Socket>(vecinos));
                    vec_tiempos.add(new Vector<Long>(vecinos));
                } 
            }
        }
        catch (IOException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    
    /******************************************************************
    * Se encarga de enviar a todos los clientes conectados un mensaje
    * indicandoles que puede comenzar el intercambio de coordenadas
    * ****************************************************************/
    void inicioComunicacion()
    {
        int i, j;
        
        try
        {
            Socket aux;
            PrintStream p;
            BufferedReader b;
             
            System.out.println("SERVIDOR ----> Enviando inicio de comunicacion ");
            
            for(i = 0; i < vec_clientes.size(); i++)
            {
                for(j = 0; j <vec_clientes.get(i).size(); j++)
                {
                    aux = (Socket)vec_clientes.get(i).get(j);
                    
                    p = new PrintStream(aux.getOutputStream());
                    b = new BufferedReader ( new InputStreamReader ( aux.getInputStream() ) );
                    
                    p.println((i * vecinos + j) + "start" + vec_clientes.get(i).size());
                    
                    p.close();
                    b.close();
                }
                System.out.println("SERVIDOR ----> Comunicacion iniciada con el grupo " + i + " que tiene " + j + " clientes");
            }
        }
        catch (IOException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    
    /******************************************************************
    * Esta función auxiliar se encarga de averiguar el grupo al que
    * pertenece el cliente que ha enviado las ultimas coordenadas
    * recibidas
    * 
    * @param id
    * ****************************************************************/
    int obtenerGrupo(int id)
    {
        int grupo_cliente;
        
        grupo_cliente = id / vecinos;
        
        return grupo_cliente;
    }
    
    /******************************************************************
    * Esta función auxiliar se encarga de averiguar el id del cliente 
    * que ha enviado las ultimas coordenadas recibidas
    * 
    * @param mensaje
    * ****************************************************************/
    int obtenerId(String mensaje)
    {
        int id;
        
        id = Integer.parseInt(mensaje.substring(0, mensaje.indexOf('-')));
        
        return id;
    }
    
    /******************************************************************
    * Se encarga de reenviar a todos los miembros del grupo las 
    * coordenadas del cliente que envio el paquete con su posición
    * 
    * @param grupo_cli
    * @param id_cli
    * @param recv_paquete
    * @param v
    * @param vs
    * ****************************************************************/
    void distribuirCoordenadas(int grupo_cli, int id_cli, DatagramPacket recv_paquete, Vector<DatagramPacket> v, Vector<String> vs)
    {
        byte[] mensaje_bytes;
        DatagramPacket env_paquete;
        int id;
        
        try
        {
            for(int i = 0; i < v.size(); i++)
            {
                id = obtenerId(vs.get(i));
                if(((id/10) == grupo_cli) && (id != id_cli))
                {
                    mensaje_bytes = recv_paquete.getData();
                    
                    //Preparamos el paquete que queremos enviar
                    env_paquete = new DatagramPacket(mensaje_bytes,mensaje_bytes.length,v.get(i).getAddress(),v.get(i).getPort()); // Envía 

                    // realizamos el envio
                    socketUDP.send(env_paquete);
                    
                    //System.out.println("SERVIDOR ----> Coordenadas del cliente " + id_cli + " enviadas al cliente " + id);
                }
            }
        }
        catch (IOException e) 
        {       
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    
    /******************************************************************
    * Se encarga de recibir las confirmaciones de cada uno de los vecinos
    * del grupo y de renviarlas al cliente que envio las coordenadas
    * 
    * @param grupo_cli
    * @param id_cli
    * @param puerto
    * @param address
    * ****************************************************************/
    void tramitarRespuestas(int grupo_cli, int id_cli, int puerto, InetAddress address)
    {
        int contador = 0;
        String mensaje;
        byte[] mensaje_bytes = new byte[256];
        DatagramPacket resp_paquete = new DatagramPacket(mensaje_bytes,256);
        DatagramPacket env_paquete;
           
        try
        {
            while(contador < vecinos - 1)
            {
                socketUDP.receive(resp_paquete);

                env_paquete = new DatagramPacket(mensaje_bytes,mensaje_bytes.length,address,puerto);

                socketUDP.send(env_paquete);

                contador++;
            }
            
            /*mensaje = "fin";
            
            mensaje_bytes = mensaje.getBytes();
            
            env_paquete = new DatagramPacket(mensaje_bytes,mensaje.length(),address,puerto);

            socketUDP.send(env_paquete);*/
        }
        catch (IOException e) 
        {       
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /******************************************************************
    * Se encarga de recibir el tiempo medio de cada uno de los clientes
    * y introducirlo en un vector con los tiempos de cada grupo
    * ****************************************************************/    
    void recibirTiempos()
    {
        String mensaje;
        Long tiempo;
        int index, id, grupo;
        byte[] mensaje_bytes = new byte[256];
        DatagramPacket resp_paquete = new DatagramPacket(mensaje_bytes,256);
           
        try
        {
            socketUDP.receive(resp_paquete);
               
            mensaje = new String(mensaje_bytes).trim();
                
            index = mensaje.indexOf('-');
            
            id = Integer.parseInt(mensaje.substring(0, index));
            
            index = mensaje.indexOf('>');
            
            tiempo = Long.parseLong(mensaje.substring(index + 1, mensaje.length()));
                
            grupo = obtenerGrupo(id);
            
            vec_tiempos.get(grupo).add(tiempo);
        }
        catch (IOException e) 
        {       
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    
    /******************************************************************
    * Se encarga de calcular todos los tiempos que se necesitan para
    * las estadisticas de ejecución
    * ****************************************************************/
    void calcularTiempos()
    {
        float tiempo_medio;
        //Tiempo medio de grupos
        
        for(int i = 0; i < vec_tiempos.size(); i++)
        {
            tiempo_medio = 0;
            for(int j = 0; j < vec_tiempos.get(i).size(); j++)
            {
                tiempo_medio += vec_tiempos.get(i).get(j);
            }
            
            tiempo_medio /= 1000;
            
            System.out.println("Tiempo medio del grupo " + i + " = " + tiempo_medio + " segundos");
        }
    }
    
    /******************************************************************
    * Función que se inicia al ejecutar el hilo ServidorThread desde el main
    * mediante el comando .start()
    * ****************************************************************/
    @Override
    public void run()
    {
        String mensaje;
        int puerto,  grupo_cli, id_cli, grupo_cli1, id_cli1, contador;
        InetAddress address;
        DatagramPacket recv_paquete1;
        
        
        try 
        {
            clientesConexion(); // Esperamos a que se conecten todos los clientes
                   
            //Creamos el socket
            socketUDP = new DatagramSocket(puerto_server);
            
            inicioComunicacion(); // Mandamos el mensaje a los clientes que les indica que puede empezar la comunicación
            
            for(int k = 0; k < iteraciones; k++)
            {
                System.out.println("\n#################################################################\n" 
                        + "ITERACION " + (k + 1) 
                        + "\n#################################################################\n");
                contador = 0;
                Vector<DatagramPacket> vec = new Vector<DatagramPacket>();
                Vector<String> vec2 = new Vector<String>();
                //Iniciamos el bucle
                do 
                {
                    if(contador == 0)
                        for(int i = 0; i < num_clientes; i++)
                        {
                            byte[] mensaje_bytes = new byte[256];
                            DatagramPacket recv_paquete = new DatagramPacket(mensaje_bytes,256);
                            // Recibimos el paquete
                            socketUDP.receive(recv_paquete);
                            vec.add(recv_paquete);
                            mensaje = new String(mensaje_bytes).trim();
                            vec2.add(mensaje);
                            //System.out.println("***********************************************************************************************\n"
                            //+ "SERVIDOR ----> El servidor recibe las coordenadas del cliente " + mensaje 
                            //+ "\n***********************************************************************************************");
                        }

                    recv_paquete1 = vec.get(contador);
                    mensaje = vec2.get(contador);

                    //Obtenemos IP Y PUERTO
                    puerto = recv_paquete1.getPort();
                    address = recv_paquete1.getAddress();

                    id_cli = obtenerId(mensaje);
                    grupo_cli = obtenerGrupo(id_cli); //Obtenemos el grupo al que pertenece el cliente que manda las coordenadas  

                    if(contador == 0)
                        for(int j = 0; j < vec.size(); j++)
                        { 
                            id_cli1 = obtenerId(vec2.get(j));
                            grupo_cli1 = obtenerGrupo(id_cli1);
                            distribuirCoordenadas(grupo_cli1, id_cli1, vec.get(j), vec, vec2); //Distribuimos sus coordenadas entre todos sus vecinos
                        }

                    tramitarRespuestas(grupo_cli, id_cli, puerto, address); // Esperamos las confirmaciones de los vecinos y las reenviamos al cliente que mando las coordenadas
                    
                    contador++;
                } 
                while (contador < num_clientes);
                
                try 
                {
                    sleep(20000);
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(ServidorThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            for(int i = 0; i < num_clientes; i++)
            {
                recibirTiempos();
            }
            
            socketTCP.close();
            socketUDP.close();
            
            calcularTiempos();
            
            System.out.println("EL SERVIDOR FINALIZA LA COMUNICACIÓN");
        }
        catch (IOException e) 
        {       
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}