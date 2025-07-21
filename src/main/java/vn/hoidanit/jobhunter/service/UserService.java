package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.controller.AuthController;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResCreatedUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdatedUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyService companyService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CompanyService companyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyService = companyService;
    }

    public User handleCreateUser(User user) {

        // check company
        if (user.getCompany() != null) {
            Optional<Company> companyOptional = this.companyService.findById(user.getCompany().getId());
            user.setCompany(companyOptional.isPresent() ? companyOptional.get() : null);
        }
        // hash password
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        return this.userRepository.save(user);
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        rs.setMeta(mt);

        List<ResUserDTO> listUser = pageUser.getContent().stream()
                .map(item -> new ResUserDTO(
                        item.getId(),
                        item.getName(),
                        item.getEmail(),
                        item.getGender(),
                        item.getAddress(),
                        item.getAge(),
                        item.getCreatedAt(),
                        item.getUpdatedAt(),
                        new ResUserDTO.CompanyUser(
                                item.getCompany() != null ? item.getCompany().getId() : 0,
                                item.getCompany() != null ? item.getCompany().getName() : "")))
                .collect(Collectors.toList());

        rs.setResult(listUser);

        return rs;
    }

    public User getUserById(long id) {
        Optional<User> user = this.userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        }
        return null;
    }

    public void deleteUserById(long id) {
        this.userRepository.deleteById(id);
    }

    public User hanldeUpdateUser(User user) {
        User currentUser = this.getUserById(user.getId());
        if (currentUser != null) {
            currentUser.setName(user.getName());
            currentUser.setGender(user.getGender());
            currentUser.setAddress(user.getAddress());
            currentUser.setAge(user.getAge());
            // check company
            if (user.getCompany() != null) {
                Optional<Company> companyOptional = this.companyService.findById(user.getCompany().getId());
                currentUser.setCompany(companyOptional.isPresent() ? companyOptional.get() : null);
            }

            return this.userRepository.save(currentUser);
        } else {
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }

    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreatedUserDTO convertToResCreatedUserDTO(User newUser) {
        ResCreatedUserDTO resUserDTO = new ResCreatedUserDTO();
        ResCreatedUserDTO.CompanyUser company = new ResCreatedUserDTO.CompanyUser();
        resUserDTO.setId(newUser.getId());
        resUserDTO.setName(newUser.getName());
        resUserDTO.setEmail(newUser.getEmail());
        resUserDTO.setGender(newUser.getGender());
        resUserDTO.setAddress(newUser.getAddress());
        resUserDTO.setAge(newUser.getAge());
        resUserDTO.setCreatedAt(newUser.getCreatedAt());
        if (newUser.getCompany() != null) {
            company.setId(newUser.getCompany().getId());
            company.setName(newUser.getCompany().getName());
            resUserDTO.setCompany(company);
        }
        return resUserDTO;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO resUserDTO = new ResUserDTO();
        ResUserDTO.CompanyUser company = new ResUserDTO.CompanyUser();

        if (user.getCompany() != null) {
            company.setId(user.getCompany().getId());
            company.setName(user.getCompany().getName());
            resUserDTO.setCompany(company);
        }

        resUserDTO.setId(user.getId());
        resUserDTO.setName(user.getName());
        resUserDTO.setEmail(user.getEmail());
        resUserDTO.setGender(user.getGender());
        resUserDTO.setAddress(user.getAddress());
        resUserDTO.setAge(user.getAge());
        resUserDTO.setCreatedAt(user.getCreatedAt());
        resUserDTO.setUpdatedAt(user.getUpdatedAt());
        return resUserDTO;
    }

    public ResUpdatedUserDTO convertToResUpdatedUserDTO(User updatedUser) {
        ResUpdatedUserDTO resUpdatedUserDTO = new ResUpdatedUserDTO();
        ResUpdatedUserDTO.CompanyUser com = new ResUpdatedUserDTO.CompanyUser();

        if (updatedUser.getCompany() != null) {
            com.setId(updatedUser.getCompany().getId());
            com.setName(updatedUser.getCompany().getName());
            resUpdatedUserDTO.setCompany(com);

        }

        resUpdatedUserDTO.setId(updatedUser.getId());
        resUpdatedUserDTO.setName(updatedUser.getName());
        resUpdatedUserDTO.setEmail(updatedUser.getEmail());
        resUpdatedUserDTO.setGender(updatedUser.getGender());
        resUpdatedUserDTO.setAddress(updatedUser.getAddress());
        resUpdatedUserDTO.setAge(updatedUser.getAge());
        resUpdatedUserDTO.setUpdatedAt(updatedUser.getUpdatedAt());
        return resUpdatedUserDTO;
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }
}
