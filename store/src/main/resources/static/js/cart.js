$(document).ready(function() {

    $('.cart-form button').click(function(event) {
        event.preventDefault();

        var $button = $(this); // Сохраняем ссылку на кнопку
        var $itemCard = $button.closest('.item-card'); // Находим item-card
        var itemId = $button.closest('.item-card').data('item-id'); // Получаем item-id из атрибута data
        var action = $button.attr('value');
        var $quantitySpan = $button.siblings('span');

        var quantity = parseInt($quantitySpan.text());

        var $addToCartButton = $button.siblings('.add-to-cart'); // Кнопка "В корзину"
        var $plusButton = $itemCard.find('.plus'); // Добавляем выборку кнопки plus
        var $minusButton = $itemCard.find('.minus'); // Добавляем выборку кнопки minus
        var $itemImage = $itemCard.find('img');

        var stockQuantity = parseInt($button.closest('.item-card').data('stock-quantity'));

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
                    $.get('/cart/is_available', function(isAvailable) {
                        if(isEmpty){
                            $('#button-new-order').hide();
                            $('#payment-unavailable').hide();
                        }
                        else{
                            if(isAvailable){
                                 $('#button-new-order').show();
                                 $('#payment-unavailable').hide();
                            }
                            else{
                                $('#button-new-order').hide();
                                $('#payment-unavailable').show();
                            }
                        }

                    });
                });
            },
            error: function(xhr, status, error) {
                console.error("Ошибка при изменении количества: " + error);
                alert("Невозможно добавить больше товаров в корзину.  Превышен остаток.");
            }
        });
    });
});
