package com.novaos.launcher.domain.usecase

import android.content.Context
import android.provider.ContactsContract
import com.novaos.launcher.domain.model.SearchResult
import com.novaos.launcher.domain.model.SearchResultType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchContactsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext emptyList()

        val results = mutableListOf<SearchResult>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")

        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                var count = 0
                while (cursor.moveToNext() && count < 5) {
                    val name = cursor.getString(nameIndex)
                    val number = cursor.getString(numberIndex)
                    val photo = cursor.getString(photoIndex)
                    
                    results.add(SearchResult(
                        id = number,
                        title = name,
                        subtitle = number,
                        type = SearchResultType.CONTACT,
                        iconUri = photo,
                        intentUri = "tel:$number"
                    ))
                    count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        results
    }
}
