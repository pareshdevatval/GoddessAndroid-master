package com.krystal.goddesslifestyle.utils

import android.os.Environment


object AppConstants {

    const val IMAGE_LIST: String = "imageList"
    const val Images_URL: String = "imageUrl"
    const val ZOOM_IMAGE_URL: String = "ZoomImageUrl"
    const val SET_ADDRESS_REQUEST_CODE = 15

    const val MONTHLY_SKU = "monthly_subscription"
    const val YEARLY_SKU = "yearly_subscription"

    const val BASIC_MONTHLY_SKU = "basic_monthly"
    const val PREMIUM_MONTHLY_SKU = "premium_monthly"
    const val BASIC_YEARLY_SKU = "basic_yearly"
    const val PREMIUM_YEARLY_SKU = "premium_yearly"

    const val INCREASE_QUANTITY = 1
    const val DECREASE_QUANTITY = -1

    const val UPDATE_TYPE_CHANGE_ITEM = "change_item"
    const val UPDATE_TYPE_DELETE_ITEM = "delete_item"


    const val REQUEST_CODE_ORDER = 5

    const val DISPLAY_VERSION = "0.5.0"
    const val EXTRA_FROM = "extraFrom"
    const val FROM_CART = "fromCart"

    const val Images_PATH = "/storage/emulated/0/"
    //const val Images_PATH = getAppD

    const val EXTRA_TAB_INDEX = "extra_tab_index"
    const val EXTRA_TAB_NAME = "extra_tab_name"
    const val EXTRA_SUB_CATEGORIES = "extra_sub_categories"
    const val EXTRA_CAT_NAME = "extra_cat_name"
    const val EXTRA_CAT_ID = "extra_cat_id"

    const val EXTRA_PROGRESS_VALUE = "progress_value"

    const val EXTRA_PRODUCT = "extra_product"

    /** The base URL of the API */
    // const val BASE_URL: String = "https://jsonplaceholder.typicode.com"
    const val DEVICE_TYPE: String = "android"

    //const val HELP_URL = "http://georeminder.reviewprototypes.com/help"
    const val TERMS_URL = "http://goddess.reviewprototypes.com/terms_and_conditions"
    const val FAQ_URL = "http://goddess.reviewprototypes.com/faq"
    const val ABOUT_US_URL = "http://goddess.reviewprototypes.com/about_us"
    const val PRIVACY_URL = "http://goddess.reviewprototypes.com/privacy"
    const val INSTAGRAM_URL = "https://www.instagram.com/krystalaranyani/"
    const val FB_URL = "https://www.facebook.com/krystalaranyani"
    const val DEMO_GOOGLE_URL = "https://www.google.com/"

    const val DB_NAME = "goddess.db"
    const val DEFAULT_DATE_FORMAT: String = "dd/MM/yyyy"

    const val SPLASH_TIME = 2000

    // Request Codes
    const val PICK_IMAGE_GALLERY_REQUEST_CODE = 31
    const val PICK_IMAGE_CAMERA_REQUEST_CODE = 32
    const val PICK_VIDEO_GALLERY_REQUEST_CODE = 33
    const val PICK_VIDEO_CAMERA_REQUEST_CODE = 34
    const val MAX_DAYS_IN_WEEK = 7

    //Extras
    const val EXTRA_DATA = "data"
    const val EXTRA_POSITION = "position"
    const val EXTRA_IS_REQUESTED = "isRequested"
    const val EXTRA_REQ_STATUS_CHANGE = "requestStatusChange"
    const val EXTRA_URL = "url"
    const val EXTRA_REQUEST_CODE = 1000

    const val REQUEST_CODE_SUBSCRIPTION_SCREEN = 87

    const val EXTRA_CALENDER_DAY_ID = "calender_day_id"

    // Paging
    const val ITEMS_LIMIT = 10


    /* Bundle and Intent */
    const val KEY_COLOR_CODE = "color_code"
    const val KEY_IMG_CODE = "image_code"

    const val KEY_WIDTH = "&w="
    const val KEY_HEIGHT = "&h="
    const val KEY_QUALITY = "&q="
    const val KEY_SCALE_TYPE = "&fit="

    const val SCALE_TYPE_CROP = "crop"
    const val SCALE_TYPE_CONTAIN = "contain"

    const val RECIPE_NAME = "recipe_name"
    const val RECIPE_ID = "recipe_id"
    const val OPINION_ID = "opinion_id"
    const val COMMENT_COUNT = "comment_count"

    const val OPEN_COMMENT = 1036
    const val VIDEO_SIZE = 5  //5 MB


    const val TITLE = "title"
    const val URL = "url"

    const val FROM = "from"
    const val FROM_ORDER = "fromOrder"
    const val SELECTED_ADDRESS = "selectedAddress"

    const val PROGRESS_VALUE = "progress_value"
    const val CURRENT_LEVEL_POINT = "current_level_point"

    const val MESSAGES_FAREBASE = "messages_firebase"
    const val TITLE_FAREBASE = "title_firebase"
    const val IS_FIREBASE_NOTIFICATION = "is_firebase_notification"

    const val EXTRA_DISABLE_BASIC_PLAN = "disable_basic_plan"


    const val ADDRESS_ID = "address_id"

    const val NO_SUBSCRIPTION = 91
    const val BASIC_SUBSCRIPTION = 92
    const val PREMIUM_SUBSCRIPTION = 93

    const val ORDER_NO = "order_no"

    const val ORDER_RECEIVED = "Order Received at "
    const val ORDER_PREPARING = "Preparing at "
    const val ORDER_READY = "Ready at "
    const val ORDER_DISPATCH = "Dispatch at "
    const val ORDER_DELIVERED = "Delivered!! at "
    const val ORDER_RESPONSE = "order_response"

    const val SELECT_CITY_STATE = 10256
    const val SELECTED_CITY = "CITY"
    const val SELECTED_STATE = "STATE"
    const val SELECTED_COUNTRY = "COUNTRY"
    const val VIDEO_URL = "FileName"
    const val SCREEN_ORIENTATION = "screen_orientation"

}
