package fr.vpm.changingtables.models

import kotlinx.serialization.Serializable

@Serializable
data class Review(val id: String, val business_id: String, val user_id: String, val text: String, val rating: Int)