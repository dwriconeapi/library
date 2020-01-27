package org.ricone.library.client.oneroster.response.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.ricone.library.client.oneroster.response.BaseMultiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"warnings","demographics"})
@JsonRootName("demographics")
public class DemographicsResponse extends BaseMultiResponse<Demographics> implements Serializable {
	private final static long serialVersionUID = 6089973485073338986L;
	@JsonIgnore
	private String requestPath;
	@JsonIgnore
	private HttpHeaders requestHeaders;
	@JsonIgnore
	private HttpStatus responseStatus;
	@JsonIgnore
	private String responseStatusText;
	@JsonIgnore
	private HttpHeaders responseHeaders;

	public DemographicsResponse() {
	}

	public DemographicsResponse(Demographics demographics) {
		super(demographics);
	}

	public DemographicsResponse(Demographics demographics, List<Error> errors) {
		super(demographics, errors);
	}

	@JsonUnwrapped @JsonProperty("demographics")
	@JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "demographic")
	@Override public Demographics getData() {
		return super.getData();
	}

	@JsonUnwrapped @JsonProperty("demographics")
	@JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "demographic")
	@Override public void setData(Demographics demographics) {
		super.setData(demographics);
	}

	@Override
	public String getRequestPath() {
		return requestPath;
	}

	@Override
	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}

	@Override
	public HttpHeaders getRequestHeaders() {
		return requestHeaders;
	}

	@Override
	public void setRequestHeaders(HttpHeaders requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	@Override
	public HttpStatus getResponseStatus() {
		return responseStatus;
	}

	@Override
	public void setResponseStatus(HttpStatus responseStatus) {
		this.responseStatus = responseStatus;
	}

	@Override
	public String getResponseStatusText() {
		return responseStatusText;
	}

	@Override
	public void setResponseStatusText(String responseStatusText) {
		this.responseStatusText = responseStatusText;
	}

	@Override
	public HttpHeaders getResponseHeaders() {
		return responseHeaders;
	}

	@Override
	public void setResponseHeaders(HttpHeaders responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
}