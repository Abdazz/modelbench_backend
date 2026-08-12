package com.example.modelbench.entity;

import com.example.modelbench.entity.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Compte permettant de se connecter a l'application.
 */
@Entity
@Table(name = "utilisateur",
        uniqueConstraints = @UniqueConstraint(name = "uk_utilisateur_login", columnNames = "login"))
@Getter
@Setter
@NoArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String login;

    /** Empreinte BCrypt du mot de passe. Jamais exposee par un DTO. */
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(name = "nom_complet", nullable = false, length = 120)
    private String nomComplet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean actif = true;

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Utilisateur utilisateur)) {
            return false;
        }
        return id != null && Objects.equals(id, utilisateur.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
