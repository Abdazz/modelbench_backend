package com.example.modelbench.service;

import com.example.modelbench.dto.MeilleurModeleResponse;
import com.example.modelbench.dto.SyntheseResponse;

import java.util.List;

/**
 * Indicateurs agreges alimentant le tableau de bord.
 */
public interface StatistiquesService {

    SyntheseResponse synthese();

    /**
     * @return pour chaque dataset ayant au moins une experimentation, celle dont l'accuracy est la
     *         plus elevee, triee par accuracy decroissante
     */
    List<MeilleurModeleResponse> meilleursModeles();
}
