$(document).ready(function() {
    $('.cart-form button').click(function(event) {
        event.preventDefault();

        var $button = $(this); // Сохраняем ссылку на кнопку
        var itemId = $button.closest('.item-card').data('item-id'); // Получаем item-id из атрибута data
        var action = $button.attr('value');
        var $quantitySpan = $button.siblings('span');
        var $addToCartButton = $button.siblings('.add-to-cart'); // Кнопка "В корзину"

        $.ajax({
            url: '/cart/items/' + itemId + '?action=' + action,
            type: 'POST',
            success: function(newQuantity) {
                // Обновляем количество на странице
                $quantitySpan.text(newQuantity);

                // Если количество стало равно нулю и действие было удалением, можно удалить элемент из корзины
                if (newQuantity <= 0 && action === 'delete') {
                     $button.closest('tr').remove();
                }
                // Обновляем итоговую сумму (необходимо запросить у сервера)
                $.get('/cart/total', function(total) {
                     $('#total-price').text('Итого: ' + total + ' руб.');
                });

                // Проверяем, пуста ли корзина и скрываем/показываем кнопку "Купить"
                $.get('/cart/is_empty', function(isEmpty) {
                     if (isEmpty) {
                          $('form[action="/buy"]').hide();
                     } else {
                          $('form[action="/buy"]').show();
                     }
                });

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