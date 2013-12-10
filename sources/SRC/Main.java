package SRC;

import java.io.*;
import java.util.*;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

//@ IF NOKIA
import com.nokia.mid.ui.*;
//@ ENDIF

//@ IF MIDP2
import javax.microedition.lcdui.game.*;
//@ ENDIF

//@ IF AMR | WAV | MIDI
import javax.microedition.media.*;
import javax.microedition.media.control.*;
//@ ENDIF

//@ IF OTT | NWAV
import com.nokia.mid.sound.Sound;
//@ ENDIF

//@ IF MMF
import com.samsung.util.AudioClip;
//@ ENDIF

import REZ.*;

import arcanum.*;

//@ IF MIDP2
public class Main extends GameCanvas implements Runnable // any
//@ ELSE
    //@ IF NOKIA & MIDP1
    //@ public class Main extends FullCanvas implements Runnable // any
    //@ ELSE
    //@ public class Main extends Canvas implements Runnable // any
    //@ ENDIF
//@ ENDIF
//@ IF COMMANDKEYS
, CommandListener
//@ ENDIF
{
        
    /**
     * used to retrieve instance of the midlet.
     */
    public static MIDletMain      _MIDLET;        
 
    private static Thread          m_thread;
    public static boolean         g_bRunning   = false;
    private static boolean         m_bPaused    = false;
    private static Display         m_display;
    
    /**
     * @Name: Main()
     * @Desc: constructor
     */      
    public Main(MIDletMain owner)
    {
 
        //@ IF MIDP2
        super(false);
        //@ ENDIF
        _MIDLET = owner;
        
        setCommandListener(this);
        
        Display.getDisplay( MIDletMain.g_instance ).setCurrent( this );
     }
    

    /**
     * @Name: deploy()
     * @Desc: start game
     */      
   public void deploy()
    {
        try
        {
            //@ IF MIDP2
            setFullScreenMode(true);
            //@ ENDIF
            
            if ( m_bPaused )
            {
                Display.getDisplay( MIDletMain.g_instance ).setCurrent( this );
                m_bPaused = false;
                return;
            }
             
            //GameCore.init( getWidth(), getHeight() );
            GameCore.init( Defs.SCREEN_WIDTH, Defs.SCREEN_HEIGHT );
            GameCore.reinitMenu();
            GameCore.reinitGame();

        }
        catch( Exception e )
        {
            //@ IF DEBUG
            System.err.println( "***Main::deploy() - Error !" + e.getMessage() );
            //@ ENDIF
        }
        
        g_bRunning = true;
        
        m_thread = new Thread( this );
        m_thread.start();
        
        //@ IF DEBUG
        System.err.println("===Main::deploy() : GAME STARTED ===");
        //@ ENDIF
    }    
    
   
   /**
    * @Name: run()
    * @Desc: game heartbeat
    */     
   public void run()
   {
       
       while( g_bRunning )
       {
           Thread.yield();
           
           if ( m_bPaused )
               continue;
          
           try
           {
               // update game
               repaint();
               
               //Thread.sleep( 10 );
           }
           catch( Exception e )
           {
               //@ IF DEBUG
               System.out.println( "*** run() exception ");
               Alert alert = new Alert("", "[ERROR][run() thread] = " + e.toString(), null, AlertType.ERROR);
               Display.getDisplay(Main._MIDLET).setCurrent( alert );               
               //@ ENDIF
           }
       }
       
       MIDletMain.g_instance.destroyApp( true );
   }   

   
   /**
    * @Name: pause()
    * @Desc: 
    */       
   public static void pause()
   {
       m_bPaused = true;
   }
   
   /**
    * @Name: release()
    * @Desc: 
    */    
   public static void release()
   {
       GameCore.release();
   }   
   
   /**
    * @Name: commandAction()
    * @Desc: 
    */  
   public void commandAction( Command c, Displayable d )
   {
       if ( Command.EXIT == c.getCommandType() )
       {
           g_bRunning = false;
       }
   }   
   
   /**
    * @Name: keyPressed()
    * @Desc: 
    */   
   protected void keyPressed( int keyCode )
   {
       KeyMan.Pressed(keyCode);
   }
   
   /**
    * @Name: keyReleased()
    * @Desc: 
    */   
   protected void keyReleased( int keyCode )
   {
       KeyMan.Released(keyCode);
   }   
   
   /**
    * @Name: paint()
    * @Desc: 
    */    
   private static long m_GameLogicLastUpdateTime = 0;
   private static long m_GraphicsLastUpdateTime = 0;
   private static long m_currentTime = 0;
   
   private static long ltc = 0;
   public void paint(Graphics g)
   {
       if ( g_bRunning )
       {
           // update game mechanics in fixed fps
           
           m_currentTime = System.currentTimeMillis();
           
           if( m_GameLogicLastUpdateTime < m_currentTime )
           {
               m_GameLogicLastUpdateTime = m_currentTime + Defs.FPS;
               GameCore.updateLogic();
           }
           
           // update 3D graphics within some time to minimize
           // bind()-release() calls
           
           m_currentTime = System.currentTimeMillis();
           
           if ( m_GraphicsLastUpdateTime < m_currentTime )
           {
               m_GraphicsLastUpdateTime = m_currentTime + Defs.FPS_3D;
               GameCore.render3DFrame( g );
           }

           // update 2D graphics as fast as possible
           GameCore.render2DFrame(g);
       }
        
   }

}
    
