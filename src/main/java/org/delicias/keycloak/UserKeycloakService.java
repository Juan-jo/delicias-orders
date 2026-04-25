package org.delicias.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.delicias.common.roles.Role;
import org.delicias.delivery_users.exception.EmailAlreadyExistsException;
import org.delicias.delivery_users.exception.UserNameAlreadyExistsException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UserKeycloakService {

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String REALM;

    public String createUser(
            String username,
            String email,
            String password,
            Role role,
            String name,
            String lastName
    ) {

        RealmResource realm = keycloak.realm(REALM);
        UsersResource users = realm.users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(name);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        Response response = users.create(user);

        if (response.getStatus() != 201) {

            String errorMessage = extractError(response);

            if (response.getStatus() == 409) {
                if (errorMessage.contains("username")) {
                    throw new UserNameAlreadyExistsException(
                            Response.Status.CONFLICT.getStatusCode(),
                            username
                    );

                }
                if (errorMessage.contains("email")) {
                    throw new EmailAlreadyExistsException(
                            Response.Status.CONFLICT.getStatusCode(),
                            email
                    );
                }
            }

            throw new RuntimeException("Error creando usuario: " + errorMessage);
        }

        String userId = getUserId(response);

        setPassword(userId, password);

        assignRealmRole(userId, role);

        return userId;
    }

    public void setPassword(String userId, String password) {
        RealmResource realm = keycloak.realm(REALM);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);

        realm.users().get(userId).resetPassword(credential);
    }


    public void assignRealmRole(String userId, Role roleName) {
        RealmResource realm = keycloak.realm(REALM);

        RoleRepresentation role = realm.roles().get(roleName.name()).toRepresentation();

        realm.users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(role));
    }


    public void updateUser(String userId,
                           String name,
                           String lastName,
                           String email,
                           boolean enabled) {
        RealmResource realm = keycloak.realm(REALM);
        UserResource userResource = realm.users().get(userId);

        UserRepresentation user = userResource.toRepresentation();
        user.setFirstName(name);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setEnabled(enabled);

        try {
            userResource.update(user);

        } catch (WebApplicationException e) {
            Response response = e.getResponse();

            String errorMessage = extractError(response);

            if (response.getStatus() == 409) {
                throw new EmailAlreadyExistsException(
                        Response.Status.CONFLICT.getStatusCode(),
                        email
                );
            }

            throw new RuntimeException("Error creando usuario: " + errorMessage);
        }
    }

    public void deleteUser(String userId) {
        try {
            RealmResource realm = keycloak.realm(REALM);
            UserResource userResource = realm.users().get(userId);

            userResource.remove();

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar usuario en Keycloak", e);
        }
    }

    private String getUserId(Response response) {
        String location = response.getLocation().getPath();
        return location.substring(location.lastIndexOf("/") + 1);
    }

    private String extractError(Response response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String json = response.readEntity(String.class);

            if (json == null || json.isEmpty()) {
                return "Sin detalle";
            }

            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            return map.getOrDefault("errorMessage", json).toString();

        } catch (Exception e) {
            return "No se pudo parsear error";
        }
    }
}
