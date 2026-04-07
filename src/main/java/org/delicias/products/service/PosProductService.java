package org.delicias.products.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.common.dto.order.CandidateOrderDTO;
import org.delicias.products.domain.model.PosProduct;
import org.delicias.products.domain.repository.PosProductRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PosProductService {

    @Inject
    PosProductRepository repository;


    public Map<Integer, PosProduct> addProducts(List<CandidateOrderDTO.Line> lines) {
        Map<Integer, PosProduct> managedProducts = new HashMap<>();
        for (CandidateOrderDTO.Line it : lines) {
            PosProduct product = PosProduct.builder()
                    .id(it.productId())
                    .name(it.name())
                    .pictureUrl(it.pictureUrl())
                    .build();

            managedProducts.put(it.productId(), repository.getEntityManager().merge(product));
        }
        return managedProducts;
    }



}
