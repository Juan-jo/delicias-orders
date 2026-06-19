package org.delicias.delivery_users.service;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.delicias.common.dto.PagedResult;
import org.delicias.common.dto.delivery.DeliveryUserStatus;
import org.delicias.common.roles.Roles;
import org.delicias.delivery_users.domain.model.DeliveryUserModel;
import org.delicias.delivery_users.domain.repository.DeliveryUserRepository;
import org.delicias.delivery_users.dto.*;
import org.delicias.keycloak.UserKeycloakService;
import org.delicias.minio.MinioStorageService;
import org.delicias.minio.utils.MinioRS;
import org.delicias.minio.utils.MinioSize;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DeliveryUserService {


    @ConfigProperty(name = "delicias.defaultPicture")
    String defaultPicture;

    @Inject
    DeliveryUserRepository deliveryUserRepository;

    @Inject
    MinioStorageService storageService;

    @Inject
    UserKeycloakService userKeycloakService;

    @Transactional
    public void createUserDelivery(CreateDeliverUserReqDTO req) {

        String userUUID = userKeycloakService.createUser(
                req.username,
                req.email,
                req.password,
                Roles.MOBILE_USER_DELIVERY,
                req.name,
                req.lastName
        );

        String pictureUrl = Optional.ofNullable(req.picture).map(fileUpload -> storageService.upload(fileUpload)).orElse(defaultPicture);

        DeliveryUserModel newUserDeliver = DeliveryUserModel.builder()
                .deliveryUUID(UUID.fromString(userUUID))
                .zoneId(req.zoneId)
                .username(req.username)
                .email(req.email)
                .status(DeliveryUserStatus.OFFLINE)
                .name(req.name)
                .lastName(req.lastName)
                .pictureURL(pictureUrl)
                .build();

        deliveryUserRepository.persist(newUserDeliver);
    }

    public DeliveryUserDTO findById(Integer id) {

        DeliveryUserModel deliveryUser = deliveryUserRepository.findById(id);

        if (deliveryUser == null) {
            throw new NotFoundException("DeliveryUser Not Found");
        }

        return DeliveryUserDTO.builder()
                .id(deliveryUser.getId())
                .name(deliveryUser.getName())
                .lastName(deliveryUser.getLastName())
                .username(deliveryUser.getUsername())
                .email(deliveryUser.getEmail())
                .pictureUrl(
                        storageService.pictureUrl(Optional.ofNullable(deliveryUser.getPictureURL()).orElse(defaultPicture),
                                MinioSize.MEDIUM, MinioRS.FIT, (short) 70)
                )
                .build();
    }

    @Transactional
    public void update(UpdateDeliveryUserReqDTO req) {

        DeliveryUserModel deliveryUser = deliveryUserRepository.findById(req.id);

        if (deliveryUser == null) {
            throw new NotFoundException("DeliveryUser Not Found");
        }

        userKeycloakService.updateUser(
                String.valueOf(deliveryUser.getDeliveryUUID()),
                req.name,
                req.lastName,
                req.email,
                true
        );

        String pictureUrl = Optional.ofNullable(req.picture).map(fileUpload -> storageService.upload(fileUpload)).orElse(null);

        deliveryUser.setName(req.name);
        deliveryUser.setLastName(req.lastName);
        deliveryUser.setEmail(req.email);

        if (pictureUrl != null) {
            deliveryUser.setPictureURL(pictureUrl);
        }
    }

    public void changePassword(ChangePasswordReqDTO req) {

        DeliveryUserModel deliveryUser = deliveryUserRepository.findById(req.deliveryUserId());

        if (deliveryUser == null) {
            throw new NotFoundException("DeliveryUser Not Found");
        }

        userKeycloakService.setPassword(
                String.valueOf(deliveryUser.getDeliveryUUID()),
                req.password()
        );
    }

    @Transactional
    public void delete(Integer deliveryUserId) {

        DeliveryUserModel deliveryUser = deliveryUserRepository.findById(deliveryUserId);

        if (deliveryUser == null) {
            throw new NotFoundException("DeliveryUser Not Found");
        }
        UUID deliveryUserUUID = deliveryUser.getDeliveryUUID();
        String currentPictureURL = deliveryUser.getPictureURL();

        deliveryUserRepository.delete(deliveryUser);
        userKeycloakService.deleteUser(String.valueOf(deliveryUserUUID));


    }

    public PagedResult<DeliveryUserItemDTO> search(
            Integer zoneId,
            String name,
            int page,
            int size,
            String orderColumn,
            String orderDir
    ) {

        List<DeliveryUserItemDTO> filtered = deliveryUserRepository.searchByFilter(
                        zoneId,
                        name,
                        page,
                        size,
                        orderColumn,
                        getOrderDirection(orderDir)
                ).stream().map(it -> DeliveryUserItemDTO.builder()
                        .id(it.getId())
                        .name(Optional.ofNullable(it.getName()).orElse("") + " " + Optional.ofNullable(it.getLastName()).orElse(""))
                        .status(it.getStatus())
                        .username(Optional.ofNullable(it.getUsername()).orElse(""))
                        .email(Optional.ofNullable(it.getEmail()).orElse(""))
                        .pictureUrl(
                                storageService.pictureUrl(
                                        Optional.ofNullable(it.getPictureURL()).orElse(defaultPicture),
                                        MinioSize.MEDIUM, MinioRS.FIT, (short) 70)
                        )
                        .build())
                .toList();

        long total = deliveryUserRepository.countByFilter(zoneId, name);

        return new PagedResult<>(
                filtered, total, page, size
        );
    }

    private Sort.Direction getOrderDirection(String orderDir) {

        if (orderDir == null) {
            return Sort.Direction.Ascending;
        }

        return switch (orderDir.toLowerCase()) {
            case "desc", "descending" -> Sort.Direction.Descending;
            default -> Sort.Direction.Ascending;
        };
    }
}
