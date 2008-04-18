// $ANTLR 3.0.1 D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g 2008-04-18 13:45:53

package com.noelios.restlet.ext.shell.controller.commands.usage;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class UsageParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "OPT", "REQ", "MULTI", "ID", "LETTER", "WS", "'['", "']'", "'|'"
    };
    public static final int LETTER=8;
    public static final int REQ=5;
    public static final int WS=9;
    public static final int EOF=-1;
    public static final int OPT=4;
    public static final int MULTI=6;
    public static final int ID=7;

        public UsageParser(TokenStream input) {
            super(input);
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g"; }


    public static class usage_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start usage
    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:21:1: usage : ( | command | command ( requiredParameter )+ | command ( optionalParameter )+ | command ( requiredParameter )+ ( optionalParameter )+ );
    public final usage_return usage() throws RecognitionException {
        usage_return retval = new usage_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        command_return command1 = null;

        command_return command2 = null;

        requiredParameter_return requiredParameter3 = null;

        command_return command4 = null;

        optionalParameter_return optionalParameter5 = null;

        command_return command6 = null;

        requiredParameter_return requiredParameter7 = null;

        optionalParameter_return optionalParameter8 = null;



        try {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:22:5: ( | command | command ( requiredParameter )+ | command ( optionalParameter )+ | command ( requiredParameter )+ ( optionalParameter )+ )
            int alt5=5;
            alt5 = dfa5.predict(input);
            switch (alt5) {
                case 1 :
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:23:5: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;
                case 2 :
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:23:7: command
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_command_in_usage87);
                    command1=command();
                    _fsp--;

                    adaptor.addChild(root_0, command1.getTree());

                    }
                    break;
                case 3 :
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:24:7: command ( requiredParameter )+
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_command_in_usage96);
                    command2=command();
                    _fsp--;

                    adaptor.addChild(root_0, command2.getTree());
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:24:15: ( requiredParameter )+
                    int cnt1=0;
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( (LA1_0==ID) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:24:15: requiredParameter
                    	    {
                    	    pushFollow(FOLLOW_requiredParameter_in_usage98);
                    	    requiredParameter3=requiredParameter();
                    	    _fsp--;

                    	    adaptor.addChild(root_0, requiredParameter3.getTree());

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
                    break;
                case 4 :
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:25:7: command ( optionalParameter )+
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_command_in_usage108);
                    command4=command();
                    _fsp--;

                    adaptor.addChild(root_0, command4.getTree());
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:25:15: ( optionalParameter )+
                    int cnt2=0;
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( (LA2_0==10) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:25:15: optionalParameter
                    	    {
                    	    pushFollow(FOLLOW_optionalParameter_in_usage110);
                    	    optionalParameter5=optionalParameter();
                    	    _fsp--;

                    	    adaptor.addChild(root_0, optionalParameter5.getTree());

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


                    }
                    break;
                case 5 :
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:26:7: command ( requiredParameter )+ ( optionalParameter )+
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_command_in_usage120);
                    command6=command();
                    _fsp--;

                    adaptor.addChild(root_0, command6.getTree());
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:26:15: ( requiredParameter )+
                    int cnt3=0;
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==ID) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:26:15: requiredParameter
                    	    {
                    	    pushFollow(FOLLOW_requiredParameter_in_usage122);
                    	    requiredParameter7=requiredParameter();
                    	    _fsp--;

                    	    adaptor.addChild(root_0, requiredParameter7.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt3 >= 1 ) break loop3;
                                EarlyExitException eee =
                                    new EarlyExitException(3, input);
                                throw eee;
                        }
                        cnt3++;
                    } while (true);

                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:26:34: ( optionalParameter )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==10) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:26:34: optionalParameter
                    	    {
                    	    pushFollow(FOLLOW_optionalParameter_in_usage125);
                    	    optionalParameter8=optionalParameter();
                    	    _fsp--;

                    	    adaptor.addChild(root_0, optionalParameter8.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt4 >= 1 ) break loop4;
                                EarlyExitException eee =
                                    new EarlyExitException(4, input);
                                throw eee;
                        }
                        cnt4++;
                    } while (true);


                    }
                    break;

            }
            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end usage

    public static class command_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start command
    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:30:1: command : ID ;
    public final command_return command() throws RecognitionException {
        command_return retval = new command_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID9=null;

        Object ID9_tree=null;

        try {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:31:5: ( ID )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:31:7: ID
            {
            root_0 = (Object)adaptor.nil();

            ID9=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_command141); 
            ID9_tree = (Object)adaptor.create(ID9);
            adaptor.addChild(root_0, ID9_tree);


            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end command

    public static class requiredParameter_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start requiredParameter
    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:34:1: requiredParameter : parameter -> ^( REQ parameter ) ;
    public final requiredParameter_return requiredParameter() throws RecognitionException {
        requiredParameter_return retval = new requiredParameter_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        parameter_return parameter10 = null;


        RewriteRuleSubtreeStream stream_parameter=new RewriteRuleSubtreeStream(adaptor,"rule parameter");
        try {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:35:5: ( parameter -> ^( REQ parameter ) )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:35:7: parameter
            {
            pushFollow(FOLLOW_parameter_in_requiredParameter156);
            parameter10=parameter();
            _fsp--;

            stream_parameter.add(parameter10.getTree());

            // AST REWRITE
            // elements: parameter
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 35:17: -> ^( REQ parameter )
            {
                // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:35:20: ^( REQ parameter )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(adaptor.create(REQ, "REQ"), root_1);

                adaptor.addChild(root_1, stream_parameter.next());

                adaptor.addChild(root_0, root_1);
                }

            }



            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end requiredParameter

    public static class optionalParameter_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start optionalParameter
    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:38:1: optionalParameter : '[' parameter ']' -> ^( OPT parameter ) ;
    public final optionalParameter_return optionalParameter() throws RecognitionException {
        optionalParameter_return retval = new optionalParameter_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal11=null;
        Token char_literal13=null;
        parameter_return parameter12 = null;


        Object char_literal11_tree=null;
        Object char_literal13_tree=null;
        RewriteRuleTokenStream stream_10=new RewriteRuleTokenStream(adaptor,"token 10");
        RewriteRuleTokenStream stream_11=new RewriteRuleTokenStream(adaptor,"token 11");
        RewriteRuleSubtreeStream stream_parameter=new RewriteRuleSubtreeStream(adaptor,"rule parameter");
        try {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:39:5: ( '[' parameter ']' -> ^( OPT parameter ) )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:39:7: '[' parameter ']'
            {
            char_literal11=(Token)input.LT(1);
            match(input,10,FOLLOW_10_in_optionalParameter179); 
            stream_10.add(char_literal11);

            pushFollow(FOLLOW_parameter_in_optionalParameter181);
            parameter12=parameter();
            _fsp--;

            stream_parameter.add(parameter12.getTree());
            char_literal13=(Token)input.LT(1);
            match(input,11,FOLLOW_11_in_optionalParameter183); 
            stream_11.add(char_literal13);


            // AST REWRITE
            // elements: parameter
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 39:25: -> ^( OPT parameter )
            {
                // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:39:28: ^( OPT parameter )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(adaptor.create(OPT, "OPT"), root_1);

                adaptor.addChild(root_1, stream_parameter.next());

                adaptor.addChild(root_0, root_1);
                }

            }



            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end optionalParameter

    public static class parameter_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start parameter
    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:42:1: parameter : ( simpleParameter | multiParameter );
    public final parameter_return parameter() throws RecognitionException {
        parameter_return retval = new parameter_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        simpleParameter_return simpleParameter14 = null;

        multiParameter_return multiParameter15 = null;



        try {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:43:5: ( simpleParameter | multiParameter )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==ID) ) {
                int LA6_1 = input.LA(2);

                if ( (LA6_1==EOF||LA6_1==ID||(LA6_1>=10 && LA6_1<=11)) ) {
                    alt6=1;
                }
                else if ( (LA6_1==12) ) {
                    alt6=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("42:1: parameter : ( simpleParameter | multiParameter );", 6, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("42:1: parameter : ( simpleParameter | multiParameter );", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:43:7: simpleParameter
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_simpleParameter_in_parameter205);
                    simpleParameter14=simpleParameter();
                    _fsp--;

                    adaptor.addChild(root_0, simpleParameter14.getTree());

                    }
                    break;
                case 2 :
                    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:44:7: multiParameter
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_multiParameter_in_parameter214);
                    multiParameter15=multiParameter();
                    _fsp--;

                    adaptor.addChild(root_0, multiParameter15.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end parameter

    public static class simpleParameter_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start simpleParameter
    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:47:1: simpleParameter : ID ;
    public final simpleParameter_return simpleParameter() throws RecognitionException {
        simpleParameter_return retval = new simpleParameter_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID16=null;

        Object ID16_tree=null;

        try {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:48:5: ( ID )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:48:7: ID
            {
            root_0 = (Object)adaptor.nil();

            ID16=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_simpleParameter228); 
            ID16_tree = (Object)adaptor.create(ID16);
            adaptor.addChild(root_0, ID16_tree);


            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end simpleParameter

    public static class multiParameter_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start multiParameter
    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:51:1: multiParameter : ID ( '|' ID )+ -> ^( MULTI ( ID )+ ) ;
    public final multiParameter_return multiParameter() throws RecognitionException {
        multiParameter_return retval = new multiParameter_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID17=null;
        Token char_literal18=null;
        Token ID19=null;

        Object ID17_tree=null;
        Object char_literal18_tree=null;
        Object ID19_tree=null;
        RewriteRuleTokenStream stream_12=new RewriteRuleTokenStream(adaptor,"token 12");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:52:5: ( ID ( '|' ID )+ -> ^( MULTI ( ID )+ ) )
            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:52:7: ID ( '|' ID )+
            {
            ID17=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_multiParameter241); 
            stream_ID.add(ID17);

            // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:52:10: ( '|' ID )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==12) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:52:12: '|' ID
            	    {
            	    char_literal18=(Token)input.LT(1);
            	    match(input,12,FOLLOW_12_in_multiParameter245); 
            	    stream_12.add(char_literal18);

            	    ID19=(Token)input.LT(1);
            	    match(input,ID,FOLLOW_ID_in_multiParameter247); 
            	    stream_ID.add(ID19);


            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);


            // AST REWRITE
            // elements: ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 52:23: -> ^( MULTI ( ID )+ )
            {
                // D:\\alaska\\forge\\build\\swc\\restlet\\trunk\\build\\temp\\modules\\com.noelios.restlet.ext.shell\\src\\main\\antlr\\com\\noelios\\restlet\\ext\\controller\\commands\\usage\\Usage.g:52:26: ^( MULTI ( ID )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(adaptor.create(MULTI, "MULTI"), root_1);

                if ( !(stream_ID.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ID.hasNext() ) {
                    adaptor.addChild(root_1, stream_ID.next());

                }
                stream_ID.reset();

                adaptor.addChild(root_0, root_1);
                }

            }



            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end multiParameter


    protected DFA5 dfa5 = new DFA5(this);
    static final String DFA5_eotS =
        "\12\uffff";
    static final String DFA5_eofS =
        "\1\1\1\uffff\1\4\1\10\5\uffff\1\10";
    static final String DFA5_minS =
        "\1\7\1\uffff\2\7\3\uffff\1\7\1\uffff\1\7";
    static final String DFA5_maxS =
        "\1\7\1\uffff\1\12\1\14\3\uffff\1\7\1\uffff\1\14";
    static final String DFA5_acceptS =
        "\1\uffff\1\1\2\uffff\1\2\1\4\1\5\1\uffff\1\3\1\uffff";
    static final String DFA5_specialS =
        "\12\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\2",
            "",
            "\1\3\2\uffff\1\5",
            "\1\3\2\uffff\1\6\1\uffff\1\7",
            "",
            "",
            "",
            "\1\11",
            "",
            "\1\3\2\uffff\1\6\1\uffff\1\7"
    };

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "21:1: usage : ( | command | command ( requiredParameter )+ | command ( optionalParameter )+ | command ( requiredParameter )+ ( optionalParameter )+ );";
        }
    }
 

    public static final BitSet FOLLOW_command_in_usage87 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_command_in_usage96 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_requiredParameter_in_usage98 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_command_in_usage108 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_optionalParameter_in_usage110 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_command_in_usage120 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_requiredParameter_in_usage122 = new BitSet(new long[]{0x0000000000000480L});
    public static final BitSet FOLLOW_optionalParameter_in_usage125 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_ID_in_command141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_requiredParameter156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_10_in_optionalParameter179 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_parameter_in_optionalParameter181 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_optionalParameter183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleParameter_in_parameter205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiParameter_in_parameter214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_simpleParameter228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_multiParameter241 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_12_in_multiParameter245 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ID_in_multiParameter247 = new BitSet(new long[]{0x0000000000001002L});

}