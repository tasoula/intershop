$(document).ready(function() {

    $('.cart-form button').click(function(event) {
        event.preventDefault();

        var $button = $(this);
        var $itemCard = $button.closest('.item-card');
        var itemId = $itemCard.data('item-id');
        var action = $button.attr('value');
        var $quantitySpan = $button.siblings('span');
        var quantity = parseInt($quantitySpan.text());
        var $addToCartButton = $button.siblings('.add-to-cart');
        var $plusButton = $itemCard.find('button[value="PLUS"]'); //Уточняем селектор
        var $minusButton = $itemCard.find('button[value="MINUS"]'); //Уточняем селектор
        var $itemImage = $itemCard.find('img');
        var stockQuantity = parseInt($itemCard.data('stock-quantity'));

        $.ajax({
            url: '/cart/items/' + itemId + '?action=' + action,
            type: 'POST',
            success: function(newQuantity) {

                // Обновляем количество на странице
                $quantitySpan.text(newQuantity);

                // Отображаем или скрываем кнопку "Добавить в корзину"
                if(newQuantity == 0){
                     $addToCartButton.show();
                }
                else{
                     $addToCartButton.hide();
                }

                // Если количество стало равно нулю и действие было удалением, можно удалить элемент из корзины
                if (newQuantity <= 0 && action === 'DELETE') {
                     $button.closest('tr').remove();
                }

                // Управляем доступностью кнопок plus, minus
                $minusButton.prop('disabled', newQuantity <= 0);
                $plusButton.prop('disabled', newQuantity >= stockQuantity);

                // Обновляем класс у изображения
                if (stockQuantity < newQuantity) {
                    $itemImage.addClass('out-of-stock');
                } else {
                    $itemImage.removeClass('out-of-stock');
                }

                // Обновляем итоговую сумму (необходимо запросить у сервера)
                $.get('/cart/total', function(total) {
                     $('#total-price').text('Итого: ' + total + ' руб.');
                });

                // Проверяем, пуста ли корзина и скрываем/показываем кнопку "Купить"
                $.get('/cart/is_empty', function(isEmpty) {
                     if (isEmpty) {
                           $('#new-order-form').hide();
                     } else {
                           $('#new-order-form').show();
                     }
                });
            },
            error: function(xhr, status, error) {
                console.error("Ошибка при изменении количества: " + error);
                alert("Невозможно добавить больше товаров в корзину.  Превышен остаток.");
            }
        });
    });

    // Инициализация видимости кнопки "В корзину" при загрузке страницы
/*    $('.item-card').each(function() {
        var $quantitySpan = $(this).find('span');
        var $addToCartButton = $(this).find('.add-to-cart');
        var quantity = parseInt($quantitySpan.text());
        if (quantity > 0) {
            $addToCartButton.hide();
        } else {
            $addToCartButton.show();
        }
    });
    */
});