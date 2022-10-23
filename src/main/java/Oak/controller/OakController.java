package Oak.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = OakController.ENTRY_POINT)
public class OakController {
    protected static final String ENTRY_POINT = "/oaks";

}
