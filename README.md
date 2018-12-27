# Quickstart
## Preface
This document will describe how to get started with activiti and spring-boot.

## Introduction

This doucument will describe about hiring by activiti.
This implementation has been standard by BPMN2.0


## Getting Started

The code for this example can be [found in my Github repository](https://github.com/mon1382/ActivitiProject).

The process we'll implement here is a hiring process for a developer. 

As said in the introduction, all shapes here have a very specific interpretation thanks to the BPMN 2.0 standard.

* When the process starts by human resource, the resume of the job candidate has stored in system.
* The process then waits for accept by human resource. This is done by a user (see the little icon of a person in the corner).
* If resume wasn't Ok by human resource, the process will be finished. Otherwise, set interview time by user has to happen.
* After that take interview by exam will be happened by user and after that information pass to technichal team.
* Informatin cheack by technical team and if wasn't OK, process go to reject method and say to cadidate by human resource user and if it's OK, it go for define set date of interview by technical team and after that take interview by technical team.
* If the result of interview was good information send to human resource for call to candidate and if it wasn't Ok,process go to reject method and say to cadidate by human resource user and it needed to simple project set define project for candidate.
* if simple project was Ok, information of caldidate send to method and method call to human resource by system and send Email by system to candidate and get full information of candidate and then finish.

Let's create a new Maven project, and add the dependencies needed to get Spring Boot, Activiti and a database. We'll use an in memory database to keep things simple.

```maven
<dependency>
    <groupId>org.activiti</groupId>
    <artifactId>spring-boot-starter-basic</artifactId>
    <version>${activiti.version}</version>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>   
</dependency>
```

So only two dependencies is what is needed to create a very first Spring Boot + Activiti application:

```java
@SpringBootApplication
public class MyApp {

    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    } 
}
```

You could already run this application, it won't do anything functionally but behind the scenes it already

* creates an in-memory H2 database
* creates an Activiti process engine using that database
* exposes all Activiti services as Spring Beans
* configures tidbits here and there such as the Activiti async job executor, mail server, etc.



Let's spice things up a bit, and add following dependency to our pom.xml:

```maven
<dependency>
    <groupId>org.activiti</groupId>
    <artifactId>spring-boot-starter-rest-api</artifactId>
    <version>${activiti.version}}</version>
</dependency>
```

Having this on the classpath does a nifty thing: it takes the Activiti REST API (which is written in Spring MVC) and exposes this fully in your application. The REST API of Activiti [is fully documented in the Activiti User Guide](http://activiti.org/userguide/index.html#_rest_api).

The REST API is secured by basic auth, and won't have any users by default. Let's add an admin user to the system as shown below (add this to the MyApp class). Don't do this in a production system of course, there you'll want to hook in the authentication to LDAP or something else.

```java
@Bean
InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {

    return new InitializingBean() {
        public void afterPropertiesSet() throws Exception {

            Group group = identityService.newGroup("user");
            group.setName("users");
            group.setType("security-role");
            identityService.saveGroup(group);

            User admin = identityService.newUser("admin");
            admin.setPassword("admin");
            identityService.saveUser(admin);

        }
    };
}
```

Start the application. 
I just want to stand still for a moment how cool this is. Just by adding one dependency, you�re getting the whole Activiti REST API embedded in your application!

Let�s make it even cooler, and add following dependency

```maven
<dependency>
    <groupId>org.activiti</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
    <version>${activiti.version}</version>
</dependency>
```

This adds a Spring Boot actuator endpoint for Activiti. If we restart the application, and hit http://localhost:8080/activiti/, we get some basic stats about our processes. With some imagination that in a live system you�ve got many more process definitions deployed and executing, you can see how this is useful.



To finish our coding, let's create a dedicated REST endpoint for our hire process.
```
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
```

It also create by PostMap.


You probably guessed it by now, JPA support is enabled by adding a dependency:

```maven
<dependency>
    <groupId>org.activiti</groupId>
    <artifactId>spring-boot-starter-jpa</artifactId>
    <version>${activiti.version}</version>
</dependency>
```

and add the entity to the MyApp class:

```java
@Entity
@Table(name = "Candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "name must not be Blank")
    @Size(min = 3,max = 50,message = "Please enter name between 2 and 50 character")
    private String name;

    @Email(message = "Please enter ")
    private String email;

    @NotBlank(message = "phoneNumber must not be Blank")
    private String phoneNumber;

    //private Object resume;
    private String hasExperience;

    private Boolean firstCondition;
    private Boolean technicalResumeOK;
    private Boolean technicalInterviewOK;
    private Boolean needSimpleProject;
    private Boolean simpleProjectOK;

    private Boolean hiringOk;


    public Candidate() {
    }

    public Candidate(@NotBlank(message = "name must not be Blank") @Size(min = 3, max = 50, message = "Please enter name between 2 and 50 character") String name
            , @Email(message = "Please enter ") String email
            , @NotBlank(message = "phoneNumber must not be Blank") String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    // Getters and setters
```

We'll also need a Repository for this Entity (put this in a separate file or also in MyApp). No need for any methods, the Repository magic from Spring will generate the methods we need for us.

```java
@Repository
public interface CandidateRepository extends JpaRepository<Candidate,Integer> {

    public List<Candidate> findAllByNameContaining(String input);
}
```

And now we can create the dedicated REST endpoint:

```java
@RestController
@RequestMapping(value = "/job",consumes = {"text/plain", "application/*"})
// consumes = {"text/plain", "application/*"}
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
```

Note we're now using a process called by `process`, we now have to use `${candidate.name}` and `${candidate.email}` and ...


## Testing

One of the strengths of using Activiti for creating business processes is that everything is simply Java. As a consequence, processes can be tested as regular Java code with unit tests.

```java
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class JobControllerTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;
    
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
        Candidate candidate = new Candidate("Mohsen", "mon1382@yahoo.com", "0363561168");
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
        taskService.complete(task.getId(), taskVariables);

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

        // Verify email
        Assert.assertEquals(1, wiser.getMessages().size());


        task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Assert.assertEquals("Get Complete Informatin", task.getName());
        taskService.complete(task.getId());       

    }


}
```

### Article author

[Mohsen Shafiei](mon.shafiei@gmail.com)