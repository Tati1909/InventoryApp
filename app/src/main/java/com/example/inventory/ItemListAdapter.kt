package com.example.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ItemEntity
import com.example.inventory.data.getFormattedPrice
import com.example.inventory.databinding.ItemListItemBinding

//см 5.1.2.8
//Функция onItemClicked будет использоваться для обработки навигации, когда элемент выбран на первом экране,
//а для второго экрана вы просто передадите пустую функцию.
//onItemClicked принимает данные Entity
class ItemListAdapter(private val onItemClicked: (ItemEntity) -> Unit) :
    ListAdapter<ItemEntity, ItemListAdapter.ItemViewHolder>(DiffCallback) {

    class ItemViewHolder(private var binding: ItemListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //функция для установки названия продукта, цены и количества на складе
        fun bind(itemEntity: ItemEntity) {
            binding.apply {
                itemName.text = itemEntity.itemName
                //Получите цену в денежном формате с помощью getFormattedPrice() функции расширения
                itemPrice.text = itemEntity.getFormattedPrice()
                itemQuantity.text = itemEntity.quantityInStock.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemListItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    /**
    Заменяет старые данные существующей View новыми данными с пом метода bind
    //Получите текущий элемент, используя метод getItem(), передав позицию.
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)
        //слушатель для вызова onItemClicked() элемента в текущей позиции
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    companion object {
        //DiffCallback класс, который вы указали для ListAdapter? Это просто объект,
        //который помогает ListAdapter определить, какие элементы в новом и старом списках отличаются при обновлении списка.
        private val DiffCallback = object : DiffUtil.ItemCallback<ItemEntity>() {

            override fun areItemsTheSame(
                oldItemEntity: ItemEntity,
                newItemEntity: ItemEntity
            ): Boolean {
                return oldItemEntity == newItemEntity
            }

            //проверка по названию продукта
            override fun areContentsTheSame(
                oldItemEntity: ItemEntity,
                newItemEntity: ItemEntity
            ): Boolean {
                return oldItemEntity.itemName == newItemEntity.itemName
            }
        }
    }
}
