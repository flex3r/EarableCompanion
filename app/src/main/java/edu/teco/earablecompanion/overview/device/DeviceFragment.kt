package edu.teco.earablecompanion.overview.device

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R

abstract class DeviceFragment : Fragment() {

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.device_menu, menu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.remove_device -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.remove_device_dialog_title))
                .setPositiveButton(getString(R.string.remove)) { _, _ -> disconnectDevice() }
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                .show()
            else -> return false
        }
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as? MainActivity)?.bottomNavigationVisible = false
    }

    override fun onDetach() {
        (activity as? MainActivity)?.bottomNavigationVisible = true
        super.onDetach()
    }

    protected abstract fun disconnectDevice()
}