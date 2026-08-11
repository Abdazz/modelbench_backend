package com.example.modelbench.entity;

import com.example.modelbench.entity.enums.FormatDataset;
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
 * Jeu de donnees catalogue par le laboratoire.
 */
@Entity
@Table(name = "dataset", uniqueConstraints = @UniqueConstraint(name = "uk_dataset_nom", columnNames = "nom"))
@Getter
@Setter
@NoArgsConstructor
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String source;

    @Column(name = "nombre_observations", nullable = false)
    private Long nombreObservations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FormatDataset format;

    @CreationTimestamp
    @Column(name = "date_ajout", nullable = false, updatable = false)
    private LocalDate dateAjout;

    /**
     * Egalite fondee sur l'identifiant uniquement. Comparer tous les champs, comme le ferait
     * {@code @Data} de Lombok, declencherait le chargement des collections paresseuses.
     */
    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Dataset dataset)) {
            return false;
        }
        return id != null && Objects.equals(id, dataset.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
