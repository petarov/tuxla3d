/*
 * MIDletMain.java
 *
 * Created on Вторник, 2005, Януари 18, 15:25
 */

package SRC;

import javax.microedition.midlet.MIDlet;

public class MIDletMain extends MIDlet
{
    
    public static MIDletMain      g_instance;
    public static Main            g_Main;
    
    /**
     * @Name: MIDletMain()
     * @Desc: Midlet constructor
     */  
    public MIDletMain() 
    {
        g_instance = this;
        g_Main = new Main( this );
    }
    

    /**
     * @Name: startApp()
     * @Desc: main() method
     */
    public void startApp() 
    {
        g_Main.deploy();
    }


    /**
     * @Name: pauseApp()
     * @Desc: 
     */
    public void pauseApp() 
    {
        g_Main.pause();
    }

    
    /**
     * @Name: destroyApp()
     * @Desc: called upon application destruction
     */
    public void destroyApp( boolean unconditional ) 
    {
        g_Main.release();
        notifyDestroyed();
    }
 
}

