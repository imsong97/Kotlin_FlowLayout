package com.yunho.flowlayout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.lang.Integer.max
import java.nio.file.Files.delete

class FlowLayout(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    private var marginVertical = 0
    private var marginHorizontal = 0
    private var spacingLeft = 0
    private var spacingTop = 0
    private var cellBackgroundType = 0

    private var items: ArrayList<String> = ArrayList()
    private var listener: CellClickListener? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.FlowLayout, 0, 0).apply {
            try {
                spacingLeft = this.getDimensionPixelSize(R.styleable.FlowLayout_spacingLeft, 0)
                spacingTop = this.getDimensionPixelSize(R.styleable.FlowLayout_spacingTop, 0)
            } catch (e: Exception) {
                e.printStackTrace()
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var childLeft = paddingLeft
        var childTop = paddingTop
        var lowestBottom = 0
        var lineHeight = 0
        val myWidth = resolveSize(100, widthMeasureSpec)
        var wantedHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            child.measure(
                getChildMeasureSpec(widthMeasureSpec, 0, child.layoutParams.width),
                getChildMeasureSpec(heightMeasureSpec, 0, child.layoutParams.height)
            )
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            lineHeight = max(childHeight, lineHeight)
            if (childWidth + childLeft + paddingRight > myWidth) {
                childLeft = paddingLeft
                childTop = marginVertical + lowestBottom
                lineHeight = childHeight
            }
            childLeft += (childWidth + marginHorizontal)
            if (childHeight + childTop > lowestBottom) {
                lowestBottom = childHeight + childTop
            }
        }

        wantedHeight += (childTop + lineHeight + paddingBottom)
        setMeasuredDimension(myWidth, resolveSize(wantedHeight, heightMeasureSpec))

    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        var childLeft = paddingLeft
        var childTop = paddingTop
        var lowestBottom = 0
        val myWidth = right - left

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight - spacingTop
            if (childWidth + childLeft + paddingRight > myWidth) {
                childLeft = paddingLeft
                childTop = marginVertical + lowestBottom + spacingTop
            }
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
            childLeft += (childWidth + marginHorizontal + spacingLeft)
            if (childHeight + childTop > lowestBottom) {
                lowestBottom = childHeight + childTop
            }
        }
    }

    fun setItems(items: ArrayList<String>) {
        this.items = items
        createChildView()
    }

    fun setClickListener(l: CellClickListener) {
        listener = l
    }

    private fun createChildView() {
        removeAllViews()

        if (items.isEmpty()) return

        items.forEachIndexed { index, item ->
            val layout = LayoutInflater.from(context).inflate(R.layout.flowlayout_childview_cell, null).apply {
                val textView = this.findViewById<TextView>(R.id.item_text)
                val imageView = this.findViewById<ImageView>(R.id.btn_delete)
                textView.text = item

                textView.setOnClickListener {
                    listener?.onItemClick(index)
                }
                imageView.setOnClickListener {
                    listener?.deleteItem(index)
                }
            }

            addView(layout)
        }

    }

    interface CellClickListener {
        fun onItemClick(index: Int)
        fun deleteItem(index: Int)
    }
}