/*
 * GameCore.java
 *
 * Created on Сряда, 2006, Януари 23, 14:56
 */

package SRC;

import java.util.Random; 
import javax.microedition.lcdui.*;
import javax.microedition.m3g.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import REZ.*;
import arcanum.*;

/**
 *
 * @author ppetrov
 */
public class GameCore 
{
    private final static int        _TOP_LEFT = Graphics.TOP | Graphics.LEFT;
    
    private static int  m_progressLoading = 0;
    
    //------ gameplay specifics ----------------------------------------------
    private static short            m_score = 0;
    private static short            m_tempScore = 0;
    private static short            m_nextBigScore = 0;
    private static short            m_level = 0;
    private static int              m_gamestate;
    private static boolean          m_bInGame = false;
    private static boolean          m_bIsThereSavedGame = false;
    
    //------ font specifics ---------------------------------------------------
    private static final int[] fontClipData         = { 1, 1, 1, 1, 3, 3, 2, 2, 3, 3, 4, 5, 4, 3, 5, 5,
        6, 5, 5, 5, 1, 5, 4, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 2, 4, 4, 2, 2, 4, 2, 5, 4, 4, 4,
        4, 3, 4, 2, 4, 4, 5, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 4, 4, 5, 4, 4, 4, 5, 4, 4, 5, 4, 5,
        5, 4, 4, 4, 4                              };
    private static Sprite2D    m_font;
    private static byte[][]    _texts;
   
    // --- current falling cube -----------------------------------------------
    private static int      m_xCube = 0;
    private static float    m_yCube = 0.0f;
    private static int      m_yEndPosCub = 0;
    private static float    m_speedInitial = 0.0f;
    private static float    m_speedCube = 0.5f;
    private static float    m_rotSpeedCube = 0.0f;
    private static int      m_clrIndexCube = 0;             // current falling cube color
    private static int      m_clrIndexCubeNext = 0;
    private static boolean  m_isFallCube = false;
    private static Mesh     m_meshCube;
    private static float    m_pos[] = new float[3];
    //@ IF MESHPREVIEW_3D
    private static Mesh     m_meshNextCube;
    //@ ENDIF
    
    // --- game matrix logic ---------------------------------------------------
    private static final int    MAX_CUBES       = 7;
    private static Image        m_imgPreview[]  = new Image[MAX_CUBES];
    private static Texture2D    m_texColors[]   = new Texture2D[MAX_CUBES];
    private static Mesh         m_cubes[]       = new Mesh[MAX_CUBES];
    
    private static final int BOARD_SIZE         = 6;
    private static final int MATRIX_EMPTY       = 0;
    private static final int MATRIX_MOVING      = 0x0f000000;
    private static final int MATRIX_POS_MASK    = 0xf0000000;
        
    private static int m_matrix[][]         = new int[BOARD_SIZE][BOARD_SIZE];
    private static Mesh m_cubeMatrix[][]    = new Mesh[BOARD_SIZE][BOARD_SIZE];
    
    private static final float _COLS[] = { 10.0f, 40.0f, 70.0f, 100.0f, 130.0f, 160.0f }; //, 190.0f, 220.0f };
    private static final float _ROWS[] =  { -35.0f, -65.0f, -95.0f, -125.0f, -155.0f, -185.0f }; //, -215.0f, -250.0f };
    
    private static final float START_ROW = _ROWS[0] + 48.0f;
    
    private static final int _CLRS[] = { 
                                  0x000294a5, // sea blue
                                  0x00914f10, // brown 
                                  0x00106500, // green
                                  0x0052006d, // violet 
                                  0x009c0000, // red  
                                  0x00666800, // yellow,
                                  0x00101095, // blue
                                  };

    private static Random       m_rnd = new Random();    
    private static boolean      m_bFoundCombination = false;
    private static boolean      m_bNeedReSync = false;
    private static int m_rows[] = new int[BOARD_SIZE],
                       m_cols[] = new int[BOARD_SIZE],
                       m_totals = 0;
    private static int row1 = 0, col1 = 0;
      
    // ---------- environment specifics --------------------------
    //@ IF LIGHTS
    private static Light        m_light;
        //@ IF DEBUG
        private static Mesh         m_cubeLight;
        //@ ENDIF
    //@ ENDIF

    private static Mesh         m_meshBackGround;
    private static Mesh         m_meshFrontGround;

    //  ---------- zooming specifics -----------------------------
    private static boolean      m_zoomIn = true;
    private static int          m_zoomType = -1;
    private static float        m_camLastPos[] = new float[3];
    private static boolean      m_boomStarted = false;    
    //@ IF EXPLOSION
    private static Texture2D    m_texBoom[] = new Texture2D[6];
    private static Mesh         m_meshBoom;
    private static int          m_boomPrevFrame = 0;
    private static float        m_boomCurFrame = 0.0f;
    private static float        m_boomSpeed = 0.4f;
    //@ ENDIF    
    
    //------- FPS -------------------------------------------------
    //@ IF SHOW_FPS
    private static int          m_nCurrentFps;
    private static int          m_nFps;
    private static long         m_lFpsLast;
    //@ ENDIF
    
    //------- Menu elements ---------------------------------------
    private static int m_menuCurIdx = 0;
    private static int m_menuGameDetails = Defs.DETAILS_MEDIUM;
    private static int m_currentGameDetails = Defs.DETAILS_MEDIUM;
    private static int m_tempVal = 0;
    
    private static Image m_menuImgTopBorder;
    private static Image m_menuImgBottomBorder;
    private static Image m_menuImgLeftBorder;
    private static Image m_menuImgRightBorder;
    private static Image m_menuImgTopLeftCorner;
    private static Image m_menuImgTopRightCorner;
    private static Image m_menuImgBottomLeftCorner;
    private static Image m_menuImgBottomRightCorner; 
    private static Sprite2D m_menuFont = null;
    private static Sprite2D m_menuSelector = null;
    private static Image m_menuUpDownIndicator;
    private static Image m_menuBarTop;
    private static Image m_menuBarTopInGame;
    private static Image m_menuBarBottom;
    private static Image m_menuKolona;
    private static Image m_menuSplash;
    private static Sprite2D m_menuLoadingBar = null;
    private static Sprite2D m_menuButton = null;

    //-----Hi-Score -----------------------------------------------
    private static byte m_scPlayer_names[][] = new byte[5][];
    private static int  m_scPlayer_scores[] = new int[5];
    private static boolean m_scLoaded = false;
    
    
    /**
     * @Desc: init GameCore modules
     */    
    public static void init( int width, int height )
    {
        m_progressLoading = 0;
        KeyMan.Released  = 0;
        setGameState( Defs.GS_LOADING );        
    
        // init game texts
        if ( null == _texts)
        {
            god2DFast.libLOAD_byBytes( texts._PACKNAME, texts._SIZE );
            _texts = god2DFast.textsByteCreate( god2DFast._libLOAD_CURRENT, 
                                                texts.text_OFFSET, texts._SIZE );
            god2DFast.libCLEAR();
        }
        
        // load splash here
        try
        {
            m_menuLoadingBar = new Sprite2D();
            m_menuLoadingBar.loadImage( Image.createImage( images.menu_loading_bar ) );
            int frames[] =  { 2 };
            m_menuLoadingBar.setClipped( frames, 2 );             
            
            //@ IF 176x208
            m_menuSplash = Image.createImage( images.menu_splash_176x208 );
            //@ ELSE
            
                //@ IF 176x220
                m_menuSplash = Image.createImage( images.menu_splash_176x220 );
                //@ ELSE
                m_menuSplash = Image.createImage( images.menu_splash_240x320 );
                //@ ENDIF
                
            //@ ENDIF
            
        }
        catch( Exception e )
        {
            //@ IF DEBUG
            System.out.print("\n [ERROR][2D] Loading Splash: " + e.toString());
            Alert alert = new Alert("", "[ERROR][2D] Loading Splash = " + e.toString(), null, AlertType.ERROR);
            Display.getDisplay(Main._MIDLET).setCurrent(alert);
            //@ ENDIF
        }
        
        // init initial score names
        for( int i = 0; i < 5; i++ )
        {
            m_scPlayer_scores[i] = 0;
            m_scPlayer_names[i] = convertText( new String( "---").getBytes() );            
        }
        
        loadSettings();
        loadScores();
        
        // imame singleton class za Graphics3D
        god3D.Initialize( width, height );
        
    }
    

    /**
     * @Desc: do game cleanups
     */    
    public static void release()
    {
        if ( m_bInGame )
        {
            saveContinueGame();
        }
        
        saveSettings();
        
        if ( m_menuFont != null )
            m_menuFont.Destroy();
        if ( m_menuSelector != null )
            m_menuSelector.Destroy();
        if ( m_menuLoadingBar != null )
            m_menuLoadingBar.Destroy();
        if ( m_menuButton != null )
            m_menuButton.Destroy();
        
        //@ IF DEBUG
        System.out.println( "*** GameCore Releasing... ");
        //@ ENDIF
    }
    
    /**
     * @Desc: load saved game attribs
     *
     */
    private static boolean loadContinueGame()
    {
        try
        {
            god2DFast.recordOpen( "Tuxla3D_continue", true );
            if( god2DFast.recordGetNum() <= 0 )
            {
                god2DFast.recordClose();
                return false;
            }           
            
            byte data[] = god2DFast.recordRead( 1 );
            
            ByteArrayInputStream bis = new ByteArrayInputStream( data );
            DataInputStream inp = new DataInputStream( bis );
            
            //save falling cube proprs
            m_isFallCube = (inp.readByte() == 1);
            m_xCube = inp.readInt();
            m_yCube = inp.readFloat();
            m_yEndPosCub = inp.readInt();
            m_rotSpeedCube = inp.readFloat();
            m_speedInitial = inp.readFloat();
            m_speedCube = inp.readFloat();
            m_level = inp.readShort();
            m_score = inp.readShort();
            m_clrIndexCube = inp.readInt();
            m_clrIndexCubeNext = inp.readInt();
            
            int idx = m_clrIndexCube & 0x00ffffff;
            for( int k = 0; k < _CLRS.length; k++ )
            {
                if ( _CLRS[ k ] == idx )
                {
                    m_meshCube = (Mesh)m_cubes[k].duplicate();
                    god3D.g_World.addChild( m_meshCube );
                    break;
                }
            }             
            
            m_pos[0] = inp.readFloat();
            m_pos[1] = inp.readFloat();
            m_pos[2] = inp.readFloat();
            m_meshCube.setTranslation( m_pos[0], m_pos[1], m_pos[2] );
            
            float ori[] = new float[4];
            ori[0] = inp.readFloat();
            ori[1] = inp.readFloat();
            ori[2] = inp.readFloat();
            ori[3] = inp.readFloat();
            m_meshCube.setOrientation( ori[0], ori[1], ori[2], ori[3] );
            
            // save board snapshot
            
            for( int i = 0; i < BOARD_SIZE; i++ )
            {
                for( int j = 0; j < BOARD_SIZE; j++ )
                {
                    m_matrix[i][j] = inp.readInt();
                    if ( MATRIX_EMPTY != m_matrix[i][j] )
                    {
                        idx = inp.readInt();
                        m_cubeMatrix[i][j] = (Mesh)m_cubes[idx].duplicate();
                        god3D.g_World.addChild( m_cubeMatrix[i][j] );
                        m_cubeMatrix[i][j].translate( _COLS[j], _ROWS[i], 0.0f );
                        
                        ori[0] = inp.readFloat();
                        ori[1] = inp.readFloat();
                        ori[2] = inp.readFloat();
                        ori[3] = inp.readFloat();
                        m_cubeMatrix[i][j].setOrientation( ori[0], ori[1], ori[2], ori[3] );
                    }
                }
            }
            
            god2DFast.recordClose();
            
            return true;
        }
        catch(Exception e)
        {
            try
            {
                god2DFast.recordClose();
            }
            catch(Exception ex)
            {}
            
            //@ IF DEBUG
            System.err.println("saveContinueGame() :" + e.toString());
            //@ ENDIF
        }
        
        return false;
    }
    
    /**
     * @Desc: remove saved game
     */
    private static void deleteContinueGame()
    {
        try
        {
            god2DFast.recordOpen( "Tuxla3D_continue", false );
            if( god2DFast.recordGetNum() <= 0 )
            {
                god2DFast.recordClose();
            }
            else
            {
                god2DFast.recordClose();
                god2DFast.recordDelete("Tuxla3D_continue");
            }
        }
        catch(Exception e)
        {
            try
            {
                god2DFast.recordClose();
            }
            catch(Exception ex)
            {}
            
            //@ IF DEBUG
            System.err.println("deleteContinueGame() :" + e.toString());
            //@ ENDIF
        }         
    }
    
    /**
     * @Desc: save current game status
     */
    private static void saveContinueGame()
    {
        try
        {
            god2DFast.recordOpen( "Tuxla3D_continue", true );
            if( god2DFast.recordGetNum() > 0 )
            {
                god2DFast.recordClose();
                god2DFast.recordDelete("Tuxla3D_continue");
                god2DFast.recordOpen("Tuxla3D_continue", true);
            }           
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream( bos );
            
            //save falling cube proprs
            out.writeByte( (byte)( m_isFallCube ? 1 : 0 ) );
            out.writeInt( m_xCube );
            out.writeFloat( m_yCube );
            out.writeInt( m_yEndPosCub );
            out.writeFloat( m_rotSpeedCube );
            out.writeFloat( m_speedInitial );
            out.writeFloat( m_speedCube );
            out.writeShort( m_level );
            out.writeShort( m_score );
            out.writeInt( m_clrIndexCube );
            out.writeInt( m_clrIndexCubeNext );
            
            m_meshCube.getTranslation( m_pos );
            out.writeFloat( m_pos[0] );
            out.writeFloat( m_pos[1] );
            out.writeFloat( m_pos[2] );
            
            float ori[] = new float[4];
            m_meshCube.getOrientation( ori );
            out.writeFloat( ori[0] );
            out.writeFloat( ori[1] );
            out.writeFloat( ori[2] );
            out.writeFloat( ori[3] );
            
            // save board snapshot
            
            for( int i = 0; i < BOARD_SIZE; i++ )
            {
                for( int j = 0; j < BOARD_SIZE; j++ )
                {
                    out.writeInt( m_matrix[i][j] );
                    if ( MATRIX_EMPTY != m_matrix[i][j] )
                    {
                        int idx = m_matrix[i][j] & 0x00ffffff;
                        for( int k = 0; k < _CLRS.length; k++ )
                        {
                            if ( _CLRS[ k ] == idx )
                            {
                                out.writeInt( k );
                                break;
                            }
                        }
                        
                        m_cubeMatrix[i][j].getOrientation( ori );
                        out.writeFloat( ori[0] );
                        out.writeFloat( ori[1] );
                        out.writeFloat( ori[2] );
                        out.writeFloat( ori[3] );
                    }
                }
            }
            
            //@ IF DEBUG
            System.err.println("***saveContinueGame() : total bytes= " + bos.size() );
            //@ ENDIF

            god2DFast.recordWrite( bos.toByteArray() );
            god2DFast.recordClose();
        }
        catch(Exception e)
        {
            try
            {
                god2DFast.recordClose();
            }
            catch(Exception ex)
            {}
            
            //@ IF DEBUG
            System.err.println("saveContinueGame() :" + e.toString());
            //@ ENDIF
        }     
    
    }  
    
    /**
     * @Desc: load game settings
     *
     * @return: true, on existing/loaded settings
     */
    private static boolean loadSettings()
    {
        try
        {
            god2DFast.recordOpen( "Tuxla3D_settings", true );
            if( god2DFast.recordGetNum() <= 0 )
            {
                god2DFast.recordClose();
                return false;
            } 
            
            byte data[] = god2DFast.recordRead( 1 );
            
            m_menuGameDetails = data[0];
            m_currentGameDetails = data[1];
            m_bIsThereSavedGame = ( 0 != data[2] );
            System.arraycopy( data, 3, m_playername, 0, m_playername.length );
            
            god2DFast.recordClose();  
            
            return true;
        }
        catch(Exception e)
        {
            try
            {
                god2DFast.recordClose();
            }
            catch(Exception ex)
            {}
            
            //@ IF DEBUG
            System.err.println("loadSettings() :" + e.toString());
            //@ ENDIF
        }
        
        return false;
    }
    
    /**
     * @Desc: save game settings
     *
     */
    private static void saveSettings()
    {
        try
        {
            god2DFast.recordOpen( "Tuxla3D_settings", true );
            if( god2DFast.recordGetNum() > 0 )
            {
                god2DFast.recordClose();
                god2DFast.recordDelete("Tuxla3D_settings");
                god2DFast.recordOpen("Tuxla3D_settings", true);
            }
            
            byte data[] = new byte[3 + m_playername.length];
            
            data[0] = (byte)m_menuGameDetails;
            data[1] = (byte)m_currentGameDetails;
            data[2] = (byte)( m_bInGame ? 1 : 0 );
            System.arraycopy( m_playername, 0, data, 3, m_playername.length );
            
            god2DFast.recordWrite( data );
            god2DFast.recordClose();            
        }
        catch(Exception e)
        {
            try
            {
                god2DFast.recordClose();
            }
            catch(Exception ex)
            {}
            
            //@ IF DEBUG
            System.err.println("saveSettings() :" + e.toString());
            //@ ENDIF
        }             
    }
    
    /**
     * @Desc: check&save player score
     *
     */
    private static void saveScores()
    {
        if ( !m_scLoaded )
        {
            loadScores();
        }
        
        if ( m_score != 0 )
        {
            for( int i = 0; i < 5; i++ )
            {
                if ( m_scPlayer_scores[i] > m_score )
                    continue;
                
                for( int j = 5 - 1; j > i; j-- )
                {
                    m_scPlayer_names[j] = m_scPlayer_names[j - 1];
                    m_scPlayer_scores[j] = m_scPlayer_scores[j - 1];
                }

                m_scPlayer_names[i] = new byte[m_inputBoxData[1]];
                System.arraycopy(   m_playername, 0, 
                                    m_scPlayer_names[i], 0, 
                                    m_inputBoxData[1] );
                m_scPlayer_scores[i] = m_score;
                
                break;
            }
  
            try
            {
                god2DFast.recordOpen( "Tuxla3D", true );
                if( god2DFast.recordGetNum() > 0 )
                {
                    god2DFast.recordClose();
                    god2DFast.recordDelete("Tuxla3D");
                    god2DFast.recordOpen("Tuxla3D", true);
                }            
    
                byte data[] = new byte[ 75 ]; // (5 * 1) + (5 * 10) + ( 5 * 4 )
                int offset = 0;
                int len = 0;
             
                for( int i = 0; i < 5; i++ )
                {
                    len = m_scPlayer_names[i].length;
                    data[offset++] = (byte)len;
                             
                    System.arraycopy( m_scPlayer_names[i], 0,
                            data, offset,
                            len );
                    
                    offset += len;
                     
                    data[offset++] = (byte) ((m_scPlayer_scores[i] >> 24) & 0xff);
                    data[offset++] = (byte) ((m_scPlayer_scores[i] >> 16) & 0xff);
                    data[offset++] = (byte) ((m_scPlayer_scores[i] >> 8) & 0xff);
                    data[offset++] = (byte) ((m_scPlayer_scores[i]) & 0xff);
                    
                    //@ IF DEBUG
                    System.err.println("[saved] PLAYER " + i + "NAME: " + m_scPlayer_names[i] + " SCORE:" + m_scPlayer_scores[i] );
                    //@ ENDIF                
                }            
    
                god2DFast.recordWrite( data );
                god2DFast.recordClose();
            }
            catch(Exception e)
            {
                try
                {
                    god2DFast.recordClose();
                }
                catch(Exception ex)
                {}
                
                //@ IF DEBUG
                System.err.println("saveScores() :" + e.toString());
                //@ ENDIF
            }     
        
        }
        
    }
    
    /**
     * @Desc: load saved scores
     */
    private static void loadScores()
    {
        
        m_scLoaded = false;
        
        try
        {
            god2DFast.recordOpen( "Tuxla3D", true );
            if( god2DFast.recordGetNum() <= 0 )
            {
                god2DFast.recordClose();
                m_scLoaded = true;
                return;
            }
            
            byte data[] = god2DFast.recordRead( 1 );
            int offset = 0;
            int len = 0;
            
            for( int i = 0; i < 5; i++ )
            {
                len = data[offset++];
                
                m_scPlayer_names[i] = new byte[len];
                System.arraycopy( data, offset,
                        m_scPlayer_names[i], 0,
                        len );
                
                offset += len;
                 
                m_scPlayer_scores[i] = (int)(data[offset++] & 0xff) << 24;
                m_scPlayer_scores[i] |= (int)(data[offset++] & 0xff ) << 16;
                m_scPlayer_scores[i] |= (int)(data[offset++] & 0xff ) << 8;
                m_scPlayer_scores[i] |= (int)(data[offset++] & 0xff );
                
                //@ IF DEBUG
                System.err.println("[loaded] PLAYER " + i + "NAME: " + m_scPlayer_names[i] + " SCORE:" + m_scPlayer_scores[i] );
                //@ ENDIF
            } 
        
            god2DFast.recordClose();
            
            m_scLoaded = true;
        }
        catch(Exception e)
        {
            try
            {
                god2DFast.recordClose();
            }
            catch(Exception ex)
            {}
            
            //@ IF DEBUG
            System.err.println("loadScores() :" + e.toString());
            //@ ENDIF
        }              
        
    }
    
    /**
     * @Desc: clear/reset menu vars
     *
     */
    public static void reinitMenu()
    {
        m_menuCurIdx = 0;
        m_menuGameDetails = Defs.DETAILS_MEDIUM;
    }
    
    /**
     * @Desc: reset game properties
     */     
    public static void reinitGame()
    {
        m_bInGame = false;
        m_tempScore = 0;
        m_score = 0;
        m_level = 0;
        m_nextBigScore = Defs.BIG_SCORE_OFFEST;
        m_clrIndexCube = 0;
        m_clrIndexCubeNext = god2D.mathGetRandom( m_rnd, MAX_CUBES );
        
        m_xCube = 0;
        m_yCube = 0.0f;
        m_yEndPosCub = 0;
        m_speedInitial = 0.8f;
        m_speedCube = 0.5f;
        m_boomStarted = false;
        m_zoomIn = true;
        
        m_totals = 0;
        
        // put player name into the index array
        for( int i = 0; i < _texts[texts.text_name].length; i++ )
            m_playername[i] = _texts[texts.text_name][i];          
        
        // --- cleanup board
        for( int i = 0; i < BOARD_SIZE; i++ )
            for( int j = 0; j < BOARD_SIZE; j++ )
            {
                m_matrix[i][j] = MATRIX_EMPTY;
                
                if ( m_cubeMatrix[i][j] != null )
                {
                    god3D.g_World.removeChild( m_cubeMatrix[i][j] );
                    m_cubeMatrix[i][j] = null;
                }
            }
        
        
        long lCurTime = System.currentTimeMillis() + 500;
        
        System.gc();
        while( lCurTime - System.currentTimeMillis() > 0 )
        {
            try
            {
                Thread.sleep(100);
            }
            catch( Exception e )
            {
                //@ IF DEBUG
                System.err.println("reinitGame() : Thread.sleep() !!" + e.toString() );
                //@ ENDIF
            }
        }

    }
    
    /**
     * @Desc: reinit game graphic details
     */
    private static void reinitDetails()
    {
        if ( m_currentGameDetails != m_menuGameDetails )
        {
            switch( m_menuGameDetails )
            {
                //@ IF RELEASE_TESTS
                case Defs.DETAILS_DEBUG:
                    m_meshBackGround.getAppearance( 0 ).setTexture( 0 , null );
                    m_meshFrontGround.getAppearance( 0 ).setTexture( 0, null );
                    
                    for( int i = 0; i < MAX_CUBES; i++ )
                    {
                        m_cubes[i].getAppearance( 0 ).setTexture( 0, null );
                    }                    
                    break;
                //@ ENDIF
                
                case Defs.DETAILS_HIGH:

                    m_meshBackGround.getAppearance( 0 ).setTexture( 0 , m_res_texBack );
                    m_meshFrontGround.getAppearance( 0 ).setTexture( 0, m_res_texFront );
                    
                    for( int i = 0; i < MAX_CUBES; i++ )
                    {
                        //m_cubes[i].getAppearance( 0 ).setTexture( 0, m_texColors[i] );
                        m_texColors[i].setFiltering( Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR );
                    }

                    break;
                    
                case Defs.DETAILS_MEDIUM:
                    
                    if ( Defs.DETAILS_LOW == m_currentGameDetails )
                    {
                        m_meshBackGround.getAppearance( 0 ).setTexture( 0 , m_res_texBack );
                        m_meshFrontGround.getAppearance( 0 ).setTexture( 0, m_res_texFront );
                    }
                    else
                    {
                        //comes from HIGH
                        for( int i = 0; i < MAX_CUBES; i++ )
                        {
                            m_texColors[i].setFiltering( Texture2D.FILTER_NEAREST, Texture2D.FILTER_NEAREST );
                        }                     
                    }
                    
                    break;
                    
                case Defs.DETAILS_LOW:
                    
                    m_meshBackGround.getAppearance( 0 ).setTexture( 0 , null );
                    m_meshFrontGround.getAppearance( 0 ).setTexture( 0, null );
                    
                    for( int i = 0; i < MAX_CUBES; i++ )
                    {
                        m_texColors[i].setFiltering( Texture2D.FILTER_NEAREST, Texture2D.FILTER_NEAREST );
                    }                    
                    
                    break;
            }
            
            m_currentGameDetails = m_menuGameDetails;
        }
        
    }
    
    /**
     * @Desc: reaload game info from RMS (if any) and continue
     *        if we're just paused to mainmenu, then unpause!
     */
    private static void continueGame()
    {
        if ( m_bInGame )
        {
            setGameState( Defs.GS_GAMEPLAY );
        }
        else
        {
            if ( m_bIsThereSavedGame && loadContinueGame() )
            {
                m_isFallCube = true;
                m_bInGame = true;
                setGameState( Defs.GS_GAMEPLAY );
            }
        }
    }
    
    /**
     * @Desc: initialize/load scene graphics
     */
    private static Texture2D m_res_texBack = null; 
    private static Texture2D m_res_texFront = null;
    private static Appearance m_app = null;
    private static PolygonMode m_pm = null;
    private static long m_splashTimeOut = 0;
    private static void initScene()
    {
 
        try 
        {
            switch( m_progressLoading )
            {
                
                //////////////////////////////////
                // load&init Font
                case 0:
                    
                    m_font = new Sprite2D();
                    m_font.loadImage( Image.createImage(images.font) );
                    m_font.setClipped( fontClipData,3 );
                    god2DFast.textSetCurrent(m_font, 0, 22, 84 );
                    
                    m_progressLoading++;
                    break;
                    
                //////////////////////////////////    
                // load menu graphics
                case 1:
                    
                    
                    //@ IF 176x208
                    m_menuKolona = Image.createImage( images.menu_bar_kolona );
                    m_menuBarTop = Image.createImage( images.menu_bar_top );
                    m_menuBarTopInGame = Image.createImage( images.menu_bar_top_notclean );
                    m_menuBarBottom = Image.createImage( images.menu_bar_bottom );
                    //@ ELSE
                    
                        //@ IF 176x220
                        m_menuKolona = Image.createImage( images.menu_bar_kolona_176x220 );
                        m_menuBarTop = Image.createImage( images.menu_bar_top );
                        m_menuBarTopInGame = Image.createImage( images.menu_bar_top_notclean );
                        m_menuBarBottom = Image.createImage( images.menu_bar_bottom );
                        //@ ELSE
                        
                            //@ IF 240x320
                            m_menuKolona = Image.createImage( images.menu_bar_kolona_240x320 );
                            m_menuBarTop = Image.createImage( images.menu_bar_top_240x320 );
                            m_menuBarTopInGame = Image.createImage( images.menu_bar_top_notclean_240x320 );
                            m_menuBarBottom = Image.createImage( images.menu_bar_bottom_240x320 );
                            //@ ENDIF
                        
                        //@ ENDIF
                    
                    //@ ENDIF
                    
                    m_menuImgTopBorder = Image.createImage( images.menu_top );
                    m_menuImgBottomBorder = Image.createImage( images.menu_bottom );
                    m_menuImgLeftBorder = Image.createImage( images.menu_left );
                    m_menuImgRightBorder = Image.createImage( images.menu_right );
                    m_menuImgTopLeftCorner = Image.createImage( images.menu_corner_top_left );
                    m_menuImgTopRightCorner = Image.createImage( images.menu_corner_top_right );
                    m_menuImgBottomLeftCorner = Image.createImage( images.menu_corner_bottom_left );
                    m_menuImgBottomRightCorner = Image.createImage( images.menu_corner_bottom_right );
                    m_menuUpDownIndicator = Image.createImage( images.arrow_updown );
                    
                    m_menuFont = new Sprite2D();
                    m_menuFont.loadImage( Image.createImage( images.menu_texts ) ) ;
                    int cdataLines[] = { 18 };
                    m_menuFont.setClipped( cdataLines , 4 );
                    
                    m_menuSelector = new Sprite2D();
                    m_menuSelector.loadImage( Image.createImage( images.menu_arrows ) );
                    cdataLines[0] = 2;
                    m_menuSelector.setClipped( cdataLines, 2 );   
                    
                    m_menuButton = new Sprite2D();
                    m_menuButton.loadImage( Image.createImage( images.menu_button ) );
                    m_menuButton.setClipped( cdataLines, 2 );
                    
                    m_progressLoading++;
                    break;

                
                //////////////////////////////////
                // init background plane
                case 2:
                    
                    m_res_texBack = new Texture2D(
                            new Image2D( Image2D.RGB, Image.createImage( images.bricks1 ) )
                            );
                    m_res_texBack.setWrapping( Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT );
                    m_res_texBack.setBlending( Texture2D.FUNC_REPLACE );
                    
                    m_pm = new PolygonMode();
                    m_pm.setPerspectiveCorrectionEnable( true );

                    m_app = new Appearance();
                    m_app.setPolygonMode( m_pm );
                    
                    m_meshBackGround = god3D.createPlaneXY( 256, m_app, m_res_texBack, (byte)4, 0x945239 );
                    //m_meshBackGround = god3D.createPlaneXY( 260, app, null, (byte)4 );
                    m_meshBackGround.translate( 85, -65, -40 );
//                    m_meshBackGround = god3D.createPlaneXY( 255, app, texBack, (byte)4 );
//                    m_meshBackGround.translate( 91, -75, -50 );            
                    
                    god3D.g_World.addChild( m_meshBackGround );                    
                    
                    m_progressLoading++;
                    break;
                    
                //////////////////////////////////
                // init front-ground plane
                case 3:
                    
                    m_res_texFront = new Texture2D(
                            new Image2D( Image2D.RGB, Image.createImage( images.grass ) )
                            );
                    m_res_texFront.setWrapping( Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT );
                    m_res_texFront.setBlending( Texture2D.FUNC_REPLACE );

                    m_pm = new PolygonMode();
                    m_pm.setPerspectiveCorrectionEnable( true );
                    
                    m_app = new Appearance();
                    m_app.setPolygonMode( m_pm );
                    
                    m_meshFrontGround = god3D.createPlaneXZ( 255, m_app, m_res_texFront, 0x008c002 );
                    m_meshFrontGround.translate( 84, -202, 55 );
                    god3D.g_World.addChild( m_meshFrontGround );                     
                    
                    m_progressLoading++;
                    break;
                    
                //////////////////////////////////
                // load cube textures & previews
                case 4:
                    
                    for( int i = 0; i < MAX_CUBES; i++ )
                    {
                        m_imgPreview[i] = Image.createImage( images.smallface[i] );
                        
                        m_texColors[i] = new Texture2D( 
                                new Image2D( Image2D.RGB, Image.createImage( images.boxface[i] ) )
                        );
                        m_texColors[i].setBlending( Texture2D.FUNC_REPLACE );
                        m_texColors[i].setFiltering( Texture2D.FILTER_NEAREST, Texture2D.FILTER_NEAREST );
                    }                    
                    
                    
                    //@ IF EXPLOSION
                    m_app = new Appearance();
                    CompositingMode cmp = new CompositingMode();
                    cmp.setBlending( CompositingMode.ALPHA);
                    m_app.setCompositingMode( cmp );
                    
                    for( int i = 0; i < 6; i++)
                    {
                        m_texBoom[i] = new Texture2D( 
                                new Image2D( Image2D.RGBA, Image.createImage( images.boom[i] ) )
                                );

                    }
                    
                    m_meshBoom = god3D.createPlaneXY( 50, m_app, m_texBoom[0], (byte)1, -1 );
                    cmp = null;
                    //@ ENDIF

                    m_progressLoading++;
                    break;
            

                //////////////////////////////////
                // create cubes mesh data
                case 5:
                    
                    //// ---    
                    
                    for( int i = 0; i < MAX_CUBES; i++)
                    {
                        m_cubes[i] = god3D.createBox((byte)10, m_texColors[i], _CLRS[i] );
                    }                    
                    
                    m_progressLoading++;
                    break;
                    
                case 6:
                    
                    //@ IF DEBUG
                    for( int i = 0; i < BOARD_SIZE; i++ )
                    {
                        for( int j = 0; j < BOARD_SIZE; j++ )
                        {
                            float nx = _COLS[j];
                            float ny = _ROWS[i];
                       
//                            Mesh meshBox = god3D.createBoxTextured((byte)10, m_texColors[j], _CLRS[i]);
//                            meshBox.translate( nx, ny, 0.0f );
//                            god3D.g_World.addChild( meshBox );
                        }
                    }
                    //@ ENDIF
                    
                    m_app = null;
                    m_pm = null;        
                    
                    // -------------- setup camera -----------------------
                    //-103.0f
                    god3D.createCamera( 85.0f, -100.0f, 207.0f, god3D.g_screenwidth, god3D.g_screenheight );
                    //god3D.createCamera( -20.0f, -3.5f, 5.0f, god3D.g_screenwidth, god3D.g_screenheight );
                    
                    //@ IF LIGHTS
                    god3D.g_graphics3D.resetLights();
                    Light liAmbient = new Light();
                    liAmbient.setColor( 0xffffff );
                    liAmbient.setMode( Light.AMBIENT );
                    liAmbient.setIntensity( 1.0f );
                    god3D.g_World.addChild( liAmbient );
                    
                    m_light = new Light();
                    m_light.setColor( 0xaaaaaa );
                    m_light.setIntensity( 0.8f );
                    m_light.setMode( Light.OMNI );
                    //m_light.setOrientation( -60.0f, 1.01f, 0.0f, 0.0f );
                    m_light.translate( 100.0f, -100.0f, -65.0f );
                    god3D.g_World.addChild( m_light );

                        //@ IF DEBUG        
                        m_cubeLight = god3D.createBox( (byte) 1, null, 0xffffff );
                        m_light.getTranslation( m_pos );
                        m_cubeLight.setTranslation( m_pos[0] , m_pos[1], 0.0f );
                
                        god3D.g_World.addChild( m_cubeLight );
                        //@ ENDIF
                        
                    //@ ENDIF
                        
                    
                    // --- LOAD GAME SETTINGS 
                        
                    if( loadSettings() )
                    {
                        m_currentGameDetails = Defs.DETAILS_NOTSET;
                        reinitDetails();
                    }
                    
                    m_splashTimeOut = System.currentTimeMillis() + 1500;
       
                    m_progressLoading++;
                    break;
                    
                case 7:
                    m_progressLoading++;
                    break;
                    
                case 8:
                    if ( m_splashTimeOut < System.currentTimeMillis() )
                    {
//                     menuCreateInputBox();
//                     m_ibFocused = true;
//                     m_menuCurIdx = 1;
//                     setGameState( Defs.GS_MENU_ENTERNAME );

                        setGameState(Defs.GS_MENU);
                        
                        m_progressLoading++;
                    }
                    break;
            }
        }
        catch( Exception e )
        {
            //@ IF DEBUG
            System.out.print("\n [ERROR] Loading: = " + e.toString());
            Alert alert = new Alert("", "[ERROR] Loading: = " + e.toString(), null, AlertType.ERROR);
            Display.getDisplay(Main._MIDLET).setCurrent(alert);
            //@ ENDIF
        }        
        
        //@ IF DEBUG
        System.err.println("[LOADING_PROGRESS] " + m_progressLoading + " finished !" );
        //@ ENDIF
    }
    
    /**
     * @Desc: create falling cube on random x-axis position
     */ 
    private static void createCubeFall()
    {
        setGameState( Defs.GS_GAMEPLAY );
        
        if ( m_isFallCube )
        {
            god3D.g_World.removeChild( m_meshCube );
            m_meshCube = null;
        }
                
        //m_xCube = god2D.mathGetRandom( m_rnd, BOARD_SIZE );
        m_yCube = START_ROW;
        
        m_speedCube = m_speedInitial;
        m_rotSpeedCube = m_speedCube + ((m_rnd.nextFloat() + 0.5f ) * m_speedCube); 
        m_isFallCube = true;
        
        // generate next random cube index
        m_clrIndexCube = m_clrIndexCubeNext;
        m_clrIndexCubeNext = god2D.mathGetRandom( m_rnd, MAX_CUBES );

        // generate next falling cube
        m_meshCube = (Mesh)m_cubes[m_clrIndexCube].duplicate();
        m_clrIndexCube = _CLRS[ m_clrIndexCube ];
        
        genereateXPos();
        recalcFallPos();        
        
        m_meshCube.translate( _COLS[m_xCube], m_yCube, 0.0f );
        god3D.g_World.addChild( m_meshCube );
                
        //@ IF MESHPREVIEW_3D
        if ( m_meshNextCube != null)
        {
            god3D.g_World.removeChild( m_meshNextCube );
        }
        m_meshNextCube = (Mesh)m_cubes[m_clrIndexCubeNext].duplicate();
        m_meshNextCube.translate( 180.0f, 15.0f, 0.0f );
        m_meshNextCube.scale( 0.6f, 0.6f, 0.6f );
        m_meshNextCube.postRotate( (m_rnd.nextFloat() * 80.0f), 0.7f, 0.5f, 0.8f );
        god3D.g_World.addChild( m_meshNextCube );
        //@ ENDIF
        
        //@ IF DEBUG
        System.err.println("[CUBE CREATED] idx: " + m_clrIndexCube);
        //@ ENDIF
        
        //! bug fix (repoted by Obi)
        KeyMan.Released = 0;
        KeyMan.Pressed = 0;
        
    }
    
    /**
     * @Desc:
     *
     */
    private static void genereateXPos()
    {
        m_xCube = -1;
        m_tempVal = -1;
        for( int i = 0; i < BOARD_SIZE; i++ )
        {
            if ( m_matrix[0][i] == MATRIX_EMPTY )
            {
                m_xCube = i;
                //break;
            }
            else if ( i != 0 && i != (BOARD_SIZE - 1) )
            {
                m_tempVal = 1; // there is at least one FULL column between the edges of the board
            }
        }

        if ( -1 == m_xCube )
        {
            //game over
            m_xCube = god2D.mathGetRandom( m_rnd, BOARD_SIZE );
        }
        else
        {
            m_xCube = -1;
            
            //try to find a spot with a matching color of the current spawned cube
            if ( -1 != m_tempVal )
            {
                for( int i = 0; i < BOARD_SIZE; i++ )
                {
                    if ( m_matrix[0][i] == MATRIX_EMPTY )
                    { 
                        for( int j = 1; j < BOARD_SIZE; j++ )
                        {
                            if ( m_matrix[j][i] == m_clrIndexCube  )
                            {
                                m_xCube = i;
                                //@ IF DEBUG
                                System.err.println("[MATCH FREE SPACE] at: (" + i + "," + j + ") !" );
                                System.err.println("m_matrix[j][i]=" + m_matrix[j][i] + " m_clrIndexCube=" + m_clrIndexCube);
                                //@ ENDIF                              
                                break;
                            }
                        }
                        
                        if ( -1 != m_xCube )
                            break;
                    }
                }
            }
            
            if ( -1 == m_xCube )
            {
                m_tempVal = 1000; // prevent deadloop!
                while(m_tempVal-- > 0)
                {
                    m_xCube = god2D.mathGetRandom( m_rnd, BOARD_SIZE );
                    if ( m_matrix[0][m_xCube] == MATRIX_EMPTY )
                        break;
                }
                
                //@ IF DEBUG
                if ( m_tempVal <= 0 )
                {
                    System.err.println("[FREE SPACE] FOUND - FAILED ***!");
                }
                //@ ENDIF    
            }
        }
    }
    
    /**
     * @Desc: determine where falling cube should stop
     * @Note: uses setGameState() 
     */
    private static void recalcFallPos()
    {
        if ( m_matrix[0][m_xCube] != MATRIX_EMPTY )
        {
            m_menuCurIdx = 5;
            setGameState( Defs.GS_GAMEOVER );

            //@ IF DEBUG
            System.err.println("[GAME OVER]");
            //@ ENDIF
        }
        else
        {
            for( int i = 1; i < BOARD_SIZE; i++ )
            {
                if ( m_matrix[i][m_xCube] != MATRIX_EMPTY )
                {
                    m_yEndPosCub = i - 1; // ! 0 -> excpt
                    return;
                }
            }
    
            m_yEndPosCub = BOARD_SIZE - 1;
        }
    }

    /**
     * @Desc: visualize falling cube
     */
    private static void updateCubeFall()
    {
        if ( m_isFallCube )
        {
            m_yCube -= m_speedCube;
            m_meshCube.translate( 0.0f, -m_speedCube, 0.0f );
            m_meshCube.postRotate( m_rotSpeedCube, 0.0f, 0.8f, 1.0f );
                        
            if ( m_yCube <= _ROWS[m_yEndPosCub] )
            {
                m_matrix[m_yEndPosCub][m_xCube] = m_clrIndexCube;
                m_cubeMatrix[m_yEndPosCub][m_xCube] = m_meshCube;                
                
                m_meshCube.getTranslation( m_pos );
                m_meshCube.setTranslation( m_pos[0], _ROWS[m_yEndPosCub], m_pos[2] );                
                m_isFallCube = false;
               
                //@ IF DEBUG
                System.err.println("[CUBE STOPPED]");
                //@ ENDIF 
                
                m_bFoundCombination = false; //!Important
                m_tempScore = 0;
                
                detectCombos( m_yEndPosCub, m_xCube );
                
                if ( m_bFoundCombination )
                {
                    // update player score here!
                    m_score += m_tempScore;

                    setGameState( Defs.GS_GAMEPLAY_ZOOMEXPLOSION );
                }
                else
                {
                    setGameState( Defs.GS_GAMEPLAY_CREATECUBE );
                }
            }
        }
    }
    
    /**
     * @Desc: animate the cubes falling down
     * 
     * @param: move_pos - LEFT or RIGHT softkey
     */
    private static void moveCubeFall( int move_pos )
    {
        boolean bMoved = false;
        
        // do not calculate if it isn't in the matrix yet
        if ( m_yCube > START_ROW || !m_isFallCube )
        {
            switch( move_pos )
            {
                case KeyMan.LEFT:
                    
                    if ( m_xCube > 0 )
                    {
                        m_xCube--;
                        bMoved = true;
                    }
                    break;
                    
                case KeyMan.RIGHT:
                    
                    if ( m_xCube < BOARD_SIZE - 1 )
                    {
                        m_xCube++;
                        bMoved = true;
                    }
                    break;
            }            
        }
        else
        {

            // check row position
            int row = 0;
            for( int i = (BOARD_SIZE - 1); i > 0; i-- )
            {
                // issue fix (moving below neighbour cube) - reported by Patter 
                if( (m_yCube - 20.0f) <= _ROWS[i] )
                {
                    row = i;
                    break;
                }
            }

            // check and move, if allowed
            switch( move_pos )
            {
                case KeyMan.LEFT:
                    
                    if ( m_xCube > 0 )
                    {
                        if( MATRIX_EMPTY == m_matrix[row][m_xCube - 1] )
                        {
                            m_xCube--;
                            bMoved = true;
                        }
                    }
                    break;
                    
                case KeyMan.RIGHT:
                    
                    if ( m_xCube < BOARD_SIZE - 1 )
                    {
                        if( MATRIX_EMPTY == m_matrix[row][m_xCube + 1] )
                        {
                            m_xCube++;
                            bMoved = true;
                        }                
                    }
                    break;
            }
        }
        
        if ( bMoved )
        {
            // update geometry
            m_meshCube.getTranslation( m_pos );
            m_meshCube.setTranslation( _COLS[ m_xCube ], m_pos[1], m_pos[2] );
            
            recalcFallPos();
        }
    }
    
    /**
     * @Desc: calculates new score and increases fall speed if needed
     * 
     * @param pawned_cubes
     */
    private static void giveScore( int pawned_cubes )
    {
        if ( pawned_cubes <= 3 )
        {
            m_tempScore += pawned_cubes * Defs.SCORE_RATIO;
        }
        else
        {
            //!bonus for more 4, 5 in a row(col)
            
            m_tempScore += pawned_cubes * Defs.SCORE_RATIO + ((pawned_cubes - 3 + m_level) * Defs.SCORE_RATIO);
        }
        
        //! not pretty, but game shouldn't go that high anyway !
        if ( m_tempScore > 31000 )
            m_tempScore = 31000;
     
        if ( (m_score + m_tempScore) > m_nextBigScore )
        {
            m_level++;

            if ( m_level < 6 )
            {
                m_speedInitial += 0.075f;
            }
            else
            {
                m_speedInitial += 0.035f;
            }

            //m_speedInitial += (m_speedInitial * 0.15f);
            
            m_nextBigScore += Defs.BIG_SCORE_OFFEST;
            if ( m_nextBigScore > 32000 )
                m_nextBigScore = 32000;
        }
    }
    
    /**
     * @Desc: Performs search algorythms for color combination patterns.
     *        If patterns are found, the blocks are destroyed and score
     *        is assigned.
     *        
     *  @param: row - row the falling cube is positioned
     *  @param: col - column the falling cube is positioned
     */
    private static void detectCombos( int row, int col )
    {
        
        m_totals = 1;

        //*** CHECK HORIZONTAL 
        for( int i = 1; i <= col; i++ )
        {
            if ( m_matrix[row][col - i] != m_clrIndexCube )
                break;
            
            m_rows[m_totals] = row;
            m_cols[m_totals] = col - i;
            m_totals++;                

            //@ IF DEBUG
            System.err.println("[H_left]      row:" + m_rows[i] + "col:" + m_cols[i] );
            //@ ENDIF
        }
        
        for( int i = 1; i < (BOARD_SIZE - col); i++ )
        {
            if ( m_matrix[row][col + i] != m_clrIndexCube )
                break;
            
            m_rows[m_totals] = row;
            m_cols[m_totals] = col + i;
            m_totals++;                
                
            //@ IF DEBUG
            System.err.println("[H_right]      row:" + m_rows[i] + "col:" + m_cols[i] );
            //@ ENDIF
        }
        m_bFoundCombination = (m_totals > 2);
        
        
        //*** CHECK VERTICAL
        if ( !m_bFoundCombination )
        {
            m_totals = 1;
             
            //NOTE: should it be checked !!?!
            for( int i = 1; i <= row; i++ )
            {
                if ( m_matrix[row - i][col] != m_clrIndexCube )
                    break;
                
                m_rows[m_totals] = row - i;
                m_cols[m_totals] = col;
                m_totals++;                
    
                //@ IF DEBUG
                System.err.println("[H_up]      row:" + m_rows[i] + "col:" + m_cols[i] );
                //@ ENDIF
            }
            
            for( int i = 1; i < (BOARD_SIZE - row); i++ )
            {
                if ( m_matrix[row + i][col] != m_clrIndexCube )
                    break;
                
                m_rows[m_totals] = row + i;
                m_cols[m_totals] = col;
                m_totals++;                
                    
                //@ IF DEBUG
                System.err.println("[H_down]      row:" + m_rows[i] + "col:" + m_cols[i] );
                //@ ENDIF
            }        
            
            m_bFoundCombination = (m_totals > 2);
        }
        
        //*** CHECK DIAGONAL -> '/' 
        if ( !m_bFoundCombination )
        {
            m_totals = 1;
            
            // Up-Right
            for( int i = 1; i < BOARD_SIZE; i++ )
            {
                row1 = row - i;
                col1 = col + i;

                if( row1 < 0 || col1 >= BOARD_SIZE )
                    break;

                if( m_matrix[row1][col1] != m_clrIndexCube )
                    break;

                m_rows[m_totals] = row1;
                m_cols[m_totals] = col1;
                m_totals++;

                //@ IF DEBUG
                System.err.println("[DIAG_UR]      row:" + m_rows[i] + "col:" + m_cols[i]);
                //@ ENDIF
            }
            
            // Left-Down
            for( int i = 1; i < BOARD_SIZE; i++ )
            {
                row1 = row + i;
                col1 = col - i;

                if( row1 >= BOARD_SIZE || col1 < 0 )
                    break;

                if( m_matrix[row1][col1] != m_clrIndexCube )
                    break;

                m_rows[m_totals] = row1;
                m_cols[m_totals] = col1;
                m_totals++;

                //@ IF DEBUG
                System.err.println("[DIAG_LD]      row:" + m_rows[i] + "col:" + m_cols[i]);
                //@ ENDIF
            }            
            
            m_bFoundCombination = (m_totals > 2);
        }        
        
        //*** CHECK DIAGONAL -> '\' 
        if ( !m_bFoundCombination )
        {
            m_totals = 1;
            
            // Up-Left
            for( int i = 1; i < BOARD_SIZE; i++ )
            {
                row1 = row - i;
                col1 = col - i;

                if( row1 < 0 || col1 < 0 )
                    break;

                if( m_matrix[row1][col1] != m_clrIndexCube )
                    break;

                m_rows[m_totals] = row1;
                m_cols[m_totals] = col1;
                m_totals++;

                //@ IF DEBUG
                System.err.println("[DIAG_UL]      row:" + m_rows[i] + "col:" + m_cols[i]);
                //@ ENDIF
            }
            
            // Right-Down
            for( int i = 1; i < BOARD_SIZE; i++ )
            {
                row1 = row + i;
                col1 = col + i;

                if( row1 >= BOARD_SIZE || col1 >= BOARD_SIZE )
                    break;

                if( m_matrix[row1][col1] != m_clrIndexCube )
                    break;

                m_rows[m_totals] = row1;
                m_cols[m_totals] = col1;
                m_totals++;

                //@ IF DEBUG
                System.err.println("[DIAG_RD]      row:" + m_rows[i] + "col:" + m_cols[i]);
                //@ ENDIF
            }            
            
            m_bFoundCombination = (m_totals > 2);
        }        
        
        
        // finalize checkings ...
        
        if ( m_bFoundCombination )
        {
            m_rows[0] = row;
            m_cols[0] = col;
            
            giveScore( m_totals );
            
            //@ IF DEBUG
            System.err.println("[COMBINATION FOUND] total cubes: " + m_totals );
            //@ ENDIF

            m_zoomType = getExplosionZone( row, col );
            startZoomExplosion();            
         }
    }
    
    /**
     * @Desc: remove cubes that are marked for deletion
     * 
     * @note!: row(0-2) , col(0-2) must be valid values set 
     */
    private static void cleanupCubes() //throws Exception
    {
        for( int i = 0; i < m_totals; i++ )
        {
            god3D.g_World.removeChild( m_cubeMatrix[ m_rows[i] ][ m_cols[i] ] );
            m_cubeMatrix[ m_rows[i] ][ m_cols[i] ] = null;
            m_matrix[ m_rows[i] ][ m_cols[i] ] = MATRIX_EMPTY;

        }
    }
    
    /**
     * @Desc: get cube position of virtual explosion table
     * 
     * @param: row - cube row pos
     * @param: col - cube col pos
     * @return: zone Id
     */
    private static int getExplosionZone( int row, int col )
    {
        int zone = 0;
        
        switch( col )
        {
            case 0:
            case 1:
                
                switch( row )
                {
                    case 0:
                    case 1:
                    case 2:
                        zone = Defs.ZONE_LEFT_UP;
                        break;
                        
                    case 3:
                    case 4:
                    case 5:
                        zone = Defs.ZONE_LEFT_DOWN;
                        break;
                        
                    default:
                        //@ IF DEBUG
                        System.err.println("[ERROR] Incorrect at c(0,1) ROW: " + row );
                        //@ ENDIF                        
                }                

                break;
                
            case 2:
            case 3:
                
                switch( row )
                {
                    case 0:
                    case 1:
                    case 2:
                        zone = Defs.ZONE_MIDDLE_UP;
                        break;
                        
                    case 3:
                    case 4:
                    case 5:
                        zone = Defs.ZONE_MIDDLE_DOWN;
                        break;
                        
                    default:
                        //@ IF DEBUG
                        System.err.println("[ERROR] Incorrect c(2,3) ROW: " + row );
                        //@ ENDIF                        
                }
                
                break;
                
            case 4:
            case 5:
                
                switch( row )
                {
                    case 0:
                    case 1:
                    case 2:
                        zone = Defs.ZONE_RIGHT_UP;
                        break;
                        
                    case 3:
                    case 4:
                    case 5:
                        zone = Defs.ZONE_RIGHT_DOWN;
                        break;
                        
                    default:
                        //@ IF DEBUG
                        System.err.println("[ERROR] Incorrect at r(4,5) ROW: " + row );
                        //@ ENDIF                        
                }  

                break;
                
            default:
                //@ IF DEBUG
                System.err.println("[ERROR] Incorrect COL: " + col );
                //@ ENDIF
        }        
        
        return zone;
    }
    
    /**
     * @Desc: re-arrange blocks to fill empty places
     */
    private static boolean syncCubeBoard_DoIt()
    {
        boolean bFound = false;
        int idx = -1;
        
        for( int j = 0; j < BOARD_SIZE - 1; j++ )
            for( int i = 0; i < BOARD_SIZE; i++ )
            {
                if ( MATRIX_EMPTY != m_matrix[j][i] )
                {
                    idx = -1;
                    
                    // find last empty row
                    for( int k = (j + 1); k < BOARD_SIZE; k++ )
                    {
                         if ( MATRIX_EMPTY == m_matrix[k][i] )
                         {
                             idx = k;
                         }
                         else if ( MATRIX_EMPTY != m_matrix[k][i] )
                         {
                             break;
                         }
                    }
                    
                    if ( -1 != idx )
                    {
                            //$NOTE: Ok, I know this is a little weird but it saves us a bit of mem
                            //       1) We OR the MATRIX_MOVING flags with current matrix pos
                            //          to specify this block is moving
                            //       2) We OR the first tetrada to specify the index of 
                            //          the row that this cube will stop to (it stores 8 possible values)
                            //          so it's just enough for us!
                            m_matrix[j][i] |= MATRIX_MOVING;
                            m_matrix[j][i] |= (idx << 28);
                            
                            bFound = true;
                            break;
                    }
                }
            }
        
        return bFound;
    }

    /** 
     * @Desc: update re-arrangement, if done then set state back to new cube creation
     */    
    private static void syncCubeBoard_Update()
    {
        boolean bFound = false;
        int idx = 0;
        
        m_bFoundCombination = false; //!Important
        m_tempScore = 0;
        m_bNeedReSync = false;
        
        for( int j = 0; j < BOARD_SIZE - 1; j++ )
            for( int i = 0; i < BOARD_SIZE; i++ )
            {
                if ( (m_matrix[j][i] & MATRIX_MOVING) == MATRIX_MOVING )
                {
                    bFound = true;
                    
                    m_cubeMatrix[j][i].translate( 0.0f, -m_speedCube - m_speedCube, 0.0f );
                    m_cubeMatrix[j][i].postRotate( m_rotSpeedCube, 0.0f, 0.8f, 1.0f );
                    m_cubeMatrix[j][i].getTranslation( m_pos );
                    
                    idx = (m_matrix[j][i] & MATRIX_POS_MASK) >> 28;
//                    System.err.println("[MARK_PLACED_IDX] " + idx );
                    
                    if ( m_pos[1] <= _ROWS[idx] )
                    {
                        // re-position
                        m_cubeMatrix[j][i].setTranslation( m_pos[0], _ROWS[idx], m_pos[2] );
                        
                        // clean 'moving' mask
                        m_matrix[j][i] ^= MATRIX_MOVING;
                        m_matrix[j][i] ^= (idx << 28);
                        
                        // swap logical matrix pos
                        m_matrix[idx][i] = m_matrix[j][i];
                        m_matrix[j][i] = MATRIX_EMPTY;
                        
                        //@ IF DEBUG
                        System.err.println("MAT[idx][i]" + m_matrix[idx][i]);
                        System.err.println("MAT[j][i]" + m_matrix[j][i]);
                        //@ ENDIF
                        
                        // swap physical matrix pos
                        m_cubeMatrix[idx][i] = m_cubeMatrix[j][i];
                        m_cubeMatrix[j][i] = null;
                        
                        // we need to check if the new placed block created a combination
                        m_clrIndexCube = m_matrix[idx][i];
                        detectCombos( idx, i );
                        
                        m_bNeedReSync = true;
                        break;
                    }
                }
            }
        
        if ( m_bFoundCombination )
        {
            // update player score here!
            m_score += m_tempScore;
            
            setGameState( Defs.GS_GAMEPLAY_ZOOMEXPLOSION );
        }
        else
        {
            if ( !bFound )
            {
                if ( !syncCubeBoard_DoIt() )
                {
                    // indicate that we are ready to go back to gameplay
                    setGameState( Defs.GS_GAMEPLAY_CREATECUBE );
                    
                    //! bug fix (repoted by Obi)
                    KeyMan.Released = 0;
                    KeyMan.Pressed = 0;
                }
            }
        }
      
    }
    
    /**
     * @Desc: create explosion and zoom camera to sector
     */    
    private static void startZoomExplosion()
    {
//        // get current camera position&orientation
//        god3D.g_camera.getTranslation( m_camLastPos );
//        god3D.g_camera.getOrientation( m_camLastOri );
        
        if ( !m_boomStarted )
        {
            m_boomStarted = true;
            m_camLastPos = god3D.getCameraPosition();
         
            //@ IF EXPLOSION
            m_boomCurFrame = 0.0f;
            m_boomPrevFrame = 0;
            m_boomSpeed = 0.4f;
            
            switch( m_zoomType )
            {
                case Defs.ZONE_MIDDLE_UP:
                    m_meshBoom.setTranslation( 90.0f, -70.0f, 40.0f );                
                    break;
                    
                case Defs.ZONE_MIDDLE_DOWN:
                    m_meshBoom.setTranslation( 90.0f, -145.0f, 40.0f );
                    break;
                    
                case Defs.ZONE_LEFT_UP:
                    m_meshBoom.setTranslation( 52.0f, -70.0f, 40.0f );
                    break;
                    
                case Defs.ZONE_LEFT_DOWN:
                    m_meshBoom.setTranslation( 52.0f, -145.0f, 40.0f );
                    break;                
                    
                case Defs.ZONE_RIGHT_UP:
                    m_meshBoom.setTranslation( 122.0f, -70.0f, 40.0f );
                    break;
                    
                case Defs.ZONE_RIGHT_DOWN:
                    m_meshBoom.setTranslation( 122.0f, -145.0f, 40.0f );
                    break;
                    
            }
            god3D.g_World.addChild( m_meshBoom );
            //@ ENDIF
            
            curtime = System.currentTimeMillis() + Defs.FPS_CUBE_ROT;
        }
    }
    
    /**
     * @Desc: update explosion and zooming
     */
    private static long curtime = 0;
    private static void updateZoomExplosion()
    {
        
        //@ IF EXPLOSION
        //@ m_boomCurFrame += m_boomSpeed;
        //@ if ( (int)m_boomCurFrame != m_boomPrevFrame )
        //@ {
        //@    m_boomPrevFrame = (int)m_boomCurFrame;
        //@    m_meshBoom.getAppearance( 0 ).setTexture( 0, m_texBoom[m_boomPrevFrame] );
        //@ }
        //@ if ( m_boomCurFrame > 5.0f )
        //@ {
        //@    m_boomSpeed = -m_boomSpeed;
        //@ }
        //@ else if ( m_boomCurFrame <= 0.0f )
        //@ {
        //@    m_boomSpeed = -m_boomSpeed;
        //@    god3D.g_World.removeChild( m_meshBoom );                
        //@ }
        //@ ENDIF
        
        if ( m_zoomIn )
        {
            //ZOOM-IN animation
            
            if ( curtime - System.currentTimeMillis() <= 0 )
            {
                for( int i = 0; i < m_totals; i++ )
                {
                    try {
                    m_cubeMatrix[m_rows[i]][m_cols[i]].postRotate( 33.0f, 2.1f, 1.1f, 0.5f );
                    } catch( Exception e )
                    {
                        System.err.println("***FSCK*** i: " + i);
                    }
                }
                
                curtime = System.currentTimeMillis() + Defs.FPS_CUBE_ROT;
            }            
                        
            switch( m_zoomType )
            {
                case Defs.ZONE_MIDDLE_DOWN:
                    god3D.g_grpCamera.translate( 0.0f, -1.4f, -3.2f );
                    god3D.rotateCamera( god3D.DIR_UP , 0.5f );
                    break;
                    
                case Defs.ZONE_MIDDLE_UP:
                    god3D.g_grpCamera.translate( 0.0f, 1.6f, -3.2f );
                    god3D.rotateCamera( god3D.DIR_DOWN, 0.6f );
                    break;
                    
                case Defs.ZONE_LEFT_UP:
                case Defs.ZONE_LEFT_DOWN:
                    god3D.g_grpCamera.translate( 0.2f, 0.0f, -3.2f );
                    god3D.rotateCamera( god3D.DIR_LEFT , 0.3f );                    
                    break;
                    
                case Defs.ZONE_RIGHT_UP:
                case Defs.ZONE_RIGHT_DOWN:
                    god3D.g_grpCamera.translate( -0.2f, 0.0f, -3.2f );
                    god3D.rotateCamera( god3D.DIR_RIGHT, 0.3f );                     
                    break;
                    
                    
                default:
                    //@ IF DEF
                    System.err.println("incorrect zoom type - " + m_zoomType );
                    //@ ENDIF
            }
            
            // check position
            m_pos = god3D.getCameraPosition();
            
            if ( m_pos[2] < (m_camLastPos[2] - 60.0f) )
            {
                m_zoomIn = false;
            }              
        }
        else
        {
            //ZOOM-OUT animation
            
            if ( curtime - System.currentTimeMillis() <= 0 )
            {
                for( int i = 0; i < m_totals; i++ )
                {
                    m_cubeMatrix[m_rows[i]][m_cols[i]].scale( 0.9f, 0.9f, 0.9f );
                }                
       
                curtime = System.currentTimeMillis() + Defs.FPS_CUBE_ROT;
            }             
            
            switch( m_zoomType )
            {
                case Defs.ZONE_MIDDLE_DOWN:
                    god3D.g_grpCamera.translate( 0.0f, 1.4f, 3.2f );
                    god3D.rotateCamera( god3D.DIR_DOWN , 0.5f );
                    break;
                    
                case Defs.ZONE_MIDDLE_UP:
                    god3D.g_grpCamera.translate( 0.0f, -1.6f, 3.2f );
                    god3D.rotateCamera( god3D.DIR_UP , 0.6f );
                    break;
                    
                case Defs.ZONE_LEFT_UP:
                case Defs.ZONE_LEFT_DOWN:
                    god3D.g_grpCamera.translate( -0.2f, 0.0f, 3.2f );
                    god3D.rotateCamera( god3D.DIR_RIGHT , 0.3f );                    
                    break;
                    
                case Defs.ZONE_RIGHT_UP:
                case Defs.ZONE_RIGHT_DOWN:
                    god3D.g_grpCamera.translate( 0.2f, 0.0f, 3.2f );
                    god3D.rotateCamera( god3D.DIR_LEFT, 0.3f );                         
                    break;
                    
                    
                default:
                    //@ IF DEF
                    System.err.println("incorrect zoom type - " + m_zoomType );
                    //@ ENDIF
            }      
            
            m_pos = god3D.getCameraPosition();
            if ( m_pos[2] >= m_camLastPos[2] )
            {
                //god3D.g_camera.setTranslation( m_camLastPos[0], m_camLastPos[1], m_camLastPos[2] );
                //god3D.g_camera.setOrientation( m_camLastOri[0], m_camLastOri[1], m_camLastOri[2], m_camLastOri[3] );
                m_zoomIn = true;
                
                cleanupCubes();
                
                if ( m_bNeedReSync )
                {
                    // go back to rearrange board if it was not completed before!
                    setGameState( Defs.GS_GAMEPLAY_REARRANGED );
                }
                else
                {
                    setGameState( Defs.GS_GAMEPLAY_SYNCBOARD );
                }
                
                m_boomStarted = false;
            }
        }
    }    
    
    /**
     * @Desc: change game state
     */
    private static void setGameState( int newState )
    {
        m_gamestate = newState;
    }
    
    /**
     * @Desc:
     */
    private static final byte[] convertText( byte[] textData )
    {
        byte[] textOut = new byte[textData.length];
        char c;
        
        for( int i = 0; i < textData.length; i++)
        {
            c = (char)textData[i];
            if ( (c >= 'A' && c <= 'Z') )
            {
                textOut[i] = (byte)(c - 39+6+26);
            }
            else if ( (c >= 'a' && c <= 'z' ) )
            {
                //$NOTE: we do *NOT* support lower letters,
                //       so we simply convert to lower here
                textOut[i] = (byte)(c - 32 - 39+6);
            }
            else if ( c >= '0' && c <= '9' )
            {
                textOut[i] = (byte)(c + 4-30);
            }
            else if ( c == '-' )
            {
                textOut[i] = 10;
            }
            else if ( c == '>' )
            {
                textOut[i] = 23;
            }
            else if ( c == '<' )
            {
                textOut[i] = 22;
            }
            else if ( c == ' ' )
            {
                textOut[i] = god2DFast._text_IndexOfSpace;
            }            
        }
        return textOut;
    }        
    
    /**
     * @Desc: render 3D graphics frame
     * 
     * @param g - Graphics
     */
    public static void render3DFrame( Graphics g )
    {
        
        if( god3D.g_World != null && (m_gamestate != Defs.GS_LOADING) )
        {
            god3D.g_graphics3D.bindTarget( g );
            
            try
            {
                god3D.g_graphics3D.render( god3D.g_World );
            } 
            catch( Exception e )
            {
                //@ IF DEBUG
                System.out.print("\n [ERROR][3D] Rendering FSCKED UP: " + e.toString());
                Alert alert = new Alert("", "[ERROR][3D] = " + e.toString(), null, AlertType.ERROR);
                Display.getDisplay(Main._MIDLET).setCurrent(alert);
                //@ ENDIF
            } 
            finally
            {
                god3D.g_graphics3D.releaseTarget();
            }
        }        
    }
        
    /**
     * @Desc: render 2D graphics frame
     * 
     * @param: g - Graphics
     */
    public static void render2DFrame( Graphics g )
    {
        
        try
        {

            m_tempVal = Defs.MENU_NORMAL_CY + 18;
            
            switch( m_gamestate )
            {
                case Defs.GS_LOADING:
                    
                    g.drawImage( m_menuSplash, 0, 0, _TOP_LEFT );
                    
                    //@ IF 176x208
                    
                    m_menuLoadingBar.curClipFrame = 1;
                    for( int i = 0; i < Defs.MENU_LOADBAR_SIZE; i++ )
                    {
                        m_menuLoadingBar.draw( g, 30 + (i * m_menuLoadingBar.width) , 165, _TOP_LEFT );
                    }
                    m_menuLoadingBar.curClipFrame = 0;
                    for( int i = 0; i < (m_progressLoading * Defs.MENU_PROGRESS_RATIO); i++)
                    {
                        m_menuLoadingBar.draw( g, 30 + (i * m_menuLoadingBar.width) , 165, _TOP_LEFT );
                    }                    
                    
                    //@ ELSE
                    
                        //@ IF 176x220
                        m_menuLoadingBar.curClipFrame = 1;
                        for( int i = 0; i < Defs.MENU_LOADBAR_SIZE; i++ )
                        {
                            m_menuLoadingBar.draw( g, 31 + (i * m_menuLoadingBar.width), 158, _TOP_LEFT );
                        }
                        m_menuLoadingBar.curClipFrame = 0;
                        for( int i = 0; i < (m_progressLoading * Defs.MENU_PROGRESS_RATIO); i++)
                        {
                            m_menuLoadingBar.draw( g, 31 + (i * m_menuLoadingBar.width) , 158, _TOP_LEFT );
                        }
                        //@ ELSE
                        
                            //@ IF 240x320
                            m_menuLoadingBar.curClipFrame = 1;
                            for( int i = 0; i < Defs.MENU_LOADBAR_SIZE; i++ )
                            {
                                m_menuLoadingBar.draw( g, 64 + (i * m_menuLoadingBar.width), 249, _TOP_LEFT );
                            }
                            m_menuLoadingBar.curClipFrame = 0;
                            for( int i = 0; i < (m_progressLoading * Defs.MENU_PROGRESS_RATIO); i++)
                            {
                                m_menuLoadingBar.draw( g, 64 + (i * m_menuLoadingBar.width) , 249, _TOP_LEFT );
                            }                        
                            //@ ENDIF
                        
                        //@ ENDIF
                        
                    //@ ENDIF
                    
                    break;
 
                // *** main menu
                case Defs.GS_MENU:
                    
                    //@ IF 240x320
                    menuDrawMenuBackground( g, true );
                    //@ ELSE
                    menuDrawMenuBackground( g, false );
                    //@ ENDIF
                    
                    if ( m_bIsThereSavedGame || m_bInGame )
                    {
                        m_menuFont.curClipFrame = Defs.MENU_CONTINUE;
                        m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                        m_tempVal += Defs.MENU_TEXT_HEIGHT ;                        
                    }
                    
                    m_menuFont.curClipFrame = Defs.MENU_NEW_GAME;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );

                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    m_menuFont.curClipFrame = Defs.MENU_OPTIONS;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    m_menuFont.curClipFrame = Defs.MENU_HIGH_SCORE;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT ;
                    m_menuFont.curClipFrame = Defs.MENU_HOW_TO_PLAY;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT ;
                    m_menuFont.curClipFrame = Defs.MENU_CREDITS;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );                    
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT ;
                    m_menuFont.curClipFrame = Defs.MENU_EXIT;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    menuDrawSelector( g );

                    break;
                    
                // *** game options
                case Defs.GS_MENU_OPTIONS:
                    
                    //@ IF 240x320
                    menuDrawMenuBackground( g, true );
                    //@ ELSE
                    menuDrawMenuBackground( g, false );
                    //@ ENDIF
                    
                    m_menuFont.curClipFrame = Defs.MENU_GAME_DETAILS;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    switch( m_menuGameDetails )
                    {
                        case Defs.DETAILS_LOW:
                            m_menuFont.curClipFrame = Defs.MENU_GAME_DETAILS_LOW;
                            break;
                            
                        case Defs.DETAILS_MEDIUM:
                            m_menuFont.curClipFrame = Defs.MENU_GAME_DETAILS_MEDIUM;
                            break;
                            
                        case Defs.DETAILS_HIGH:
                            m_menuFont.curClipFrame = Defs.MENU_GAME_DETAILS_HIGH;                            
                            break;
                    }                    
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += ( Defs.MENU_TEXT_HEIGHT << 1 );
                    m_menuFont.curClipFrame = Defs.MENU_OK;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    menuDrawSelector( g );
                    
                    break;
                
                // *** game high score list    
                case Defs.GS_MENU_HIGHSCORE:
                    
                    //@ IF 240x320
                    menuDrawMenuBackground( g, true );
                    //@ ELSE
                    menuDrawMenuBackground( g, false );
                    //@ ENDIF
                    
                    m_menuFont.curClipFrame = Defs.MENU_HIGH_SCORE;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_menuFont.curClipFrame = Defs.MENU_OK ;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal + Defs.MENU_TEXT_HEIGHT * 6 );
                    
                    //@ IF 240x320
                    m_tempVal += (Defs.MENU_TEXT_HEIGHT + (Defs.MENU_TEXT_HEIGHT >> 1) );
                    //@ ELSE
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    //@ ENDIF
                    
                    god2DFast._text_CharSpace  = 1;
                    for( int i = 0; i < m_scPlayer_names.length; i++ )
                    {
                        god2DFast.textDraw3Numbered( g, Defs.MENU_NORMAL_CX + 17, m_tempVal ,
                                i + 1, Graphics.LEFT | Graphics.TOP );
                        
                        god2DFast.textDraw3( g, Defs.MENU_NORMAL_CX + 25, m_tempVal ,
                                m_scPlayer_names[i], Graphics.LEFT | Graphics.TOP );

                        if ( m_scPlayer_scores[i] != 0 )
                        {
                            god2DFast.textDraw3Numbered(g, 
                                    Defs.MENU_NORMAL_CX + Defs.MENU_WIDTH + 15 - 35, m_tempVal,
                                    m_scPlayer_scores[i], Graphics.RIGHT | Graphics.TOP );
                        }
                        
                        m_tempVal += (god2DFast._text_Font.height + (god2DFast._text_Font.height >> 1));
                    }
                    god2DFast._text_CharSpace  = 0;

                    menuDrawSelector( g );
                    break;
                    
                // *** game instructions
                case Defs.GS_MENU_HOW_TO_PLAY:
                    
                    m_tempVal = Defs.MENU_LARGE_CY + 18;
                  
                    menuDrawMenuBackground( g, true );
                    
                    m_menuFont.curClipFrame = Defs.MENU_HOW_TO_PLAY;
                    m_menuFont.draw( g, Defs.MENU_LARGE_CX + 38 , m_tempVal );

                    g.setClip( Defs.MENU_LARGE_CX + 79, Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT - 24, 7, 8 );
                    g.drawImage( m_menuUpDownIndicator, Defs.MENU_LARGE_CX + 79 , 
                            Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT - 24 , _TOP_LEFT );
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    
                    god2DFast._text_CharSpace = 2;
                    god2DFast.textDraw3Multilined( g, 
                            Defs.MENU_LARGE_CX + 20, m_tempVal, 133, 0, _texts[ texts.text_instructions ] );
                    god2DFast._text_CharSpace = 0;
                    break;
                    
                case Defs.GS_MENU_HOW_TO_PLAY_MORE:
                    
                    m_tempVal = Defs.MENU_LARGE_CY + 18;
                    
                    menuDrawMenuBackground( g, true );
                    
                    m_menuFont.curClipFrame = Defs.MENU_HOW_TO_PLAY;
                    m_menuFont.draw( g, Defs.MENU_LARGE_CX + 38 , m_tempVal );
                    
                    m_menuFont.curClipFrame = Defs.MENU_OK;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , 
                            Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT - 28 );
                    
                    m_menuSelector.curClipFrame = 0;
                    m_menuSelector.draw( g, Defs.MENU_NORMAL_CX + 15 + 8, Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT - 28 );
                    m_menuSelector.curClipFrame = 1;
                    m_menuSelector.draw( g, Defs.MENU_NORMAL_CX + 92 + 3, Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT - 28 );                    
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    
                    god2DFast._text_CharSpace = 2;
                    god2DFast.textDraw3Multilined( g, 
                            Defs.MENU_LARGE_CX + 20, m_tempVal, 133, 0, _texts[ texts.text_instructions2 ] );
                    god2DFast._text_CharSpace = 0;
                    
                    break;
                    
                // *** game credits
                case Defs.GS_MENU_CREDITS:
                    
                    //@ IF 240x320
                    menuDrawMenuBackground( g, true );
                    //@ ELSE
                    menuDrawMenuBackground( g, false );
                    //@ ENDIF
                    
                    m_tempVal += ( Defs.MENU_TEXT_HEIGHT + Defs.MENU_TEXT_HEIGHT );
                    m_menuFont.curClipFrame = Defs.MENU_GAME_CREDITS;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += ( Defs.MENU_TEXT_HEIGHT + Defs.MENU_TEXT_HEIGHT );
                    m_menuFont.curClipFrame = Defs.MENU_OK;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    menuDrawSelector( g );                  
                    
                    break;                    
                    
                // *** game exit confirmation
                case Defs.GS_MENU_EXIT:
                    
                    //@ IF 240x320
                    menuDrawMenuBackground( g, true );
                    //@ ELSE
                    menuDrawMenuBackground( g, false );
                    //@ ENDIF
                    
                    m_menuFont.curClipFrame = Defs.MENU_ARE_YOU_SURE;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    m_menuFont.curClipFrame = Defs.MENU_YES;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    m_menuFont.curClipFrame = Defs.MENU_NO;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    menuDrawSelector( g );                  
                    
                    break;
                    
                //*** game enter player name
                case Defs.GS_MENU_ENTERNAME:
                    
                    //@ IF 240x320
                    menuDrawMenuBackground( g, true );
                    //@ ELSE
                    menuDrawMenuBackground( g, false );
                    //@ ENDIF
                    
                    m_menuFont.curClipFrame = Defs.MENU_ENTER_YOUR_NAME;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    
                    g.setStrokeStyle( Graphics.SOLID );
                    
                    god2DFast._text_CharSpace = 1;
                    
                    god2DFast.inputBoxDraw3_TimeUpdate( g, Defs.MENU_NORMAL_CX + 35, m_tempVal, 
                            m_inputBoxData, 
                            m_playername, 
                            m_ibFocused,
                            m_inputBoxTime );
                    
                    god2DFast._text_CharSpace = 0;
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    m_menuFont.curClipFrame = Defs.MENU_OK;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );

                    menuDrawSelector( g );                     
                    
                    break;
                    
                // *** show "game over" title 
                case Defs.GS_GAMEOVER:
                    
                    //@ IF 240x320
                    menuDrawMenuBackground( g, true );
                    //@ ELSE
                    menuDrawMenuBackground( g, false );
                    //@ ENDIF
                    
                    m_tempVal += Defs.MENU_TEXT_HEIGHT;
                    m_menuFont.curClipFrame = Defs.MENU_GAMEOVER;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );
                    
                    m_tempVal += (Defs.MENU_TEXT_HEIGHT << 2) ;
                    m_menuFont.curClipFrame = Defs.MENU_OK;
                    m_menuFont.draw( g, Defs.MENU_NORMAL_CX + 17 , m_tempVal );

                    menuDrawSelector( g );                       
                    break;
                    
                // *** in-game stuff
                default:
                    
                    g.drawImage( m_menuBarTopInGame, 0, 0, _TOP_LEFT );
                    g.drawImage( m_menuBarBottom, 0, Defs.SCREEN_HEIGHT - m_menuBarBottom.getHeight(), _TOP_LEFT );
                    
                    //@ IF 240x320
                    g.drawImage( m_imgPreview[m_clrIndexCubeNext], 
                            Defs.SCREEN_WIDTH - 26, 2, _TOP_LEFT );
                    
                    god2DFast._text_CharSpace = 1;
                    god2DFast.textDraw3Numbered( g, 57, 9, m_score, _TOP_LEFT  );
                    god2DFast.textDraw3Numbered( g, 132, 9, (m_level + 1), _TOP_LEFT  );
                    god2DFast._text_CharSpace = 0;
                    
                    //@ ELSE
                    
                    g.drawImage( m_imgPreview[m_clrIndexCubeNext], 
                            Defs.SCREEN_WIDTH - 23, 2, _TOP_LEFT );                    
                    
                    god2DFast._text_CharSpace = 1;
                    god2DFast.textDraw3Numbered( g, 43, 9, m_score, _TOP_LEFT  );
                    god2DFast.textDraw3Numbered( g, 108, 9, (m_level + 1), _TOP_LEFT  );
                    god2DFast._text_CharSpace = 0;
                    //@ ENDIF
                    
                    m_menuButton.curClipFrame = 1;
                    m_menuButton.draw( g, Defs.SCREEN_WIDTH - m_menuButton.width , Defs.SCREEN_HEIGHT - m_menuButton.height, _TOP_LEFT );
                    
                    
                    //@ IF SHOW_FPS
                    long lCurTime = System.currentTimeMillis();
                        
                    if ( (lCurTime - m_lFpsLast) >= 1000  )
                    {
                        m_nFps = m_nCurrentFps;
                        m_nCurrentFps = 0;
                        m_lFpsLast = lCurTime;
                    }
                    else
                        m_nCurrentFps++;

                    god2DFast.textDraw3Numbered( g, 10, Defs.SCREEN_HEIGHT - 10, m_nFps, Graphics.LEFT | Graphics.TOP );
                    //@ ENDIF
                    
                    break;
            }

        }
        catch( Exception e )
        {
            //@ IF DEBUG
            System.out.print( "\n [ERROR][2D] BLITING FSCKED UP: " + e.toString()  );
            Alert alert = new Alert("", "[ERROR][2D] = " + e.toString(), null, AlertType.ERROR);
            Display.getDisplay(Main._MIDLET).setCurrent( alert );
            //@ ENDIF
        }

    }
 
    /**
     * @Desc: update game mechanics 
     */
    public static void updateLogic()
    {
//        long lStartTime = System.currentTimeMillis() - m_lWorldTime;

        switch( m_gamestate )
        {
            //// MENUS
            
            case Defs.GS_MENU:
                
                if ( KeyMan.Released == KeyMan.DOWN  )
                {
                    m_menuCurIdx++;
                    if ( m_menuCurIdx > 5 )
                    {
                        if ( (m_bInGame || m_bIsThereSavedGame) )
                        {
                            if ( m_menuCurIdx > 6 )
                                m_menuCurIdx = 0;
                        }
                        else
                            m_menuCurIdx = 0;
                    }
                }
                else if ( KeyMan.Released == KeyMan.UP )
                {
                    m_menuCurIdx--;
                    if ( m_menuCurIdx < 0 )
                    {
                        if ( (m_bInGame || m_bIsThereSavedGame) )
                            m_menuCurIdx = 6;
                        else
                            m_menuCurIdx = 5;
                    }                    
                }
                else if ( KeyMan.Released == KeyMan.FIRE )
                {
                     
                    if ( (m_bInGame || m_bIsThereSavedGame) && m_menuCurIdx == 0 )
                    {
                        continueGame();
                    }
                    else
                    {
                        if ( (m_bInGame || m_bIsThereSavedGame) )
                            m_menuCurIdx--;
                        
                        switch( m_menuCurIdx )
                        {
                            // go to [new game]
                            case 0:
                                reinitGame();
                                setGameState( Defs.GS_GAMEPLAY_CREATECUBE );
                                break;
                                
                            // go to [options]
                            case 1:
                                m_menuCurIdx = 1;
                                setGameState( Defs.GS_MENU_OPTIONS );
                                break;
                                
                            // go to [high score]
                            case 2:
                                m_menuCurIdx = 6;
                                setGameState( Defs.GS_MENU_HIGHSCORE );
                                break;
                              
                            // go to [how to play]
                            case 3:
                                setGameState( Defs.GS_MENU_HOW_TO_PLAY );
                                break;
                            
                            // go to [credits]
                            case 4:
                                m_menuCurIdx = 4;
                                setGameState( Defs.GS_MENU_CREDITS );
                                break;
                                
                            // go to [exit]
                            case 5:
                                m_menuCurIdx = 1;
                                setGameState( Defs.GS_MENU_EXIT );
                                break;
                        }
                    }
                }
                
                KeyMan.Released = 0;
                
                break;
            
            case Defs.GS_MENU_OPTIONS:
                
                if ( KeyMan.Released == KeyMan.DOWN  )
                {
                    ++m_menuCurIdx;
                    
                    if ( m_menuCurIdx > 3 )
                        m_menuCurIdx = 1;
                    else if ( m_menuCurIdx == 2 )
                        m_menuCurIdx = 3;
                }
                else if ( KeyMan.Released == KeyMan.UP )
                {
                    --m_menuCurIdx;
                    
                    if ( m_menuCurIdx < 1 )
                        m_menuCurIdx = 3;
                    else if ( m_menuCurIdx == 2 )
                        m_menuCurIdx = 1;
                }
                else if ( KeyMan.Released == KeyMan.FIRE )
                {
                    switch( m_menuCurIdx )
                    {
                        case 1:
                            switch( m_menuGameDetails )
                            {
                                case Defs.DETAILS_LOW:
                                    m_menuGameDetails = Defs.DETAILS_MEDIUM;
                                    break;
                                    
                                case Defs.DETAILS_MEDIUM:
                                    m_menuGameDetails = Defs.DETAILS_HIGH;
                                    break;
                                    
                                case Defs.DETAILS_HIGH:
                                    m_menuGameDetails = Defs.DETAILS_LOW;
                                    break;
                            }
                            break;

                        case 3:
                            m_menuCurIdx = 0;
                            setGameState( Defs.GS_MENU );
                            
                            // change detail level 
                            reinitDetails();
                            
                            break;
                    }
                
                }
                
                KeyMan.Released = 0;
                
                break;
                
            case Defs.GS_MENU_CREDITS:
            case Defs.GS_MENU_HIGHSCORE:
                
                if ( KeyMan.Released == KeyMan.FIRE )
                {
                    m_menuCurIdx = 0;
                    setGameState( Defs.GS_MENU );                    
                }
                
                KeyMan.Released = 0;
                break;

                
            
            case Defs.GS_MENU_HOW_TO_PLAY:
                
                if ( KeyMan.Released == KeyMan.FIRE || 
                     KeyMan.Released == KeyMan.DOWN )
                {
                    setGameState( Defs.GS_MENU_HOW_TO_PLAY_MORE );                    
                }
                
                KeyMan.Released = 0;
                
                break;
                
            case Defs.GS_MENU_HOW_TO_PLAY_MORE:
                
                if ( KeyMan.Released == KeyMan.FIRE )
                {
                    m_menuCurIdx = 0;
                    setGameState( Defs.GS_MENU );                    
                }
                else if ( KeyMan.Released == KeyMan.UP )
                {
                    setGameState( Defs.GS_MENU_HOW_TO_PLAY );
                }
                
                KeyMan.Released = 0;                

                break;

  
            case Defs.GS_MENU_EXIT:
                
                
                if ( KeyMan.Released == KeyMan.DOWN  )
                {
                    if ( ++m_menuCurIdx > 2 )
                        m_menuCurIdx = 1;
                }
                else if ( KeyMan.Released == KeyMan.UP )
                {
                    if ( --m_menuCurIdx < 1 )
                        m_menuCurIdx = 2;
                }
                else if ( KeyMan.Released == KeyMan.FIRE )
                {
                    switch( m_menuCurIdx )
                    {
                        case 1:
                            Main.g_bRunning = false;
                            break;
                            
                        case 2:
                            m_menuCurIdx = 0;
                            setGameState( Defs.GS_MENU );
                            break;
                    }
                
                }
                
                KeyMan.Released = 0;
                
                break;
                
                
            case Defs.GS_MENU_ENTERNAME:
                
                if ( KeyMan.Released != 0 )
                {
                    if( m_ibFocused && 
                            (KeyMan.Released >= KeyMan.NUM0 && KeyMan.Released <= KeyMan.NUM9) )
                    {
                        if ( god2DFast.inputBoxEnterKey_TimeUpdate(
                                m_inputBoxData,
                                m_playername,
                                m_keyPadChars,
                                KeyMan.RealCode,
                                m_inputBoxTime) )
                        {
                            m_inputBoxTime = System.currentTimeMillis() + 1000;
                        }
                        
                        KeyMan.Released = 0;
                        return;
                    }
                    else if( m_ibFocused && 
                            (KeyMan.Released == KeyMan.CLEAR || KeyMan.Released == KeyMan.LEFT) )
                    {
                        god2DFast.inputBoxClearLastKey(m_inputBoxData);
                    }
                    else if ( KeyMan.Released == KeyMan.DOWN  )
                    {
                        if ( ++m_menuCurIdx > 2 )
                        {
                            m_menuCurIdx = 1;
                            m_ibFocused = true;
                        }
                        else
                        {
                            m_ibFocused = false;
                        }
                    }
                    else if ( KeyMan.Released == KeyMan.UP )
                    {
                        if ( --m_menuCurIdx < 1 )
                        {
                            m_menuCurIdx = 2;
                            m_ibFocused = false;
                        }
                        else
                        {
                            m_ibFocused = true;
                        }
                    }     
                    else if ( KeyMan.Released == KeyMan.FIRE && m_menuCurIdx == 2 )
                    {
                        // ensure some name has been entered ?!
                        if ( m_inputBoxData[1] > 0 )
                        {
                            saveScores();
                            m_menuCurIdx = 6;
                            setGameState( Defs.GS_MENU_HIGHSCORE );
                        }
                    }                    
                    
                    KeyMan.Released = 0;
                }
                
                break;
                
            
            //// LOADING ///////////////////////////////////////////////////////
            case Defs.GS_LOADING:
                
                initScene();
                
                break;
                
            
            //// GAMEPLAY //////////////////////////////////////////////////////
            case Defs.GS_GAMEPLAY:
                
                if ( KeyMan.Released == KeyMan.RIGHTSOFTKEY )
                {
                    //!go to main menu
                    setGameState( Defs.GS_MENU );
                }                
                else if ( KeyMan.Released == KeyMan.NUM4 || KeyMan.Released == KeyMan.LEFT )
                {
                    moveCubeFall( KeyMan.LEFT );
                }
                else if ( KeyMan.Released == KeyMan.NUM6 || KeyMan.Released == KeyMan.RIGHT )
                {
                    moveCubeFall( KeyMan.RIGHT );
                }
                else if ( KeyMan.Pressed == KeyMan.NUM5 || KeyMan.Released == KeyMan.DOWN || KeyMan.Released == KeyMan.NUM8 )
                {
                    m_yCube = _ROWS[m_yEndPosCub];
                }                
                //@ IF RELEASE_TESTS
                else if ( KeyMan.Released == KeyMan.NUM7 )
                {
                    createCubeFall();
                }
                else if ( KeyMan.Released == KeyMan.NUM9 )
                {
                    m_tempScore = 0;
                    giveScore( 5 );
                    m_score += m_tempScore;
                }
                else if ( KeyMan.Released == KeyMan.NUM1 )
                {
                    m_menuGameDetails = Defs.DETAILS_DEBUG;
                    reinitDetails();
                }
                else if ( KeyMan.Released == KeyMan.NUM3 )
                {
                    m_menuGameDetails = Defs.DETAILS_MEDIUM;
                    reinitDetails();
                }                
                //@ ENDIF
                
                KeyMan.Released = 0;
                KeyMan.Pressed = 0;
  
                updateCubeFall();
                
                break;

            //// UPDATE BOARD REARRANGEMENT ////////////////////////////////////                
            case Defs.GS_GAMEPLAY_REARRANGED:
                syncCubeBoard_Update();
                break;
            
            //// ZOOM EXPLOSION ////////////////////////////////////////////////
            case Defs.GS_GAMEPLAY_ZOOMEXPLOSION:
                updateZoomExplosion();
                break;
                
            //// Synchronize combinations //////////////////////////////////////                
            case Defs.GS_GAMEPLAY_SYNCBOARD:
                
                if ( syncCubeBoard_DoIt() )
                {
                    setGameState( Defs.GS_GAMEPLAY_REARRANGED );
                } 
                else
                {
                    setGameState( Defs.GS_GAMEPLAY_CREATECUBE );
                }
                
                break;
                
            //// Start Falling //////////////////////////////////////////////////                
            case Defs.GS_GAMEPLAY_CREATECUBE:
                m_bInGame = true;
                createCubeFall();
                break;
                
            //// END OF GAME ///////////////////////////////////////////////////    
            case Defs.GS_GAMEOVER:
                
                if ( KeyMan.Released == KeyMan.FIRE )
                {
                    KeyMan.Released = 0;
                    
                    m_bInGame = false;
                    m_bIsThereSavedGame = false;
                    deleteContinueGame();
                    
                    //check score
                    for( int i = 0; i < 5; i++ )
                    {
                        if ( m_score > m_scPlayer_scores[i] )
                        {
                            menuCreateInputBox();
                            m_ibFocused = true;
                            m_menuCurIdx = 1;
                            setGameState( Defs.GS_MENU_ENTERNAME );
                            
                            return;
                        }
                    }
                    
                    m_menuCurIdx = 0;
                    setGameState( Defs.GS_MENU );
                }
                
                break;
        }
    }
    
    
    /////////////////////////////////////////////////////////////////////
    //// Menu Functions
    
    /**
     * @Desc: draw select bar for Menu
     * 
     * @param: g
     */
    private static void menuDrawSelector( Graphics g )
    {
        m_tempVal = Defs.MENU_NORMAL_CY + 18 +  m_menuCurIdx * Defs.MENU_TEXT_HEIGHT  ;
        
        m_menuSelector.curClipFrame = 0;
        m_menuSelector.draw( g, Defs.MENU_NORMAL_CX + 15 + 8, m_tempVal );
        m_menuSelector.curClipFrame = 1;
        m_menuSelector.draw( g, Defs.MENU_NORMAL_CX + 92 + 3, m_tempVal );
    }
 
    /**
     * @Desc: creates/inits user input box
     */
    private static short m_inputBoxData[] = null;
    private static byte[][] m_keyPadChars = null;
    private static byte m_playername[] = new byte[10];    
    private static boolean m_ibFocused = false;
    private static long m_inputBoxTime = 0;
    private static void menuCreateInputBox()
    {
        if( m_inputBoxData == null )
        {
            m_ibFocused = false;
            m_keyPadChars = new byte[10][];
  
            m_inputBoxData = god2DFast.inputBoxCreateData_TimeUpdate(10, _texts[ texts.text_name ].length );
            m_inputBoxTime = System.currentTimeMillis() + 1000;
            
            for( int i = 0; i < 10; i++ )
                m_keyPadChars[i] = _texts[ texts.text_keyPadChar0 + i ];
        }
        else
        {
            m_inputBoxData[0] = 10;
            m_inputBoxData[1] = (short)_texts[ texts.text_name ].length;
            m_inputBoxData[3] = -1;
            m_inputBoxTime = System.currentTimeMillis() + 1000;
        }
    }
    
    
    private static void menuDrawMenuBackground( Graphics g, boolean bDrawLarge )
    {
        // draw borders
        g.drawImage( m_menuBarTop, 0, 0, _TOP_LEFT );
        g.drawImage( m_menuBarBottom, 0, Defs.SCREEN_HEIGHT - m_menuBarBottom.getHeight(), _TOP_LEFT );
        g.drawImage( m_menuKolona, 0, m_menuBarTop.getHeight(), _TOP_LEFT );
        g.drawImage( m_menuKolona, Defs.SCREEN_WIDTH - m_menuKolona.getWidth() , m_menuBarTop.getHeight(), _TOP_LEFT );
        
        // draw menu box
        if ( bDrawLarge )
        {
            g.setClip( Defs.MENU_LARGE_CX, Defs.MENU_LARGE_CY, Defs.MENU_LARGE_WIDTH, Defs.MENU_LARGE_HEIGHT + 20);
            g.setColor( 0x292929 );
            g.fillRect( Defs.MENU_LARGE_CX, Defs.MENU_LARGE_CY, Defs.MENU_LARGE_WIDTH, Defs.MENU_LARGE_HEIGHT );

            g.drawImage( m_menuImgTopLeftCorner, 
                    Defs.MENU_LARGE_CX, Defs.MENU_LARGE_CY, 
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgTopBorder, 
                    Defs.MENU_LARGE_CX + 15, Defs.MENU_LARGE_CY,
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgTopBorder, 
                    Defs.MENU_LARGE_CX + 15 + 57 - 15, Defs.MENU_LARGE_CY,
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgTopRightCorner, 
                    Defs.MENU_LARGE_CX + 92 + 15 + 57 - 15, Defs.MENU_LARGE_CY, 
                    _TOP_LEFT );            
            
            //--- MIDDLE BEGIN HERE
            g.drawImage( m_menuImgLeftBorder, 
                    Defs.MENU_LARGE_CX, Defs.MENU_LARGE_CY + 14, 
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgLeftBorder, 
                    Defs.MENU_LARGE_CX, Defs.MENU_LARGE_CY + 14 + 74, 
                    _TOP_LEFT );            

            g.drawImage( m_menuImgRightBorder, 
                    Defs.MENU_LARGE_CX + 92 + 15 + 57 - 15, Defs.MENU_LARGE_CY + 14, 
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgRightBorder, 
                    Defs.MENU_LARGE_CX + 92 + 15 + 57 - 15, Defs.MENU_LARGE_CY + 14 + 74, 
                    _TOP_LEFT );
            
            // --- MIDDLE END HERE            
            
            g.drawImage( m_menuImgBottomLeftCorner, 
                    Defs.MENU_LARGE_CX, Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT - 14, 
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgBottomBorder, 
                    Defs.MENU_LARGE_CX + 15, Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT - 14,
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgBottomBorder, 
                    Defs.MENU_LARGE_CX + 15 + 57 - 15, Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT - 14,
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgBottomRightCorner, 
                    Defs.MENU_LARGE_CX + 92 + 15 + 57 - 15, Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT - 14, 
                    _TOP_LEFT );   
            
//            g.drawImage( m_menuImgRing, 
//                    Defs.MENU_LARGE_CX + 10, Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT, 
//                    _TOP_LEFT );
//            
//            g.drawImage( m_menuImgRing, 
//                    Defs.MENU_LARGE_CX + Defs.MENU_LARGE_WIDTH - 10 - 11, Defs.MENU_LARGE_CY + Defs.MENU_LARGE_HEIGHT, 
//                    _TOP_LEFT );              
//            
        }
        else
        {
            g.setClip( Defs.MENU_NORMAL_CX, Defs.MENU_NORMAL_CY, Defs.MENU_WIDTH, Defs.MENU_HEIGHT + 20);
            g.setColor( 0x292929 );
            g.fillRect( Defs.MENU_NORMAL_CX, Defs.MENU_NORMAL_CY, Defs.MENU_WIDTH, Defs.MENU_HEIGHT );
            
            g.drawImage( m_menuImgTopLeftCorner, 
                    Defs.MENU_NORMAL_CX, Defs.MENU_NORMAL_CY, 
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgTopBorder, 
                    Defs.MENU_NORMAL_CX + 15, Defs.MENU_NORMAL_CY,
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgTopRightCorner, 
                    Defs.MENU_NORMAL_CX + 92 + 15, Defs.MENU_NORMAL_CY, 
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgLeftBorder, 
                    Defs.MENU_NORMAL_CX, Defs.MENU_NORMAL_CY + 14, 
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgLeftBorder, 
                    Defs.MENU_NORMAL_CX, Defs.MENU_NORMAL_CY + 14 + 15, 
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgRightBorder, 
                    Defs.MENU_NORMAL_CX + 15 + 92, Defs.MENU_NORMAL_CY + 14, 
                    _TOP_LEFT );            
            
            g.drawImage( m_menuImgRightBorder, 
                    Defs.MENU_NORMAL_CX + 15 + 92, Defs.MENU_NORMAL_CY + 14 + 15, 
                    _TOP_LEFT );             
            
            g.drawImage( m_menuImgBottomLeftCorner, 
                    Defs.MENU_NORMAL_CX, Defs.MENU_NORMAL_CY + 88 + 14, 
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgBottomBorder, 
                    Defs.MENU_NORMAL_CX + 15, Defs.MENU_NORMAL_CY + 88 + 14,
                    _TOP_LEFT );
            
            g.drawImage( m_menuImgBottomRightCorner, 
                    Defs.MENU_NORMAL_CX + 92 + 15, Defs.MENU_NORMAL_CY + 88 + 14, 
                    _TOP_LEFT );       

//            g.drawImage( m_menuImgRing, 
//                    Defs.MENU_NORMAL_CX + 10, Defs.MENU_NORMAL_CY + 88 + 29, 
//                    _TOP_LEFT );
//            
//            g.drawImage( m_menuImgRing, 
//                    Defs.MENU_NORMAL_CX + Defs.MENU_WIDTH - 10 - 11, Defs.MENU_NORMAL_CY + 88 + 29, 
//                    _TOP_LEFT );                
            
        }
        
        // draw menu box

        m_menuButton.curClipFrame = 0;
        m_menuButton.draw( g, Defs.SCREEN_WIDTH - m_menuButton.width, Defs.SCREEN_HEIGHT - m_menuButton.height, _TOP_LEFT );
        
    }
}
