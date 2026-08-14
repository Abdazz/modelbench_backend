package com.example.modelbench.service;

import com.example.modelbench.dto.UtilisateurAdminResponse;
import com.example.modelbench.dto.UtilisateurCreationRequest;
import com.example.modelbench.dto.UtilisateurModificationRequest;
import com.example.modelbench.entity.Utilisateur;
import com.example.modelbench.entity.enums.Role;
import com.example.modelbench.exception.DuplicateResourceException;
import com.example.modelbench.exception.ResourceInUseException;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.mapper.UtilisateurMapper;
import com.example.modelbench.repository.UtilisateurRepository;
import com.example.modelbench.service.impl.UtilisateurServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository depot;

    @Spy
    private UtilisateurMapper mapper = new UtilisateurMapper();

    @Mock
    private PasswordEncoder encodeur;

    @InjectMocks
    private UtilisateurServiceImpl service;

    private UtilisateurCreationRequest requeteCreation;

    @BeforeEach
    void preparer() {
        requeteCreation = new UtilisateurCreationRequest(
                "Marie Curie", "marie.curie@example.com", "motdepasse123", Role.CHERCHEUR, true);
    }

    @AfterEach
    void nettoyerLeContexteDeSecurite() {
        SecurityContextHolder.clearContext();
    }

    private void connecterEnTantQue(String login) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(login, null));
    }

    private Utilisateur unUtilisateurPersiste(Long id, String login, Role role, boolean actif) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        utilisateur.setLogin(login);
        utilisateur.setMotDePasse("HACHE_EXISTANT");
        utilisateur.setNomComplet("Utilisateur " + id);
        utilisateur.setRole(role);
        utilisateur.setActif(actif);
        return utilisateur;
    }

    @Test
    void creeUnUtilisateurEtHacheLeMotDePasse() {
        when(depot.existsByLoginIgnoreCase("marie.curie@example.com")).thenReturn(false);
        when(encodeur.encode("motdepasse123")).thenReturn("HACHE_BCRYPT");
        when(depot.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur aEnregistrer = invocation.getArgument(0);
            aEnregistrer.setId(1L);
            return aEnregistrer;
        });

        UtilisateurAdminResponse reponse = service.creer(requeteCreation);

        assertThat(reponse.id()).isEqualTo(1L);
        assertThat(reponse.login()).isEqualTo("marie.curie@example.com");
        verify(encodeur).encode("motdepasse123");
    }

    @Test
    void refuseLaCreationSiLEmailExisteDeja() {
        when(depot.existsByLoginIgnoreCase("marie.curie@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(requeteCreation))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("marie.curie@example.com");

        verify(depot, never()).save(any(Utilisateur.class));
    }

    @Test
    void leveUneExceptionQuandLUtilisateurDemandeNExistePas() {
        when(depot.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void modifieUnUtilisateurSansChangerLeMotDePasseQuandIlEstAbsent() {
        Utilisateur existant = unUtilisateurPersiste(1L, "marie.curie@example.com",
                Role.CHERCHEUR, true);
        connecterEnTantQue("un.autre.admin@example.com");
        when(depot.findById(1L)).thenReturn(Optional.of(existant));
        when(depot.existsByLoginIgnoreCaseAndIdNot("nouveau@example.com", 1L)).thenReturn(false);
        when(depot.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UtilisateurAdminResponse reponse = service.modifier(1L, new UtilisateurModificationRequest(
                "Marie Curie-Sklodowska", "nouveau@example.com", null, Role.ADMIN, true));

        assertThat(reponse.login()).isEqualTo("nouveau@example.com");
        assertThat(existant.getMotDePasse()).isEqualTo("HACHE_EXISTANT");
        verify(encodeur, never()).encode(any());
    }

    @Test
    void modifieUnUtilisateurEtReHacheLeMotDePasseQuandIlEstFourni() {
        Utilisateur existant = unUtilisateurPersiste(1L, "marie.curie@example.com",
                Role.CHERCHEUR, true);
        connecterEnTantQue("un.autre.admin@example.com");
        when(depot.findById(1L)).thenReturn(Optional.of(existant));
        when(depot.existsByLoginIgnoreCaseAndIdNot("marie.curie@example.com", 1L)).thenReturn(false);
        when(encodeur.encode("nouveaumotdepasse")).thenReturn("NOUVEAU_HACHE");
        when(depot.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.modifier(1L, new UtilisateurModificationRequest(
                "Marie Curie", "marie.curie@example.com", "nouveaumotdepasse", Role.CHERCHEUR, true));

        assertThat(existant.getMotDePasse()).isEqualTo("NOUVEAU_HACHE");
    }

    @Test
    void refuseLaModificationSiLeNouvelEmailAppartientADunAutreUtilisateur() {
        Utilisateur existant = unUtilisateurPersiste(1L, "marie.curie@example.com",
                Role.CHERCHEUR, true);
        when(depot.findById(1L)).thenReturn(Optional.of(existant));
        when(depot.existsByLoginIgnoreCaseAndIdNot("pris@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.modifier(1L, new UtilisateurModificationRequest(
                "Marie Curie", "pris@example.com", null, Role.CHERCHEUR, true)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void refuseQuUnAdministrateurDesactiveSonProprePropreCompte() {
        Utilisateur soi = unUtilisateurPersiste(1L, "admin@example.com", Role.ADMIN, true);
        connecterEnTantQue("admin@example.com");
        when(depot.findById(1L)).thenReturn(Optional.of(soi));
        when(depot.existsByLoginIgnoreCaseAndIdNot("admin@example.com", 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.modifier(1L, new UtilisateurModificationRequest(
                "Administrateur", "admin@example.com", null, Role.ADMIN, false)))
                .isInstanceOf(ResourceInUseException.class);

        verify(depot, never()).save(any(Utilisateur.class));
    }

    @Test
    void refuseQuUnAdministrateurSeSupprimeLuiMeme() {
        Utilisateur soi = unUtilisateurPersiste(1L, "admin@example.com", Role.ADMIN, true);
        connecterEnTantQue("admin@example.com");
        when(depot.findById(1L)).thenReturn(Optional.of(soi));

        assertThatThrownBy(() -> service.supprimer(1L))
                .isInstanceOf(ResourceInUseException.class);

        verify(depot, never()).delete(any(Utilisateur.class));
    }

    @Test
    void refuseDeDesactiverLeDernierAdministrateurActif() {
        Utilisateur dernierAdmin = unUtilisateurPersiste(1L, "admin@example.com", Role.ADMIN, true);
        connecterEnTantQue("un.autre.admin@example.com");
        when(depot.findById(1L)).thenReturn(Optional.of(dernierAdmin));
        when(depot.existsByLoginIgnoreCaseAndIdNot("admin@example.com", 1L)).thenReturn(false);
        when(depot.countByRoleAndActifTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.modifier(1L, new UtilisateurModificationRequest(
                "Administrateur", "admin@example.com", null, Role.ADMIN, false)))
                .isInstanceOf(ResourceInUseException.class);

        verify(depot, never()).save(any(Utilisateur.class));
    }

    @Test
    void refuseDeRetrograderLeDernierAdministrateurActifEnChercheur() {
        Utilisateur dernierAdmin = unUtilisateurPersiste(1L, "admin@example.com", Role.ADMIN, true);
        connecterEnTantQue("un.autre.admin@example.com");
        when(depot.findById(1L)).thenReturn(Optional.of(dernierAdmin));
        when(depot.existsByLoginIgnoreCaseAndIdNot("admin@example.com", 1L)).thenReturn(false);
        when(depot.countByRoleAndActifTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.modifier(1L, new UtilisateurModificationRequest(
                "Administrateur", "admin@example.com", null, Role.CHERCHEUR, true)))
                .isInstanceOf(ResourceInUseException.class);
    }

    @Test
    void refuseDeSupprimerLeDernierAdministrateurActif() {
        Utilisateur dernierAdmin = unUtilisateurPersiste(1L, "admin@example.com", Role.ADMIN, true);
        connecterEnTantQue("un.autre.admin@example.com");
        when(depot.findById(1L)).thenReturn(Optional.of(dernierAdmin));
        when(depot.countByRoleAndActifTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.supprimer(1L))
                .isInstanceOf(ResourceInUseException.class);

        verify(depot, never()).delete(any(Utilisateur.class));
    }

    @Test
    void supprimeUnUtilisateurQuiNEstNiLeCompteCourantNiLeDernierAdministrateur() {
        Utilisateur chercheur = unUtilisateurPersiste(2L, "chercheur@example.com",
                Role.CHERCHEUR, true);
        connecterEnTantQue("admin@example.com");
        when(depot.findById(2L)).thenReturn(Optional.of(chercheur));

        service.supprimer(2L);

        verify(depot).delete(chercheur);
    }
}
