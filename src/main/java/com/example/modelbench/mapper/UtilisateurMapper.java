package com.example.modelbench.mapper;

import com.example.modelbench.dto.UtilisateurAdminResponse;
import com.example.modelbench.dto.UtilisateurCreationRequest;
import com.example.modelbench.dto.UtilisateurModificationRequest;
import com.example.modelbench.entity.Utilisateur;
import org.springframework.stereotype.Component;

/**
 * Conversion entre l'entite Utilisateur et ses DTO. Le hachage du mot de passe reste la
 * responsabilite du service, qui seul connait le PasswordEncoder.
 */
@Component
public class UtilisateurMapper {

    public Utilisateur versEntite(UtilisateurCreationRequest requete, String motDePasseHache) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNomComplet(requete.nomComplet());
        utilisateur.setLogin(requete.login());
        utilisateur.setMotDePasse(motDePasseHache);
        utilisateur.setRole(requete.role());
        utilisateur.setActif(requete.actif());
        return utilisateur;
    }

    public UtilisateurAdminResponse versReponse(Utilisateur utilisateur) {
        return new UtilisateurAdminResponse(
                utilisateur.getId(),
                utilisateur.getLogin(),
                utilisateur.getNomComplet(),
                utilisateur.getRole(),
                utilisateur.isActif());
    }

    /**
     * Reporte les champs modifiables de la requete sur une entite existante. L'identifiant et le
     * mot de passe ne sont volontairement jamais touches ici : le mot de passe est gere a part par
     * le service, uniquement quand la requete en fournit un nouveau.
     */
    public void mettreAJour(Utilisateur cible, UtilisateurModificationRequest requete) {
        cible.setNomComplet(requete.nomComplet());
        cible.setLogin(requete.login());
        cible.setRole(requete.role());
        cible.setActif(requete.actif());
    }
}
