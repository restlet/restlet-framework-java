/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.shell.controller.commands.usage;

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

    public static String[] buildArguments(final String usage,
            final String[] args) {
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

    private static void isValidMultiValueArgument(CommonTree t,
            final String argument) {
        for (int i = 0; i < t.getChildCount(); i++) {
            if (t.getChild(i).getText().equals(argument)) {
                return;
            }
        }

        throw new UsageException("invalid %s argument", argument);
    }

    private static void validateUsage(CommonTree t, String[] args) {
        for (int i = 1; i < t.getChildCount(); i++) {
            String type = t.getChild(i).getText();

            if (type.equals("REQ")) {
                if (t.getChild(i).getChild(0).getText().equals("MULTI")) {
                    isValidMultiValueArgument((CommonTree) t.getChild(i)
                            .getChild(0), args[i - 1]);
                }

                if (args[i - 1].equals("")) {
                    throw new UsageException("missing required argument");
                }
            } else if (type.equals("OPT")) {
                try {
                    if (!args[i - 1].equals("")
                            && t.getChild(i).getChild(0).getText().equals(
                                    "MULTI")) {
                        isValidMultiValueArgument((CommonTree) t.getChild(i)
                                .getChild(0), args[i - 1]);
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
    }
}
