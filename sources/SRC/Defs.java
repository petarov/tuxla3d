package SRC;

class Defs
{
    
    //@ IF 176x208
    public static final int SCREEN_WIDTH = 176;
    public static final int SCREEN_HEIGHT = 208;
    //@ ELSE
        //@ IF 176x220
        //@ public static final int SCREEN_WIDTH = 176;
        //@ public static final int SCREEN_HEIGHT = 220;
        //@ ELSE
            //@ IF 240x320
            //@ public static final int SCREEN_WIDTH = 240;
            //@ public static final int SCREEN_HEIGHT = 320;             
            //@ ELSE
            //@ public static final int SCREEN_WIDTH = 176;
            //@ public static final int SCREEN_HEIGHT = 208;             
            //@ ENDIF
        //@ ENDIF
    //@ ENDIF
    

    final static int MENU_LOADBAR_SIZE = 24;
    final static int MENU_PROGRESS_RATIO = 3; //  MENU_LOADBAR_SIZE / loading_states(8) = 3
   
    final static int MENU_WIDTH = 125;
    final static int MENU_HEIGHT = 117; //+13px
    final static int MENU_NORMAL_CX = (SCREEN_WIDTH>>1) - (MENU_WIDTH>>1);
    final static int MENU_NORMAL_CY = (SCREEN_HEIGHT>>1) - (MENU_HEIGHT>>1);
    
    final static int MENU_LARGE_WIDTH = 166;
    final static int MENU_LARGE_HEIGHT = 178;
    final static int MENU_LARGE_CX = (SCREEN_WIDTH>>1) - (MENU_LARGE_WIDTH>>1);
    final static int MENU_LARGE_CY = (SCREEN_HEIGHT>>1) - (MENU_LARGE_HEIGHT>>1);

    final static int MENU_TEXT_HEIGHT = 12;
    final static int MENU_TEXT_SPACE = 17;

    final static int MENU_NEW_GAME = 0;
    final static int MENU_OPTIONS = 1;
    final static int MENU_GAME_DETAILS = 2;
    final static int MENU_GAME_DETAILS_HIGH = 3;
    final static int MENU_GAME_DETAILS_LOW = 4;
    final static int MENU_GAME_DETAILS_MEDIUM = 5;
    final static int MENU_CREDITS = 6;
    final static int MENU_HIGH_SCORE = 7;
    final static int MENU_HOW_TO_PLAY = 8;
    final static int MENU_CONTINUE = 9;
    final static int MENU_EXIT = 10;
    final static int MENU_ARE_YOU_SURE = 11;
    final static int MENU_NO = 12;
    final static int MENU_YES = 13;
    final static int MENU_ENTER_YOUR_NAME = 14;
    final static int MENU_OK = 15;
    final static int MENU_GAMEOVER = 16;
    final static int MENU_GAME_CREDITS = 17;
    
    // game core system defines
    final static int FPS = 25; //40FPS  // -> this is how often physics is updated!
    final static int FPS_3D = 15;   //60FPS // -> render 3D graphics as fast as 60fps 
    final static int FPS_CUBE_ROT = 33;
    
    // game deatils 
    final static int DETAILS_NOTSET = 10;
    final static int DETAILS_LOW    = 0;
    final static int DETAILS_MEDIUM = 1;
    final static int DETAILS_HIGH   = 2;

    //@ IF RELEASE_TESTS    
    final static int DETAILS_DEBUG  = 3;
    //@ ENDIF
    
    // game scores
    final static short SCORE_RATIO          = 10;
    final static short BIG_SCORE_OFFEST     = 100; 
    
    // game states
    final static int GS_LOADING = 1;
    final static int GS_GAMEPLAY = 2;
    final static int GS_GAMEPLAY_REARRANGED = 3;        // dedicated to Lim'B.
    final static int GS_GAMEPLAY_ZOOMEXPLOSION = 5;
    final static int GS_GAMEPLAY_SYNCBOARD = 6;
    final static int GS_GAMEPLAY_CREATECUBE = 7;
//    final static int GS_PAUSED = 30;
//    final static int GS_CLOSING = 31;
    final static int GS_GAMEOVER = 40;
    final static int GS_MENU = 50; 
    final static int GS_MENU_OPTIONS = 51;
    final static int GS_MENU_HOW_TO_PLAY = 52;
    final static int GS_MENU_HOW_TO_PLAY_MORE = 53;
    final static int GS_MENU_CREDITS = 54;
    final static int GS_MENU_EXIT = 55;
    final static int GS_MENU_HIGHSCORE = 56;
    final static int GS_MENU_ENTERNAME = 57;
    
    // zoom zones
    final static int ZONE_LEFT_UP          = 1;
    final static int ZONE_LEFT_DOWN        = 2;
    final static int ZONE_RIGHT_UP         = 3;
    final static int ZONE_RIGHT_DOWN       = 4;
    final static int ZONE_MIDDLE_UP        = 5;
    final static int ZONE_MIDDLE_DOWN      = 6;
    
}
