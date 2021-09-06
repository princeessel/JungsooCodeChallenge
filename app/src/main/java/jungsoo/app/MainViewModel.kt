package jungsoo.app

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jungsoo.app.repo.LocalRepo
import jungsoo.app.repo.models.Item
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.ParseException

class MainViewModel: ViewModel() {
    private lateinit var items: List<Item>

    private val cart = mutableListOf<Item>()

    private val _cartLiveData = MutableLiveData<Pair<List<Item>, String>> ()
    val cartLiveData: LiveData<Pair<List<Item>, String>>
        get () = _cartLiveData

    private val _notificationLiveData = MutableLiveData<String> ()
    val notificationLiveData: LiveData<String>
        get () = _notificationLiveData

    fun loadItems (content: Context): LiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>> ()

        viewModelScope.launch {
            items = LocalRepo.getItems(content)
            if (items.isEmpty()) {
                _notificationLiveData.postValue("Item database is empty!")
            }
            _cartLiveData.postValue(Pair(cart, calculateTotal ()))
        }
        return liveData
    }

    fun processScanResult (result: String) {
        val item = searchItemById(result)

        if (item != null) {
            addItemToCart(item)
        } else {
            _notificationLiveData.postValue("Item not found!")
        }
    }

    fun removeItemByIndex (index: Int) {
        if (index > cart.size-1) {
            _notificationLiveData.postValue("Invalid index!")
            return
        }

        cart.removeAt(index)
        _cartLiveData.postValue(Pair(cart, calculateTotal ()))
    }

    private fun searchItemById (id: String): Item? {
        return items.find { item ->
            item.id == id
        }
    }

    private fun addItemToCart (item: Item) {
        val existingItem = cart.find { it.id == item.id }
        if (existingItem != null) {
            existingItem.quantity ++
        } else {
            item.quantity = 1
            cart.add(item)
        }
        _cartLiveData.postValue(Pair(cart, calculateTotal ()))
    }

    private fun calculateTotal (): String {
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        var total: Double = 0.0
        cart.forEach { item ->
            val price = try {
                format.parse(item.price).toDouble()
            } catch (e: ParseException) {
                0.0
            }

            total += price * item.quantity
        }

        return String.format("$%.2f", total)
    }
}