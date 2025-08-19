$(document).ready(function() {
    $('.cart-form button').click(function(event) {
        event.preventDefault();

        var itemId = $(this).closest('.item-card').data('item-id'); // Получаем item-id из атрибута data
        var action = $(this).val();
        var $quantitySpan = $(this).siblings('span');
        var $addToCartButton = $(this).siblings('.add-to-cart'); // Кнопка "В корзину"

        $.ajax({
            url: '/cart/items/' + itemId + '?action=' + action,
            type: 'POST',
            success: function(newQuantity) {
                // Обновляем количество на странице
                $quantitySpan.text(newQuantity);

                // Показываем или скрываем кнопку "В корзину" в зависимости от нового количества
                if (newQuantity > 0) {
                    $addToCartButton.hide(); // Скрываем кнопку "В корзину"
                } else {
                    $addToCartButton.show(); // Показываем кнопку "В корзину"
                }
            },
            error: function(xhr, status, error) {
                console.error("Ошибка при изменении количества: " + error);
                alert("Невозможно добавить больше товаров в корзину.  Превышен остаток.");
            }
        });
    });

    // Инициализация видимости кнопки "В корзину" при загрузке страницы
    $('.item-card').each(function() {
        var $quantitySpan = $(this).find('span'); // span с количеством
        var $addToCartButton = $(this).find('.add-to-cart'); // Кнопка "В корзину"
        var quantity = parseInt($quantitySpan.text()); // Текущее количество

        if (quantity > 0) {
            $addToCartButton.hide(); // Скрываем кнопку, если количество > 0
        } else {
            $addToCartButton.show(); // Показываем, если количество == 0
        }
    });
});