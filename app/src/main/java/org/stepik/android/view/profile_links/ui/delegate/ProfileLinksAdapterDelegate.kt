package org.stepik.android.view.profile_links.ui.delegate

import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.model.SocialProfile
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class ProfileLinksAdapterDelegate(
    private val onItemClick: (String) -> Unit
) : AdapterDelegate<SocialProfile, DelegateViewHolder<SocialProfile>>() {
    override fun isForViewType(position: Int, data: SocialProfile): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SocialProfile> =
        ViewHolder(createView(parent, R.layout.profile_link_item))

    private inner class ViewHolder(
        root: View
    ) : DelegateViewHolder<SocialProfile>(root) {

        private val profileLinkTextView = root as AppCompatTextView

        init {
            root.setOnClickListener { onItemClick(itemData?.url ?: "") }
        }

        override fun onBind(data: SocialProfile) {
            profileLinkTextView.movementMethod = LinkMovementMethod.getInstance()
            profileLinkTextView.text = data.name
            profileLinkTextView.setCompoundDrawables(start = resolveProfileLinkDrawable(data.provider))
        }
    }

    private fun resolveProfileLinkDrawable(provider: String): Int =
        when (provider) {
            "facebook" ->
                R.drawable.ic_profile_fb
            "instagram" ->
                R.drawable.ic_profile_instagram
            "twitter" ->
                R.drawable.ic_profile_twitter
            "vk" ->
                R.drawable.ic_profile_vk
            else ->
                R.drawable.ic_profile_web
        }
}