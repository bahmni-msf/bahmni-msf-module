package org.bahmni.module.bahmnimsf.controller;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Privilege;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;

import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({Context.class, IOUtils.class})
@RunWith(PowerMockRunner.class)
public class AmmanExportsControllerTest {

    @Rule
    private ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private FileSystemResource fileSystemResource;

    @Mock
    private User authenticatedUser;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AdministrationService administrationService;

    private AmmanExportsController ammanExportsController;
    private String FILENAME = "ammanExport-20171010.zip";

    @Before
    public void setup() throws Exception {
        ammanExportsController = new AmmanExportsController();
        ammanExportsController.setResourceLoader(resourceLoader);
        PowerMockito.mockStatic(Context.class);
        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.when(IOUtils.copy(any(FileInputStream.class), any(OutputStream.class))).thenReturn(1);
    }

    @Test
    public void shouldThrowAPIAuthenticationExceptionIfUserisNotAuthenticated() throws Exception {
        when(Context.isAuthenticated()).thenReturn(false);

        expectedException.expect(APIAuthenticationException.class);
        expectedException.expectMessage("User is not Logged In");

        ammanExportsController.downloadFile(response, FILENAME);
    }

    @Test
    public void shouldThrowAPIAuthenticationExceptionIfUserisAuthenticatedWithNoPrivilege() throws Exception {
        when(Context.isAuthenticated()).thenReturn(true);
        when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
        ArrayList<Privilege> privileges = new ArrayList<>();
        Privilege privilege = new Privilege("registration");
        privileges.add(privilege);
        when(authenticatedUser.getPrivileges()).thenReturn(privileges);

        expectedException.expect(APIAuthenticationException.class);
        expectedException.expectMessage("User does not have required privileges app:exports" );

        ammanExportsController.downloadFile(response, FILENAME);
    }

    @Test
    public void shouldGetTheResourceAndAssignItToTheResponse() throws Exception {
        when(Context.isAuthenticated()).thenReturn(true);
        when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
        ArrayList<Privilege> privileges = new ArrayList<>();
        Privilege privilege = new Privilege("app:exports");
        privileges.add(privilege);
        when(authenticatedUser.getPrivileges()).thenReturn(privileges);
        String exportsFilePath = "/temp/exports";
        String filePath = "file:" + exportsFilePath + FILENAME;
        String ammanExportsLocation = "amman.exports.location";
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(ammanExportsLocation)).thenReturn(exportsFilePath);
        when(resourceLoader.getResource(filePath)).thenReturn(fileSystemResource);
        TemporaryFolder folder= new TemporaryFolder();
        folder.create();
        File file= folder.newFile(FILENAME);
        when(fileSystemResource.getFile()).thenReturn(file);
        FileInputStream in = PowerMockito.mock(FileInputStream.class);
        PowerMockito.whenNew(FileInputStream.class).withArguments(file).thenReturn(in);

        ammanExportsController.downloadFile(response, FILENAME);

        verify(response).setContentType("application/zip");
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"" + FILENAME + "\"");
        verify(response, times(1)).flushBuffer();
    }
}