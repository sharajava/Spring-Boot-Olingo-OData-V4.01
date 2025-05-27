package com.opendev.odata.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.opendev.odata.data.Storage;
import com.opendev.odata.service.DemoEdmProvider;
import com.opendev.odata.service.DemoEntityCollectionProcessor;
import com.opendev.odata.service.DemoEntityProcessor;
import com.opendev.odata.service.DemoPrimitiveProcessor;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Subash
 * @since 2/28/2021
 */
@RestController
@RequestMapping(ODataController.URI)
public class ODataController {
	
	protected static final String URI = "/OData/V1.0";

	@Autowired
	CsdlEdmProvider edmProvider;
	
	@RequestMapping(value = "**")
	public void process(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(true);
		Storage storage = (Storage) session.getAttribute(Storage.class.getName());
		if (storage == null) {
			storage = new Storage();
			session.setAttribute(Storage.class.getName(), storage);
		}

		// create odata handler and configure it with EdmProvider and Processor
		OData odata = OData.newInstance();
		ServiceMetadata edm = odata.createServiceMetadata(new DemoEdmProvider(), new ArrayList<EdmxReference>());
		ODataHttpHandler handler = odata.createHandler(edm);
		handler.register(new DemoEntityCollectionProcessor(storage));
		handler.register(new DemoEntityProcessor(storage));
		handler.register(new DemoPrimitiveProcessor(storage));
		handler.process(new HttpServletRequestWrapper(request) {
			// Spring MVC matches the whole path as the servlet path
			// Olingo wants just the prefix, ie upto /OData/V1.0, so that it
			// can parse the rest of it as an OData path. So we need to override
			// getServletPath()
			@Override
			public String getServletPath() {
				return ODataController.URI;
			}
		}, response);
	}
}
