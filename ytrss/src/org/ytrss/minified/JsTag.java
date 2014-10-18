package org.ytrss.minified;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class JsTag extends SimpleTagSupport {

	@Override
	public void doTag() throws JspException, IOException {
		final PageContext pageContext = (PageContext) getJspContext();
		final JspWriter out = pageContext.getOut();

		final WebApplicationContext webApp = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
		final JsProvider jsProvider = webApp.getBean(JsProvider.class);

		out.write("<script src=\"js?v=");
		out.write(jsProvider.getId());
		out.write("\" type=\"text/javascript\"></script>");
	}

}
