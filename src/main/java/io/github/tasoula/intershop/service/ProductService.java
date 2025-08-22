package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.OrderRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository repository, CartItemRepository cartItemRepository, OrderRepository orderRepository) {
        this.productRepository = repository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
    }

    public Page<ProductDto> findAll(UUID userId, String search, Pageable pageable) {
       Page<Product> productPage = (search == null || search.isEmpty())
                ? productRepository.findAllByStockQuantityGreaterThan(0, pageable)
                : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(search.toLowerCase(), search.toLowerCase(), 0, pageable);


        List<ProductDto> productCatalogItemDtos = productPage.getContent().stream()
                .map(product -> map(userId, product))
                .collect(Collectors.toList());

        return new PageImpl<>(productCatalogItemDtos, pageable, productPage.getTotalElements());
    }

    public ProductDto findByUserId(UUID userId, UUID productId) {
        Product product = productRepository.findById(productId).get();
        //todo: обработка product == null

        return map(userId, product);
    }

    public List<ProductDto> findByUserId(UUID userId) {
        List<CartItem> items = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return items.stream()
                .map(item -> map(item.getProduct(), item.getQuantity()))
                .collect(Collectors.toList());
    }

    private ProductDto map(UUID userId, Product product){
        int cartQuantity = 0;
        if(userId != null) {
            cartQuantity = cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                    .map(CartItem::getQuantity)
                    .orElse(0); // Если нет записи в корзине, то 0
        }

        return map(product, cartQuantity);
    }

    private ProductDto map(Product product, int cartQuantity){
        return new ProductDto(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                cartQuantity
        );
    }


    //todo возможно этот метод надо перенести в CartService
    @Transactional
    public int changeCartQuantity(UUID userId, UUID productId, int changeQuantity) {
        //todo обработка productId = null
        Product product = productRepository.findById(productId).get();

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> new CartItem(new User(userId), product));

        int newQuantity = cartItem.getQuantity() + changeQuantity;

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
            return 0;
        }

        if (newQuantity > product.getStockQuantity()) newQuantity = product.getStockQuantity();

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);

        return cartItem.getQuantity();
    }

    @Transactional
    public void deleteCartItem(UUID userId, UUID productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }


    public BigDecimal calculateTotalPriceByUserId(UUID userId) {
        BigDecimal result = cartItemRepository.calculateTotalPriceByUserId(userId);
        return (result != null) ? result : BigDecimal.valueOf(0);
    }

    public boolean isEmpty(UUID userId) {
        return !cartItemRepository.existsByUserId(userId);
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
                                    .map(item -> map(item.getProduct(), item.getQuantity()))
                                    .toList(),
                            order.getTotalAmount()
                    );
                }).toList();
    }
}
