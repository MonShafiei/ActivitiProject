package org.activiti.web.controller;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.model.entitiy.Candidate;
import org.activiti.model.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/job",consumes = {"text/plain", "application/*"})
public class JobController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private CandidateService candidateService;


    @RequestMapping(method = RequestMethod.POST)//,produces = MediaType.APPLICATION_JSON_VALUE)
    public String saveNewCandidatePostMethod(@RequestBody @Valid Candidate candidate, Errors errors) {

        if (errors.hasErrors()) {
            List<ObjectError> error = errors.getAllErrors();
            String errorStr = "";
            for (ObjectError item : error) {
                errorStr += item.getObjectName() + " : " + item.getDefaultMessage() + "\n";
            }
            return errorStr;
        }
        candidateService.insert(candidate);

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("candidate", candidate);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

        return "OK";
    }

    @RequestMapping(method = RequestMethod.GET)//,produces = MediaType.APPLICATION_JSON_VALUE)
    public String saveNewCandidateGetMethod(@Valid Candidate candidate, Errors errors) {

        if (errors.hasErrors()) {
            List<ObjectError> error = errors.getAllErrors();
            String errorStr = "";
            for (ObjectError item : error) {
                errorStr += item.getObjectName() + " : " + item.getDefaultMessage() + "\n";
            }
            return errorStr;
        }
        candidateService.insert(candidate);

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("candidate", candidate);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

        return "OK";
    }
}
