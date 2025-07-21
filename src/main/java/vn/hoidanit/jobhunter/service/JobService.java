package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.job.ResCreateJobDTO;
import vn.hoidanit.jobhunter.domain.response.job.ResUpdateJobDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;
import vn.hoidanit.jobhunter.repository.JobRepository;
import vn.hoidanit.jobhunter.repository.SkillRepository;

@Service
public class JobService {

    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobService(SkillRepository skillRepository, JobRepository jobRepository,
            CompanyRepository companyRepository) {
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    public Optional<Job> fetchJobById(Long id) {
        return this.jobRepository.findById(id);
    }

    public ResultPaginationDTO fetchAll(Specification<Job> spec, Pageable pageable) {
        Page<Job> pageJobs = this.jobRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageJobs.getTotalPages());
        mt.setTotal(pageJobs.getTotalElements());

        rs.setMeta(mt);

        rs.setResult(pageJobs.getContent());

        return rs;
    }

    public ResCreateJobDTO createJob(Job job) {
        // check skills
        if (job.getSkills() != null) {
            List<Long> reqSkills = job.getSkills().stream()
                    .map(x -> x.getId())
                    .collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            job.setSkills(dbSkills);
        }
        // check company
        if (job.getCompany() != null) {
            Optional<Company> cOptional = this.companyRepository.findById(job.getCompany().getId());
            if (cOptional.isPresent()) {
                job.setCompany(cOptional.get());
            }
        }
        // create job
        Job currentJob = this.jobRepository.save(job);

        // convert response
        ResCreateJobDTO dto = this.convertToResCreateJobDTO(currentJob);

        return dto;
    }

    public ResUpdateJobDTO updateJob(Job job, Job jobInDB) {
        // check skills
        if (job.getSkills() != null) {
            List<Long> reqSkills = job.getSkills().stream()
                    .map(x -> x.getId())
                    .collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            jobInDB.setSkills(dbSkills);
        }

        // update job
        if (job.getCompany() != null) {
            Optional<Company> cOptional = this.companyRepository.findById(job.getCompany().getId());
            if (cOptional.isPresent()) {
                jobInDB.setCompany(cOptional.get());
            }
        }
        // update correct info
        jobInDB.setName(job.getName());
        jobInDB.setSalary(job.getSalary());
        jobInDB.setQuantity(job.getQuantity());
        jobInDB.setLocation(job.getLocation());
        jobInDB.setLevel(job.getLevel());
        jobInDB.setStartDate(job.getStartDate());
        jobInDB.setEndDate(job.getEndDate());
        jobInDB.setActive(job.isActive());

        // update job
        Job currentJob = this.jobRepository.save(jobInDB);

        // convert response
        ResUpdateJobDTO dto = this.convertToResUpdateJobDTO(currentJob);

        return dto;
    }

    public void deleteJob(long id) {
        this.jobRepository.deleteById(id);
    }

    public ResCreateJobDTO convertToResCreateJobDTO(Job job) {
        ResCreateJobDTO dto = new ResCreateJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setSalary(job.getSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLocation(job.getLocation());
        dto.setLevel(job.getLevel());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(job.isActive());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setCreatedBy(job.getCreatedBy());
        if (job.getSkills() != null) {
            List<String> skills = job.getSkills().stream()
                    .map(item -> item.getName())
                    .collect(Collectors.toList());
            dto.setSkills(skills);
        }

        return dto;
    }

    public ResUpdateJobDTO convertToResUpdateJobDTO(Job job) {
        ResUpdateJobDTO dto = new ResUpdateJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setSalary(job.getSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLocation(job.getLocation());
        dto.setLevel(job.getLevel());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(job.isActive());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setCreatedBy(job.getCreatedBy());
        if (job.getSkills() != null) {
            List<String> skills = job.getSkills().stream()
                    .map(item -> item.getName())
                    .collect(Collectors.toList());
            dto.setSkills(skills);
        }

        return dto;
    }
}