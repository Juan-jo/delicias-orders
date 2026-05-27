package org.delicias.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.minio.MinioStorageService;
import org.delicias.order.domain.model.PosOrderHistory;
import org.delicias.order.dto.OrderHistoryItemDTO;
import org.delicias.rest.security.SecurityContextService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrdersHistoryService {

    @Inject
    SecurityContextService securityContextService;

    @Inject
    MinioStorageService minioStorageService;

    @ConfigProperty(name = "delicias.defaultPicture")
    String defaultPicture;


    public List<OrderHistoryItemDTO> loadHistory(Integer page) {

        return PosOrderHistory.findByUserUUID(UUID.fromString(securityContextService.userId()), page)
                .stream().map(it -> OrderHistoryItemDTO.builder()
                        .orderId(it.getId())
                        .status(it.getStatus())
                        .orderedAt(it.getOrderedAt())
                        .restaurantName(it.getRestaurant().getName())
                        .restaurantPictureUrl(minioStorageService.fitThumbnailUrl(
                                Optional.ofNullable(it.getRestaurant().getImageLogoUrl()).orElse(defaultPicture))
                        )
                        .build())
                .toList();
    }
}
