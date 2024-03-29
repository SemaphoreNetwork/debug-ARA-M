package com.gieseckedevrient.javacard.allowall;

import java.applet.Applet;

import javacard.framework.*;

public class AllowAll extends Applet implements MultiSelectable
{    
    static final byte INS_GET_DATA            = (byte)  0xCA;
    static final short TAG_CMD_GET_NEXT       = (short) 0xFF60;
    static final short TAG_CMD_GET_SPECIFIC   = (short) 0xFF50;
    static final short TAG_CMD_GET_ALL        = (short) 0xFF40;
    static final short TAG_CMD_GET_REFRESH    = (short) 0xDF20;
    

    private final static byte[] RESPONSE_GET_REFRESH = { (byte)0xDF, (byte)0x20, (byte)0x08, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08 };
        
    private final static byte[] RESPONSE_GET_SPECIFIC = { (byte)0xFF, (byte)0x50, (byte)0x08, (byte) 0xE3, (byte) 0x06, (byte)0xD0, (byte)0x01, (byte)0x01, (byte)0xD1, (byte)0x01, (byte)0x01 };
    
    //private final static byte[] RESPONSE_GET_ALL = {(byte)0xFF, (byte)0x40,(byte)0x10, (byte)0xE2,(byte)0x0E, (byte)0xE1,(byte)0x04, (byte)0x4F,(byte)0x00, (byte)0xC1, (byte)0x00, (byte)0xE3, (byte)0x06, (byte)0xD0, (byte)0x01, (byte)0x01, (byte)0xD1, (byte)0x01, (byte)0x01 };
    // According to: https://android.googlesource.com/platform/frameworks/opt/telephony/+/refs/heads/oreo-m8-release/src/java/com/android/internal/telephony/uicc/UiccCarrierPrivilegeRules.java
    /*
     FF40 26
	E2 24
	  E1 16
	    C1 14 3263FBC1526C9020A38BD5F4B2A091784CF6E313
	  E3 0A
	    DB 08 FFFFFFFFFFFFFFFF
     */
    //private final static byte[] RESPONSE_GET_ALL = {(byte)0xFF, (byte)0x40, (byte)0x26, (byte)0xE2, (byte)0x24, (byte)0xE1, (byte)0x16, (byte)0xC1, (byte)0x14, (byte)0x32, (byte)0x63, (byte)0xFB, (byte)0xC1, (byte)0x52, (byte)0x6C, (byte)0x90, (byte)0x20, (byte)0xA3, (byte)0x8B, (byte)0xD5, (byte)0xF4, (byte)0xB2, (byte)0xA0, (byte)0x91, (byte)0x78, (byte)0x4C, (byte)0xF6, (byte)0xE3, (byte)0x13, (byte)0xE3, (byte)0x0A, (byte)0xDB, (byte)0x08, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
    
    /*
     *FF40 41
	    E2 3E
	      E1 30
	        C1 14 3263FBC1526C9020A38BD5F4B2A091784CF6E313
	        CA 18 636f6d2e73656d6170686f72656e6574776f726b2e737373
	      E3 0A
	        DB 08 0000000000000001
     * */
	private final static byte[] RESPONSE_GET_ALL = {(byte)0xFF, (byte)0x40, (byte)0x41, (byte)0xE2, (byte)0x3E, (byte)0xE1, (byte)0x30, (byte)0xC1, (byte)0x14, (byte)0x32, (byte)0x63, (byte)0xFB, (byte)0xC1, (byte)0x52, (byte)0x6C, (byte)0x90, (byte)0x20, (byte)0xA3, (byte)0x8B, (byte)0xD5, (byte)0xF4, (byte)0xB2, (byte)0xA0, (byte)0x91, (byte)0x78, (byte)0x4C, (byte)0xF6, (byte)0xE3, (byte)0x13, (byte)0xCA, (byte)0x18, (byte)0x63, (byte)0x6f, (byte)0x6d, (byte)0x2e, (byte)0x73, (byte)0x65, (byte)0x6d, (byte)0x61, (byte)0x70, (byte)0x68, (byte)0x6f, (byte)0x72, (byte)0x65, (byte)0x6e, (byte)0x65, (byte)0x74, (byte)0x77, (byte)0x6f, (byte)0x72, (byte)0x6b, (byte)0x2e, (byte)0x73, (byte)0x73, (byte)0x73, (byte)0xE3, (byte)0x0A, (byte)0xDB, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01};

    public static void install(byte[] abArray, short sOffset, byte bLength)
    {        
        (new AllowAll()).register( abArray, (short) (sOffset + 1), abArray[sOffset] );
    }
    
	public boolean select(boolean appInstAlreadyActive)
	{
	   return true;
	}

	public void deselect(boolean appInstStillActive)
	{
	   return;
	}
    
    public void process(APDU oAPDU) throws ISOException
    {
        short sSW1SW2 = ISO7816.SW_NO_ERROR;
        short sOutLength = (short)0;
        
        byte[] abData = oAPDU.getBuffer();
        
        byte bINS = abData[ ISO7816.OFFSET_INS ];
        
        short sMode = Util.getShort( abData, ISO7816.OFFSET_P1 );
        
        if( selectingApplet() == true )
        {
            return;
        }

        try
        {
            if( bINS != INS_GET_DATA )
                ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED );
            
            if( sMode == TAG_CMD_GET_ALL )
            {
                sOutLength = Util.arrayCopyNonAtomic( RESPONSE_GET_ALL,
                                                      (short)0,
                                                      abData,
                                                      (short)0,
                                                      (short)RESPONSE_GET_ALL.length
                                                    );
            }
            else if( sMode == TAG_CMD_GET_SPECIFIC )
            {
                oAPDU.setIncomingAndReceive();
                
                sOutLength = Util.arrayCopyNonAtomic( RESPONSE_GET_SPECIFIC,
                                                      (short)0,
                                                      abData,
                                                      (short)0,
                                                      (short)RESPONSE_GET_SPECIFIC.length
                                                    );
            }
            else if( sMode == TAG_CMD_GET_NEXT )
            {
                ISOException.throwIt( ISO7816.SW_CONDITIONS_NOT_SATISFIED );
            }
            else if( sMode == TAG_CMD_GET_REFRESH )
            {
                sOutLength = Util.arrayCopyNonAtomic( RESPONSE_GET_REFRESH,
                                                      (short)0,
                                                      abData,
                                                      (short)0,
                                                      (short)RESPONSE_GET_REFRESH.length
                                                    );
            }
            else
            {
                ISOException.throwIt( ISO7816.SW_WRONG_P1P2 );
            }
        }
        catch( ISOException e )
        {
            sSW1SW2 = e.getReason();
        }
        catch( Exception e )
        {
            sSW1SW2 = ISO7816.SW_UNKNOWN;
        }
        
        if( sSW1SW2 != ISO7816.SW_NO_ERROR )
        {
            ISOException.throwIt( sSW1SW2 );
        }
        
        if( sOutLength > (short)0 )
        {
            oAPDU.setOutgoingAndSend( (short)0, sOutLength );
        }
    }
}
