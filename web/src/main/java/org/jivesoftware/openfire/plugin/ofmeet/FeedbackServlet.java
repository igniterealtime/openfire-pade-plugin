package org.jivesoftware.openfire.plugin.ofmeet;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.openfire.security.SecurityAuditManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.poi.util.ReplacingInputStream;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// @MultipartConfig <- don't work, done in web.xml
public class FeedbackServlet extends HttpServlet
{
    private static final long serialVersionUID = -8057457730888335346L;
    private static final Logger LOG = LoggerFactory.getLogger( FeedbackServlet.class );
    private final SecurityAuditManager securityAuditManager = SecurityAuditManager.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String descriptionMessageDefault = LocaleUtils.getLocalizedString("ofmeet.feedback.description.default", "pade");
        String placeholderTextDefault = LocaleUtils.getLocalizedString("ofmeet.feedback.placeholder.default", "pade");
        String submitTextDefault = LocaleUtils.getLocalizedString("ofmeet.feedback.submit.default", "pade");
        String successMessageDefault = LocaleUtils.getLocalizedString("ofmeet.feedback.success.default", "pade");
        String errorMessageDefault = LocaleUtils.getLocalizedString("ofmeet.feedback.error.default", "pade");

        String descriptionMessage = JiveGlobals.getProperty( "ofmeet.feedback.description", descriptionMessageDefault );
        String placeholderText    = JiveGlobals.getProperty( "ofmeet.feedback.placeholder", placeholderTextDefault );
        String submitText         = JiveGlobals.getProperty( "ofmeet.feedback.submit", submitTextDefault );
        String successMessage     = JiveGlobals.getProperty( "ofmeet.feedback.success", successMessageDefault );
        String errorMessage       = JiveGlobals.getProperty( "ofmeet.feedback.error", errorMessageDefault );

        // Add response headers that instruct not to cache this data.
        response.setHeader( "Expires",       "Sun, 18 Sep 1966 12:00:00 GMT" );
        response.setHeader( "Cache-Control", "no-store, no-cache, must-revalidate" );
        response.addHeader( "Cache-Control", "post-check=0, pre-check=0" );
        response.setHeader( "Pragma",        "no-cache" );
        response.setHeader( "Content-Type",  "text/html" );
        response.setHeader( "Connection",    "close" );

        final ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream template = classLoader.getResourceAsStream("static/feedback.html"))
        {
            // IOUtils.copy(inputStream, response.getOutputStream());
            IOUtils.copy
            (
                new ReplacingInputStream
                (
                    new ReplacingInputStream
                    (
                        new ReplacingInputStream
                        (
                            new ReplacingInputStream
                            (
                                new ReplacingInputStream
                                (
                                    template
                                    , "${description-message}", descriptionMessage
                                )
                                , "${placeholder-text}", placeholderText
                            )
                        ,   "${submit-text}", submitText
                        )
                        , "${success-message}", successMessage
                        )
                    ,"${error-message}", errorMessage
                )
                , response.getOutputStream()
            );
        }
        catch (final Exception e)
        {
            LOG.error(e.getMessage(), e);
            response.getOutputStream().println(e.getMessage());
            response.setStatus(500);
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try {
            final JSONObject feedback = new JSONObject();
            for (final Part part : request.getParts())
            {
                String partName = part.getName();
                StringWriter writer = new StringWriter();
                IOUtils.copy(part.getInputStream(), writer, Charset.defaultCharset());
                feedback.put(partName, writer.toString());
            }
            LOG.info(feedback.toString());
            final String comment = feedback.optString("comment","");
            securityAuditManager.logEvent
            (
                feedback.getString("callStatsUserName")
                , "pade feedback A/V-rating: " + feedback.optString("audio","-") +"/" + feedback.optString("video","-") + ( ! comment.isEmpty() ? " +" + Integer.toString(comment.length()) + "c" : "" )
                , comment
            );
        }
        catch (final Exception e)
        {
            LOG.error("FeedbackServlet post failed", e);
        }
        response.setStatus(200);
    }
}
