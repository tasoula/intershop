package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.OrderRepository;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(CartItemRepository cartItemRepository, OrderRepository orderRepository) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
    }


    @Transactional
    public Optional<UUID> createOrder(UUID userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        if (items.stream().anyMatch(c -> c.getProduct().getStockQuantity() < c.getQuantity()))
            return Optional.empty();

        Order order = new Order();
        order.setUser(new User(userId));

        List<OrderItem> orderItems = items.stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    Product product = cartItem.getProduct();
                    orderItem.setProduct(product);
                    orderItem.setPriceAtTimeOfOrder(product.getPrice());
                    orderItem.setQuantity(cartItem.getQuantity());
                    product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                    return orderItem;
                }).toList();
        order.setOrderItems(orderItems);

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getPriceAtTimeOfOrder)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.valueOf(0));
        order.setTotalAmount(totalAmount);

        UUID orderId = orderRepository.save(order).getId();
        cartItemRepository.deleteByUserId(userId);

        return Optional.of(orderId);
    }

    public Optional<OrderDto> getById(UUID id) {
        //todo handle order == null
        Optional<Order> orderOptional = orderRepository.findById(id);

        if(orderOptional.isPresent()) {
            Order order = orderOptional.get();

            OrderDto orderDto = new OrderDto();
            orderDto.setId(order.getId());

            List<ProductDto> items = order.getOrderItems()
                    .stream()
                    .map(item -> {
                        ProductDto dto = new ProductDto();
                        dto.setId(item.getProduct().getId());
                        dto.setTitle(item.getProduct().getTitle());
                        dto.setQuantity(item.getQuantity());
                        dto.setPrice(item.getPriceAtTimeOfOrder());
                        return dto;
                    })
                    .toList();
            orderDto.setItems(items);

            orderDto.setTotalAmount(items.stream()
                    .map(i ->  i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            return Optional.of(orderDto);
        }
        return  Optional.empty();
    }

    public List<OrderDto> getByUserId(UUID userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders
                .stream()
                .map(order -> {
                    return new OrderDto(
                            order.getId(),
                            order.getOrderItems()
                                    .stream()
                                    .map(item -> new ProductDto(item.getProduct(), item.getQuantity()))
                                    .toList(),
                            order.getTotalAmount()
                    );
                }).toList();
    }
}
