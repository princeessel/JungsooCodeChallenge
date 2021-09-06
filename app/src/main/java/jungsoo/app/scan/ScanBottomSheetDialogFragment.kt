package jungsoo.app.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jungsoo.app.databinding.ScanBottomSheetDialogFragmentBinding

class ScanBottomSheetDialogFragment: BottomSheetDialogFragment() {

    companion object {
        fun newInstance(callback: ((result: String) -> Unit)):ScanBottomSheetDialogFragment {
            val fragment = ScanBottomSheetDialogFragment()
            fragment.callback = callback
            return fragment
        }
    }

    private lateinit var binding: ScanBottomSheetDialogFragmentBinding
    private lateinit var callback: ((result: String) -> Unit)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScanBottomSheetDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.barcodeView.decodeSingle { result ->
            binding.manualInput.setText(result.text)
            callback(result.text)

            dismiss()
        }

        binding.submitButton.setOnClickListener {
            if (binding.manualInput.text.isNotBlank()) {
                callback(binding.manualInput.text.toString())
            }

            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pause()
    }

    fun show(manager: FragmentManager) {
        super.show(manager, ScanBottomSheetDialogFragment::class.simpleName)
    }
}