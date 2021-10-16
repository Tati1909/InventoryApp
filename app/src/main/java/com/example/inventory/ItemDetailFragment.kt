/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.inventory.data.Item
import com.example.inventory.data.getFormattedPrice
import com.example.inventory.databinding.FragmentItemDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * [ItemDetailFragment] displays the details of the selected item.
 */
class ItemDetailFragment : Fragment() {

    private val navigationArgs: ItemDetailFragmentArgs by navArgs()

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!

    //Вы будете использовать это свойство для хранения информации об одной Entity
    lateinit var item: Item

    //Используйте by делегат, чтобы передать инициализацию свойства классу activityViewModels
    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database.itemDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Это будет наш !!!!!аргумент в навигации к ItemDetailFragment из ItemListFragment
        //(этот аргумент мы просто будем передавать в ItemDetailFragment -
        // мы будем использовать эту id переменную для получения сведений об элементе)
        val id = navigationArgs.itemId

        //Присоединяем наблюдателя к возвращаемому значению Item(Entity) из метода retrieveItem
        viewModel.retrieveItem(id)
            .observe(this@ItemDetailFragment.viewLifecycleOwner) { selectedItem ->
                item = selectedItem
                bind(item)
                //Внутри лямбды передаем selectedItem в качестве параметра, который содержит Item объект,
                //полученный из базы данных. В теле лямбда-функции присвоим selectedItem значение item.
            }
    }

    /**
    Этот метод отображает диалоговое окно с предупреждением, чтобы получить подтверждение пользователя
    перед удалением элемента, и вызывает deleteItem() функцию при нажатии положительной кнопки
     */
    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteItem()
            }
            .show()
    }

    /**
    метод для вызова функции удаления продукта
    и обработки навигации.
     */
    private fun deleteItem() {
        viewModel.deleteItem(item)
        findNavController().navigateUp()
    }

    /**
    метод для вызова функции редактирования продукта
    и обработки навигации.
     */
    private fun editItem() {
        //Переходим на AddItemFragment, передавая параметры нового заголовка('Редактировать') и id Entity
        //в AddItemFragment. Т. е. AddItemFragment используем повторно, только меняем заголовок.
        //Саму кнопку слушаем в методе bind()
        val action = ItemDetailFragmentDirections.actionItemDetailFragmentToAddItemFragment(
            getString(R.string.edit_fragment_title),
            item.id
        )
        this.findNavController().navigate(action)
    }

    /**
     * Called when fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //функция для установки названия продукта, цены и количества на складе
    //такая же функция есть в ItemListAdapter
    //И слушатель для кнопок
    private fun bind(item: Item) {
        binding.apply {
            itemName.text = item.itemName
            itemPrice.text = item.getFormattedPrice()
            itemCount.text = item.quantityInStock.toString()
            //Отключите кнопку Продать , если количество равно нулю
            sellButton.isEnabled = viewModel.isStockAvailable(item)
            //слушатель на кнопку продать
            sellButton.setOnClickListener { viewModel.sellItem(item) }
            //слушатель на кнопку удалить
            deleteButton.setOnClickListener { showConfirmationDialog() }
            //слушатель на кнопку редактировать
            //переходим к экрану редактирования
            editButton.setOnClickListener { editItem() }
        }
    }
}
