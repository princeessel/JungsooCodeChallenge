package jungsoo.app.repo

import android.content.Context
import com.google.gson.Gson
import jungsoo.app.repo.models.Item
import jungsoo.app.utils.fromJson
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

object LocalRepo {
    fun getItems (context: Context): List<Item> {
        val json = getJsonFromAssets (context, "items.json")

        return if (json.isNullOrBlank()) {
            emptyList()
        } else {
            Gson().fromJson(json.toString())
        }
    }

    private fun getJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            val `is`: InputStream = context.getAssets().open(fileName)
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}