package org.activiti.model.entitiy;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Size;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getHasExperience() {
        return hasExperience;
    }

    public void setHasExperience(String hasExperience) {
        this.hasExperience = hasExperience;
    }

    public Boolean getFirstCondition() {
        return firstCondition;
    }

    public void setFirstCondition(Boolean firstCondition) {
        this.firstCondition = firstCondition;
    }

    public Boolean getTechnicalResumeOK() {
        return technicalResumeOK;
    }

    public void setTechnicalResumeOK(Boolean technicalResumeOK) {
        this.technicalResumeOK = technicalResumeOK;
    }

    public Boolean getTechnicalInterviewOK() {
        return technicalInterviewOK;
    }

    public void setTechnicalInterviewOK(Boolean technicalInterviewOK) {
        this.technicalInterviewOK = technicalInterviewOK;
    }

    public Boolean getNeedSimpleProject() {
        return needSimpleProject;
    }

    public void setNeedSimpleProject(Boolean needSimpleProject) {
        this.needSimpleProject = needSimpleProject;
    }

    public Boolean getSimpleProjectOK() {
        return simpleProjectOK;
    }

    public void setSimpleProjectOK(Boolean simpleProjectOK) {
        this.simpleProjectOK = simpleProjectOK;
    }

    public Boolean getHiringOk() {
        return hiringOk;
    }

    public void setHiringOk(Boolean hiringOk) {
        this.hiringOk = hiringOk;
    }
}
