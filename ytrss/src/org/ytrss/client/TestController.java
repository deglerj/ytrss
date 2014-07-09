package org.ytrss.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {

	@RequestMapping("/")
	public String helloWorld(Model model) {
		model.addAttribute("test", System.currentTimeMillis() + " muh");
		return "welcome";
	}

}
