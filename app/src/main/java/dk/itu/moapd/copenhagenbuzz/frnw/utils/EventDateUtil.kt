package dk.itu.moapd.copenhagenbuzz.frnw.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for handling event date operations.
 * Provides methods to parse, format, and manipulate event dates.
 */
class EventDateUtil {
    companion object {
        private const val TAG = "EventDateUtil"

        /**
         * Parses a date string into a Calendar object.
         * Handles multiple date formats including ranges like "July 5-14, 2025".
         *
         * @param dateString The date string to parse
         * @return A Calendar object or null if parsing fails
         */
        fun parseDate(dateString: String): Calendar? {
            return try {
                // Handle date ranges (e.g., "July 5-14, 2025")
                if (dateString.contains("-")) {
                    val parts = dateString.split("-")
                    val startDatePart = parts[0].trim()

                    // Check if this is a day range in the same month (e.g., "July 5-14, 2025")
                    if (!startDatePart.contains(",")) {
                        // Month and year are in the second part
                        val monthAndYearPart = parts[1].trim()
                        if (monthAndYearPart.contains(",")) {
                            val monthName = startDatePart
                            val dayStr = parts[0].trim().replace(monthName, "").trim()
                            val fullStartDate = "$monthName $dayStr, ${monthAndYearPart.substringAfter(",").trim()}"
                            return parseSimpleDate(fullStartDate)
                        }
                    }

                    // Full date format in first part (e.g., "July 5, 2025")
                    return parseSimpleDate(startDatePart)
                }

                // Try standard date formats
                parseSimpleDate(dateString)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing date: $dateString", e)
                null
            }
        }

        /**
         * Attempts to parse a date string using multiple common formats.
         *
         * @param dateString The date string to parse
         * @return A Calendar object or null if parsing fails
         */
        private fun parseSimpleDate(dateString: String): Calendar? {
            val possibleFormats = arrayOf(
                "MMMM d, yyyy", // July 5, 2025
                "MMMM dd, yyyy", // July 05, 2025
                "MM/dd/yyyy",    // 07/05/2025
                "dd/MM/yyyy",    // 05/07/2025
                "yyyy-MM-dd",    // 2025-07-05
                "MMMM yyyy",     // July 2025
                "MMM d, yyyy",   // Jul 5, 2025
                "d MMMM yyyy"    // 5 July 2025
            )

            for (format in possibleFormats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                    sdf.isLenient = false
                    val date = sdf.parse(dateString)
                    if (date != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        return calendar
                    }
                } catch (e: Exception) {
                    // Try next format
                }
            }

            return null
        }

        /**
         * Formats a Calendar date as a string in the specified format.
         *
         * @param calendar The Calendar to format
         * @param format The format pattern (defaults to "MMMM d, yyyy")
         * @return Formatted date string
         */
        fun formatDate(calendar: Calendar, format: String = "MMMM d, yyyy"): String {
            val sdf = SimpleDateFormat(format, Locale.ENGLISH)
            return sdf.format(calendar.time)
        }

        /**
         * Checks if a date falls within the given month and year.
         *
         * @param date The date to check
         * @param month The month (0-11)
         * @param year The year
         * @return true if the date is in the given month and year
         */
        fun isInMonth(date: Calendar, month: Int, year: Int): Boolean {
            return date.get(Calendar.MONTH) == month &&
                    date.get(Calendar.YEAR) == year
        }

        /**
         * Gets the day of month from a Calendar.
         *
         * @param date The Calendar to extract from
         * @return The day of month (1-31)
         */
        fun getDayOfMonth(date: Calendar): Int {
            return date.get(Calendar.DAY_OF_MONTH)
        }
    }
}