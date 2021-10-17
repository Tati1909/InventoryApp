package com.example.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//Теперь, когда мы определили ViewModel, DAO - нам все еще нужно указать Room что делать со всеми этими классами.
// Вот где на помощь приходит класс ItemRoomDatabase. Android-приложение, использующее Room создает подклассы
// и выполняет несколько ключевых функций. В нашем приложении AppDatabase необходимо:

//Указать, какие entities определены в базе данных(Item как единственный класс - entities = [Item::class]) .
//Предоставить доступ к одному экземпляру каждого класса DAO.
//Выполнить любую дополнительную настройку, например предварительное заполнение базы данных.
//Установите exportSchema значение false, чтобы не сохранять резервные копии истории версий схемы.
@Database(entities = [ItemEntity::class], version = 1, exportSchema = false)
abstract class ItemRoomDatabase : RoomDatabase() {

    //База данных должна знать о DAO. Внутри тела класса объявите абстрактную функцию, которая возвращает ItemDao.
    //У вас может быть несколько DAO. Данный метод возвращает объект ItemDao
    abstract fun itemDao(): ItemDao

    //При использовании ItemRoomDatabase класса вы хотите убедиться, что существует только один экземпляр базы данных,
    //чтобы предотвратить состояние гонки или другие потенциальные проблемы. Экземпляр хранится в companion object,
    // и вам также понадобится метод, который либо возвращает существующий экземпляр,
    // либо создает базу данных в первый раз. Это определено в companion object.
    //companion object предоставляет доступ к методам создания или получения базы данных, используя имя класса в качестве квалификатора
    companion object {
        //Значение изменчивой переменной никогда не будет кэшироваться, и все операции записи и чтения будут выполняться в основную память и из нее.
        //Это помогает убедиться, что значение INSTANCE всегда актуально и одинаково для всех потоков выполнения.
        // Это означает, что изменения INSTANCE, сделанные одним потоком, сразу  же видны всем другим потокам.
        @Volatile
        private var INSTANCE: ItemRoomDatabase? = null

        //функция для возврата экземпляра нашей базы ItemRoomDatabase
        //В реализации для getDatabase()вы используете оператор Элвиса, чтобы либо вернуть существующую базу данных (INSTANCE) (если она уже существует),
        // либо создать базу данных в первый раз в блоке synchronized (если  INSTANCE =null).
        //Несколько потоков потенциально могут попасть в состояние гонки и одновременно создать 2 базы данных.
        //Обертывание кода для помещения базы данных в synchronized блок означает,
        //что только один поток выполнения может одновременно входить в этот блок кода.
        fun getDatabase(context: Context): ItemRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItemRoomDatabase::class.java,
                    "item_database"
                )
                    //Миграция выходит за рамки этой лаборатории. Простое решение -
                    // уничтожить и восстановить базу данных, что означает потерю данных.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}