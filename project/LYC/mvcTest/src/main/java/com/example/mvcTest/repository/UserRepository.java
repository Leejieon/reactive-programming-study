package com.example.mvcTest.repository;

import com.example.mvcTest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
