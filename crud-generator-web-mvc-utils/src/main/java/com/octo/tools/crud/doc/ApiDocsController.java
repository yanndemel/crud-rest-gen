package com.octo.tools.crud.doc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ApiDocsController {
	
	public static final String _DOC = "doc";
	public static final String DOC = "/" + _DOC;
	
	@RequestMapping(DOC)
    public ModelAndView index() {
        return new ModelAndView("redirect:docs/api/api-guide.html");
    }
}