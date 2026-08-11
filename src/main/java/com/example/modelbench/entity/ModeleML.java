package com.example.modelbench.entity;

import com.example.modelbench.entity.enums.TypeModele;
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
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Modele de Machine Learning entraine par le laboratoire, identifie par son nom et sa version.
 */
@Entity
@Table(name = "modele_ml", uniqueConstraints = @UniqueConstraint(
        name = "uk_modele_nom_version", columnNames = {"nom", "version"}))
@Getter
@Setter
@NoArgsConstructor
public class ModeleML {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeModele type;

    @Column(nullable = false, length = 120)
    private String algorithme;

    @Column(nullable = false, length = 20)
    private String version;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDate dateCreation;

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof ModeleML modele)) {
            return false;
        }
        return id != null && Objects.equals(id, modele.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
