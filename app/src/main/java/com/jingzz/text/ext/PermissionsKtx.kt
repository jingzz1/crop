package com.jingzz.text.ext

import android.Manifest.permission.*
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX
import java.util.*

private typealias Granted = (allGranted: Boolean, grantedList: List<String>, deniedList: List<String>) -> Unit

private fun permissions(
    activity: FragmentActivity,
    fragment: Fragment?,
    pers: List<String>,
    request: Granted = { _, _, _ -> },
    granted: () -> Unit = {}
) {
    val appName = activity.applicationInfo.loadLabel(activity.packageManager).toString()
    (if (fragment == null) PermissionX.init(activity) else PermissionX.init(fragment)).permissions(
        pers
    )
        .onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList,
                "您拒绝了${transformText(deniedList).joinToString("、")}等权限，${appName}无法正常运行,是否重新申请权限?",
                "重新申请",
                "取消"
            )
        }.onForwardToSettings { scope, deniedList ->
            val settingMessage =
                "缺少${transformText(deniedList).joinToString("、")}等权限，程序无法正常运行，请在设置界面重新开启权限\n开启权限路径：设置->应用管理->${appName}->权限"
            scope.showForwardToSettingsDialog(deniedList, settingMessage, "去设置", "取消")
        }
        .request { allGranted, grantedList, deniedList ->
            request(allGranted, grantedList, deniedList)
            if (allGranted)
                granted()
        }
}

fun FragmentActivity.permission(
    vararg permissions: String,
    request: Granted = { _, _, _ -> },
    granted: () -> Unit = {}
) = permissions(this, null, permissions.toList(), request, granted)

fun Fragment.permission(
    vararg permissions: String,
    request: Granted = { _, _, _ -> },
    granted: () -> Unit = {}
) = permissions(requireActivity(), this, permissions.toList(), request, granted)

fun Dialog.permission(
    vararg permissions: String,
    request: Granted = { _, _, _ -> },
    granted: () -> Unit = {}
) = (getActivity() as? FragmentActivity)?.permission(
    *permissions,
    request = request,
    granted = granted
) ?: throw RuntimeException("activity 为 null")

fun Dialog.getActivity(): Activity? {
    var context: Context = context
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

private fun transformText(permissions: List<String>): List<String> {
    val textList: MutableList<String> =
        ArrayList()

    fun addMessage(message: String) {
        if (!textList.contains(message))
            textList.add(message)
    }
    for (permission in permissions) {
        when (permission) {
            READ_CALENDAR, WRITE_CALENDAR -> addMessage("访问日历")
            CAMERA -> addMessage("调用摄像头")
            READ_CONTACTS, WRITE_CONTACTS, GET_ACCOUNTS -> addMessage("访问通讯录")
            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION -> addMessage("位置信息")
            RECORD_AUDIO -> addMessage("录制音频")
            READ_PHONE_STATE, CALL_PHONE, READ_CALL_LOG, WRITE_CALL_LOG, USE_SIP, PROCESS_OUTGOING_CALLS -> addMessage(
                "获取设备信息"
            )
            BODY_SENSORS -> addMessage("身体传感器")
            SEND_SMS, RECEIVE_SMS, READ_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS -> addMessage("获取短信列表")
            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE -> addMessage("读写存储空间")
        }
    }
    return textList
}
