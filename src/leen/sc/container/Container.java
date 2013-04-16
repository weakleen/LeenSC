package leen.sc.container;

import java.io.IOException;

import javax.servlet.ServletException;

import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;

public interface Container {
	public void leen(LeenRequest request, LeenResponse response)  throws ServletException, IOException ;
}
