package org.delicias.users.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.users.domain.model.PosUserAddress;
import org.delicias.users.domain.repository.PosUserAddressRepository;
import org.delicias.users.dto.UserAddressDTO;

import java.util.Optional;

@ApplicationScoped
public class PosUserAddressService {

    @Inject
    PosUserAddressRepository repository;

    public PosUserAddress createOrUpdate(UserAddressDTO addressDTO) {

        PosUserAddress address = Optional.ofNullable(repository.findById(addressDTO.id()))
                .map(it -> {
                    it.setTypeAddress(addressDTO.typeAddress());
                    it.setDetails(addressDTO.details());
                    it.setStreet(addressDTO.street());
                    it.setAddress(addressDTO.address());
                    it.setIndications(addressDTO.indications());
                    it.setFullName(addressDTO.fullName());
                    it.setPictureUrl(addressDTO.pictureUrl());
                    return it;
                })
                .orElse(PosUserAddress.builder()
                        .id(addressDTO.id())
                        .typeAddress(addressDTO.typeAddress())
                        .details(addressDTO.details())
                        .street(addressDTO.street())
                        .address(addressDTO.address())
                        .indications(addressDTO.indications())
                        .fullName(addressDTO.fullName())
                        .pictureUrl(addressDTO.pictureUrl())
                        .build());

        repository.persist(address);

        return address;
    }

}
