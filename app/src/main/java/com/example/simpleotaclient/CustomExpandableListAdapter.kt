package com.example.simpleotaclient

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class CustomExpandableListAdapter(
    private val groupData: List<Map<String, String>>,
    private var childData: List<List<Map<String, String>>>
) : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return groupData.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        // Fix getChildrenCount error: java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        return if (groupPosition >= 0 && groupPosition < childData.size) {
            childData[groupPosition].size
        } else {
            0
        }
    }

    override fun getGroup(groupPosition: Int): Any {
        return groupData[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return childData[groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val inflater = LayoutInflater.from(parent?.context)
        val view = convertView ?: inflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        text1.setTextColor(Color.BLACK)
        text1.setTypeface(null, Typeface.BOLD)
        text1.textSize = 32f
        text1.text = groupData[groupPosition]["groupTitle"]
        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val inflater = LayoutInflater.from(parent?.context)
        val view = convertView ?: inflater.inflate(R.layout.child_item_layout, parent, false)
        val text1 = view.findViewById<TextView>(R.id.text1)
        val text2 = view.findViewById<TextView>(R.id.text2)
        text1.setTextColor(Color.BLUE)
        text1.setTypeface(null, Typeface.BOLD)
        text1.textSize = 24f
        text2.setTextColor(Color.BLACK)
        text2.textSize = 18f
        val childItem = childData[groupPosition][childPosition]
        text1.text = childItem["line1"]
        text2.text = childItem["line2"]

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    fun setChildData(data: List<List<Map<String, String>>>) {
        childData = data
        notifyDataSetChanged()
    }

    fun updateChildData(data: List<List<Map<String, String>>>) {
        childData = data
        notifyDataSetChanged()
    }
}