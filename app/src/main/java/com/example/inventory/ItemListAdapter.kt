package com.example.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ItemEntity
import com.example.inventory.data.getFormattedPrice
import com.example.inventory.databinding.ItemListItemBinding

//см 5.1.2.8
//Функция onItemClicked будет использоваться для навигации, когда мы нажимаем на элемент списка на первом экране,
class ItemListAdapter(
    private val onItemClicked: (ItemEntity) -> Unit
) : ListAdapter<ItemEntity, ItemListAdapter.ItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewType, parent, false)

        return ItemViewHolder(view)
    }

    /**
    Заменяет старые данные существующей View новыми данными с пом метода bind
    Получите текущий элемент, используя метод getItem(), передав позицию.
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //getItem возвращает данные Entity определенного элемента из таблицы на основе заданного id
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
        holder.bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_list_item
    }

    //DiffCallback помогает адаптеру определить, какие элементы в новом и старом списках отличаются при обновлении
    object DiffCallback : DiffUtil.ItemCallback<ItemEntity>() {

        override fun areItemsTheSame(oldItemEntity: ItemEntity, newItemEntity: ItemEntity): Boolean {
            return oldItemEntity == newItemEntity
        }

        //проверка по названию продукта
        override fun areContentsTheSame(oldItemEntity: ItemEntity, newItemEntity: ItemEntity): Boolean {
            return oldItemEntity.itemName == newItemEntity.itemName
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ItemListItemBinding.bind(view)

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
}
