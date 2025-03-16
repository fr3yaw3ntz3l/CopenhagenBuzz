/*
 * This file is part of CopenhagenBuzz
 *
 * Copyright (c) 2025 Freya Nørlund Wentzel
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the root of this project for more details.
 */

package dk.itu.moapd.copenhagenbuzz.frnw.models

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
    val eventName: String,
    var eventLocation: String,
    var eventDate: String,
    val eventType: String,
    var eventDescription: String,
    var eventPhotoUrl: String,
    var isFavorite: Boolean = false
)