package org.bahmni.module.bahmnimsf;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class CloseVisitTaskTest extends BaseModuleContextSensitiveTest {
    @Test
    public void shouldCloseVisitWhenValueOfTheObservationMatches() throws Exception {
        executeDataSet("visit.xml");
        CloseVisitTask closeVisitTask = new CloseVisitTask();
        closeVisitTask.execute();

        Visit visit = Context.getVisitService().getVisit(4000);
        List<Visit> allVisits = Context.getVisitService().getAllVisits();
        ArrayList<Visit> stoppedVisits = new ArrayList<Visit>();
        for (Visit allVisit : allVisits) {
            if (allVisit.getStopDatetime() != null) {
                stoppedVisits.add(allVisit);
            }
        }
        Assert.assertEquals(1, stoppedVisits.size());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm-dd-yyyy");
        String stoppedDateTime = simpleDateFormat.format(visit.getStopDatetime());
        String today = simpleDateFormat.format(new Date());
        Assert.assertEquals(today, stoppedDateTime);
    }
}