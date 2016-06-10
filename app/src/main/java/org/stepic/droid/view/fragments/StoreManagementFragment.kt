package org.stepic.droid.view.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.squareup.otto.Subscribe
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.events.loading.FinishLoadEvent
import org.stepic.droid.events.loading.StartLoadEvent
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.FileUtil
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.StorageUtil
import org.stepic.droid.view.custom.LoadingProgressDialog
import org.stepic.droid.view.dialogs.ChooseStorageDialog
import org.stepic.droid.view.dialogs.ClearVideosDialog

class StoreManagementFragment : FragmentBase() {
    companion object {
        fun newInstance(): Fragment {
            val fragment = StoreManagementFragment()
            return fragment
        }
    }

    lateinit var clearCacheButton: View
    lateinit var clearCacheLabel: TextView
    private var mClearCacheDialogFragment: DialogFragment? = null
    private var loadingProgressDialog: ProgressDialog? = null

    private lateinit var notMountExplanation: View
    private lateinit var mountExplanation: View
    private lateinit var chooseStorageButton: View
    private lateinit var userStorageInfo: TextView

    private var kb: String? = null
    private var mb: String? = null
    private var empty: String? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_space_management, container, false)
        return v
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.let {
            initResStrings()
            initClearCacheFeature(it)
            initAccordingToStoreState(it)
        }
        loadingProgressDialog = LoadingProgressDialog(context)
    }

    private fun initAccordingToStoreState(view: View) {
        notMountExplanation = view.findViewById(R.id.notMountExplanation)
        mountExplanation = view.findViewById(R.id.mountExplanation)
        chooseStorageButton = view.findViewById(R.id.choose_storage_button)
        userStorageInfo = view.findViewById(R.id.user_storage_info) as TextView

        fun hideAllStorageInfo() {
            notMountExplanation.visibility = View.GONE
            mountExplanation.visibility = View.GONE
            chooseStorageButton.visibility = View.GONE
        }

        val storageState = StorageUtil.getSDState(context)
        if (storageState == null) {
            hideAllStorageInfo()
        } else {
            if (storageState == StorageUtil.SDState.sdcardMounted) {
                notMountExplanation.visibility = View.GONE
                mountExplanation.visibility = View.VISIBLE
                chooseStorageButton.visibility = View.VISIBLE
                val chooseStorageDialog = ChooseStorageDialog()
                chooseStorageButton.setOnClickListener {
                    if (!chooseStorageDialog.isAdded) {
                        chooseStorageDialog.show(fragmentManager, null)
                    }
                }
                //TODO: ADD user_storage_info from user prefs IN userStorageInfo!
            } else if (storageState == StorageUtil.SDState.sdCardNotMounted) {
                notMountExplanation.visibility = View.VISIBLE
                mountExplanation.visibility = View.GONE
                chooseStorageButton.visibility = View.GONE
            } else {
                //restricted and not available
                hideAllStorageInfo()
            }

        }
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onDestroyView() {
        clearCacheButton.setOnClickListener(null)
        chooseStorageButton.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun initResStrings() {
        kb = context?.getString(R.string.kb)
        mb = context?.getString(R.string.mb)
        empty = context?.getString(R.string.empty)
    }

    private fun initClearCacheFeature(v: View) {
        clearCacheButton = v.findViewById(R.id.clear_cache_button)
        clearCacheLabel = v.findViewById(R.id.clear_cache_label) as TextView
        mClearCacheDialogFragment = ClearVideosDialog()
        setUpClearCacheButton()
    }

    @Subscribe
    fun onStartLoading(event: StartLoadEvent) {
        ProgressHelper.activate(loadingProgressDialog)
    }

    @Subscribe
    fun onFinishLoading(event: FinishLoadEvent) {
        setUpClearCacheButton()
        ProgressHelper.dismiss(loadingProgressDialog)
    }

    private fun setUpClearCacheButton() {
        clearCacheButton.setOnClickListener {
            YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_CLEAR_CACHE)
            mClearCacheDialogFragment?.show(fragmentManager, null)
        }

        val clearCacheStringBuilder = StringBuilder()
        var size = FileUtil.getFileOrFolderSizeInKb(mUserPreferences.userDownloadFolder)
        size+= FileUtil.getFileOrFolderSizeInKb(mUserPreferences.sdCardDownloadFolder)
        if (size > 0) {
            clearCacheButton.isEnabled = true
            if (size > 1024) {
                size /= 1024
                clearCacheStringBuilder.append(size)
                clearCacheStringBuilder.append(mb)
            } else {
                clearCacheStringBuilder.append(size)
                clearCacheStringBuilder.append(kb)
            }
            clearCacheLabel.text = clearCacheStringBuilder.toString()
        } else {
            clearCacheButton.isEnabled = false
            clearCacheLabel.text = empty
        }

    }


}