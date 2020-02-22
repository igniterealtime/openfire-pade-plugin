package org.jivesoftware.openfire.plugin.rest.service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.jivesoftware.openfire.plugin.rest.exceptions.RESTExceptionMapper;
import org.jivesoftware.util.JiveGlobals;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.ifsoft.meet.MeetService;


public class JerseyWrapper extends ServletContainer
{
    private static final String AUTHFILTER = "org.jivesoftware.openfire.plugin.rest.AuthFilter";
    private static final String CORSFILTER = "org.jivesoftware.openfire.plugin.rest.CORSFilter";
    private static final String CONTAINER_REQUEST_FILTERS = "com.sun.jersey.spi.container.ContainerRequestFilters";
    private static final String CONTAINER_RESPONSE_FILTERS = "com.sun.jersey.spi.container.ContainerResponseFilters";
    private static final String GZIP_FILTER = "com.sun.jersey.api.container.filter.GZIPContentEncodingFilter";
    private static final String RESOURCE_CONFIG_CLASS_KEY = "com.sun.jersey.config.property.resourceConfigClass";
    private static final String RESOURCE_CONFIG_CLASS = "com.sun.jersey.api.core.PackagesResourceConfig";
    private static final String SCAN_PACKAGE_DEFAULT = JerseyWrapper.class.getPackage().getName();
    private static final long serialVersionUID = 1L;
    private static Map<String, Object> config;
    private static PackagesResourceConfig prc;
    private final static Logger JERSEY_LOGGER = Logger.getLogger("com.sun.jersey");

    private static String loadingStatusMessage = null;

    static
    {
        JERSEY_LOGGER.setLevel(Level.SEVERE);
        config = new HashMap<String, Object>();
        config.put(RESOURCE_CONFIG_CLASS_KEY, RESOURCE_CONFIG_CLASS);
        config.put("com.sun.jersey.api.json.POJOMappingFeature", true);

        prc = new PackagesResourceConfig(SCAN_PACKAGE_DEFAULT);
        prc.setPropertiesAndFeatures(config);
        prc.getProperties().put(CONTAINER_RESPONSE_FILTERS, CORSFILTER);
        //prc.getProperties().put(CONTAINER_RESPONSE_FILTERS, GZIP_FILTER);
        loadAuthenticationFilter();

        prc.getClasses().add(RestAPIService.class);

        prc.getClasses().add(MUCRoomService.class);
        prc.getClasses().add(MUCRoomOwnersService.class);
        prc.getClasses().add(MUCRoomAdminsService.class);
        prc.getClasses().add(MUCRoomMembersService.class);
        prc.getClasses().add(MUCRoomOutcastsService.class);

        prc.getClasses().add(UserService.class);
        prc.getClasses().add(UserRosterService.class);
        prc.getClasses().add(UserGroupService.class);
        prc.getClasses().add(UserLockoutService.class);

        prc.getClasses().add(GroupService.class);
        prc.getClasses().add(SessionService.class);
        prc.getClasses().add(MsgArchiveService.class);
        prc.getClasses().add(StatisticsService.class);
        prc.getClasses().add(MessageService.class);
        prc.getClasses().add(SipService.class);
        prc.getClasses().add(BookmarkService.class);
        prc.getClasses().add(ChatService.class);
        prc.getClasses().add(MeetService.class);
        prc.getClasses().add(AskService.class);

        prc.getClasses().add(RESTExceptionMapper.class);
    }

    public static String loadAuthenticationFilter()
    {
        //prc.getProperties().put(CONTAINER_REQUEST_FILTERS, GZIP_FILTER);
        prc.getProperties().put(CONTAINER_REQUEST_FILTERS, AUTHFILTER);
        return loadingStatusMessage;
    }


    public JerseyWrapper()
    {
        super(prc);
    }


    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        loadAuthenticationFilter();
        super.init(servletConfig);
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }


    public static String getLoadingStatusMessage()
    {
        return loadingStatusMessage;
    }

}
