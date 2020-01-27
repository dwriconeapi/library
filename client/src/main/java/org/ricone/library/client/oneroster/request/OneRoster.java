package org.ricone.library.client.oneroster.request;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.ricone.library.authentication.Endpoint;
import org.ricone.library.client.oneroster.response.OffsetResponse;
import org.ricone.library.client.oneroster.response.OffsetUtil;
import org.ricone.library.client.oneroster.response.Response;
import org.ricone.library.client.oneroster.response.model.*;
import org.ricone.library.client.xpress.response.XResponseErrorHandler;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.Class;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @project: client
 * @author: Dan on 01/16/2020.
 */

public class OneRoster {
	private Endpoint endpoint;
	private RestTemplate restTemplate;

	public OneRoster(Endpoint endpoint) {
		ObjectMapper mapper = new ObjectMapper();

		//Modules
		mapper.registerModule(new ParameterNamesModule());
		mapper.registerModule(new Jdk8Module());
		mapper.registerModule(new JavaTimeModule()); // new module, NOT JSR310Module

		//Features
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(mapper);

		this.endpoint = endpoint;
		this.restTemplate = new RestTemplate();
		this.restTemplate.setErrorHandler(new XResponseErrorHandler());
		this.restTemplate.setMessageConverters(Collections.singletonList(converter));
	}

	/* PUBLIC REQUESTS */
	public OrgResponse getOrg(Request request) {
		return request(request, OrgResponse.class);
	}

	public OrgsResponse getOrgs(Request request) {
		return request(request, OrgsResponse.class);
	}

	public AcademicSessionResponse getAcademicSession(Request request) {
		return request(request, AcademicSessionResponse.class);
	}

	public AcademicSessionsResponse getAcademicSessions(Request request) {
		return request(request, AcademicSessionsResponse.class);
	}

	public CourseResponse getCourse(Request request) {
		return request(request, CourseResponse.class);
	}

	public CoursesResponse getCourses(Request request) {
		return request(request, CoursesResponse.class);
	}

	public ClassResponse getClass(Request request) {
		return request(request, ClassResponse.class);
	}

	public ClassesResponse getClasses(Request request) {
		return request(request, ClassesResponse.class);
	}

	public EnrollmentResponse getEnrollment(Request request) {
		return request(request, EnrollmentResponse.class);
	}

	public EnrollmentsResponse getEnrollments(Request request) {
		return request(request, EnrollmentsResponse.class);
	}

	public UserResponse getUser(Request request) {
		return request(request, UserResponse.class);
	}

	public UsersResponse getUsers(Request request) {
		return request(request, UsersResponse.class);
	}

	public DemographicResponse getDemographic(Request request) {
		return request(request, DemographicResponse.class);
	}

	public DemographicsResponse getDemographics(Request request) {
		return request(request, DemographicsResponse.class);
	}

	public OffsetResponse getOffset(Request request) {
		return requestOffsetResponse(request);
	}

	/* PRIVATE REQUEST */
	private <T extends Response> T request(Request request, Class<T> clazz) {
		//Before doing anything, make sure that the request path can return the response object.
		verifyRequestAndResponse(request, clazz);

		T data = null;
		String requestPath = getRequestPath(request);
		HttpEntity<?> httpEntity = getHttpEntity(request);
		try {
			ResponseEntity<T> response = restTemplate.exchange(requestPath, HttpMethod.GET, httpEntity, clazz);
			if(response.hasBody()) {
				data = response.getBody();
				assert data != null;
				data.setClazz(clazz);
				data.setRequestPath(requestPath);
				data.setRequestHeaders(httpEntity.getHeaders());
				data.setResponseStatus(response.getStatusCode());
				data.setResponseHeaders(response.getHeaders());
			}
			else {
				data = setDataOnNoContent(clazz, requestPath, httpEntity, response);
			}
		}
		catch (HttpClientErrorException e) {
			data = setDataOnError(clazz, requestPath, httpEntity, e);
		}
		catch(HttpStatusCodeException c) {
			c.printStackTrace();
		}
		return data;
	}

	private OffsetResponse requestOffsetResponse(Request request) {
		OffsetResponse data = null;
		String requestPath = getRequestPath(request);
		HttpEntity httpEntity = getHttpEntity(request);
		try {
			ResponseEntity<String> response = restTemplate.exchange(requestPath, HttpMethod.HEAD, httpEntity, String.class);

			String totalRecords = response.getHeaders().getFirst("X-Total-Count");
			int limit = request.with().paging().getPaging().getLimit();
			int offset = request.with().paging().getPaging().getOffset();

			if(StringUtils.hasText(totalRecords)) {
				data = new OffsetResponse(OffsetUtil.getOffsetArray(limit, offset, totalRecords));
				data.setRequestPath(requestPath);
				data.setRequestHeaders(httpEntity.getHeaders());
				data.setResponseStatus(response.getStatusCode());
				data.setResponseHeaders(response.getHeaders());
			}
			else {
				data = setDataOnNoContent(OffsetResponse.class, requestPath, httpEntity, response);
			}
		}
		catch (HttpClientErrorException e) {
			e.printStackTrace();
			data = setDataOnError(OffsetResponse.class, requestPath, httpEntity, e);
		}
		return data;
	}

	/* GET URL */
	private String getRequestPath(Request request) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpoint.getHref());
		if(!request.request().path().getServicePathType().equals(ServicePathType.OBJECT)) {

			if(!request.request().path().getServicePathType().equals(ServicePathType.PREDICATES)) {
				//If the request path has one instance of {id}, replace it with the first value from the ids list.
				builder.path(StringUtils.replace(request.request().path().getValue(), "{id}", request.request().ids().getIds().get(0)));
			}
			else {
				//If Predicates, replace {id} two times, once with each id value from the ids list.
				builder.path(Stream.of(
						request.request().ids().getIds().get(0),
						request.request().ids().getIds().get(1)
					).reduce(request.request().path().getValue(), (id1, id2) -> id1.replaceFirst("\\{([id}]+)\\}", id2))
				);
			}
		}
		else {
			//If the request path has no instances of {id}, don't do anything special.
			builder.path(request.request().path().getValue());
		}

		if(request.hasPaging()) {
			builder.queryParam("limit", request.with().paging().getPaging().getLimit());
			builder.queryParam("offset", request.with().paging().getPaging().getOffset());
		}

		if(request.hasSorting()) {
			builder.queryParam("sort", request.with().sorting().getSorting().getField().getValue());
			builder.queryParam("orderBy", request.with().sorting().getSorting().getOrderBy().getValue());
		}

		if(request.hasFieldSelection()) {
			System.out.println("hasFieldSelection: " + request.hasFieldSelection());
			List<String> fields = request.with().fieldSelection().getFields().stream().map(IField::getValue).collect(Collectors.toList());

			System.out.println("fieldSelection: " + String.join(",", fields));

			builder.queryParam("fields", String.join(",", fields));
		}

		if(request.hasFiltering()) {
			Filter filter1 = request.with().filtering().getFiltering().getFilters().get(0);
			if(request.with().filtering().getFiltering().getLogicalOperation() != LogicalOperation.NONE) {
				Filter filter2 = request.with().filtering().getFiltering().getFilters().get(1);
				builder.queryParam("filter",
				filter1.getField().getValue() + filter1.getPredicate().getValue() + "'" + filter1.getValue() + "'" +
						request.with().filtering().getFiltering().getLogicalOperation().getValue() +
						filter2.getField().getValue() + filter2.getPredicate().getValue() + "'" + filter2.getValue() + "'"
				);
			}
			else {
				builder.queryParam("filter", filter1.getField().getValue() + filter1.getPredicate().getValue() + "'" + filter1.getValue() + "'");
			}
		}
		return builder.build().toUriString();
	}

	/* GET HEADERS */
	private HttpEntity<?> getHttpEntity(Request request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Bearer " + this.endpoint.getDecodedToken().getToken());
		return new HttpEntity<>(headers);
	}

	/* ON ERROR */
	private <T extends Response> T setDataOnError(Class<T> clazz, String requestPath, HttpEntity httpEntity, HttpClientErrorException exception) {
		T xResponse = null;
		try {
			xResponse = clazz.getDeclaredConstructor().newInstance();
			xResponse.setData(null);
			xResponse.setRequestPath(requestPath);
			xResponse.setRequestHeaders(httpEntity.getHeaders());
			xResponse.setResponseHeaders(exception.getResponseHeaders());
			xResponse.setResponseStatusText(exception.getStatusText());
			xResponse.setResponseStatus(exception.getStatusCode());
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return xResponse;
	}

	private <T extends Response> T setDataOnNoContent(Class<T> clazz, String requestPath, HttpEntity httpEntity, ResponseEntity response) {
		T xResponse = null;
		try {
			xResponse = clazz.getDeclaredConstructor().newInstance();
			xResponse.setData(null);
			xResponse.setRequestPath(requestPath);
			xResponse.setRequestHeaders(httpEntity.getHeaders());
			xResponse.setResponseHeaders(response.getHeaders());
			xResponse.setResponseStatusText(response.getStatusCode().getReasonPhrase());
			xResponse.setResponseStatus(response.getStatusCode());
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return xResponse;
	}

	private <T extends Response> void verifyRequestAndResponse(Request request, Class<T> clazz) {
		if(!request.request().path().getResponseClass().equals(clazz)) {
			throw new IllegalArgumentException("ServicePath: " + request.request().path() + " requires that the response return: " + request.request().path().getResponseClass().getCanonicalName()
					+ ", however the response will return: " + clazz.getCanonicalName());
		}
	}
}