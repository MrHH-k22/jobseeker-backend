package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.Meta;
import vn.hoidanit.jobhunter.domain.dto.ResCreatedUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResUpdatedUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User handleCreateUser(User user) {

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        return this.userRepository.save(user);
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        Meta mt = new Meta();

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
                        item.getUpdatedAt()))
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
        Optional<User> existingUser = this.userRepository.findById(user.getId());
        if (existingUser.isPresent()) {
            User updatedUser = existingUser.get();
            updatedUser.setName(user.getName());
            updatedUser.setEmail(user.getEmail());
            updatedUser.setPassword(user.getPassword());
            updatedUser.setGender(user.getGender());
            updatedUser.setAddress(user.getAddress());
            updatedUser.setAge(user.getAge());
            return this.userRepository.save(updatedUser);
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
        resUserDTO.setId(newUser.getId());
        resUserDTO.setName(newUser.getName());
        resUserDTO.setEmail(newUser.getEmail());
        resUserDTO.setGender(newUser.getGender());
        resUserDTO.setAddress(newUser.getAddress());
        resUserDTO.setAge(newUser.getAge());
        resUserDTO.setCreatedAt(newUser.getCreatedAt());
        return resUserDTO;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO resUserDTO = new ResUserDTO();
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
