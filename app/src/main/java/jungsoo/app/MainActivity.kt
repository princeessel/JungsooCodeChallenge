package jungsoo.app

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.zxing.integration.android.IntentIntegrator
import jungsoo.app.databinding.ActivityMainBinding
import jungsoo.app.databinding.ItemViewCartBinding
import jungsoo.app.repo.models.Item
import jungsoo.app.scan.ScanBottomSheetDialogFragment

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
    }

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener { view ->
            showScanUi()
        }

        loadItems ()
        setupUi ()
        setupObservers ()
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showScanUi ()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this@MainActivity, "Please accept camera permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupUi () {
        adapter = Adapter { position, delete ->
            if (delete) {
                viewModel.removeItemByIndex(position)
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager (this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers () {
        viewModel.cartLiveData.observe(this) {
            adapter.updateItems(it.first)
            binding.totalValue.text = it.second
        }

        viewModel.notificationLiveData.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
    }

    private fun loadItems () {
        viewModel.loadItems(this).observe(this) {
            //no-op
        }
    }

    private fun showScanUi () {
        if (checkPermission()) {
            //IntentIntegrator(this).setOrientationLocked(false).initiateScan()
            ScanBottomSheetDialogFragment.newInstance { result ->
                onScan(result)
            }.show(supportFragmentManager)
        } else {
            requestPermission()
        }
    }

    private fun onScan (result: String) {
        viewModel.processScanResult(result)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private class Adapter (val callback: (position: Int, delete: Boolean) -> Unit): RecyclerView.Adapter<ViewHolder> () {
        private val items = mutableListOf<Item>()

        fun updateItems (items: List<Item>) {
            this.items.clear()
            this.items.addAll(items)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemViewCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.thumbnail.load(item.thumbnail)
            holder.binding.name.text = item.name
            holder.binding.quantity.text = "Qty: ${item.quantity}"
            holder.binding.price.text = item.price
            holder.binding.delete.setOnClickListener {
                callback (position, true)
            }
        }

        override fun getItemCount() = items.size
    }

    private class ViewHolder (val binding: ItemViewCartBinding): RecyclerView.ViewHolder (binding.root)
}