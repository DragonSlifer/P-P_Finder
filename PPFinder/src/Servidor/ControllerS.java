/*
 *  Role Game Character Creator
 *  Programa para crear fichas de personaes de rol en varios
 *  sistemas: Mundo de Tinieblas, NSD20, Malefic Time: Plenilunio,
 *  ...
 *  Permite además, guardar la ficha, editarla y enviarla por
 *  correo electrónico al director de partida.
 */
package Servidor;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;


/**
 * 
 * Los mensajes del controlador seguiran la estructura:
      "ControllerS -- nombre_de_funcion -- mensaje"
 * @author Jorge
 */
public class ControllerS {
    private ModelS model;    
    
    public ControllerS(JTextArea console){
        
        try {
            this.model = new ModelS(console);
        } catch (UnknownHostException ex) {
            Logger.getLogger(ControllerS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String UpdateMap() {
        String coord = "Fallo al Actualizar el Mapa";

        if (!"Fallo al Actualizar el Mapa".equals(coord))
            System.out.println("Controller Message -- UpdateMap -- Updating Failed");
        else
            System.out.println("Controller Message -- UpdateMap -- Successful Update");
        return coord;
    }

    public void SendInfo() {
        Boolean correcto = false;
        /// correcto = model.sendInfo();
        if (correcto == false)
            System.out.println("Controller Message -- SendInfo -- Error Sending Location");
        else
            System.out.println("Controller Message -- SendInfo -- Location Sended");
    }
    
    public String MapUpdate(String genero, String modo, String juego, String busco) {
        String res = "No se ha encontrado nada con esos datos";
        ///< Peticion al modelo de encontrar jugadores y mostrarlos.
        ///< Con el nombre del cliente y las cordenadas será suficiente por ahora
        if(!"No se ha encontrado nada con esos datos".equals(res))
            System.out.println("Controller Message -- MapUpdate -- Not Found Anything");
        else
            System.out.println("Controller Message -- MapUpdate -- Succesful Search");
        return res;
    }

    public void iniciarServidor() {
        model.IniciaServer();
    }
}
