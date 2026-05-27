package fr.vpm.changingtables.models

import kotlinx.serialization.Serializable

@Serializable
data class Review(val id: String, val businessId: String, val userId: String, val text: String, val rating: Int)