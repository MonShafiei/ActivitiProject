package org.activiti.model.repository;

import org.activiti.model.entitiy.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate,Integer> {

    public List<Candidate> findAllByNameContaining(String input);
}
