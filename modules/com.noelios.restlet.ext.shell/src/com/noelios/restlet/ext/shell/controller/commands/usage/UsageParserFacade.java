package com.noelios.restlet.ext.shell.controller.commands.usage;

import java.util.Arrays;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

public class UsageParserFacade {

    static final TreeAdaptor adaptor = new CommonTreeAdaptor() {

        @Override
        public Object create(Token payload) {
            return new CommonTree(payload);
        }
    };

    public static String[] buildArguments(final String usage, final String[] args) {
        CommonTree t;

        try {
            final UsageLexer lex = new UsageLexer(new ANTLRStringStream(usage));
            final CommonTokenStream tokens = new CommonTokenStream(lex);
            final UsageParser parser = new UsageParser(tokens);
            parser.setTreeAdaptor(adaptor);
            UsageParser.usage_return r = parser.usage();
            t = (CommonTree) r.getTree();
        } catch (Exception e) {
            throw new RuntimeException("internal parser error (bug)", e);
        }

        int size = t.getChildCount() - 1;

        if (size < 0) {
            size = 0;
        }

        final String[] realArgs = new String[size];
        Arrays.fill(realArgs, "");

        for (int i = 0; i < args.length - 1; i++) {
            realArgs[i] = args[i + 1];
        }

        validateUsage(t, realArgs);
        return realArgs;
    }

    private static void validateUsage(CommonTree t, String[] args) {
        for (int i = 1; i < t.getChildCount(); i++) {
            String type = t.getChild(i).getText();

            if (type.equals("REQ")) {
                if (t.getChild(i).getChild(0).getText().equals("MULTI")) {
                    isValidMultiValueArgument((CommonTree) t.getChild(i).getChild(0), args[i - 1]);
                }

                if (args[i - 1].equals("")) {
                    throw new UsageException("missing required argument");
                }
            } else if (type.equals("OPT")) {
                try {
                    if (!args[i - 1].equals("") && t.getChild(i).getChild(0).getText().equals("MULTI")) {
                        isValidMultiValueArgument((CommonTree) t.getChild(i).getChild(0), args[i - 1]);
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
    }

    private static void isValidMultiValueArgument(CommonTree t, final String argument) {
        for (int i = 0; i < t.getChildCount(); i++) {
            if (t.getChild(i).getText().equals(argument)) {
                return;
            }
        }

        throw new UsageException("invalid %s argument", argument);
    }
}
