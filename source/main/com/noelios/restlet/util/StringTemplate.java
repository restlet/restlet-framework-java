/*
 * Copyright 2005-2006 Jerome LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * String template that enforces a strict separation between the pattern and the model.<br/>
 * It supports variable insertion and non-nestable conditions.<br/>
 * The default delimiters are "${" and "}". For variable insertion, use "${variable_name}" and for conditions use:<br/>
 *  1) "${if variable_name}" to test the availability of a variable (non null).<br/>
 *  2) "${else if variable_name}" to chain another test.<br/>
 *  3) "${else}" to chain a default operation.<br/>
 *  4) "${end}" to close a condition.<br/>
 */
public class StringTemplate
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.util.StringTemplate");

   protected static final int STATE_TEXT = 1;

   protected static final int STATE_POTENTIAL_DELIMITER_START = 2;

   protected static final int STATE_INSTRUCTION = 3;

   protected static final int STATE_POTENTIAL_DELIMITER_END = 4;

   protected static final int STATE_INSTRUCTION_APPEND = 1;

   protected static final int STATE_INSTRUCTION_CONDITION_APPEND = 2;

   protected static final int STATE_INSTRUCTION_CONDITION_SKIP = 3;

   protected static final int STATE_INSTRUCTION_CONDITION_DONE = 4;

   /** The template to process. */
   protected CharSequence template;

   /** The string that defines instructions start delimiters. */
   protected String delimiterStart;

   /** The string that defines instructions end delimiters. */
   protected String delimiterEnd;

   /**
    * Constructor. Uses the default delimiters "${" and "}".
    * @param template The template to process.
    */
   public StringTemplate(CharSequence template)
   {
      this(template, "${", "}");
   }

   /**
    * Constructor.
    * @param pattern The template pattern to process.
    * @param delimiterStart The string that defines instructions start delimiters.
    * @param delimiterEnd The string that defines instructions end delimiters.
    */
   public StringTemplate(CharSequence pattern, String delimiterStart, String delimiterEnd)
   {
      this.template = pattern;
      this.delimiterStart = delimiterStart;
      this.delimiterEnd = delimiterEnd;
   }

   public String getTemplate()
   {
      return this.template.toString();
   }

   /**
    * Processes the template using the given data model.
    * @param model The template model to use.
    * @return The generated string.
    */
   public String process(ReadableModel model)
   {
      StringBuilder sb = new StringBuilder();
      char nextChar = 0;
      int potentialStart = 0;
      int potentialEnd = 0;
      int potentialIndex = 0;
      int textStart = 0;
      int instructionStart = 0;
      int parseState = STATE_TEXT;
      int state = STATE_INSTRUCTION_APPEND;

      int i = 0;
      for(; i < template.length(); i++)
      {
         nextChar = template.charAt(i);

         switch(parseState)
         {
            case STATE_TEXT:
               if(nextChar == delimiterStart.charAt(0))
               {
                  if(delimiterStart.length() == 1)
                  {
                     state = processText(state, textStart, i, sb, model);
                     instructionStart = i + 1;
                     parseState = STATE_INSTRUCTION;
                  }
                  else
                  {
                     potentialStart = i;
                     parseState = STATE_POTENTIAL_DELIMITER_START;
                  }
               }
               else
               {
                  // Continue
               }
               break;

            case STATE_POTENTIAL_DELIMITER_START:
               potentialIndex = i - potentialStart;

               if(nextChar == delimiterStart.charAt(potentialIndex))
               {
                  if(delimiterStart.length() == potentialIndex + 1)
                  {
                     // End of delimiter reached
                     state = processText(state, textStart, potentialStart, sb, model);
                     instructionStart = i + 1;
                     parseState = STATE_INSTRUCTION;
                  }
                  else
                  {
                     // Continue
                  }
               }
               else
               {
                  // This was not a start delimiter
                  parseState = STATE_TEXT;

                  // Go back to the potential start
                  i = potentialStart;
               }
               break;

            case STATE_INSTRUCTION:
               if(nextChar == delimiterEnd.charAt(0))
               {
                  if(delimiterEnd.length() == 1)
                  {
                     state = processInstruction(state, instructionStart, i, sb, model);
                     textStart = i + 1;
                     parseState = STATE_TEXT;
                  }
                  else
                  {
                     potentialEnd = i;
                     parseState = STATE_POTENTIAL_DELIMITER_END;
                  }
               }
               else
               {
                  // Continue inside instruction
               }

               break;

            case STATE_POTENTIAL_DELIMITER_END:
               potentialIndex = i - potentialEnd;

               if(nextChar == delimiterEnd.charAt(potentialIndex))
               {
                  if(delimiterEnd.length() == potentialIndex + 1)
                  {
                     // End of delimiter reached
                     state = processInstruction(state, instructionStart, potentialEnd, sb, model);
                     textStart = i + 1;
                     parseState = STATE_TEXT;
                  }
                  else
                  {
                     // Continue
                  }
               }
               else
               {
                  // This was not an end delimiter
                  parseState = STATE_INSTRUCTION;

                  // Go back to the potential end
                  i = potentialEnd;
               }
               break;
         }

      }

      // Flush any trailing text
      if(parseState == STATE_TEXT)
      {
         processText(state, textStart, i, sb, model);
      }

      logger.log(Level.FINE, "Template result", sb);
      return sb.toString();
   }

   /**
    * Processes a text token.
    * @param state The current instruction state. (see STATE_INSTRUCTION_* constants).
    * @param tokenStart The start index of the token to process.
    * @param tokenEnd The end index of the token to process.
    * @param buffer The string buffer containing the template result.
    * @param model The template model to use.
    * @return The new state after processing.
    */
   protected int processText(int state, int tokenStart, int tokenEnd, StringBuilder buffer, ReadableModel model)
   {
      logger.log(Level.FINER, "Process text", buffer);

      if((state == STATE_INSTRUCTION_APPEND) || (state == STATE_INSTRUCTION_CONDITION_APPEND))
      {
         append(tokenStart, tokenEnd, buffer);
      }

      return state;
   }

   /**
    * Processes an instruction token.
    * @param state The current instruction state. (see STATE_INSTRUCTION_* constants).
    * @param tokenStart The start index of the token to process.
    * @param tokenEnd The end index of the token to process.
    * @param buffer The string buffer containing the template result.
    * @param model The template model to use.
    * @return The state after processing.
    */
   protected int processInstruction(int state, int tokenStart, int tokenEnd, StringBuilder buffer,
         ReadableModel model)
   {
      String instruction = template.subSequence(tokenStart, tokenEnd).toString();
      logger.log(Level.FINER, "processInstruction: " + instruction, buffer);

      if(instruction.startsWith("if "))
      {
         String condition = template.subSequence(tokenStart + 3, tokenEnd).toString();

         if(evaluateCondition(condition, model))
         {
            state = STATE_INSTRUCTION_CONDITION_APPEND;
         }
         else
         {
            state = STATE_INSTRUCTION_CONDITION_SKIP;
         }
      }
      else if(instruction.startsWith("else if "))
      {
         if(state == STATE_INSTRUCTION_CONDITION_SKIP)
         {
            String condition = template.subSequence(tokenStart + 4, tokenEnd).toString();

            if(evaluateCondition(condition, model))
            {
               state = STATE_INSTRUCTION_CONDITION_APPEND;
            }
         }
      }
      else if(instruction.equals("else"))
      {
         if(state == STATE_INSTRUCTION_CONDITION_SKIP)
         {
            state = STATE_INSTRUCTION_CONDITION_APPEND;
         }
         else if(state == STATE_INSTRUCTION_CONDITION_APPEND)
         {
            state = STATE_INSTRUCTION_CONDITION_SKIP;
         }
      }
      else if(instruction.equals("end"))
      {
         state = STATE_INSTRUCTION_APPEND;
      }
      else
      {
         if((state == STATE_INSTRUCTION_APPEND) || (state == STATE_INSTRUCTION_CONDITION_APPEND))
         {
            if(model != null)
            {
               buffer.append(model.get(instruction));
            }
         }
      }

      return state;
   }

   /**
    * Evalutes an instruction's condition.
    * @param condition The condition to evaluate.
    * @param model The template model to use.
    * @return The evaluation result.
    */
   protected boolean evaluateCondition(String condition, ReadableModel model)
   {
      logger.log(Level.FINER, "evaluateCondition: " + condition, model);
      boolean result = false;

      if(model != null)
      {
         if(condition.endsWith("?exists"))
         {
            String key = condition.subSequence(0, condition.length() - 7).toString();
            result = model.contains(key);
         }
         else if(model.contains(condition))
         {
            Object value = model.get(condition);

            if(value instanceof Boolean)
            {
               result = ((Boolean)value).booleanValue();
            }
            else if(value instanceof Integer)
            {
               result = (((Integer)value).intValue() != 0);
            }
            else if(value instanceof Float)
            {
               result = (((Float)value).floatValue() != (float)0);
            }
            else if(value instanceof Long)
            {
               result = (((Long)value).longValue() != (long)0);
            }
            else
            {
               result = (value != null);
            }
         }
      }

      return result;
   }

   /**
    * Appends template characters to an appendable object.
    * @param startIndex The start index in the template.
    * @param endIndex The end index in the template.
    * @param appendable The appendable object to update.
    */
   protected void append(int startIndex, int endIndex, Appendable appendable)
   {
      try
      {
         for(int i = startIndex; i < endIndex; i++)
         {
            appendable.append(template.charAt(i));
         }
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

}
