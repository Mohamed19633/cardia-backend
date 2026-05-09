package com.java.backend.repository;

import com.java.backend.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person,Long> {
    Optional<Person> findByEmail(String username);

    @Query("SELECT p FROM Person p WHERE p.role.name <> 'ADMIN' AND p.email <> :adminEmail")
    List<Person> findAllExceptAdmins(@Param("adminEmail") String adminEmail);

}
