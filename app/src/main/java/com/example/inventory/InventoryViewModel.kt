package com.example.inventory

import androidx.lifecycle.*
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.launch

//К настоящему времени вы создали базу данных, а классы пользовательского интерфейса были частью начального кода.
// Чтобы сохранить временные данные приложения, а также получить доступ к базе данных, вам понадобится ViewModel.
// Ваша Inventory ViewModel будет взаимодействовать с базой данных через DAO и предоставлять данные пользовательскому интерфейсу.
// Все операции с базой данных должны выполняться в стороне от основного потока пользовательского интерфейса,
// вы сделаете это с помощью сопрограмм и viewModelScope.
class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {

    //Функция getItems() возвращает Flow. Чтобы использовать данные как LiveData значение, используйте asLiveData()функцию.
    val allItems: LiveData<List<Item>> = itemDao.getItems().asLiveData()

    //сохраняем в базу данных новый продукт
    //Функция будет вызываться из фрагмента пользовательского интерфейса
    fun addNewItem(itemName: String, itemPrice: String, itemCount: String) {
        val newItem = getNewItemEntry(itemName, itemPrice, itemCount)
        insertItem(newItem)
    }

    //функция принимает объект Item (продукт) и добавляет данные в базу данных неблокирующим способом.
    private fun insertItem(item: Item) {
        //ViewModelScope - это свойство ViewModel, которое автоматически отменяет свои дочерние сопрограммы при уничтожении ViewModel .
        viewModelScope.launch {
            itemDao.insert(item)
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
    private fun getNewItemEntry(itemName: String, itemPrice: String, itemCount: String): Item {
        return Item(
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt()
        )
    }

    //Обратите внимание, что вы не использовали viewModelScope.launch для addNewItem(), но это необходимо только в insertItem(),
    //когда мы напрямую отправляем  запросы в базу данных, т. е. вызываем методы DAO.
    // В Dao suspend функции и их разрешено вызывать только из сопрограммы или другой функции приостановки
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