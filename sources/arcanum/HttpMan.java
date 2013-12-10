package arcanum;

import javax.microedition.io.*;

import REZ.tresTexts;

import java.io.*;

/*
 * (t3h) HttpMan v 1.2
 * 
 * $Log:
 * 
 *      $Date: 07 Feb 2006 
 *      $Author: p.petrov 
 *      $Desc:   added instance init()
 *               modified thread creation   
 * 
 *      $Date: 30 Jan 2006 
 *      $Author: p.petrov 
 *      $Desc:   added error handling   
 *      
 *      $Date: 25 Jan 2006 
 *      $Author: p.petrov 
 *      $Desc:   Initial
 *      
 */


public final class HttpMan implements Runnable
{
    public static HttpMan g_instance = null;
    
    private static String m_sUrl;
    private static InputStream  m_is;
    private static HttpConnection m_conn;
        
    public static boolean g_bIsBusy = false;            // finished http query!
    public static boolean g_bCancel = false;
    public static StringBuffer g_sResponse;
    public static int g_errorResponseCode = 0;
    public static int g_errorMsgIndex = 0;
    
    public static void init()
    {
        g_instance = new HttpMan();
    }
    
    private boolean Open()
    {
        
        //@ IF DEBUG
        System.err.println("*** [HttpMan] Opening connection to : " + m_sUrl);
        //@ ENDIF
        
        try
        {
            m_conn = (HttpConnection)Connector.open(
                    m_sUrl, 
                    Connector.READ_WRITE, 
                    true);
                       
            //@ IF DEBUG
            System.err.println("*** [HttpMan] Opened ! Getting response ... ");
            //@ ENDIF           
    
            m_conn.setRequestMethod( HttpConnection.GET );
            m_conn.setRequestProperty("User-Agent", "Profile/MIDP-1.0 Configuration/CLDC-1.0");
            m_conn.setRequestProperty("Content-Language", "en-US");
            m_conn.setRequestProperty("Connection", "close");
            
            g_errorResponseCode = m_conn.getResponseCode();
            if ( HttpConnection.HTTP_OK != g_errorResponseCode )
            {
                //g_errorResponseCode = m_conn.getResponseCode();
                g_errorMsgIndex = tresTexts.tx_http_bad_response;
                
                //@ IF DEBUG                
                System.err.println("*** [HttpMan] Bad Response: "  + g_errorResponseCode );
                //@ ENDIF
                
                Close();
                
                return false;
            }
            
            g_errorResponseCode = 0;
            m_is = m_conn.openInputStream();
        }
        catch( Exception e )
        {
            g_errorResponseCode = -1;
            g_errorMsgIndex = tresTexts.tx_http_url_open_failed;
            
            //@ IF DEBUG            
            System.err.println("***[HttpMan] Could not connect: " + e.getMessage() );
            //@ ENDIF            

            
            Close();
            
            return false;
        }
    
        return true;
    }
    
    private void Close()
    {
        //@ IF DEBUG
        System.err.println("*** [HttpMan] Closing...");
        //@ ENDIF
        
        try
        {
            if ( null != m_is )
            {
                m_is.close();
                m_is = null;
            }
            
            if ( null != m_conn )
            {
                m_conn.close();
                m_conn = null;
            }
            
            System.gc();
            
            //@ IF DEBUG
            System.err.println("*** [HttpMan] Closed !");
            //@ ENDIF            
        }
        catch( Exception e )
        {
            //@ IF DEBUG
            System.err.println("*** [HttpMan] Exception while closing : " + e.getMessage() );
            //@ ENDIF
        }

    }
    
    private void getResponse()
    {
        StringBuffer buf = new StringBuffer(512);
        
        int idx = 0, c = 0;
        do
        {
            try
            {
                c = m_is.read();
            }
            catch( Exception e )
            {
                //@ IF DEBUG
                System.err.println("*** [HttpMan] getRespnse() : Failed reading from InputStream() !");
                //@ ENDIF
                break;
            }
            
            if ( c < 0 )
                break;
            else if ( c < 32 )
            {
                if ( idx != 0 && c == 13 )  // ignore \r
                    continue;
            }
            else
            {
                buf.append( (char)c );
                idx++;
            }
        } while( c > 0 );
           
        // write final result
        g_sResponse = buf; //new String( buf );
    }
    
    public void run()
    {
        try
        {
        Thread.sleep( 2000 );
        }
        catch( Exception e )
        {}
        
        g_sResponse = null;
        
        if( Open() )
        {
            if( !g_bCancel )
            {
                getResponse();
            }
            else
            {
                g_errorMsgIndex = tresTexts.tx_http_canceled;
                g_errorResponseCode = -1;
                
                //@ IF DEBUG
                System.err.println("*** [HttpMan] User Canceled the operation !");
                //@ ENDIF
            }
        }
       
        Close();
        
        g_bIsBusy = false;
    }
    
    
    public static void postMessage( String url )
    {
        //@ IF DEBUG
        if ( null == g_instance )
        {
            System.err.println("*** [HttpMan] you must call init() once in an upper module (MAIN) !");
            return;
        }
        //@ ENDIF        
        
        g_errorMsgIndex = 0;        // no error ! (http://www.noerror.org/)
        g_errorResponseCode = 0;    // HTTP response !    
        g_bIsBusy = true;
        g_bCancel = false;
        m_sUrl = url;

        Thread th = new Thread( g_instance );        
        th.start();
    }

}
