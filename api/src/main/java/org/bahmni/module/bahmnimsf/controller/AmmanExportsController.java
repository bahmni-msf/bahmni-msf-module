package org.bahmni.module.bahmnimsf.controller;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.openmrs.Privilege;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
public class AmmanExportsController extends BaseRestController implements ResourceLoaderAware {

    public static final String APP_EXPORTS_PRIVILEGE = "app:exports";
    private static Logger logger = Logger.getLogger(AmmanExportsController.class);
    private static final String AMMAN_EXPORTS_LOCATION = "amman.exports.location";

    private final String baseUrl = "/rest/" + RestConstants.VERSION_1 + "/amman";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @RequestMapping(value = baseUrl + "/export", method = RequestMethod.GET)
    @ResponseBody
    public void downloadFile(HttpServletResponse response, @RequestParam("filename") String filename) throws IOException {
        if (Context.isAuthenticated()) {
            if (!isTheAuthenticatedUserHasReportsPrivilege()) {
                throw new APIAuthenticationException("User does not have required privileges " + APP_EXPORTS_PRIVILEGE);
            }
        } else {
            throw new APIAuthenticationException("User is not Logged In");
        }
        String filePath = "file:" + Context.getAdministrationService().getGlobalProperty(AMMAN_EXPORTS_LOCATION) + filename;
        Resource resource = resourceLoader.getResource(filePath);
        IOUtils.copy(new FileInputStream(resource.getFile()), response.getOutputStream());
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.flushBuffer();
    }

    private boolean isTheAuthenticatedUserHasReportsPrivilege() {
        for (Privilege privilege : Context.getAuthenticatedUser().getPrivileges()){
            if (privilege.getName().equalsIgnoreCase(APP_EXPORTS_PRIVILEGE)) {
                return true;
            }
        }
        return false;
    }
}