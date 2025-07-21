package vn.hoidanit.jobhunter.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.controller.AuthController;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.SkillRepository;

@Service
public class SkillService {

    private final AuthController authController;
    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository, AuthController authController) {
        this.skillRepository = skillRepository;
        this.authController = authController;
    }

    public boolean isNameExists(String name) {
        return this.skillRepository.existsByName(name);
    }

    public Skill createSkill(Skill skill) {
        return this.skillRepository.save(skill);
    }

    public Skill updateSkill(Skill skill) {
        return this.skillRepository.save(skill);
    }

    public void deleteSkill(Long id) {
        // delete skill inside each job
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        Skill currentSkill = skillOptional.get();
        currentSkill.getJobs().forEach(job -> job.getSkills().remove(currentSkill));

        // delete skill
        this.skillRepository.delete(currentSkill);
    }

    public Skill fetchSkillById(Long id) {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        if (skillOptional.isPresent()) {
            return skillOptional.get();
        }
        return null;
    }

    public ResultPaginationDTO fetchAllSkills(Specification<Skill> spec, Pageable pageable) {
        Page<Skill> pageSkills = this.skillRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageSkills.getTotalPages());
        mt.setTotal(pageSkills.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pageSkills.getContent());

        return rs;
    }
}
