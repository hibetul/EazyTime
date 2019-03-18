package ch.bfh.mad.eazytime.calendar.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ch.bfh.mad.eazytime.R
import ch.bfh.mad.eazytime.di.Injector
import ch.bfh.mad.eazytime.util.CalendarUtil
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.inject.Inject


class CalendarDetailActivity: AppCompatActivity() {

    private lateinit var calendarDetailViewModel: CalendarDetailViewModel
    private lateinit var calendarDetailListView: ListView

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_detail)
        Injector.appComponent.inject(this)

        calendarDetailViewModel = ViewModelProviders.of(this, viewModelFactory).get(CalendarDetailViewModel::class.java)
        calendarDetailListView = findViewById(R.id.lv_calendar_detail)

        val workdayId = intent.extras?.getLong(INTENT_EXTRA_KEY_WORKDAY_ID)
        workdayId?.let {id  ->
            val calendarDetails = calendarDetailViewModel.getCalendarDetail(id)
            calendarDetails.first().workDayDate?.let { date ->
                val locale = Locale(getString(R.string.language))
                val pattern = DateTimeFormat.forPattern(getString(R.string.date_pattern)).withLocale(locale)
                title = pattern.print(date)
            }

            findViewById<TextView>(R.id.calendar_detail_total_hours_label).text = getFormattedTotalWorkHours(id)

            val calendarListAdapter = CalendarDetailListAdapter(this, android.R.layout.simple_list_item_1)
            calendarDetailListView.adapter = calendarListAdapter
            calendarListAdapter.addAll(calendarDetails)
        }

    }

    private fun getFormattedTotalWorkHours(workDayId: Long): String{
        val timeSlots = calendarDetailViewModel.getTimeSlotList(workDayId)
        val periodOfTotalWorkHours = CalendarUtil.getPeriodOfTotalWorkHours(timeSlots)
        val totalWorkHoursPattern = getString(R.string.total_workhours_pattern)
        val hoursAndMinutesFormatter = CalendarUtil.getHoursAndMinutesFormatter()
        return String.format(totalWorkHoursPattern, hoursAndMinutesFormatter.print(periodOfTotalWorkHours.normalizedStandard()))
    }

    companion object {

        private const val INTENT_EXTRA_KEY_WORKDAY_ID = "Constants"

        fun getIntent(ctx: Context, workDayId: Long): Intent {
            return Intent(ctx, CalendarDetailActivity::class.java).also { intent ->
                intent.putExtra(INTENT_EXTRA_KEY_WORKDAY_ID, workDayId)
            }
        }
    }
}