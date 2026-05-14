package org.delicias.delivery_users.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.delicias.common.dto.delivery.DeliveryUserStatus;
import org.delicias.delivery_users.domain.model.DeliveryUserModel;
import org.delicias.delivery_users.domain.repository.DeliveryUserRepository;
import org.delicias.rest.security.SecurityContextService;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class MobileDeliveryUserStatusService {

    @Inject
    SecurityContextService security;


    @Inject
    DeliveryUserRepository deliveryUserRepository;

    public Map<String, Object> loadStatus() {

        DeliveryUserStatus status = deliveryUserRepository.find(
                        "select status from DeliveryUserModel where deliveryUUID = ?1",
                        UUID.fromString(security.userId())
                )
                .project(DeliveryUserStatus.class)
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("DeliveryUser Not Found"));

        return Map.of("status", status);
    }

    @Transactional
    public Map<String, Object> setStatus(boolean available) {

        DeliveryUserModel delivery = deliveryUserRepository.findByUUID(
                UUID.fromString(security.userId())
        ).orElseThrow(() -> new NotFoundException("DeliveryUser Not Found"));

        delivery.setStatus(
                available ? DeliveryUserStatus.AVAILABLE : DeliveryUserStatus.OFFLINE
        );

        return Map.of("status", delivery.getStatus());
    }

}
