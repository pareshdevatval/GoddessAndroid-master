package com.krystal.goddesslifestyle.custom_views

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.CalenderDaysGridAdapter
import com.krystal.goddesslifestyle.data.model.CalenderDay
import com.krystal.goddesslifestyle.databinding.CalenderViewLayoutBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import kotlin.math.floor


/**
 * Created by Bhargav Thanki on 21 February,2020.
 * Custom calenderView class
 *
 * Features
 * -- Not showing in-dates and out-dates
 * -- lable under Date with 2 or 3 lines(based on no.of days in month)
 * -- Expand and collapse animation (For switching between monthly and weekly mode of calender)
 *
 */
class GoddessCalenderView : LinearLayout {

    /*layout var for binding*/
    lateinit var binding: CalenderViewLayoutBinding

    /*Dates of the curret month*/
    lateinit var originalDates: ArrayList<CalenderDay>

    /*Events listener interface, to implement in hosting screen*/
    lateinit var listener: CalenderEventListener

    /*a variable indicating calenderView is expanded or collapsed*/
    var isCollapsed = false

    var selectedDate = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        /*Getting inflater object*/
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        /*layout binding for custom calenderView*/
        binding = CalenderViewLayoutBinding.inflate(inflater as LayoutInflater, this, true)

        /*[START] Grid layout for header to show day names in EEE format*/
        val weekDaysLayoutManager = GridLayoutManager(context, 7)
        binding.daysGrid.layoutManager = weekDaysLayoutManager

        /*We have stored */
        val weekDaysArray = resources.getStringArray(R.array.week_days)
        /*converting an array to arrayList*/
        val weekDaysList: ArrayList<String> = ArrayList()
        for (day in weekDaysArray) {
            weekDaysList.add(day)
        }

        val adapter = CalenderDaysGridAdapter()
        adapter.setItem(weekDaysList)
        binding.daysGrid.adapter = adapter

        val layoutTransition = binding.llCalender.layoutTransition
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        /*[END] Grid layout for header to show day names in EEE format*/
    }

    fun initCalender(list: ArrayList<CalenderDay>, height: Int) {
        binding.daysGrid.post {
            val margin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10f,
                context!!.resources.displayMetrics
            )
            var newHeight = height - binding.daysGrid.height - margin.toInt()
            initCalenderByLazy(list, newHeight)
        }

    }

    var rowLayout: LinearLayout? = null
    fun initCalenderByLazy(list: ArrayList<CalenderDay>, height: Int) {
        //val height = height - binding.daysGrid.height
        //val height = binding.llCalender.height
        originalDates = list

        // binding.llCalender.setBackgroundColor(ContextCompat.getColor(context, R.color.black))

        /*[START] Code to show an actual calender*/
        /*We have taken a variable for the rowNo and will set it as a tag to the date
        * So, when a date clicks, We can detect which row is selected*/
        var rowNo = 0
        var totalRowCount = 0
        var totalChildCount = 0
        var currentChildNo = 0
        for (i in 0 until list.size) {

            /*One row will contain 1 week data and hence 7 days
            * So we will create a new row each times when week changes*/
            if (i == 0 || i % AppConstants.MAX_DAYS_IN_WEEK == 0) {
                rowNo += 1

                /*Dynamic linear layout for the week row*/
                rowLayout = LinearLayout(context)
                currentChildNo = 0
                //val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                if (list.size > 35) {
                    val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, height / 6)
                    rowLayout?.layoutParams = layoutParams
                    totalChildCount = 6
                } else {
                    val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, height / 5)
                    rowLayout?.layoutParams = layoutParams
                    totalChildCount = 5
                }
                totalRowCount =
                    floor((list.size / AppConstants.MAX_DAYS_IN_WEEK).toDouble()).toInt()
                //layoutParams.weight = 1F
                // orientation set to HORIZONTAL
                rowLayout?.orientation = HORIZONTAL
                rowLayout!!.weightSum = 7f
                // adding row to the main LL
                binding.llCalender.addView(rowLayout)
            }

            /*Original date cell view*/
            val cellView =
                LayoutInflater.from(context).inflate(R.layout.calender_date_grid_row, null)
            val ll_root = cellView.findViewById<LinearLayout>(R.id.ll_root)
            val tvDate = cellView.findViewById<TextView>(R.id.tvDate)
            val tvDesc = cellView.findViewById<TextView>(R.id.tvDesc)

            /*We will show a desc of 2 lines if week is 6 otherwise 3*/
            //tvDesc.gravity = Gravity.TOP
            if (list.size > 35) {
                tvDesc.setLines(2)
                tvDesc.maxLines = 2
            } else {
                tvDesc.setLines(3)
                tvDesc.maxLines = 3
            }

            /*We have added in-dates and out-dates item with date 0
            * So, when date != 0, at that time only, we will show a date in calender*/
            if (list[i].date != 0) {
                tvDate.text = list[i].date.toString()
                tvDesc.text = list[i].desc
                /*set tag as row no to date tv*/
                tvDate.tag = rowNo
                ll_root.setBackgroundResource(R.drawable.white_outline)

                /* if (rowNo < totalRowCount) {
                     if (rowNo == 1) {
                         if (currentChildNo < totalChildCount) {
                             ll_root.setBackgroundResource(R.drawable.no_right_bottom_two_sided_stroke)
                         } else {
                             ll_root.setBackgroundResource(R.drawable.no_bottom_three_sided_stroke)
                         }
                     } else {
                         if (currentChildNo < 6) {
                             ll_root.setBackgroundResource(R.drawable.no_right_bottom_two_sided_stroke)
                         } else {
                             ll_root.setBackgroundResource(R.drawable.no_bottom_three_sided_stroke)
                         }
                     }
                 } else {
                     if (list[i + 1].date != 0) {
                         ll_root.setBackgroundResource(R.drawable.no_right_three_sided_stroke)
                     } else {
                         ll_root.setBackgroundResource(R.drawable.white_outline)
                     }
                 }*/

                currentChildNo++
            } else {
                /* if (rowNo == totalRowCount) {
                     ll_root.setBackgroundResource(R.drawable.onlu_top_sided_stroke)
                 } else {*/
                ll_root.setBackgroundResource(0)
                //}
            }

            cellView.setOnClickListener { tvDate.performClick() }

            /*Date click listener*/
            tvDate.setOnClickListener {

                if(tvDate.text.isEmpty()) {
                    return@setOnClickListener
                }
                // We will only select the dates, that are actually a date of the month
                // and not the dummy dates those we eneterd with 0 value for adjusting grid
                if (tvDate.text.toString().isNotBlank()) {
                    /*Clear any previous date selection.*/
                    clearSelection()
                    /*setting text color pink*/
                    tvDate?.setTextColor(ContextCompat.getColor(context, R.color.pink))
                    tvDesc?.setTextColor(ContextCompat.getColor(context, R.color.pink))
                    /*and background to white*/
                    ll_root?.setBackgroundResource(R.drawable.white_solid)
                }
                /*We will collapse the calender, only if it is in expanded state, otherwise not*/
                if (!isCollapsed) {
                    // collapse the calender
                    tvDate?.tag?.let {
                        if (!tvDate.text.toString().isBlank()) {
                            collapse(it as Int, tvDate.text.toString().toInt())
                            selectedDate = tvDate.text.toString().toInt()
                        } else {
                            collapse(it as Int, 0)
                        }
                    }
                } else {
                    // calling interface method. So, control can go to fragment
                    if (!tvDate.text.toString().isBlank()) {
                        if (tvDate.text.toString().toInt() != selectedDate) {
                            listener.onDateSelected(tvDate.text.toString().toInt())
                            selectedDate = tvDate.text.toString().toInt()
                        }
                    } else {
                        //listener.onDateSelected(0)
                    }
                }
            }

            val cellLayoutParams =
                LayoutParams(
                    0, LayoutParams.MATCH_PARENT
                )
            // setting weight to 1 for every grid cell
            cellLayoutParams.weight = 1f
            cellView.layoutParams = cellLayoutParams
            rowLayout?.addView(cellView)
        }
        /*[END] Code to show an actual calender*/

        binding.llExpandCollapse.setOnClickListener { expand() }
    }

    // collapsing the calender
    private fun collapse(tag: Int, selectedDate: Int) {
        // We will show only 1 week which contains selected date

        // getting total row count
        val rowCount = binding.llCalender.childCount
        for (i in 0 until rowCount) {
            // We will hide all other weeks except current selected week
            if (i != (tag - 1)) {
                binding.llCalender.getChildAt(i).visibility = View.GONE
            }
        }
        // setting bool to true
        isCollapsed = true
        (binding.llExpandCollapse).visibility = View.INVISIBLE

        // calling interface method. So, control can go to fragment
        android.os.Handler().postDelayed(Runnable {
            listener.onDateSelected(selectedDate)
        }, 100)

        android.os.Handler().postDelayed(Runnable {
            // showing expand arrow
            (binding.llExpandCollapse).visibility = View.VISIBLE
        }, 700)

    }

    /*Expanding the original calender*/
    private fun expand() {
        android.os.Handler().postDelayed(Runnable {
            /* getting all the rows. And set them visible*/
            val rowCount = binding.llCalender.childCount
            for (i in 0 until rowCount) {
                binding.llCalender.getChildAt(i).visibility = View.VISIBLE

            }
            // setting boolean to false
            isCollapsed = false
            // hiding down arrow
            (binding.llExpandCollapse).visibility = View.GONE
        }, 100)

        // listener method
        listener.onExpanded()
    }

    /*Clearing previous selected date*/
    private fun clearSelection() {
        // getting no. of rows(weeks)
        val childCount = binding.llCalender.childCount
        for (i in 0 until childCount) {
            val row = binding.llCalender.getChildAt(i) as LinearLayout
            // all the dates inside par. week
            val cellCount = row.childCount
            for (j in 0 until cellCount) {
                val cell = row.getChildAt(j)

                val ll_root = cell.findViewById<LinearLayout>(R.id.ll_root)
                val tvDate = cell.findViewById<TextView>(R.id.tvDate)
                val tvDesc = cell.findViewById<TextView>(R.id.tvDesc)

                if (!tvDate.text.toString().isBlank()) {
                    /*white text color*/
                    tvDate?.setTextColor(ContextCompat.getColor(context, R.color.white))
                    tvDesc?.setTextColor(ContextCompat.getColor(context, R.color.white))
                    /*Transparent background*/
                    ll_root?.setBackgroundResource(R.drawable.white_outline)
                }
            }
        }
    }

    private fun getRotateDrawable(d: Drawable, angle: Float): Drawable? {
        val arD: Array<Drawable> = arrayOf<Drawable>(d)
        return object : LayerDrawable(arD) {
            override fun draw(canvas: Canvas) {
                canvas.save()
                canvas.rotate(
                    angle, (d.getBounds().width() / 2).toFloat(),
                    (d.getBounds().height() / 2).toFloat()
                )
                super.draw(canvas)
                canvas.restore()
            }
        }
    }

    /*An interface for Calender events*/
    interface CalenderEventListener {
        /*An event, when par. date is selected by a user*/
        fun onDateSelected(selectedDate: Int)

        /*When calender again expanded from collapsed state*/
        fun onExpanded()
    }
}