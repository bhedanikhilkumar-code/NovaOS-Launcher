package com.novaos.launcher.domain.model

data class SearchResult(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val type: SearchResultType,
    val iconUri: String? = null,
    val intentUri: String? = null
)

enum class SearchResultType {
    APP, CONTACT, WEB_SUGGESTION
}
