/*
 * god3D.java
 * $Id$
 *
 * Created on Петък, 2005, Февруари 25, 15:56
 */

package arcanum;

import javax.microedition.m3g.*;

/**
 *
 * @whatisit: God3D is G.lobal O.bject D.eployment
 *            part of pi-mobile.com Arcanum Engine
 * 
 * @whatdowesay: In God we trust...
 * 
 * @author:- ppetrov
 *    
 * @Log:
 *      
 */


public final class god3D 
{

    // engine properties (Read-Only)
    public final static boolean _P_LIGHT_ENABLED = true;
    
    // direction constants
    public final static byte DIR_LEFT =     0x01;
    public final static byte DIR_RIGHT =    0x02;
    public final static byte DIR_UP =       0x04;
    public final static byte DIR_DOWN =     0x08;
    public final static byte DIR_FORWARD =  0x10;
    public final static byte DIR_BACKWARD = 0x20;
   
    
    public static Graphics3D  g_graphics3D; // singleton 3D graphics object, no rendering is done without this class
    public static Background  g_background = new Background();
    public static World       g_World; // reference to the current world object, that is used to render groups
    
    public static int g_screen_x;
    public static int g_screen_y;
    public static int g_screenwidth;
    public static int g_screenheight;
      
    
    ///////////////////////////////////////////////////////////////////////////
    // Camera object variables
    private static Camera      g_camera;
    public static Group       g_grpCamera;
    
    // position and orientation
    private static float m_xposCamera = 0.0f;
    private static float m_yposCamera = 0.0f;
    private static float m_zposCamera = 0.0f;
    private static float m_xrotCamera = 0.0f;
    private static float m_yrotCamera = 0.0f;
    private static float m_zrotCamera = 0.0f;
  
    private static Transform m_transCamera = new Transform(); // tranformation matrix that keeps camera postion
    private static Transform m_rotCamera = new Transform(); // camera's current rotation
    private static float     m_matTrans[] = new float[16];
        

    ///////////////////////////////////////////////////////////////////////////
    // SkyDome object variables
    
    public final static int SKYDOME_NOT_SET = 0;
    public final static int SKYDOME_STATIC = 1;
    public final static int SKYDOME_TILED = 2;
    public final static int SKYDOME_SKYBOX = 3;
    
    public final static int SKYDOME_STEP = 3;
    
    public static int        g_skydometype;
    public static Group      g_grpSkybox;    
    
    ///////////////////////////////////////////////////////////////////////////
    // Mesh spawn variables  
    
    private static byte[] m_boxNormals = 
    { 
        0, 0, 127,      0, 0, 127,      0, 0, 127,      0, 0, 127,
        0, 0, -127,     0, 0, -127,     0, 0, -127,     0, 0, -127, 
        -127, 0, 0,     -127, 0, 0,     -127, 0, 0,     -127, 0, 0, 
        127, 0, 0,      127,  0, 0,     127,  0, 0,     127, 0, 0, 
        0, 127, 0,      0, 127,  0,     0, 127,  0,     0, 127, 0, 
        0, -127, 0,     0, -127, 0,     0, -127, 0,     0, -127, 0, 
        };        
 
    /**
     * Name: Initialize()
     * Desc: init objects and prepare god class for usage
     */  
    public static boolean Initialize( int screen_width, int screen_height ) 
    {
        // defaults
        g_graphics3D = Graphics3D.getInstance();
        g_World = null;
        g_World = new World();
        
        g_screen_x = 0;
        g_screen_y = 0;
        g_screenwidth = screen_width;
        g_screenheight = screen_height;
        g_skydometype = SKYDOME_NOT_SET;
        
        return true;
    }
   

    /**
     * @Name: rgb()
     * @Desc: compose a RGB color
     */    
    public static int rgb( int red, int green, int blue )
    {
        return ( (blue) | ((green) << 8) | ((red) << 16)  ); // RGB
    }
    
    /**
     * @Name: bgr()
     * @Desc: compose a BGR color
     */     
    public static int bgr( int red, int green, int blue )
    {
        return ( (red) | ((green) << 8) | ((blue) << 16)  ); // BGR 
    }
    
     
    ////////////////////////////////////////////////////////////////////////////
    // Camera Methods

    
    /**
     * @Name: createCamera()
     * @Desc: creates and attches a camera object to the World
     */     
    public static void createCamera( float xpos, float ypos, float zpos, int width, int height )
    {
        m_xposCamera = xpos;
        m_yposCamera = ypos;
        m_zposCamera = zpos;
        
        g_camera = new Camera();

        float fRatio = (float)width / (float)height;
        g_camera.setPerspective( 60.0f, fRatio, 0.1f, 1000.0f );
        //g_camera.setTranslation( xpos, ypos, zpos );
        
        // setup new group initial position and attach camera to it
        g_grpCamera = new Group();
        m_transCamera.postTranslate( xpos, ypos, zpos );
        g_grpCamera.setTransform( m_transCamera );
        g_grpCamera.addChild( g_camera );
        
        setupAspectRatio();
        
        // attach to World
        g_World.addChild( g_grpCamera );
        g_World.setActiveCamera( g_camera );
    }   
    
    /**
     * @Name: setupAspectRatio()
     * @Desc: 
     */      
    private static void setupAspectRatio()
    {
        //Camera cam = m_World.getActiveCamera();
		float[] params = new float[4];
		int type = g_camera.getProjection( params );
		if( type != Camera.GENERIC )
		{
	                int owidth = g_screenwidth,
	                    oheight = g_screenheight;
	                
			//calculate window aspect ratio
			float waspect = g_screenwidth / g_screenheight;
	
			if ( waspect < params[1] )
			{
	                    float height = g_screenwidth / params[1];
	                    g_screenheight = (int)height;
			    g_screen_y = ( oheight - g_screenheight ) / 2;
			}
			else
			{
	                    float width = g_screenheight * params[1];
			    g_screenwidth = (int)width;
			    g_screen_x = ( owidth - g_screenwidth ) / 2;
			}
		}
    }   

    
    /**
     * @Name: rotateCamera()
     * @Desc: rotates camera view
     */     
    public static void rotateCamera( int rotateTo, float Speed )
    {
        boolean bTransform = false;
        
        if ( (rotateTo & DIR_LEFT) == DIR_LEFT )
        {
            m_rotCamera.postRotate( Speed, 0.0f, 1.0f, 0.0f );
            m_yrotCamera += Speed;
            bTransform = true;
            moveSkyDome( DIR_RIGHT, 3 );
        }
        else if ( (rotateTo & DIR_RIGHT) == DIR_RIGHT )
        {
            m_rotCamera.postRotate( -Speed, 0.0f, 1.0f, 0.0f );
            m_yrotCamera -= Speed;
            bTransform = true;
            moveSkyDome( DIR_LEFT, 3 );
        }
        
        if ( (rotateTo & DIR_UP) == DIR_UP )
        {
            g_camera.postRotate( Speed, 1.0f, 0.0f, 0.0f );
            moveSkyDome( DIR_DOWN, 3 );
        }
        else if ( (rotateTo & DIR_DOWN) == DIR_DOWN )
        {
            g_camera.postRotate( -Speed, 1.0f, 0.0f, 0.0f );
            //m_xrot -= m_rotSpeed;
            moveSkyDome( DIR_UP, 3 );
        }
        
        // TOOD: implement PITCH and YAW rotations
        
        // {!} Not needed right now
        /*
        if ( m_xrotCamera >= 360.0f )
            m_xrotCamera -= 360.0f;78
        else if ( m_yrotCamera <= -360.0f )
            m_xrotCamera += 360.0f;
        */
        if ( m_yrotCamera >= 360.0f )
            m_yrotCamera -= 360.0f;
        else if ( m_yrotCamera <= -360.0f )
            m_yrotCamera += 360.0f;

        if ( bTransform )
        {
            
            
            g_grpCamera.getTransform( m_transCamera );
            m_transCamera.get( m_matTrans );      // matrix pri translte preobrazuvanie
            m_xposCamera = m_matTrans[3];         // 0 0 0 x
            m_yposCamera = m_matTrans[7];         // 0 0 0 y
            m_zposCamera = m_matTrans[11];        // 0 0 0 z
            
            m_transCamera.setIdentity();  // zaredi 1-4na matrixa
            m_transCamera.postTranslate( m_xposCamera, m_yposCamera, m_zposCamera );
            m_transCamera.postRotate( m_yrotCamera, 0.0f, 1.0f, 0.0f );
            g_grpCamera.setTransform( m_transCamera );
        }
       
        
    }
    
    /**
     * @Name: moveCamera()
     * @Desc: moves camera position
     */     
    public static void moveCamera( byte moveTo, float Speed )
    {
        
        // get transformation matrix & multiply desired posiciq by the current trans matrix-a
        g_grpCamera.getTransform( m_transCamera );
               
        // z-axis
        if ( (moveTo & DIR_FORWARD) == DIR_FORWARD )
        {
            m_transCamera.postTranslate( 0.0f, 0.0f, -Speed );
            moveSkyDome( DIR_UP, 1 );
            //m_skydome.moveUp( 1 );
        }
        else if ( (moveTo & DIR_BACKWARD) == DIR_BACKWARD )
        {
            m_transCamera.postTranslate( 0.0f, 0.0f, Speed );
            moveSkyDome( DIR_DOWN, 1 );
        }
        
        // x-axis
        if ( (moveTo & DIR_LEFT) == DIR_LEFT )
        {
            m_transCamera.postTranslate( -Speed, 0.0f, 0.0f );
            moveSkyDome( DIR_LEFT, 1 );
        }
        else if ( (moveTo & DIR_RIGHT) == DIR_RIGHT )
        {
            m_transCamera.postTranslate( Speed, 0.0f, 0.0f );            
            moveSkyDome( DIR_RIGHT, 1 );
        }        
        
        // y-axis
        if ( (moveTo & DIR_UP) == DIR_UP )
        {
            m_transCamera.postTranslate( 0.0f, -Speed, 0.0f );
            moveSkyDome( DIR_UP, 1 );
        }
        else if ( (moveTo & DIR_DOWN) == DIR_DOWN )
        {
            m_transCamera.postTranslate( 0.0f, Speed, 0.0f );            
            moveSkyDome( DIR_DOWN, 1 );
        }                
        
        // apply new transformed matrix to camera group position
        g_grpCamera.setTransform( m_transCamera );
    }    
    
    
    public static float[] getCameraPosition()
    {
            g_grpCamera.getCompositeTransform( m_transCamera );
            m_transCamera.get( m_matTrans );
            
            float x,y,z;
            x = m_matTrans[3];
            y = m_matTrans[7];
            z = m_matTrans[11];
            
            return new float[] { x, y, z };
    }
    
    public static float[] getCameraDirection()
    {
        float z_vector[] = { 0.0f, 0.0f, -1.0f, 0.0f };
        m_rotCamera.transform( z_vector );
        return new float[] { z_vector[0], z_vector[1], z_vector[2] };
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // SkyDome Methods
    

    /**
     * @Name: createSkyDome()
     * @Desc: creates a static backgrond for the scene
     */
    public static void createSkyDome( Image2D img, int background_color )
    {
        if ( img != null )
        {
            g_background.setImage( img );
            g_background.setImageMode( Background.REPEAT, Background.REPEAT );
        }
        
        g_background.setColor( background_color );
        g_skydometype = SKYDOME_STATIC;
        
        // attch background to the world object     
        g_World.setBackground( g_background );
    }
      

    /**
     * @Name: createSkyDome()
     * @Desc: creates a tiled backgrond for the scene that responds to camera 
     * moving events
     */
    public static void createSkyDome( Image2D img, int width, int height, int background_color ) 
    {
        if ( img != null )
        {
            g_background.setImage( img );
            g_background.setImageMode( Background.REPEAT, Background.REPEAT );
            g_background.setCrop( g_background.getCropX(), g_background.getCropY(), 
                                  width, height );
        
            g_skydometype = SKYDOME_TILED;
        }
        else
        {
            g_skydometype = SKYDOME_STATIC;
            g_background.setColor( background_color );
        }
       
        // attch background to the world object     
        g_World.setBackground( g_background );
    }

    
    /**
     * @Name: createBox()
     * @Desc: creates a 3D box with specified size & color
     * 
     * @param: size - size of cube
     * @param: texture - use (null) for none, any other value to map
     * @param: color - use (-1) for none, any other value to set
     */
    public static Mesh createBox( byte size, Texture2D texture, int color )
    {
        //@ IF DEBUG
        if ( null == texture)
            System.err.println("*** (null) FOR TEXTURE!");
        //@ ENDIF
            
        byte[] vert = { 
            (byte)size, (byte)size, (byte)size, (byte)-size, (byte)size, (byte)size, (byte)size, (byte)-size, (byte)size, (byte)-size, (byte)-size, (byte)size, (byte)-size, (byte)size, (byte)-size, (byte)size, (byte)size, (byte)-size,
            (byte)-size, (byte)-size, (byte)-size, (byte)size, (byte)-size, (byte)-size, (byte)-size, (byte)size, (byte)size, (byte)-size, (byte)size, (byte)-size, (byte)-size, (byte)-size, (byte)size, (byte)-size, (byte)-size, (byte)-size, (byte)size, (byte)size,
            (byte)-size, (byte)size, (byte)size, (byte)size, (byte)size, (byte)-size, (byte)-size, (byte)size, (byte)-size, (byte)size, (byte)size, (byte)size, (byte)-size, (byte)-size, (byte)size, (byte)-size, (byte)size, (byte)size, (byte)size, (byte)-size, (byte)size,
            (byte)size, (byte)size, (byte)-size, (byte)size, (byte)-size, (byte)-size, (byte)size, (byte)size, (byte)-size, (byte)-size, (byte)-size, (byte)-size, (byte)-size, };

        VertexArray vertArray = new VertexArray(vert.length / 3, 3, 1);
        vertArray.set(0, vert.length / 3, vert);

        VertexArray normArray = new VertexArray(m_boxNormals.length / 3, 3, 1);
        normArray.set(0, m_boxNormals.length / 3, m_boxNormals);
            
        byte[] tex = {  0, 1,       1, 1,       0, 0,       1, 0,
                        0, 1,       1, 1,       0, 0,       1, 0,
                        0, 1,       1, 1,       0, 0,       1, 0,
                        0, 1,       1, 1,       0, 0,       1, 0,
                        0, 1,       1, 1,       0, 0,       1, 0, 
                        0, 1,       1, 1,       0, 0,       1, 0
        };
        
        VertexArray vertTex = new VertexArray( tex.length / 2, 2, 1 );
        vertTex.set( 0, tex.length / 2, tex );
        
        // spawn vb
        VertexBuffer vertexBuf = new VertexBuffer();
        vertexBuf.setPositions(vertArray, 1.0f, null);
        vertexBuf.setNormals(normArray);
        vertexBuf.setTexCoords( 0, vertTex, 1.0f, null );
        
//          int[] TRIANGLE_INDICES = {
//            0, 1, 3, 2, 7, 1, 5, 4, 7, 6, 2, 4, 0, 1
//            //0, 1, 2, 3, 4, 5, 6, 7, 7, 6, 2, 4, 0, 1
//          };
        
        int[] stripLen = { 4, 4, 4, 4, 4, 4 };
//        IndexBuffer indexBuf = new TriangleStripArray( TRIANGLE_INDICES, new int[]  {TRIANGLE_INDICES.length} );
        IndexBuffer indexBuf = new TriangleStripArray( 0,  stripLen );
        
        Appearance app = new Appearance();
        app.setPolygonMode( new PolygonMode() );
        app.setTexture( 0, texture );

        //@ IF LIGHTS
        app.getPolygonMode().setShading( PolygonMode.SHADE_FLAT );
        app.getPolygonMode().setTwoSidedLightingEnable( false );
        //@ ENDIF
        app.getPolygonMode().setCulling( PolygonMode.CULL_BACK );
        app.getPolygonMode().setPerspectiveCorrectionEnable( true );
        
        if ( -1 != color )
        {
            vertexBuf.setDefaultColor( color );
           // app.getMaterial().setColor( Material.AMBIENT, color );
        }        

        return new Mesh(vertexBuf, indexBuf, app);
    }    

      
    /**
     * @Name: createSkyDome()
     * @Desc: creates a skybox dome centered over the given positions
     * makes the scene more realistic
     *
     * $NOTE: using SkyBoxes is still burdensome in hi-level moblie 3D games
     * TODO: change side texturing into one TEXUTRE array!
     */
    public static void createSkyDome( Image2D[] img, int center_x, int center_y, int center_z, 
                                      int width, int height, int depth ) 
    {
        
        int   half_width = (width / 2),
              half_height = (height / 2),
              half_depth = (depth / 2);
        
        short posCX = (short)(center_x + half_width), negCX = (short)(center_x - half_width),
              posCY = (short)(center_y + half_height), negCY =(short)(center_y - half_height),
              posCZ = (short)(center_z + half_depth), negCZ  = (short)(center_z - half_depth);
        
        try
        {
            
            // load textures
            Texture2D[] textures = new Texture2D[6];
            for( int i = 0; i < 6; i++ )
            {
                textures[i] = new Texture2D( img[i] );
                textures[i].setFiltering( Texture2D.FILTER_NEAREST, Texture2D.FILTER_NEAREST );
                textures[i].setWrapping( Texture2D.WRAP_CLAMP, Texture2D.WRAP_CLAMP );
                textures[i].setBlendColor( Texture2D.FUNC_MODULATE );
            }
            

            // front
            short[] vSide1 = { posCX, posCY, posCZ,  negCX, posCY, posCZ, posCX, negCY, posCZ,  negCX, negCY, posCZ };
            // back
            short[] vSide2 = { negCX, posCY, negCZ, posCX, posCY, negCZ, negCX, negCY, negCZ, posCX, negCY, negCZ };
            // left
            short[] vSide3 = { negCX, posCY, posCZ, negCX, posCY, negCZ, negCX, negCY, posCZ, negCX, negCY, negCZ };
            // right                
            short[] vSide4 = { posCX, posCY, negCZ, posCX, posCY, posCZ, posCX, negCY, negCZ, posCX, negCY, posCZ };
            // top
            short[] vSide5 = { posCX, posCY, negCZ, negCX, posCY, negCZ, posCX, posCY, posCZ, negCX, posCY, posCZ };
            // bottom            
            short[] vSide6 = { posCX, negCY, posCZ, negCX, negCY, posCZ, posCX, negCY, negCZ, negCX, negCY, negCZ };
                    
            // prepare texture coordinates
            short[] tex = {  
                1, 0,       0, 0,       1, 1,       0, 1,
            };
            
            Mesh skymesh[] = new Mesh[6];
            skymesh[0] = createSkyBoxFace( vSide1, textures[0], tex ); // front
            skymesh[1] = createSkyBoxFace( vSide2, textures[1], tex ); // back
            skymesh[2] = createSkyBoxFace( vSide3, textures[2], tex ); // left
            skymesh[3] = createSkyBoxFace( vSide4, textures[3], tex ); // right
            skymesh[4] = createSkyBoxFace( vSide5, textures[4], tex ); // top
            skymesh[5] = createSkyBoxFace( vSide6, textures[5], tex ); // bottom

            // compose skybox mesh as a group of submeshes
            g_grpSkybox = new Group();            
            for( int i = 0; i < 6; i++)
                g_grpSkybox.addChild( skymesh[i] );
      
             g_skydometype = SKYDOME_SKYBOX;
             g_background.setColor( 0x123312 ); // default background color, should not be viewable !
             
            // attch background to the world object     
            g_World.setBackground( g_background );
        }
        catch( Exception e )
        {
            System.out.println( "*** SkyDome::SkyBox Construction Exception **** ");
             e.printStackTrace();
        }
    }

   /**
    * @Name: createSkyBoxFace()
    * @Desc: 
    */    
    private static Mesh createSkyBoxFace( short[] vertices, Texture2D texture, short[] texcoords )
    {
        VertexArray v = new VertexArray( vertices.length / 3, 3, 2 );
        v.set( 0, vertices.length / 3, vertices );
        
        VertexArray t = new VertexArray( texcoords.length / 2, 2, 2 );
        t.set( 0, texcoords.length / 2, texcoords );
        
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions( v, 1.0f, null );
        vb.setTexCoords( 0, t, 1.0f, null );
        
        // create the appearance
        Appearance app = new Appearance();        
        app.setTexture( 0, texture ) ;
       
        PolygonMode poly = new PolygonMode();
        poly.setCulling( PolygonMode.CULL_FRONT );
        app.setPolygonMode( poly );

        /*Material mat = new Material();
        mat.setColor( Material.EMISSIVE, 0x1212a2ff );
        mat.setColor(Material.DIFFUSE, 0x12FFFFFF);   // white
        mat.setColor(Material.SPECULAR, 0x12FFFFFF);  // white
        //mat.setShininess(100.0f);        
        app.setMaterial( mat );
         **/
        
        int strips[] = { 4 };        // 2 triangle stripa, ( 6 vertexa  = 4 s index buffer (-2) )
        //int[] ind = { 1, 2, 3, 0, 1, 2 };
        IndexBuffer ib = new TriangleStripArray( 0, strips );
        Mesh meshFlat = new Mesh( vb, ib, app );
        meshFlat.setAppearance( 0, app );
        
        return meshFlat;
    }

    /**
     * 
     * @Name: createPlaneXY()
     * @Desc: 
     * 
     * @param: size - valid plane size (1.0 - ??? )
     * @param: app - valid appearance object
     * @param: tex - can be null or valid texture!
     * @param: texWarpRatio - defines layout of texturing (default should be 1)
     * @param: color - use (-1) for none, any other to set
     */  
    public static Mesh createPlaneXY( int size, Appearance app, Texture2D tex, byte texWarpRatio, int color )
    {
        VertexBuffer vb = new VertexBuffer();
    
        short yy = (short)(size / 2);
        short vertices[] = { (short)-yy, (short)-yy, 0,
                             yy, (short)-yy, 0,
                             (short)-yy, yy, 0,
                             yy, yy, 0 
        };
        
        VertexArray arVert = new VertexArray( vertices.length / 3, 3, 2 );
        arVert.set( 0, vertices.length / 3, vertices );
        vb.setPositions( arVert,  1.0f, null );
        
        if ( null != tex )
        {
             byte tex_coords[] = {    0, texWarpRatio,
                                      texWarpRatio, texWarpRatio,
                                      0, 0,
                                      texWarpRatio, 0 
            };

            VertexArray arTex = new VertexArray( tex_coords.length / 2, 2, 1 );
            arTex.set( 0, tex_coords.length / 2, tex_coords );
            vb.setTexCoords( 0, arTex, 1.0f, null );
            app.setTexture( 0, tex );
        }
        
        if ( -1 != color)
            vb.setDefaultColor( color );
                 
        int idc[] = { 0, 1, 2, 3 };  // quad sys 4ri vertices
        int strips[] = { 4 };        // 2 triangle stripa, ( 4 vertexa )
        IndexBuffer ib = new TriangleStripArray( idc, strips );

        return new Mesh( vb, ib, app );
    }    
    
    /**
     * 
     * @Name: createPlaneXZ()
     * @Desc: 
     * 
     * @param: size - valid plane size (1.0 - ??? )
     * @param: app - valid appearance object
     * @param: tex - can be null or valid texture!
     * @param: color - use (-1) for none, any other to set
     */  
    public static Mesh createPlaneXZ( int size, Appearance app, Texture2D tex, int color )
    {
        VertexBuffer vb = new VertexBuffer();
    
        short yy = (short)(size / 2);
         short vertices[] = {
            (short)-yy, 0, yy,
            yy, 0, yy,
            yy, 0, (short)-yy,
            (short)-yy, 0, (short)-yy
        };
        
        
        VertexArray arVert = new VertexArray( vertices.length / 3, 3, 2 );
        arVert.set( 0, vertices.length / 3, vertices );
        vb.setPositions( arVert,  1.0f, null );
        
        if ( null != tex )
        {
          
            byte tex_coords[] = {   0, 2,
                                    2, 2,
                                    2, 0,
                                    0, 0 
          
            };            
            
            VertexArray arTex = new VertexArray( tex_coords.length / 2, 2, 1 );
            arTex.set( 0, tex_coords.length / 2, tex_coords );
            vb.setTexCoords( 0, arTex, 1.0f, null );
            app.setTexture( 0, tex );
        }
        
        if ( -1 != color )
            vb.setDefaultColor( color );
        
        int idc[] = { 1, 2, 0, 3 };
        int strips[] = { 4 };
        IndexBuffer ib = new TriangleStripArray( idc, strips );

        return new Mesh( vb, ib, app );
    }      
    
    /**
    * @Name: moveSkydome
    * @Desc: moves the skydome at given direction with given speed factor
    * 0 >= factor <= ?
    * 
    */    
    public static void moveSkyDome( byte direction, int factor )
    {
        switch( g_skydometype )
        {
            case SKYDOME_STATIC:
                break;
            
            case SKYDOME_TILED:
                
                switch( direction )
                {
                    case DIR_LEFT:
                        g_background.setCrop( g_background.getCropX() - (SKYDOME_STEP*factor), g_background.getCropY(), 
                                g_screenwidth, g_screenheight );
                        break;
                        
                    case DIR_RIGHT:
                        g_background.setCrop( g_background.getCropX() + (SKYDOME_STEP*factor), g_background.getCropY(), 
                                g_screenwidth, g_screenheight );                        
                        break;
                        
                    case DIR_UP:
                        g_background.setCrop( g_background.getCropX(), g_background.getCropY() + (SKYDOME_STEP*factor), 
                                g_screenwidth, g_screenheight );
                        break;
                        
                    case DIR_DOWN:
                        g_background.setCrop( g_background.getCropX(), g_background.getCropY() - (SKYDOME_STEP*factor), 
                                g_screenwidth, g_screenheight );
                        
                        break;
                }
                
                break;
                
            case SKYDOME_SKYBOX:
                //TODO: add inifinte world check and move box with camera
                break;

        }
    }
    
   

}
