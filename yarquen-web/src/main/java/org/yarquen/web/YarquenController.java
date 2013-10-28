package org.yarquen.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 30/10/2012
 * 
 */
@Controller
public class YarquenController {
	@RequestMapping("/about")
	public String about() {
		return "about";
	}

	@RequestMapping("/")
	public String home() {
		return "home";
	}
}
