package vn.hoidanit.jobhunter.domain.response;

import java.time.Instant;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import vn.hoidanit.jobhunter.util.constant.GenderEnum;

public class ResUpdatedUserDTO {
    private Long id;

    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    private String address;

    private int age;
    private Instant updatedAt;

    private CompanyUser company;

    public static class CompanyUser {
        private long id;
        private String name;

        public CompanyUser() {
        }

        public CompanyUser(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public CompanyUser getCompany() {
        return company;
    }

    public void setCompany(CompanyUser company) {
        this.company = company;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public GenderEnum getGender() {
        return gender;
    }

    public void setGender(GenderEnum gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
