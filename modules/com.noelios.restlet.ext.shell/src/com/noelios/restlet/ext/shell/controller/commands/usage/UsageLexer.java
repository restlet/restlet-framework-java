// $ANTLR 3.0.1 D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g 2008-04-18 13:45:53

package com.noelios.restlet.ext.shell.controller.commands.usage;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class UsageLexer extends Lexer {
    public static final int T10=10;
    public static final int LETTER=8;
    public static final int REQ=5;
    public static final int T11=11;
    public static final int EOF=-1;
    public static final int WS=9;
    public static final int T12=12;
    public static final int Tokens=13;
    public static final int OPT=4;
    public static final int MULTI=6;
    public static final int ID=7;
    public UsageLexer() {} 
    public UsageLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g"; }

    // $ANTLR start T10
    public final void mT10() throws RecognitionException {
        try {
            int _type = T10;
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:6:5: ( '[' )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:6:7: '['
            {
            match('['); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T10

    // $ANTLR start T11
    public final void mT11() throws RecognitionException {
        try {
            int _type = T11;
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:7:5: ( ']' )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:7:7: ']'
            {
            match(']'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T11

    // $ANTLR start T12
    public final void mT12() throws RecognitionException {
        try {
            int _type = T12;
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:8:5: ( '|' )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:8:7: '|'
            {
            match('|'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T12

    // $ANTLR start ID
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:56:5: ( ( LETTER )+ )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:56:7: ( LETTER )+
            {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:56:7: ( LETTER )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:56:7: LETTER
            	    {
            	    mLETTER(); 

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ID

    // $ANTLR start LETTER
    public final void mLETTER() throws RecognitionException {
        try {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:60:5: ( 'a' .. 'z' )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:60:7: 'a' .. 'z'
            {
            matchRange('a','z'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end LETTER

    // $ANTLR start WS
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:64:5: ( ( ' ' | '\\t' )+ )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:64:7: ( ' ' | '\\t' )+
            {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:64:7: ( ' ' | '\\t' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='\t'||LA2_0==' ') ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


                    channel = HIDDEN;
                

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WS

    public void mTokens() throws RecognitionException {
        // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:1:8: ( T10 | T11 | T12 | ID | WS )
        int alt3=5;
        switch ( input.LA(1) ) {
        case '[':
            {
            alt3=1;
            }
            break;
        case ']':
            {
            alt3=2;
            }
            break;
        case '|':
            {
            alt3=3;
            }
            break;
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
            {
            alt3=4;
            }
            break;
        case '\t':
        case ' ':
            {
            alt3=5;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens : ( T10 | T11 | T12 | ID | WS );", 3, 0, input);

            throw nvae;
        }

        switch (alt3) {
            case 1 :
                // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:1:10: T10
                {
                mT10(); 

                }
                break;
            case 2 :
                // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:1:14: T11
                {
                mT11(); 

                }
                break;
            case 3 :
                // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:1:18: T12
                {
                mT12(); 

                }
                break;
            case 4 :
                // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:1:22: ID
                {
                mID(); 

                }
                break;
            case 5 :
                // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:1:25: WS
                {
                mWS(); 

                }
                break;

        }

    }


 

}