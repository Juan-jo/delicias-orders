package org.delicias.products.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.common.dto.order.CandidateOrderDTO;
import org.delicias.products.domain.model.PosProduct;
import org.delicias.products.domain.repository.PosProductRepository;

import java.util.List;

@ApplicationScoped
public class PosProductService {

    @Inject
    PosProductRepository repository;

    public void addProducts(List<CandidateOrderDTO.Line> lines) {

        for (CandidateOrderDTO.Line it : lines) {

            PosProduct product = PosProduct.builder()
                    .id(it.productId())
                    .name(it.name())
                    .pictureUrl(it.pictureUrl())
                    .build();

            repository.getEntityManager().merge(product);
        }
    }



}
