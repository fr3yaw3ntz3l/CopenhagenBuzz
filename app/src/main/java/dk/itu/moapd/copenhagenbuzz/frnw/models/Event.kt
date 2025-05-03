/*
 * This file is part of CopenhagenBuzz
 *
 * Copyright (c) 2025 Freya Nørlund Wentzel
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the root of this project for more details.
 */

package dk.itu.moapd.copenhagenbuzz.frnw.models

import dk.itu.moapd.copenhagenbuzz.frnw.utils.EventDateUtil
import java.util.Calendar

/**
 * Represents an event in the CopenhagenBuzz application.
 *
 * This data class holds information about an event, including its name, location,
 * date, type, description, and an optional photo URL.
 *
 * @property eventName The name of the event. This value is immutable.
 * @property eventLocation The location where the event takes place.
 * @property startDate The start date of the event.
 * @property endDate The end date of the event.
 * @property eventType The type of event (e.g., concert, festival).
 * @property eventDescription A description of the event.
 * @property eventPhotoUrl A URL pointing to an image of the event.
 */

data class Event(
    var id: String = "",
    var userId: String = "",
    val eventName: String = "",
    var eventLocation: EventLocation = EventLocation(),
    var eventDate: String = "",
    val eventType: String = "",
    var eventDescription: String = "",
    var eventPhotoUrl: String = "",
    var isFavorite: Boolean = false
) {
    constructor() : this("", "", "", EventLocation(), "", "", "", "", false)

    /**
     * Gets the event's start date as a Calendar object.
     * Uses EventDateUtil to parse the date string.
     *
     * @return Calendar object or null if parsing fails
     */
    fun getStartDateCalendar() = EventDateUtil.parseDate(eventDate)

    /**
     * Checks if this event falls on the specified day within a month.
     *
     * @param day Day of month (1-31)
     * @param month Month (0-11)
     * @param year Year
     * @return true if the event occurs on the specified day
     */
    fun isOnDay(day: Int, month: Int, year: Int): Boolean {
        val calendar = getStartDateCalendar() ?: return false
        return calendar.get(Calendar.DAY_OF_MONTH) == day &&
                calendar.get(Calendar.MONTH) == month &&
                calendar.get(Calendar.YEAR) == year
    }
}