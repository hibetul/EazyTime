package ch.bfh.mad.eazytime.data

import androidx.room.TypeConverter
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime


class DatabaseTypeConverters {

    @TypeConverter
    fun longToDateTime(v: Long?): LocalDateTime? {
        return if(v == null){
            null
        } else {
            LocalDateTime(v)
        }
    }

    @TypeConverter
    fun dateTimeToLong(v: LocalDateTime?): Long? {
        return v?.toDateTime()?.millis
    }

    @TypeConverter
    fun longToDate(v: Long?): LocalDate? {
        return if(v == null){
            null
        } else {
            LocalDate(v)
        }
    }

    @TypeConverter
    fun dateToLong(v: LocalDate?): Long? {
        return v?.toDateTimeAtStartOfDay()?.toDateTime()?.millis
    }
}