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
            <div id="logoRmep"><a href="${rootRef}"><img src="${rootRef}/images/rmep.png" alt="Logo RMEP" /></a>Rest Mail Exchange Protocol</div>
            
            <div id="quickSummary">
               <p><span>A simple illustration of <acronym title="REpresentational State Transfert">REST</acronym>-based design relying on the Restlet framework.</span></p>
            </div>
         </div>

         <div id="hello">Hello ${currentUser.firstName} ${currentUser.lastName}</div>

         <div id="content">
            ${content}
         </div>

         <div id="menu">
            ${menu}
         </div>
      </div>
   </body>
</html>