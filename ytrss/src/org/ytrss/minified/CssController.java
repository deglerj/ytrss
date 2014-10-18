package org.ytrss.minified;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CssController extends BaseMinifyController {

	@Autowired
	private CssProvider	cssProvider;

	@RequestMapping(value = "/css", method = RequestMethod.GET)
	public void getCss(final HttpServletResponse response, final HttpServletRequest request) throws IOException {
		writeResponse(cssProvider.getMinified(), response, request);
	}

	@Override
	protected String getMimeType() {
		return "text/css";
	}

}
