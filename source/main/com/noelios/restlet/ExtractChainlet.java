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

package com.noelios.restlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.AbstractChainlet;
import org.restlet.Call;
import org.restlet.component.Component;
import org.restlet.data.Form;
import org.restlet.data.Statuses;

import com.noelios.restlet.util.CallModel;

/**
 * Chainlet extracting some attributes from a call. Multiple extractions can be defined, based on the query 
 * string of the resource reference, on the input form (posted from a browser), on the context URI matches 
 * or on the call's template model.
 */
public class ExtractChainlet extends AbstractChainlet
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.ExtractChainlet");

   /**
    * List of query parameters to extract.
    */
   protected List<ExtractInfo> queryExtracts;

   /**
    * List of input parameters to extract.
    */
   protected List<ExtractInfo> inputExtracts;

   /**
    * List of context matches to extract.
    */
   protected List<ExtractInfo> contextExtracts;

   /**
    * List of call's model attributes to extract.
    */
   protected List<ExtractInfo> modelExtracts;

   /**
    * Constructor.
    * @param parent The parent component.
    */
   public ExtractChainlet(Component parent)
   {
      super(parent);
      this.queryExtracts = null;
      this.inputExtracts = null;
      this.contextExtracts = null;
      this.modelExtracts = null;
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      try
      {
         // Extract the query parameters
         if(this.queryExtracts != null)
         {
            Form input = call.getResourceRef().getQueryAsForm();

            if(input != null)
            {
               for(ExtractInfo qe : getQueryExtracts())
               {
                  if(qe.multiple)
                  {
                     call.getAttributes().put(qe.attribute, input.getParameters(qe.value));
                  }
                  else
                  {
                     call.getAttributes().put(qe.attribute, input.getFirstParameter(qe.value));
                  }
               }
            }
         }

         // Extract the input parameters
         if(this.inputExtracts != null)
         {
            Form input = call.getInputAsForm();

            if(input != null)
            {
               for(ExtractInfo ie : getInputExtracts())
               {
                  if(ie.multiple)
                  {
                     call.getAttributes().put(ie.attribute, input.getParameters(ie.value));
                  }
                  else
                  {
                     call.getAttributes().put(ie.attribute, input.getFirstParameter(ie.value));
                  }
               }
            }
         }

         // Extract the context matches
         if(this.contextExtracts != null)
         {
            for(ExtractInfo ce : getContextExtracts())
            {
               call.getAttributes().put(ce.attribute, call.getContextMatches().get(ce.index));
            }
         }

         // Extract the model patterns
         if(this.modelExtracts != null)
         {
            CallModel model = new CallModel(call, null);
            for(ExtractInfo me : getModelExtracts())
            {
               call.getAttributes().put(me.attribute, model.get(me.value));
            }
         }

         super.handle(call);
      }
      catch(Exception e)
      {
         logger.log(Level.SEVERE, "Unhandled error intercepted", e);
         call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
      }
   }

   /**
    * Returns the list of query extracts.
    * @return The list of query extracts.
    */
   private List<ExtractInfo> getQueryExtracts()
   {
      if(this.queryExtracts == null) this.queryExtracts = new ArrayList<ExtractInfo>();
      return this.queryExtracts;
   }

   /**
    * Returns the list of query extracts.
    * @return The list of query extracts.
    */
   private List<ExtractInfo> getInputExtracts()
   {
      if(this.inputExtracts == null) this.inputExtracts = new ArrayList<ExtractInfo>();
      return this.inputExtracts;
   }

   /**
    * Returns the list of query extracts.
    * @return The list of query extracts.
    */
   private List<ExtractInfo> getContextExtracts()
   {
      if(this.contextExtracts == null) this.contextExtracts = new ArrayList<ExtractInfo>();
      return this.contextExtracts;
   }

   /**
    * Returns the list of query extracts.
    * @return The list of query extracts.
    */
   private List<ExtractInfo> getModelExtracts()
   {
      if(this.modelExtracts == null) this.modelExtracts = new ArrayList<ExtractInfo>();
      return this.modelExtracts;
   }

   /**
    * Extracts an attribute from the query string of the resource reference. Only the first occurrence
    * of a query string parameter is set.
    * @param attributeName The name of the call attribute to set.
    * @param parameterName The name of the query string parameter to extract.
    * @return The current chainlet.
    */
   public ExtractChainlet fromQuery(String attributeName, String parameterName)
   {
      return fromQuery(attributeName, parameterName, false);
   }

   /**
    * Extracts an attribute from the query string of the resource reference.
    * @param attributeName The name of the call attribute to set.
    * @param parameterName The name of the query string parameter to extract.
    * @param multiple Indicates if the parameters should be set as a List in the attribute value. Useful for repeating parameters.
    * @return The current chainlet.
    */
   public ExtractChainlet fromQuery(String attributeName, String parameterName, boolean multiple)
   {
      getQueryExtracts().add(new ExtractInfo(attributeName, parameterName, multiple));
      return this;
   }

   /**
    * Extracts an attribute from the input form. Only the first occurrence
    * of a query string parameter is set.
    * @param attributeName The name of the call attribute to set.
    * @param parameterName The name of the input form parameter to extract.
    * @return The current chainlet.
    */
   public ExtractChainlet fromInput(String attributeName, String parameterName)
   {
      return fromInput(attributeName, parameterName, false);
   }

   /**
    * Extracts an attribute from the input form.
    * @param attributeName The name of the call attribute to set.
    * @param parameterName The name of the input form parameter to extract.
    * @param multiple Indicates if the parameters should be set as a List in the attribute value. Useful for repeating parameters.
    * @return The current chainlet.
    */
   public ExtractChainlet fromInput(String attributeName, String parameterName, boolean multiple)
   {
      getInputExtracts().add(new ExtractInfo(attributeName, parameterName, multiple));
      return this;
   }

   /**
    * Extracts an attribute from the context matches.
    * @param attributeName The name of the call attribute to set.
    * @param matchIndex The index of the match to extract from the Call.getMatches() list.
    * @return The current chainlet.
    */
   public ExtractChainlet fromContext(String attributeName, int matchIndex)
   {
      getContextExtracts().add(new ExtractInfo(attributeName, matchIndex));
      return this;
   }

   /**
    * Extracts an attribute from the call's model.
    * @param attributeName The name of the call attribute to set.
    * @param pattern The model pattern to resolve.
    * @return The current chainlet.
    * @see com.noelios.restlet.util.CallModel
    */
   public ExtractChainlet fromModel(String attributeName, String pattern)
   {
      getModelExtracts().add(new ExtractInfo(attributeName, pattern));
      return this;
   }

   /**
    * Internal class holding extraction information.
    */
   protected class ExtractInfo
   {
      /**
       * Holds the attribute name.
       */
      protected String attribute;

      /**
       * Holds information to extract the attribute value.
       */
      protected String value;

      /**
       * Holds indicator on how to handle repeating values.
       */
      protected boolean multiple;

      /**
       * Holds information to extract the attribute value.
       */
      protected int index;

      /**
       * Constructor.
       * @param attribute
       * @param value
       * @param multiple
       */
      public ExtractInfo(String attribute, String value, boolean multiple)
      {
         this.attribute = attribute;
         this.value = value;
         this.multiple = multiple;
         this.index = -1;
      }

      /**
       * Constructor.
       * @param attribute
       * @param index
       */
      public ExtractInfo(String attribute, int index)
      {
         this.attribute = attribute;
         this.value = null;
         this.multiple = false;
         this.index = index;
      }

      /**
       * Constructor.
       * @param attribute
       * @param value
       */
      public ExtractInfo(String attribute, String value)
      {
         this.attribute = attribute;
         this.value = value;
         this.multiple = false;
         this.index = -1;
      }
   }

}
