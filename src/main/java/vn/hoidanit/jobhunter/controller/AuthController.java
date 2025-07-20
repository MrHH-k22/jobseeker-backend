package vn.hoidanit.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.jobhunter.config.SecurityConfiguration;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.LoginDTO;
import vn.hoidanit.jobhunter.domain.dto.ResLoginDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

        private final PasswordEncoder passwordEncoder;

        private final JwtEncoder jwtEncoder;

        private final CompanyRepository companyRepository;

        private final SecurityConfiguration securityConfiguration;

        private final CompanyController companyController;
        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final SecurityUtil securityUtil;
        private final UserService userService;

        @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenValidityInSeconds;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
                        UserService userService, CompanyController companyController,
                        SecurityConfiguration securityConfiguration,
                        CompanyRepository companyRepository, JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
                this.companyController = companyController;
                this.securityConfiguration = securityConfiguration;
                this.companyRepository = companyRepository;
                this.jwtEncoder = jwtEncoder;
                this.passwordEncoder = passwordEncoder;
        }

        /**
         * Đăng nhập
         * 
         * @Valid use to validate the input data inside model class
         */
        @PostMapping("/auth/login")
        public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
                // Nạp input gồm username/password vào Security
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());

                // xác thực người dùng => cần viết hàm loadUserByUsername
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);

                // lưu thông tin vào SecurityContext
                // SecurityContextHolder là một class static, nó sẽ lưu thông tin của người dùng
                // và context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                ResLoginDTO resLoginDTO = new ResLoginDTO();
                User currentUserDB = this.userService.handleGetUserByUsername(loginDTO.getUsername());
                if (currentUserDB != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());
                        resLoginDTO.setUser(userLogin);
                }
                // create an access token
                String access_token = this.securityUtil.createAccessToken(authentication.getName(),
                                resLoginDTO.getUser());

                resLoginDTO.setAccessToken(access_token);

                // create refresh token
                String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), resLoginDTO);

                // update user
                this.userService.updateUserToken(refresh_token, loginDTO.getUsername());

                // set cookies
                ResponseCookie resCookie = ResponseCookie
                                .from("refresh_token", refresh_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenValidityInSeconds)
                                .build();
                return ResponseEntity
                                .ok()
                                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                                .body(resLoginDTO);
        }

        @GetMapping("/auth/account")
        @ApiMessage("fetch account")
        public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                User currentUserDB = this.userService.handleGetUserByUsername(email);
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
                ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
                if (currentUserDB != null) {
                        userLogin.setId(currentUserDB.getId());
                        userLogin.setEmail(currentUserDB.getEmail());
                        userLogin.setName(currentUserDB.getName());
                        userGetAccount.setUser(userLogin);
                }
                return ResponseEntity.ok().body(userGetAccount);
        }

        @GetMapping("/auth/refresh")
        @ApiMessage("Get user by refresh token")
        public ResponseEntity<ResLoginDTO> getRefreshToken(@CookieValue(name = "refresh_token") String refresh_token)
                        throws IdInvalidException {
                // -- check valid --
                Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
                String email = decodedToken.getSubject();

                // -- check user by token + email --
                User currentUser = this.userService.getUserByRefreshTokenAndEmail(refresh_token, email);
                if (currentUser == null) {
                        throw new IdInvalidException("Refresh token is invalid or expired");
                }

                // -- issue new token/set refresh token as cookie --
                ResLoginDTO resLoginDTO = new ResLoginDTO();
                User currentUserDB = this.userService.handleGetUserByUsername(email);
                if (currentUserDB != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());
                        resLoginDTO.setUser(userLogin);
                }
                // create an access token
                String access_token = this.securityUtil.createAccessToken(email, resLoginDTO.getUser());

                resLoginDTO.setAccessToken(access_token);

                // create refresh token
                String new_refresh_token = this.securityUtil.createRefreshToken(email, resLoginDTO);

                // update user
                this.userService.updateUserToken(new_refresh_token, email);

                // set cookies
                ResponseCookie resCookie = ResponseCookie
                                .from("refresh_token", new_refresh_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenValidityInSeconds)
                                .build();
                return ResponseEntity
                                .ok()
                                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                                .body(resLoginDTO);

        }

        @PostMapping("/auth/logout")
        @ApiMessage("Logout user")
        public ResponseEntity<Void> logout()
                        throws IdInvalidException {
                // get user from email
                String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";

                if (email.equals("")) {
                        throw new IdInvalidException("Access token is invalid");
                }
                // update user to remove refresh token
                this.userService.updateUserToken(null, email);

                // set cookies
                ResponseCookie deleteCookie = ResponseCookie
                                .from("refresh_token", null)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .build();
                return ResponseEntity
                                .ok()
                                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                                .body(null);
        }
}
