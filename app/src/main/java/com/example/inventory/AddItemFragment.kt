package com.example.inventory

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.inventory.data.ItemEntity
import com.example.inventory.databinding.FragmentAddItemBinding

/**
 * Fragment to add or update an item in the Inventory database.
 */
class AddItemFragment : Fragment() {

    private val navigationArgs: ItemDetailFragmentArgs by navArgs()
    lateinit var itemEntity: ItemEntity

    // Привязка экземпляра объекта, соответствующего макету fragment_add_item.xml
    // Это свойство не равно нулю между обратными вызовами жизненного цикла onCreateView () и onDestroyView (),
    // когда к фрагменту прикреплена иерархия представления
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    //Внутри лямбда вызовите InventoryViewModelFactory()конструктор и передайте ItemDao экземпляр.
    //Используйте database экземпляр, который вы создали в одной из предыдущих задач, для вызова itemDao конструктора.
    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database
                .itemDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    //Возвращает true, если EditTexts не пустые
    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.itemName.text.toString(),
            binding.itemPrice.text.toString(),
            binding.itemCount.text.toString()
        )
    }

    //Вставляет новый элемент в базу данных и переходит к фрагменту списка.
    private fun addNewItem() {
        if (isEntryValid()) {
            viewModel.addNewItem(
                binding.itemName.text.toString(),
                binding.itemPrice.text.toString(),
                binding.itemCount.text.toString(),
            )
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            //импортируем import androidx.navigation.fragment.findNavController
            findNavController().navigate(action)
        }
    }

    //Эту функцию вызываем после нажатия кнопки 'сохранить', когда редактировали продукт.
    //if условие для проверки ввода пользователя(если текстовые поля заполнены,
    //то обновляем наши новые данные с помощью DAO метода suspend fun update(item: Item))
    //Используем itemId из аргументов навигации для передачи его в AddItemFragment(!но фрагмент редактирования)
    private fun updateItem() {
        if (isEntryValid()) {
            viewModel.updateItem(
                this@AddItemFragment.navigationArgs.itemId,
                this@AddItemFragment.binding.itemName.text.toString(),
                this@AddItemFragment.binding.itemPrice.text.toString(),
                this@AddItemFragment.binding.itemCount.text.toString()
            )
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }


    //Вызывается при создании View.
    //      * Аргумент itemId Navigation определяет элемент редактирования или добавление нового элемента.
    //      * Если itemId положительный, этот метод извлекает информацию из базы данных и
    //      * позволяет пользователю обновлять его.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //получаем  itemId из аргументов навигации(ложили в ItemDetailFragment в методе editItem)
        val id = navigationArgs.itemId

        //Добавьте if-else блок с условием, чтобы проверить, id больше ли нуля,
        //и переместите прослушиватель нажатия кнопки « Сохранить» в else блок.
        //Внутри if блока извлеките объект с помощью id и добавьте к нему наблюдателя.
        //Внутри наблюдателя получите выбранный продукт(item) и вызовите bind() передавая данные этого продукта в текстовые поля фрагмента.
        //Полная функция предназначена для копирования и вставки. Это просто и легко понять;
        if (id > 0) {
            viewModel.retrieveItem(id).observe(this.viewLifecycleOwner) { selectedItem ->
                itemEntity = selectedItem
                bind(itemEntity)
            }
        } else {
            //кнопка сохранить при добавлении нового продукта
            binding.saveButton.setOnClickListener {
                addNewItem()
            }
        }
    }

    /**
     * Called before fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // функция скрывает клавиатуру перед разрушением фрагмента.
        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }

    //эта функция нужна для РЕДАКТИРОВАНИЯ заметки.
    //функция для привязки текстовых полей c деталями Entity
    //Реализация bind()функции очень похожа на то, что вы делали ранее в ItemDetailFragment
    private fun bind(itemEntity: ItemEntity) {
        //округлите цену до двух десятичных знаков с помощью format()функции
        val price = "%.2f".format(itemEntity.itemPrice)
        binding.apply {
            itemName.setText(itemEntity.itemName, TextView.BufferType.SPANNABLE)
            itemPrice.setText(price, TextView.BufferType.SPANNABLE)
            //не забудьте преобразовать item.quantityInStock в String
            itemCount.setText(itemEntity.quantityInStock.toString(), TextView.BufferType.SPANNABLE)
            //обработка кнопки сохранить после ее редактирования
            saveButton.setOnClickListener { updateItem() }
        }
    }
}
