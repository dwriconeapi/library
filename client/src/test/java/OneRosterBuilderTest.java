import org.ricone.library.authentication.API;
import org.ricone.library.authentication.Authenticator2;
import org.ricone.library.authentication.Endpoint;
import org.ricone.library.client.oneroster.request.*;
import org.ricone.library.client.oneroster.response.OffsetResponse;
import org.ricone.library.client.oneroster.response.Response;
import org.ricone.library.client.oneroster.response.Util;
import org.ricone.library.client.oneroster.response.model.*;
import org.ricone.library.exception.InvalidPathException;

import java.util.Optional;

/**
 * @project: client
 * @author: Dan on 01/16/2020.
 */

public class OneRosterBuilderTest {
    public static void main(String[] args) throws InvalidPathException {

        Authenticator2 authenticator = Authenticator2.builder()
            .url("https://auth.test.ricone.org/oauth/login").api(API.OneRoster)
            .credentials("DataValidationTool", "65ec5dbdf2c18d70abb4996d90")
            .provider("localhost")
        .authenticate();

        Optional<Endpoint> endpoint = authenticator.getEndpoint("localhost");
        if(endpoint.isPresent()) {
            OneRoster oneRoster = new OneRoster(endpoint.get());

            //offsetTest(oneRoster);
            //allFeaturesTest(oneRoster);
            buildVerifyTest(oneRoster);

            //orgTest(oneRoster);
            //academicSessionsTest(oneRoster);
            //coursesTest(oneRoster);
            //classesTest(oneRoster);
            //enrollmentsTest(oneRoster);
            //usersTest(oneRoster);
            //demographicsTest(oneRoster);
        }
    }

    private static void orgTest(OneRoster oneRoster) throws InvalidPathException {
        Response<Org> response = oneRoster.getOrg(Request.builder()
            .request()
                .path(ServicePath.GET_Org_By_SourcedId)
                .ids().id("03ACF04F-DC12-411A-9A42-E8323516D699").end()
            .end()
        .build());

        Util.debugResponse(response);
    }

    private static void academicSessionsTest(OneRoster oneRoster) throws InvalidPathException {
        Response<AcademicSessions> response = oneRoster.getAcademicSessions(Request.builder()
            .request().path(ServicePath.GET_AcademicSessions).end()
            .with().paging().limit(2).end().end()
        .build());
        Util.debugResponse(response);
    }

    private static void coursesTest(OneRoster oneRoster) throws InvalidPathException {
        Response<Courses> response = oneRoster.getCourses(Request.builder()
            .request().path(ServicePath.GET_Courses).end()
            .with().paging().limit(2).end().end()
        .build());
        Util.debugResponse(response);
    }

    private static void classesTest(OneRoster oneRoster) throws InvalidPathException {
        Response<Classes> response = oneRoster.getClasses(Request.builder()
            .request().path(ServicePath.GET_Classes).end()
            .with().paging().limit(2).end().end()
        .build());
        Util.debugResponse(response);
    }

    private static void enrollmentsTest(OneRoster oneRoster) throws InvalidPathException {
        Response<Enrollments> response = oneRoster.getEnrollments(Request.builder()
            .request().path(ServicePath.GET_Enrollments).end()
            .with().paging().limit(2).end().end()
        .build());
        Util.debugResponse(response);
    }

    private static void usersTest(OneRoster oneRoster) throws InvalidPathException {
        Response<Users> response = oneRoster.getUsers(Request.builder()
            .request().path(ServicePath.GET_Users).end()
            .with().paging().limit(2).end().end()
        .build());
        Util.debugResponse(response);
    }

    private static void demographicsTest(OneRoster oneRoster) throws InvalidPathException {
        Response<Demographics> response = oneRoster.getDemographics(Request.builder()
            .request().path(ServicePath.GET_demographics).end()
            .with().paging().limit(2).end().end()
        .build());
        Util.debugResponse(response);
    }

    private static void offsetTest(OneRoster oneRoster) throws InvalidPathException {
        int limit = 2;
        OffsetResponse offset = oneRoster.getOffset(Request.builder()
            .request().path(ServicePath.GET_Orgs).end()
            .with().paging().limit(limit).end().end()
        .build());

        while(offset.hasNext()) {
            Response<Orgs> response = oneRoster.getOrgs(Request.builder()
                .request()
                    .path(ServicePath.GET_Orgs)
                .end()
                .with()
                    .paging()
                        .limit(limit)
                        .offset(offset.next())
                    .end()
                .end()
            .build());

            Util.debugResponse(response);
        }
    }

    private static void allFeaturesTest(OneRoster oneRoster) throws InvalidPathException {
        int limit = 1;
        Filter filter1 = new Filter(Field.Orgs.type, Predicate.Equals, "school");
        Filter filter2 = new Filter(Field.Orgs.name, Predicate.Contains, "HS");

        OffsetResponse offset = oneRoster.getOffset(Request.builder()
            .request().path(ServicePath.GET_Orgs).end()
            .with()
                .paging().limit(limit).end()
                .filtering().filter(filter1).and().filter(filter2).end()
            .end()
        .build());

        /* The offset request above requires both the paging and filtering as they are used below.
           The sorting and field selection features used below are/can be ignored, as they don't
           alter the number of records returned.
         */

        while(offset.hasNext()) {
            Response<Orgs> response = oneRoster.getOrgs(Request.builder()
                .request()
                    .path(ServicePath.GET_Orgs)
                .end()
                .with()
                    .paging()
                        .limit(limit)
                        .offset(offset.next()).end()
                    .filtering()
                        .filter(filter1)
                        .and()
                        .filter(filter2).end()
                    .fieldSelection()
                        .field(Field.sourcedId)
                        .field(Field.Orgs.name)
                        .field(Field.Orgs.type).end()
                    .sorting()
                        .field(Field.sourcedId)
                        .orderBy(SortOrder.ASC).end()
                .end()
            .build());

            Util.debugResponseJsonNoXml(response);
        }
    }

    private static void buildVerifyTest(OneRoster oneRoster) throws InvalidPathException {
        Response<Orgs> response = oneRoster.getOrgs(Request.builder()
            .request()
                .path(ServicePath.GET_Orgs)
                //.ids().id("1").end()
            .end()
            /*.with()
                .paging().offset(20).end()
                .sorting().field(Field.sourcedId).end()
                .filtering()
                    .filter(Field.sourcedId, Predicate.Equals, "03ACF04F-DC12-411A-9A42-E8323516D699").and()
                    .filter(Field.sourcedId, Predicate.Equals, "03ACF04F-DC12-411A-9A42-E8323516D699").end()
                .fieldSelection()
                    .field(Field.sourcedId)
                    .field(Field.Orgs.name)
                    //.field(Field.Users.email)
                    .end()
            .end()*/
        .build());
        Util.debugResponseJsonNoXml(response);
    }
}
