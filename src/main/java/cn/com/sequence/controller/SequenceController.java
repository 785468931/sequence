package cn.com.sequence.controller;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.com.sequence.service.SequenceService;

@RestController
@RequestMapping("sequence")
public class SequenceController {
	@Autowired
	SequenceService  sequenceService;
	
	@RequestMapping("getSequenceId")
	public Long getSequenceId(HttpServletRequest req) {
		return sequenceService.getSequenceId(req.getRemoteAddr());
	}

}
