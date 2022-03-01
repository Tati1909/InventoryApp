package com.example.inventory

import androidx.lifecycle.*
import com.example.inventory.data.ItemDao
import com.example.inventory.data.ItemEntity
import kotlinx.coroutines.launch

//К настоящему времени вы создали базу данных, а классы пользовательского интерфейса были частью начального кода.
// Чтобы сохранить временные данные приложения, а также получить доступ к базе данных, вам понадобится ViewModel.
// Ваша Inventory ViewModel будет взаимодействовать с базой данных через DAO и предоставлять данные пользовательскому интерфейсу.
// Все операции с базой данных должны выполняться в стороне от основного потока пользовательского интерфейса,
// вы сделаете это с помощью сопрограмм и viewModelScope.
class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {

    //Функция getItems() возвращает Flow. Чтобы использовать данные как LiveData значение, используйте asLiveData()функцию.
    val allItems: LiveData<List<ItemEntity>> = itemDao.getItems().asLiveData()

    //Добавляем в базу данных новый продукт
    //Функция будет вызываться из фрагмента пользовательского интерфейса
    fun addNewItem(itemName: String, itemPrice: String, itemCount: String) {
        val newItem = getNewItemEntry(itemName, itemPrice, itemCount)
        insertItem(newItem)
    }
    //Обратите внимание, что вы не использовали viewModelScope.launch для addNewItem(), но это необходимо только в insertItem(),
    //когда мы напрямую отправляем  запросы в базу данных, т. е. вызываем методы DAO.
    // В Dao suspend функции и их разрешено вызывать только из сопрограммы или другой функции приостановки

    //функция принимает объект Item (продукт) и добавляет данные в базу данных неблокирующим способом.
    private fun insertItem(itemEntity: ItemEntity) {
        //ViewModelScope - это свойство ViewModel, которое автоматически отменяет свои дочерние сопрограммы при уничтожении ViewModel .
        viewModelScope.launch {
            itemDao.insert(itemEntity)
        }
    }

    //функция для обновления Entity(при продаже и редактировании продукта)
    private fun updateItem(itemEntity: ItemEntity) {
        viewModelScope.launch {
            itemDao.update(itemEntity)
        }
    }

    //Экран « Добавить элемент» содержит три текстовых поля для получения сведений об элементе от пользователя.
//На этом шаге вы добавите функцию, чтобы проверить, не является ли текст в текстовых полях пустым.
//Вы будете использовать эту функцию для проверки ввода данных пользователем перед добавлением или обновлением объекта в базе данных.
//Эта проверка должна выполняться во ViewModel фрагменте, а не во фрагменте.
    fun isEntryValid(itemName: String, itemPrice: String, itemCount: String): Boolean {
        if (itemName.isBlank() || itemPrice.isBlank() || itemCount.isBlank()) {
            return false
        }
        return true
    }

    //В текущей задаче мы используем три строки в качестве входных данных (то, что мы будем вводить) и конвертируем их в Item entity
    // То есть 'Арбуз' конвертируется в @ColumnInfo(name = "name")
    //                                 val itemName: String
    private fun getNewItemEntry(
        itemName: String,
        itemPrice: String,
        itemCount: String
    ): ItemEntity {
        return ItemEntity(
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt()
        )
    }

    //Когда мы нажимаем на элемент списка, то переходим на детальное отображение данных(fragment_item_detail.xml)
    //Именно это отображение данных будет хранить в себе retrieveItem, которую мы вызываем в ItemDetailFragment
    //retrieveItem - получить элемент (по Id)
    fun retrieveItem(id: Int): LiveData<ItemEntity> {
        return itemDao.getItem(id).asLiveData()
        //Функция возвращает Flow. Чтобы использовать Flow как функцию LiveData вызываем asLiveData()
        //Т. е. asLiveData конвертирует itemDao.getItem(id) в LiveData<Item>
    }

    //Продать продукт(уменьшаем товар на единицу)
    //Про copy см.5.2.2.5
    fun sellItem(itemEntity: ItemEntity) {
        if (itemEntity.quantityInStock > 0) {
            val newItem = itemEntity.copy(quantityInStock = itemEntity.quantityInStock - 1)
            //обновляем Entity в базе данных
            updateItem(newItem)
        }
    }

    //Мы можем отключить кнопку «Продать» , когда нет товаров для продажи.
    //Возвращает false, если товары закончились
    fun isStockAvailable(itemEntity: ItemEntity): Boolean {
        return (itemEntity.quantityInStock > 0)
    }

    // функция для удаления объекта из базы данных
    fun deleteItem(itemEntity: ItemEntity) {
        viewModelScope.launch {
            itemDao.delete(itemEntity)
        }
    }

    //5.2.2.5
    //Функция нужна для обновления Entity
    //Для сохранения продукта после его редактирования
    //getUpdatedItemEntry() конвертирует входящие параметры в данные Entity и переводит в нужный тип
    private fun getUpdatedItemEntry(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String
    ): ItemEntity {
        return ItemEntity(
            id = itemId,
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt()
        )
    }

    //Обновляем Entity
    //Для сохранения продукта после его редактирования
    //Функция public
    fun updateItem(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String
    ) {
        //getUpdatedItemEntry() передает информацию о данных сущности
        val updatedItem = getUpdatedItemEntry(itemId, itemName, itemPrice, itemCount)
        updateItem(updatedItem)
    }
}

//InventoryViewModelFactory класс для создания InventoryViewModel экземпляра
//Реализуйте create()метод. Проверьте, modelClass совпадает ли InventoryViewModel класс с классом,
// и верните его экземпляр. В противном случае выбросить исключение.
class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}