package com.example.modelbench.mapper;

import com.example.modelbench.dto.ModeleMLRequest;
import com.example.modelbench.dto.ModeleMLResponse;
import com.example.modelbench.entity.ModeleML;
import org.springframework.stereotype.Component;

/**
 * Conversion entre l'entite ModeleML et ses DTO.
 */
@Component
public class ModeleMLMapper {

    public ModeleML versEntite(ModeleMLRequest requete) {
        ModeleML modele = new ModeleML();
        mettreAJour(modele, requete);
        return modele;
    }

    public ModeleMLResponse versReponse(ModeleML modele) {
        return new ModeleMLResponse(
                modele.getId(),
                modele.getNom(),
                modele.getType(),
                modele.getAlgorithme(),
                modele.getVersion(),
                modele.getDateCreation());
    }

    public void mettreAJour(ModeleML cible, ModeleMLRequest requete) {
        cible.setNom(requete.nom());
        cible.setType(requete.type());
        cible.setAlgorithme(requete.algorithme());
        cible.setVersion(requete.version());
    }
}
