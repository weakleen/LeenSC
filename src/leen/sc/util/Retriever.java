package leen.sc.util;

import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;

public class Retriever {

	public static LeenRequest retrieveRequest(HttpServletRequest originRequest) {
		HttpServletRequest request = originRequest;
		while (request instanceof ServletRequestWrapper) {
			request = (HttpServletRequest) ((HttpServletRequestWrapper) request)
					.getRequest();
		}
		LeenRequest lRequest = (LeenRequest) request;
		if (originRequest instanceof HttpServletRequestWrapper)
			lRequest.setWrapper((HttpServletRequestWrapper) originRequest);
		return lRequest;
	}

	public static LeenResponse retrieveResponse(
			HttpServletResponse originResponse) {
		HttpServletResponse response = originResponse;
		while (response instanceof HttpServletResponseWrapper) {
			response = (HttpServletResponse) ((HttpServletResponseWrapper) response)
					.getResponse();
		}
		LeenResponse lResponse = (LeenResponse) response;
		if (originResponse instanceof HttpServletResponseWrapper)
			lResponse.setWrapper((HttpServletResponseWrapper) originResponse);
		return lResponse;
	}
}
