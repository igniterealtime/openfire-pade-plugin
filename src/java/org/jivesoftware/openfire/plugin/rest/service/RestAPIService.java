package org.jivesoftware.openfire.plugin.rest.service;

import java.util.*;
import javax.annotation.PostConstruct;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jivesoftware.openfire.plugin.rest.entity.SystemProperties;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperty;
import org.jivesoftware.openfire.plugin.rest.exceptions.*;

import org.jivesoftware.util.*;

@Path("restapi/v1/system/properties")
public class RestAPIService {

    @PostConstruct
    public void init() {
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SystemProperties getSystemProperties() {
        SystemProperties systemProperties = new SystemProperties();
        List<SystemProperty> propertiesList = new ArrayList<SystemProperty>();

        for(String propertyKey : JiveGlobals.getPropertyNames()) {
            String propertyValue = JiveGlobals.getProperty(propertyKey);
            propertiesList.add(new SystemProperty(propertyKey, propertyValue));
        }
        systemProperties.setProperties(propertiesList);
        return systemProperties;
    }

    @GET
    @Path("/{propertyKey}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SystemProperty getSystemProperty(@PathParam("propertyKey") String propertyKey) throws ServiceException {
        String propertyValue = JiveGlobals.getProperty(propertyKey);
        if(propertyValue != null) {
        return new SystemProperty(propertyKey, propertyValue);
        } else {
            throw new ServiceException("Could not find property", propertyKey, ExceptionType.PROPERTY_NOT_FOUND,
                    Response.Status.NOT_FOUND);
        }
    }

    @POST
    public Response createSystemProperty(SystemProperty systemProperty) throws ServiceException {
        JiveGlobals.setProperty(systemProperty.getKey(), systemProperty.getValue());
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{propertyKey}")
    public Response updateUser(@PathParam("propertyKey") String propertyKey, SystemProperty systemProperty) throws ServiceException {
        if(JiveGlobals.getProperty(propertyKey) != null) {
            if(systemProperty.getKey().equals(propertyKey)) {
                JiveGlobals.setProperty(propertyKey, systemProperty.getValue());
            } else {
                throw new ServiceException("Path property name and entity property name doesn't match", propertyKey, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION,
                        Response.Status.BAD_REQUEST);
            }
        } else {
            throw new ServiceException("Could not find property for update", systemProperty.getKey(), ExceptionType.PROPERTY_NOT_FOUND,
                    Response.Status.NOT_FOUND);
        }
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{propertyKey}")
    public Response deleteUser(@PathParam("propertyKey") String propertyKey) throws ServiceException {
        if(JiveGlobals.getProperty(propertyKey) != null) {
            JiveGlobals.deleteProperty(propertyKey);
        } else {
            throw new ServiceException("Could not find property", propertyKey, ExceptionType.PROPERTY_NOT_FOUND,
                    Response.Status.NOT_FOUND);
        }
        return Response.status(Response.Status.OK).build();
    }
}
