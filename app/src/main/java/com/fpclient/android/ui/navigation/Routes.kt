package com.fpclient.android.ui.navigation

object Routes {
    const val SERVER_SETUP = "server_setup"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY = "verify"
    const val PASSWORD_RESET = "password_reset"
    const val MAIN = "main"
    const val ACTIVITY_DETAIL = "activity/{activityId}"
    const val CREATE = "create"
    const val PROFILE = "profile/{username}"
    const val ME = "me"
    const val EDIT_PROFILE = "edit_profile"
    const val PRIVACY_ZONES = "privacy_zones"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val BATCH_IMPORT = "batch_import"
    const val FOLLOW_LIST = "follow_list/{username}/{type}"

    fun activityDetail(activityId: String) = "activity/$activityId"
    fun profile(username: String) = "profile/$username"
    fun followList(username: String, type: String) = "follow_list/$username/$type"

    /** Bottom navigation destinations shown on the main scaffold. */
    enum class BottomTab(
        val route: String,
        val label: String,
    ) {
        TIMELINE("timeline", "Timeline"),
        SEARCH_TAB("search_tab", "Discover"),
        ANALYTICS("analytics_tab", "Analytics"),
        NOTIFICATIONS("notifications_tab", "Activity"),
        ME_TAB("me_tab", "Me"),
    }
}