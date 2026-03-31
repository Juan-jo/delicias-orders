package org.delicias.kanban.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.kanban.domain.model.Kanban;
import org.delicias.kanban.domain.repository.KanbanRepository;
import org.delicias.kanban.dto.KanbanChangeStatusReqDTO;
import org.delicias.kanban.dto.KanbanDTO;
import org.delicias.kanban.dto.KanbanDetailDTO;
import org.delicias.order.application.OrderStateFactory;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.service.OrderChangeStatusService;
import org.delicias.products.domain.model.PosProduct;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@ApplicationScoped
public class KanbanService {

    @ConfigProperty(name = "delicias.defaultPicture")
    String defaultPicture;

    @Inject
    KanbanRepository kanbanRepository;

    @Inject
    OrderChangeStatusService orderChangeStatusService;

    public KanbanDTO loadKanban(Integer restaurantTmplId) {

        List<OrderStatus> orderStatus = List.of(
                OrderStatus.ORDERED,
                OrderStatus.ACCEPTED,
                OrderStatus.COOKING,
                OrderStatus.READY_FOR_DELIVERY,
                OrderStatus.DELIVERY_ASSIGNED_ORDER,
                OrderStatus.DELIVERY_ROAD_TO_STORE
        );

        var result = kanbanRepository.findKanbanByRestaurant(
                restaurantTmplId,
                orderStatus
        );


        Map<OrderStatus, List<Kanban>> grouped = result.stream()
                .collect(Collectors.groupingBy(k -> k.getOrder().getStatus()));


        KanbanDTO.KanbanDTOBuilder response = KanbanDTO.builder()
                .id(restaurantTmplId)
                .label("")
                .children(orderStatus.stream().map(it -> {

                    var orders = grouped.getOrDefault(it, List.of());

                    return KanbanDTO.Board.builder()
                            .id(it.name())
                            .children(orders.stream().map(k -> KanbanDTO.BoardItem.builder()
                                    .kanbanId(k.getId())
                                    .orderId(k.getOrder().getId())
                                    .status(k.getOrder().getStatus().name())
                                    .totalAmount(k.getOrder().getTotalAmountRestaurant())
                                    .createdAt(k.getOrder().getCreatedDate())
                                    .products(k.getOrder().getLines().stream().map(line -> KanbanDTO.ProductItem.builder()
                                            .name(Optional.ofNullable(line.getProduct()).map(PosProduct::getName).orElse("Product Unknow"))
                                            .qty(line.getQty())
                                            .attrValuesDesc(line.getAttributes())
                                            .build()).toList())
                                    .build()).toList())
                            .build();

                }).toList());


        return response.build();
    }

    @Transactional
    public void changeStatus(Long kanbanId, KanbanChangeStatusReqDTO req) {

        var kanban = kanbanRepository.findById(kanbanId);

        if(kanban == null) {
            throw new NotFoundException("Kanban Not Found");
        }

        orderChangeStatusService.changeStatus(kanban.getOrder(), req.status());
    }

    public KanbanDetailDTO getDetail(Long kanbanId) {

        var kanban = kanbanRepository.findById(kanbanId);

        if(kanban == null) {
            throw new NotFoundException("Kanban Not Found");
        }

        PosOrder order = kanban.getOrder();

        return KanbanDetailDTO.builder()
                .kanbanId(kanbanId)
                .order(KanbanDetailDTO.Order.builder()
                        .orderId(kanban.getId())
                        .status(order.getStatus())
                        .paymentType("CASH")
                        .createdAt(order.getCreatedDate())
                        .totalAmount(order.getTotalAmountRestaurant())
                        .lines(order.getLines().stream().map(it -> {

                            KanbanDetailDTO.Line.LineBuilder r = KanbanDetailDTO.Line.builder();
                            r.attrs(it.getAttributes());
                            r.priceTotal(it.getPriceTotal());
                            r.qty(it.getQty());

                            Optional.ofNullable(it.getProduct()).ifPresentOrElse(p -> {
                                r.productName(p.getName());
                                r.pictureUrl(p.getPictureUrl());
                            }, () -> {
                                r.productName("Product Unknow");
                                r.pictureUrl(defaultPicture);
                            });

                            return r.build();

                        }).toList())
                        .build())
                .build();
    }

}
