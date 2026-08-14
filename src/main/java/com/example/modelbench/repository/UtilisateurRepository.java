package com.example.modelbench.repository;

import com.example.modelbench.entity.Utilisateur;
import com.example.modelbench.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UtilisateurRepository
        extends JpaRepository<Utilisateur, Long>, JpaSpecificationExecutor<Utilisateur> {

    Optional<Utilisateur> findByLogin(String login);

    boolean existsByLogin(String login);

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByLoginIgnoreCaseAndIdNot(String login, Long id);

    long countByRoleAndActifTrue(Role role);
}
