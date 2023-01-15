package com.lu.mask.donate

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.lu.magic.util.CursorUtil
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.permission.PermissionUtil
import org.json.JSONObject
import java.io.File


class DonateModel {

    @TargetApi(Build.VERSION_CODES.Q)
    private fun savePayImgForQ(context: Context, payImgResId: Int, fileName: String) {
        //mediaStore数据库返回的相对路径最后可能带有/，比较是否相等时需要注意
        val relativePath = "${Environment.DIRECTORY_DCIM}/${context.packageName}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }
        // MediaStore定义了一系列的uri常量，但有些文件比较特殊，不在定义中，此时可使用MediaStore.Files.getContentUri来查询
        // MediaStore.Downloads.INTERNAL_CONTENT_URI、MediaStore.Downloads.EXTERNAL_CONTENT_URI
        // MediaStore.Files.getContentUri("external")
        // val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val resolver = context.contentResolver
        val cursor = resolver.query(
            queryUri,
            //id和名称
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH
            ),
            //条件：显示名称和相对路径
            "${MediaStore.Images.Media.DISPLAY_NAME} =? AND (${MediaStore.Images.Media.RELATIVE_PATH} =? OR ${MediaStore.Images.Media.RELATIVE_PATH} =?)",
            arrayOf(fileName, relativePath, "$relativePath/"),
            null
        )
        val queryResult = cursor.use {
            CursorUtil.getAll(it)
        }

        if (queryResult.isNotEmpty()) {
            //已经保存过，更新没毛用，删除吧
            //这两属性不许更新
            //contentValues.remove(MediaStore.MediaColumns.DISPLAY_NAME)
            //contentValues.remove(MediaStore.MediaColumns.RELATIVE_PATH)
            val _id = queryResult[0][MediaStore.Images.Media._ID].toString()
            val rowCount = resolver.delete(queryUri, "${MediaStore.Images.Media._ID}=?", arrayOf(_id))
            if (rowCount == 0) {
                LogUtil.w("MediaStore删除失败！！")
            }
        }
        val imageUri = resolver.insert(queryUri, contentValues)
        imageUri?.let {
            val payImgByteArray = context.resources.openRawResource(payImgResId).readBytes()
            resolver.openOutputStream(it, "w").use { out ->
                out?.write(payImgByteArray)
            }
            contentValues.clear()
            resolver.update(it, contentValues, null, null)
        }

    }

    fun savePayImg(context: Context, payImgResId: Int, fileName: String, callBack: (Result<Boolean>) -> Unit) {
        Result.success(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ExecutorUtil.runOnIo {
                runCatching {
                    savePayImgForQ(context, payImgResId, fileName)
                }.onFailure {
                    callBack.invoke(Result.failure(it))
                }.onSuccess {
                    callBack.invoke(Result.success(true))
                }
            }
        } else {
            PermissionUtil.permission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onResult { _, result, grant ->
                    if (grant) {
                        ExecutorUtil.runOnIo {
                            runCatching {
                                val file =
                                    File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/magic")
                                val payImgByteArray = context.resources.openRawResource(payImgResId).readBytes()
                                file.writeBytes(payImgByteArray)
                            }.onFailure {
                                callBack.invoke(Result.failure(it))
                            }.onSuccess {
                                callBack.invoke(Result.success(true))
                            }
                        }
                    } else {
                        PermissionRejectException(JSONObject(result).toString()).let {
                            callBack.invoke(Result.failure(it))
                        }
                    }
                }
                .call()
        }

    }

}