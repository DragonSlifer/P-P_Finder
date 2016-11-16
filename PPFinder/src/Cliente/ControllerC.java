/*
 *  Role Game Character Creator
 *  Programa para crear fichas de personaes de rol en varios
 *  sistemas: Mundo de Tinieblas, NSD20, Malefic Time: Plenilunio,
 *  ...
 *  Permite además, guardar la ficha, editarla y enviarla por
 *  correo electrónico al director de partida.
 */
package Cliente;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;


/**
 * 
 * Los mensajes del controlador seguiran la estructura:
      "ControllerC -- nombre_de_funcion -- mensaje"
 * @author Jorge
 */
public class ControllerC {
    private Model model;
    
    
    public ControllerC(JTextArea console){
        
        try {
            this.model = new Model(console);
        } catch (UnknownHostException ex) {
            Logger.getLogger(ControllerC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Esta función cambia el texto del recuadro de ayuda rápida
     * @param command Sirve para determinar que ayuda se debe mostrar
     * @return 
     */
    public String changeHelpBox (int command){
        String helpText = null;
        /** 
         * Llamada a funcion del modelo
         * Esta funcion debe, en funcion del comando (0-3) devolver un string
         * con el texto a introducir en el Recuadro de Ayuda. Podria ser algo
         * así la fucion del modelo:
         * helpText = model.changeHelpBox(command);
         */
        if (helpText != null)
            System.out.println("Controlador -- changeHelpBox -- Correctly Changed");
        else
            System.out.println("Controlador -- changeHelpBox -- ¡helpText is Null!");
        return helpText;
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

    public String UpdateMap() {
        String coord = "Fallo al Actualizar el Mapa";
        ///< model.updateMap()
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

    public void iniciarServidor() {
        model.IniciaServer();
    }

    public void iniciarClientes() {
        model.iniciaClientes();
    }
}
