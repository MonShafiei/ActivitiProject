package org.activiti.model.service;

import org.activiti.model.entitiy.Candidate;
import org.activiti.model.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Mohsen on 18/10/27.
 */
@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    public List<Candidate> findAll(){
        return candidateRepository.findAll();
    }

    public void insert(Candidate candidate){
        candidateRepository.save(candidate);
    }
}
