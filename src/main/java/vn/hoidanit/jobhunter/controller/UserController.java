package vn.hoidanit.jobhunter.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.jobhunter.config.DateTimeFormatConfiguration;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResCreatedUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdatedUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.CompanyService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final DateTimeFormatConfiguration dateTimeFormatConfiguration;
    private final UserService userService;

    public UserController(UserService userService, DateTimeFormatConfiguration dateTimeFormatConfiguration) {
        this.userService = userService;
        this.dateTimeFormatConfiguration = dateTimeFormatConfiguration;
    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<ResCreatedUserDTO> createnewUser(@Valid @RequestBody User user) throws IdInvalidException {

        // check email
        boolean isEmailExist = this.userService.isEmailExist(user.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException("Email " + user.getEmail() + " already exists in the system");
        }

        User newUser = this.userService.handleCreateUser(user);

        ResCreatedUserDTO resUserDTO = this.userService.convertToResCreatedUserDTO(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(resUserDTO);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Get user by ID")
    public ResponseEntity<ResUserDTO> getuserById(@PathVariable("id") long id) throws IdInvalidException {
        User newUser = this.userService.getUserById(id);
        if (newUser == null) {
            throw new IdInvalidException("User with ID " + id + " not found.");
        }

        ResUserDTO resUserDTO = this.userService.convertToResUserDTO(newUser);
        return ResponseEntity.status(HttpStatus.OK).body(resUserDTO);
    }

    @GetMapping("/users")
    @ApiMessage("Get all users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec,
            // @RequestParam("current") Optional<String> currentOptional,
            // @RequestParam("pageSize") Optional<String> pageSizeOptional
            Pageable pageable) {

        // cách 1:
        /*
         *
         * 
         * String sCurrent = currentOptional.isPresent() ? currentOptional.get() : "";
         * String sPageSize = pageSizeOptional.isPresent() ? pageSizeOptional.get() :
         * 
         * "";
         * 
         * int current = Integer.parseInt(sCurrent) - 1;
         * int pageSize = Integer.parseInt(sPageSize);
         * 
         * Pageable pageable = PageRequest.of(current, pageSize);
         */

        ResultPaginationDTO rs = this.userService.getAllUsers(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(rs);

    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete user by ID")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) throws IdInvalidException {

        User currentUser = this.userService.getUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("User with ID " + id + " not found.");
        }

        // Logic to delete user by id
        this.userService.deleteUserById(id);
        // For now, just return a success message
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PutMapping("/users")
    @ApiMessage("Update user by ID")
    public ResponseEntity<ResUpdatedUserDTO> updateUser(@RequestBody User user)
            throws IdInvalidException {
        User updatedUser = this.userService.hanldeUpdateUser(user);
        if (updatedUser == null) {
            throw new IdInvalidException("User with ID " + user.getId() + " not found.");
        }

        ResUpdatedUserDTO resUpdatedUserDTO = this.userService.convertToResUpdatedUserDTO(updatedUser);

        return ResponseEntity.ok(resUpdatedUserDTO);
    }
}
