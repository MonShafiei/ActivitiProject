package org.activiti;

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
public class HireProcessTest {

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
    public void testHappyPath() {

        // Create test applicant
        Applicant applicant = new Applicant("John Doe", "john@activiti.org", "12344");
        applicantRepository.save(applicant);

        // Start process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applicant", applicant);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("hireProcessWithJpa", variables);

        // First, the 'phone interview' should be active
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskCandidateGroup("dev-managers")
                .singleResult();
        Assert.assertEquals("Telephone interview", task.getName());

        // Completing the phone interview with success should trigger two new tasks
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("telephoneInterviewOutcome", true);
        taskService.complete(task.getId(), taskVariables);

        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        Assert.assertEquals(2, tasks.size());
        Assert.assertEquals("Financial negotiation", tasks.get(0).getName());
        Assert.assertEquals("Tech interview", tasks.get(1).getName());

        // Completing both should wrap up the subprocess, send out the 'welcome mail' and end the process instance
        taskVariables = new HashMap<String, Object>();
        taskVariables.put("techOk", true);
        taskService.complete(tasks.get(0).getId(), taskVariables);

        taskVariables = new HashMap<String, Object>();
        taskVariables.put("financialOk", true);
        taskService.complete(tasks.get(1).getId(), taskVariables);

        // Verify email
        //Assert.assertEquals(1, wiser.getMessages().size());

        // Verify process completed
        //Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());

    }

    @Test
    public void testMyProcess() {

        // Create test applicant
        Candidate candidate = new Candidate("Mohsen","mon1382@yahoo.com","09126151102");
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





        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .orderByTaskName().asc()
                .list();
        Assert.assertEquals(2, tasks.size());
        Assert.assertEquals("Financial negotiation", tasks.get(0).getName());
        Assert.assertEquals("Tech interview", tasks.get(1).getName());

        // Completing both should wrap up the subprocess, send out the 'welcome mail' and end the process instance
        taskVariables = new HashMap<String, Object>();
        taskVariables.put("techOk", true);
        taskService.complete(tasks.get(0).getId(), taskVariables);

        taskVariables = new HashMap<String, Object>();
        taskVariables.put("financialOk", true);
        taskService.complete(tasks.get(1).getId(), taskVariables);

        // Verify email
        //Assert.assertEquals(1, wiser.getMessages().size());

        // Verify process completed
        //Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());

    }

}
