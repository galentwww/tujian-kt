package io.nichijou.tujian.ui.upload

import android.app.*
import android.net.*
import androidx.lifecycle.*
import io.nichijou.tujian.R
import io.nichijou.tujian.common.*
import io.nichijou.tujian.common.BuildConfig
import io.nichijou.tujian.common.entity.*
import io.nichijou.tujian.common.ext.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.net.*
import java.util.regex.*


class UploadViewModel(application: Application, private val tujianService: TujianService) : AndroidViewModel(application) {
  val msg = MutableLiveData<String>()
  val url = MutableLiveData<String>()
  val result = MutableLiveData<Post>()

  private val okHttpClient = OkHttpClient()

  fun upload(uri: Uri) {
    viewModelScope.launch(Dispatchers.IO) {
      val requestBody = UriRequestBody(getApplication(), uri)
      val contentType = requestBody.contentType()?.toString() ?: "image/jpeg"
      val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
      if (inputStream == null) {
        msg.postValue(getApplication<Application>().getString(R.string.cant_resolve_selected_picture))
        return@launch
      }
      SimpleUpload(URL(BuildConfig.API_UPLOAD))
        .addFilePart("file", inputStream, "upload.$contentType", contentType)
        .upload(object : SimpleUpload.OnFileUploadedListener {
          override fun onFileUploadingSuccess(response: String) {
            logd("resp: $response, $contentType")
            val matcher = Pattern.compile("<h1>MD5:\\s([a-z0-9]*)</h1>").matcher(response)
            if (matcher.find()) {
              val md5 = matcher.group(1)
              if (md5.isNullOrBlank()) {
                msg.postValue(getApplication<Application>().getString(R.string.resolve_upload_result_is_null))
              } else {
                url.postValue(BuildConfig.API_PREFIX + md5)
              }
            } else {
              msg.postValue(getApplication<Application>().getString(R.string.resolve_upload_result_is_null))
            }
          }

          override fun onFileUploadingFailed(responseCode: Int) {
            msg.postValue(getApplication<Application>().getString(R.string.upload_error))
          }
        })
    }
  }

  fun post(upload: Upload, success: (Call, Response) -> Unit, error: (Call, IOException) -> Unit = { _, _ -> }) {
    val url = BuildConfig.API_TG
    val json = "application/json; charset=utf-8".toMediaTypeOrNull()
    val map = mapOf(
      "title" to upload.title,
      "content" to upload.content,
      "url" to upload.url,
      "user" to upload.user,
      "sort" to upload.sort,
      "hz" to upload.sort
    )
    val requestBody = JSONObject(map).toString().toRequestBody(json)
    val request = Request.Builder()
      .url(url)
      .post(requestBody)
      .build()
    val call = okHttpClient.newCall(request)
    call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        error(call, e)
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response: Response) {
        success(call, response)
      }
    })
  }
}
