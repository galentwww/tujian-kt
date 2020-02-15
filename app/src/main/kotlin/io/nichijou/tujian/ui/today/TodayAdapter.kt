package io.nichijou.tujian.ui.today

import android.view.*
import androidx.recyclerview.widget.*
import io.nichijou.tujian.BuildConfig
import io.nichijou.tujian.common.entity.*

class TodayAdapter(private val items: List<Picture>) : RecyclerView.Adapter<TodayAdapter.ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(TodayItemView(parent.context).apply {
      layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
    })
  }

  override fun getItemCount(): Int = items.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) = (holder.itemView as TodayItemView).updateUrl(BuildConfig.API_SS + items[position].nativePath)

  class ViewHolder(itemView: TodayItemView) : RecyclerView.ViewHolder(itemView)
}
