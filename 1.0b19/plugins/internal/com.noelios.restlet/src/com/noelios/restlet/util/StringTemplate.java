/*
 * Copyright 2005-2006 Noelios Consulting.
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
 * String template that enforces a strict separation between the pattern and the model. It supports 
 * variable insertion and non-nestable conditions. The default delimiters are "${" and "}" for variables 
 * and "#[" and "]" for instructions. Currently, the only instructions supported are conditions:<br/>
 *  1) "#[if variableName]" to test the availability of a variable (non null).<br/>
 *  2) "#[else if variableName]" to chain another test.<br/>
 *  3) "#[else]" to chain a default operation.<br/>
 *  4) "#[end]" to close a condition.<br/>
 * Also, note that a condition can also be applied on variable insertions by using the following syntax: 
 * "${variableName?exists}". This will ensure that the insertion only happens if the model contains such
 * a variable.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class StringTemplate
{
   private static final int STATE_INSTRUCTION = 1;
   private static final int STATE_INSTRUCTION_POTENTIAL_DELIMITER_START = 2;
   private static final int STATE_INSTRUCTION_POTENTIAL_DELIMITER_END = 3;
   private static final int STATE_TEXT = 4;
   private static final int STATE_VARIABLE = 5;
   private static final int STATE_VARIABLE_POTENTIAL_DELIMITER_START = 6;
   private static final int STATE_VARIABLE_POTENTIAL_DELIMITER_END = 7;

   private static final int TEXT_APPEND = 1;
   private static final int TEXT_SKIP = 2;
   
   /** The template to process. */
   private CharSequence template;

   /** The string that defines variable start delimiters. */
   private String variableStart;

   /** The string that defines variable end delimiters. */
   private String variableEnd;

   /** The string that defines instruction start delimiters. */
   private String instructionStart;

   /** The string that defines instruction end delimiters. */
   private String instructionEnd;
   
   /** The logger to use. */
   private Logger logger;

   /**
    * Constructor. Uses the default delimiters "${" and "}" for variables, "#[" and "]" for instructions.
    * @param template The template to process.
    */
   public StringTemplate(CharSequence template)
   {
      this(template, "${", "}", "#[", "]");
   }

   /**
    * Constructor.
    * @param pattern The template pattern to process.
    * @param variableStart The string that defines instructions start delimiters.
    * @param variableEnd The string that defines instructions end delimiters.
    * @param instructionStart The string that defines instructions start delimiters.
    * @param instructionEnd The string that defines instructions end delimiters.
    */
   public StringTemplate(CharSequence pattern, String variableStart, String variableEnd, String instructionStart, String instructionEnd)
   {
      this.template = pattern;
      this.variableStart = variableStart;
      this.variableEnd = variableEnd;
      this.instructionStart = instructionStart;
      this.instructionEnd = instructionEnd;
      
      if((variableStart.charAt(0) == instructionStart.charAt(0)) || (variableEnd.charAt(0) == instructionEnd.charAt(0)))
      {
      	throw new IllegalArgumentException("Variable and instruction delimiters must start with a different character");
      }
   }

   /**
    * Returns the template to process.
    * @return The template to process.
    */
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
      
      /** Potential start index of an instruction or variable. */
      int potentialStart = 0;

      /** Potential end index of the current instruction or variable. */
      int potentialEnd = 0;

      /** Current index of a potential instruction or variable. */
      int potentialIndex = 0;
      
      /** Start index of the current text token. */
      int textStartIndex = 0;

      /** Start index of the current instruction. */
      int instructionStartIndex = 0;

      /** Start index of the current variable. */
      int variableStartIndex = 0;

      /** Current parsing state. */
      int parseState = STATE_TEXT;
      
      /** Current text state. */
      int textState = TEXT_APPEND;

      // Start parsing the template
      int i = 0;
      for(; i < template.length(); i++)
      {
         nextChar = template.charAt(i);

         switch(parseState)
         {
            case STATE_TEXT:
               if(nextChar == instructionStart.charAt(0))
               {
                  if(instructionStart.length() == 1)
                  {
                     processText(textState, textStartIndex, i, sb, model);
                     instructionStartIndex = i + 1;
                     parseState = STATE_INSTRUCTION;
                  }
                  else
                  {
                     potentialStart = i;
                     parseState = STATE_INSTRUCTION_POTENTIAL_DELIMITER_START;
                  }
               }
               else if(nextChar == variableStart.charAt(0))
               {
                  if(variableStart.length() == 1)
                  {
                     processText(textState, textStartIndex, i, sb, model);
                     variableStartIndex = i + 1;
                     parseState = STATE_VARIABLE;
                  }
                  else
                  {
                     potentialStart = i;
                     parseState = STATE_VARIABLE_POTENTIAL_DELIMITER_START;
                  }
               }
               else
               {
                  // Continue
               }
               break;

            case STATE_INSTRUCTION_POTENTIAL_DELIMITER_START:
               potentialIndex = i - potentialStart;

               if(nextChar == instructionStart.charAt(potentialIndex))
               {
                  if(instructionStart.length() == potentialIndex + 1)
                  {
                     // End of delimiter reached
                     processText(textState, textStartIndex, potentialStart, sb, model);
                     instructionStartIndex = i + 1;
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

            case STATE_VARIABLE_POTENTIAL_DELIMITER_START:
               potentialIndex = i - potentialStart;

               if(nextChar == variableStart.charAt(potentialIndex))
               {
                  if(variableStart.length() == potentialIndex + 1)
                  {
                     // End of delimiter reached
                     processText(textState, textStartIndex, potentialStart, sb, model);
                     variableStartIndex = i + 1;
                     parseState = STATE_VARIABLE;
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
               if(nextChar == instructionEnd.charAt(0))
               {
                  if(instructionEnd.length() == 1)
                  {
                     textState = processInstruction(textState, instructionStartIndex, i, sb, model);
                     textStartIndex = i + 1;
                     parseState = STATE_TEXT;
                  }
                  else
                  {
                     potentialEnd = i;
                     parseState = STATE_INSTRUCTION_POTENTIAL_DELIMITER_END;
                  }
               }
               else
               {
                  // Continue inside instruction
               }

               break;

            case STATE_VARIABLE:
               if(nextChar == variableEnd.charAt(0))
               {
                  if(variableEnd.length() == 1)
                  {
                     textState = processVariable(textState, variableStartIndex, i, sb, model);
                     textStartIndex = i + 1;
                     parseState = STATE_TEXT;
                  }
                  else
                  {
                     potentialEnd = i;
                     parseState = STATE_VARIABLE_POTENTIAL_DELIMITER_END;
                  }
               }
               else
               {
                  // Continue inside instruction
               }

               break;

            case STATE_INSTRUCTION_POTENTIAL_DELIMITER_END:
               potentialIndex = i - potentialEnd;

               if(nextChar == instructionEnd.charAt(potentialIndex))
               {
                  if(instructionEnd.length() == potentialIndex + 1)
                  {
                     // End of delimiter reached
                     textState = processInstruction(textState, instructionStartIndex, potentialEnd, sb, model);
                     textStartIndex = i + 1;
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

            case STATE_VARIABLE_POTENTIAL_DELIMITER_END:
               potentialIndex = i - potentialEnd;

               if(nextChar == variableEnd.charAt(potentialIndex))
               {
                  if(variableEnd.length() == potentialIndex + 1)
                  {
                     // End of delimiter reached
                     textState = processVariable(textState, variableStartIndex, potentialEnd, sb, model);
                     textStartIndex = i + 1;
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
                  parseState = STATE_VARIABLE;

                  // Go back to the potential end
                  i = potentialEnd;
               }
               break;
         }
      }

      // Flush any trailing text
      if(parseState == STATE_TEXT)
      {
         processText(textState, textStartIndex, i, sb, model);
      }

      getLogger().log(Level.FINE, "Template result", sb);
      return sb.toString();
   }

   /**
    * Processes a text token.
    * @param state The current instruction state. (see STATE_* constants).
    * @param tokenStart The start index of the token to process.
    * @param tokenEnd The end index of the token to process.
    * @param buffer The string buffer containing the template result.
    * @param model The template model to use.
    */
   protected void processText(int state, int tokenStart, int tokenEnd, StringBuilder buffer, ReadableModel model)
   {
      if(state == TEXT_APPEND)
      {
      	getLogger().log(Level.FINER, "Appending text", buffer);
         append(tokenStart, tokenEnd, buffer);
      }
      else
      {
      	getLogger().log(Level.FINER, "Ignoring text", buffer);
      }
   }

   /**
    * Processes an instruction token.
    * @param textState The current text state. (see TEXT_* constants).
    * @param tokenStart The start index of the token to process.
    * @param tokenEnd The end index of the token to process.
    * @param buffer The string buffer containing the template result.
    * @param model The template model to use.
    * @return The state after processing.
    */
   protected int processInstruction(int textState, int tokenStart, int tokenEnd, StringBuilder buffer, ReadableModel model)
   {
      String instruction = template.subSequence(tokenStart, tokenEnd).toString();
      getLogger().log(Level.FINER, "processInstruction: " + instruction, buffer);

      if(instruction.startsWith("if "))
      {
         String condition = template.subSequence(tokenStart + 3, tokenEnd).toString();

         if(evaluateCondition(condition, model))
         {
            textState = TEXT_APPEND;
         }
         else
         {
            textState = TEXT_SKIP;
         }
      }
      else if(instruction.startsWith("else if "))
      {
         if(textState == TEXT_SKIP)
         {
            String condition = template.subSequence(tokenStart + 4, tokenEnd).toString();

            if(evaluateCondition(condition, model))
            {
               textState = TEXT_APPEND;
            }
         }
      }
      else if(instruction.equals("else"))
      {
         if(textState == TEXT_SKIP)
         {
            textState = TEXT_APPEND;
         }
         else if(textState == TEXT_APPEND)
         {
            textState = TEXT_SKIP;
         }
      }
      else if(instruction.equals("end"))
      {
         textState = TEXT_APPEND;
      }
      else
      {
      	getLogger().log(Level.WARNING, "Unsupported instruction ignored: ", instruction);
      }

      return textState;
   }

   /**
    * Processes a variable token.
    * @param textState The current text state. (see TEXT_* constants).
    * @param tokenStart The start index of the token to process.
    * @param tokenEnd The end index of the token to process.
    * @param buffer The string buffer containing the template result.
    * @param model The template model to use.
    * @return The state after processing.
    */
   protected int processVariable(int textState, int tokenStart, int tokenEnd, StringBuilder buffer, ReadableModel model)
   {
      String variable = template.subSequence(tokenStart, tokenEnd).toString();
      getLogger().log(Level.FINER, "processVariable: " + variable, buffer);
      
      int conditionIndex = variable.indexOf('?');
      boolean append = true;
      
      if(conditionIndex != -1)
      {
      	String condition = variable.substring(conditionIndex + 1);
      	variable = variable.substring(0, conditionIndex);
      	
      	if(condition.equals("exists"))
      	{
      		append = model.contains(variable);
      	}
      }
      
      if(append && (textState == TEXT_APPEND))
      {
      	buffer.append(model.get(variable));
      }
      
      return textState;
   }
   
   /**
    * Evalutes an instruction's condition.
    * @param condition The condition to evaluate.
    * @param model The template model to use.
    * @return The evaluation result.
    */
   protected boolean evaluateCondition(String condition, ReadableModel model)
   {
      getLogger().log(Level.FINER, "evaluateCondition: " + condition, model);
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

	/**
	 * Returns the logger.
	 * @return the logger.
	 */
	public Logger getLogger()
	{
		if(this.logger == null) this.logger = Logger.getLogger(StringTemplate.class.getCanonicalName());
		return this.logger;
	}

	/**
	 * Sets the logger.
	 * @param logger The logger.
	 */
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}

}
