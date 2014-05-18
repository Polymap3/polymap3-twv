/*
 * polymap.org Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.twv.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.Entity;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.JsonState;
import org.polymap.twv.model.TwvRepository;

/**
 * Provides the content of the entities of the {@link Anta2Repository}
 * encoded as JSON via HTTP/REST interface. As this depends on Qi4j API
 * it is part of the anta2 package instead of Rhei.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RestServlet
        extends HttpServlet {

    private static Log log = LogFactory.getLog( RestServlet.class );
    

    public RestServlet() {
        log.info( "RestServlet." );
    }

    
    public boolean isValid() {
        return true;
    }


    public void init( ServletConfig config )
            throws ServletException {
        super.init( config );
        log.info( "    contextPath: " + config.getServletContext().getContextPath() );
    }


    protected void doGet( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
        log.debug( "Request: " + request.getPathInfo() );
        
        // prevent caching
        response.setHeader( "Cache-Control", "no-cache" ); // HTTP 1.1
        response.setHeader( "Pragma", "no-cache" ); // HTTP 1.0
        response.setDateHeader( "Expires", 0 ); // prevents caching at the proxy
        // MS crap!?
        response.setHeader( "Pragma", "public" );
        response.setHeader( "Cache-Control", "must-revalidate, post-check=0, pre-check=0" );
        response.setHeader( "Cache-Control", "public" );

        try {
            String[] pathInfo = StringUtils.split( request.getPathInfo(), "/" );
            // forms
            if (pathInfo[0].equalsIgnoreCase( "forms" )) {
                doGetForm( request, response );
            }
//            // backup
//            else if (pathInfo[0].equalsIgnoreCase( "backup" )) {
//                new Backup().doGet( this, request, response );
//            }
            // entity
            else {
                doGetEntity( request, response );
            }
        }
        catch (Exception e) {
            log.debug( "", e );
            throw new ServletException( e );
        }
    }


    private void doGetEntity( HttpServletRequest request, HttpServletResponse response )
    throws Exception {
        String[] pathInfo = StringUtils.split( request.getPathInfo(), "/" );
        String typeName = pathInfo[0];
        String id = pathInfo[1];
        log.debug( "Request: type=" + typeName + ", id=" + id );

        // TODO bei ANTA2 wurde hier ne globalInstance gezogen, aber mein Q4jPlugin kann das nicht
        TwvRepository repo = TwvRepository.instance();        
        
        Class<Entity> type = (Class<Entity>)Thread.currentThread().getContextClassLoader().loadClass( typeName );
        Entity entity = repo.findEntity( type, id );
        
        if (entity instanceof JsonState) {
            JSONObject json = ((JsonState)entity).getJsonState( true );
            
            log.debug( "Response: " + json.toString( 4 ) );
            response.setContentType( "text/html" );
            response.getWriter().println( json.toString( 4 ) );
        }
//        repo.done();
    }


    private void doGetForm( HttpServletRequest request, HttpServletResponse response )
    throws IOException {
        InputStream in = TwvPlugin.getDefault().getResourceAsStream( request.getPathInfo() );
        try {
            IOUtils.copy( in, response.getOutputStream() );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }
    
}
