package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.dto.Meta;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;

@Service
public class CompanyService {
    public final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company handleCreateCompany(Company company) {
        return this.companyRepository.save(company);
    }

    public ResultPaginationDTO getallCompanies(Specification<Company> sepc, Pageable pageable) {
        Page<Company> pageCompany = this.companyRepository.findAll(sepc, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        Meta mt = new Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageCompany.getTotalPages());
        mt.setTotal(pageCompany.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageCompany.getContent());
        return rs;
    }

    public Company handleUpdateCompany(Company company) {
        Optional<Company> existingCompany = this.companyRepository.findById(company.getId());
        if (existingCompany.isPresent()) {
            Company updatedCompany = existingCompany.get();
            updatedCompany.setName(company.getName());
            updatedCompany.setAddress(company.getAddress());
            updatedCompany.setDescription(company.getDescription());
            updatedCompany.setLogo(company.getLogo());
            return this.companyRepository.save(updatedCompany);

        }
        return company;
    }

    public void deleteCompanyById(long id) {
        this.companyRepository.deleteById(id);
    }
}
