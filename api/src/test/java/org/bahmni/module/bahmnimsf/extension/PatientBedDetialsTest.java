package org.bahmni.module.bahmnimsf.extension;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;

import org.openmrs.module.bedmanagement.BedDetails;

import org.openmrs.module.bedmanagement.entity.Bed;
import org.openmrs.module.bedmanagement.service.BedManagementService;
import org.openmrs.module.bedmanagement.service.impl.BedManagementServiceImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class PatientBedDetialsTest {

    private PatientBedDetails patientBedDetails;

    @Mock
    private BedManagementServiceImpl bedManagementService;

    @Before
    public void setup() throws NoSuchMethodException {
        PowerMockito.mockStatic(Context.class);
        when(Context.getService(BedManagementService.class)).thenReturn(bedManagementService);

    }

    @Test
    public void shouldGetPatientBedDetails() {
        Patient patient = new Patient();
        patientBedDetails = new PatientBedDetails();
        patient.setUuid("patientUuid");
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        Bed bed = new Bed();
        bed.setBedNumber("1234");
        BedDetails bedDetails = new BedDetails();
        bedDetails.setBedNumber("1234");
        bedDetails.setBed(bed);
        Location location = new Location();
        location.setName("FirstAddress");
        bedDetails.setPhysicalLocation(location);
        when(bedManagementService.getBedAssignmentDetailsByPatient(appointment.getPatient())).thenReturn(bedDetails);
        Map<String, String> patientDetails = patientBedDetails.run(appointment);
        Assert.assertEquals("FirstAddress", patientDetails.get("LOCATION_KEY"));
        Assert.assertEquals("1234", patientDetails.get("BED_NUMBER_KEY"));
    }

    @Test
    public void bedInfoShouldNotHaveAnyDetailsIfBedDetailsIsNull() throws Exception {
        Patient patient = new Patient();
        patient.setUuid("patientUuid");
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        patientBedDetails = new PatientBedDetails();
        when(bedManagementService.getBedAssignmentDetailsByPatient(appointment.getPatient())).thenReturn(null);
        Map<String, String> patientDetails = patientBedDetails.run(appointment);
        Map<String, String> bedInfo = new HashMap<>();
        Assert.assertEquals(bedInfo, patientDetails);
    }
}
