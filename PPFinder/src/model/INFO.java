/*
 *  Role Game Character Creator
 *  Programa para crear fichas de personaes de rol en varios
 *  sistemas: Mundo de Tinieblas, NSD20, Malefic Time: Plenilunio,
 *  ...
 *  Permite además, guardar la ficha, editarla y enviarla por
 *  correo electrónico al director de partida.
 */
package model;

public class INFO {
    boolean changed;        ///< Dirá si la información es nueva o no
    /**
     * La información se guardará como un entero
     * El entero se dividirá en los datos correspondientes
     * Codigo:
     *      Cardinalidad    Numero
     *          N/E           1
     *          N/W           2
     *          S/E           3
     *          S/W           4
     * Número:
     * [C][La|La|La|La|La|La][Lo|Lo|Lo|Lo|Lo|Lo][G][M][J][B]
     * Siendo los caracteres correspondiendo a los dígitos de:
     *      C   --> Cardinalidad
     *      La  --> Latitud (precision de 3 decimales
     *      Lo  --> Longitud (precision de 3 decimales
     *      G   --> Identifica el genero del juego
     *      M   --> Identifica el modo de juego
     *      J   --> Identifica el juego
     *      B   --> Identifica lo que se busca
     */
    long info;
    
    public INFO (){
        changed = false;
    }
    
    public void changeChangedAtt(){
        if (changed == false)
            changed = true;
        else
            changed = false;
    }
    
    public boolean changedInfoAsk(){
        return changed;
    }
    
    public void addInfo(long nfo){
        this.info = nfo;
        changed = true;              ///< Hemos modificado la informacion, por lo que lo marcamos en este
    }
    
    public long getInfo(){
        changed = false;            ///< Cuando ejecutamos esta orden, al recuperar la informacion ya no esta modificada
        return info;
    }
    
    public float retrieveLatitud(){
        float retval = 0;
        long nfo = info;
        long aux = (info%10^15)*10^15;
        
        nfo = (nfo-aux)/10^9;       ///< Nos quedamos solo con la informacion que queremos
        retval = nfo/100;           ///< Ponemos la Latitud en la medida necesaria
        
        return retval;
    }
    
    public float retrieveLongitud(){
        float retval;
        long nfo = info;
        long aux = (info%10^9)*10^9;
        
        nfo = (nfo-aux)/10^3;       ///< Nos quedamos solo con la informacion que queremos
        retval = nfo/100;           ///< Ponemos la Longitud en la medida necesaria
        
        return retval;
    }
    
    
}