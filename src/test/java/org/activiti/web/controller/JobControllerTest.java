package org.activiti.web.controller;

import org.activiti.ApplicantRepository;
import org.activiti.MyApp;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.model.entitiy.Candidate;
import org.activiti.model.repository.CandidateRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.subethamail.wiser.Wiser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {MyApp.class})
@WebAppConfiguration
@IntegrationTest
public class JobControllerTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    private Wiser wiser;

    @Before
    public void setup() {
        wiser = new Wiser();
        wiser.setPort(5025);
        wiser.start();
    }

    @After
    public void cleanup() {
        wiser.stop();
    }



    @Test
    public void saveNewCandidateGetMethod() throws Exception {

        // Create test hiring
        Candidate candidate = new Candidate("Mohsen","mon1382@yahoo.com","0363561168");
        candidateRepository.save(candidate);

        // Start process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("candidate", candidate);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

        // First, the 'First Check Resume' should be active
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Assert.assertEquals("First Check Resume", task.getName());
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("isFirstCondition", true);
        candidate.setFirstCondition(true);
        taskService.complete(task.getId(), taskVariables);

        task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Assert.assertEquals("Set Interview Time", task.getName());
        taskService.complete(task.getId());

        task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Assert.assertEquals("Take Interview with Exam", task.getName());
        taskService.complete(task.getId());

        task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Assert.assertEquals("Check By Technical Team", task.getName());
        taskVariables = new HashMap<String, Object>();
        taskVariables.put("isTechnicalResumeOK", true);
        candidate.setTechnicalInterviewOK(true);
        taskService.complete(task.getId(),taskVariables);

        task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Assert.assertEquals("Set Date of Interview", task.getName());
        taskService.complete(task.getId());

        task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Assert.assertEquals("Take an Interview", task.getName());
        taskVariables = new HashMap<String, Object>();
        taskVariables.put("isTechnicalInterviewOK", false);
        taskVariables.put("isNeedSimpleProject", true);
        candidate.setTechnicalInterviewOK(false);
        candidate.setNeedSimpleProject(true);
        taskService.complete(task.getId(), taskVariables);

        task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Assert.assertEquals("Define Project", task.getName());
        taskVariables = new HashMap<String, Object>();
        taskVariables.put("isSimpleProjectOK", true);
        candidate.setSimpleProjectOK(true);
        taskService.complete(task.getId(), taskVariables);

        task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Assert.assertEquals("Run Accept Method", task.getName());


        // Verify email
        Assert.assertEquals(1, wiser.getMessages().size());

        // Verify process completed
        //Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());

    }




}