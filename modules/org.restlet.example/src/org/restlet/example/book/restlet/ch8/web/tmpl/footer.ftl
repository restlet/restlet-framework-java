<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" >
   <head>
      <meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />
      <title>${title}</title>
      <link rel="stylesheet" type="text/css" href='${rootRef}/stylesheets/main.css' title="Main CSS stylesheet" />
      ${feedHeader}
   </head>
   <body>
      <div id="container">
         <div id="header">
            <div id="logoRmep"><a href="${rootRef}" title="Go to the login page"><img src="${rootRef}/images/logo.png" alt="Logo Application" /></a></div>
            
            <div id="quickSummary">
               <p><span><acronym title="REpresentational State Transfert">REST</acronym>ful mail application based on the Restlet framework.</span></p>
            </div>
         </div>
<#if !currentUser??>
<#-- Anonymous access -->
         <div id="hello">Hello Anonymous <a href="${rootRef}" title="Sign in" style="padding-right:2px;padding-left: 2px;background-color:#f57900;color:white;">Sign in</a></div>
<#else>
         <div id="hello">Hello ${currentUser.firstName} ${currentUser.lastName}</div>
</#if>
         <div id="content">
            ${content}
         </div>

         <div id="menu">
            ${menu}
         </div>
<!--
         <div id="footer">
            <small>
               Copyright &copy; 2005-2008 <a href="http://www.noelios.com">Noelios Consulting</a>
            </small>
         </div>	
-->
      </div>
   </body>
</html>