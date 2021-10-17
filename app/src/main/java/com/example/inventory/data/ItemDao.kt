package com.example.inventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    //Запрос SQLite возвращает все столбцы из item таблицы, упорядоченные в порядке возрастания
    @Query("SELECT * from item ORDER BY name ASC")
    fun getItems(): Flow<List<ItemEntity>>

    //SQLite для извлечения определенного элемента из таблицы элементов на основе заданного id
    @Query("SELECT * from item WHERE id = :id")
    fun getItem(id: Int): Flow<ItemEntity>

    //Выполнение операций с базой данных может занять много времени, поэтому они должны выполняться в отдельном потоке.
    //Аргумент OnConflict говорит Room, что делать в случае конфликта. OnConflictStrategy.IGNORE игнорирует новый item ,
    // если этот первичный ключ уже есть в базе данных.
    //Когда вы вызываете insert()из своего кода Kotlin, Room выполняет SQL-запрос для вставки объекта(item) в базу данных.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(itemEntity: ItemEntity)

    @Update
    suspend fun update(itemEntity: ItemEntity)

    @Delete
    suspend fun delete(itemEntity: ItemEntity)

    //Использование Flow или в LiveData  в качестве возвращаемого типа гарантирует, что вы будете получать уведомления всякий раз,
// когда данные в базе данных изменяются. Рекомендуется использовать Flow в слое сохранения. Он обновляет Room для вас,
// что означает, что вам нужно явно получить данные только один раз.
//Так как возвращается тип Flow(поток), Room также выполняет запрос в фоновом потоке.
// Вам не нужно явно делать его suspend функцией и вызывать внутри области сопрограммы.
}