package vn.hoidanit.jobhunter.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.SkillService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;

    }

    @PostMapping("/skills")
    @ApiMessage("create a skill")
    public ResponseEntity<Skill> create(@Valid @RequestBody Skill skill) throws IdInvalidException {
        if (skill.getName() != null && this.skillService.isNameExists(skill.getName())) {
            throw new IdInvalidException("Skill name " + skill.getName() + " already exists");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.skillService.createSkill(skill));
    }

    @PutMapping("/skills")
    @ApiMessage("update a skill")
    public ResponseEntity<Skill> update(@Valid @RequestBody Skill skill) throws IdInvalidException {
        // check id
        Skill currentSkill = this.skillService.fetchSkillById(skill.getId());
        if (currentSkill == null) {
            throw new IdInvalidException("Skill id " + skill.getId() + " is invalid");
        }
        // check name
        if (skill.getName() != null && this.skillService.isNameExists(skill.getName())) {
            throw new IdInvalidException("Skill name " + skill.getName() + " already exists");
        }

        currentSkill.setName(skill.getName());
        return ResponseEntity.ok(this.skillService.updateSkill(currentSkill));
    }

    @DeleteMapping("/skills/{id}")
    @ApiMessage("delete a skill")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") Long id) throws IdInvalidException {
        Skill currentSkill = this.skillService.fetchSkillById(id);
        if (currentSkill == null) {
            throw new IdInvalidException("Skill id " + id + " is invalid");
        }

        this.skillService.deleteSkill(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/skills")
    @ApiMessage("fetch all skills")
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<Skill> spec, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.skillService.fetchAllSkills(spec, pageable));
    }
}
