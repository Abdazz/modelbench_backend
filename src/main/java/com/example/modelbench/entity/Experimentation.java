package com.example.modelbench.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Execution d'un modele sur un dataset, avec les metriques obtenues.
 */
@Entity
@Table(name = "experimentation")
@Getter
@Setter
@NoArgsConstructor
public class Experimentation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_experimentation_dataset"))
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modele_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_experimentation_modele"))
    private ModeleML modele;

    @Column(nullable = false)
    private Double accuracy;

    @Column(name = "f1_score", nullable = false)
    private Double f1Score;

    /** Durée d'entrainement exprimée en secondes. */
    @Column(name = "duree_entrainement", nullable = false)
    private Long dureeEntrainement;

    @Column(name = "date_execution", nullable = false)
    private LocalDateTime dateExecution;

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Experimentation experimentation)) {
            return false;
        }
        return id != null && Objects.equals(id, experimentation.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
