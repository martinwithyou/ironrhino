/*
 * $Id: FreeMarkerPageFilter.java 651946 2008-04-27 13:41:38Z apetrelli $
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ironrhino.core.struts;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts2.sitemesh.FreeMarkerPageFilter;
import org.ironrhino.core.session.HttpWrappedSession;

import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.xwork2.ActionContext;

public class HookedFreeMarkerPageFilter extends FreeMarkerPageFilter {

	protected void applyDecorator(Page page, Decorator decorator,
			HttpServletRequest req, HttpServletResponse res,
			ServletContext servletContext, ActionContext ctx)
			throws ServletException, IOException {
		beforeApplyDecorator(page, decorator, req, res, servletContext, ctx);
		super.applyDecorator(page, decorator, req, res, servletContext, ctx);
	}

	protected void beforeApplyDecorator(Page page, Decorator decorator,
			HttpServletRequest req, HttpServletResponse res,
			ServletContext servletContext, ActionContext ctx) {
		HttpSession session = req.getSession();
		if (session instanceof HttpWrappedSession)
			((HttpWrappedSession) session).save();
	}

}
