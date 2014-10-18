package org.ytrss.minified;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class CssTag extends SimpleTagSupport {

	@Override
	public void doTag() throws JspException, IOException {
		final PageContext pageContext = (PageContext) getJspContext();
		final JspWriter out = pageContext.getOut();

		final WebApplicationContext webApp = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
		final CssProvider cssProvider = webApp.getBean(CssProvider.class);

		out.write("<link href=\"css?v=");
		out.write(cssProvider.getId());
		out.write("\" rel=\"stylesheet\" type=\"text/css\">");
	}

}
