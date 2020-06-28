package com.krystal.goddesslifestyle.data

import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.response.*
import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiService {
    // [START] Demo APIs
    /*@GET("test=123")
    fun apiGet(): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("login")
    fun apiSignIn(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @GET("profile")
    fun apiProfile(@QueryMap params: HashMap<String, String>): Observable<BaseResponse>

    @Multipart
    @POST("update_profile")
    fun apiUpdateProfile(@PartMap params: HashMap<String, RequestBody>): Observable<BaseResponse>*/
    // [END] Demo APIs

    @GET("profile")
    fun apiProfile(@QueryMap params: HashMap<String, String>): Observable<BaseResponse>

    @GET("current_month_calendar")
    fun apiGetCalenderData(): Observable<CalenderResponse>

    @GET("sync_calendar")
    fun apiSyncCalenderData(@QueryMap params: HashMap<String, String>): Observable<SyncCalenderResponse>

    @GET("sync_calendar_events")
    fun apiSyncCalenderEventData(@QueryMap params: HashMap<String, String>): Observable<SyncCalenderEventResponse>

    @FormUrlEncoded
    @POST("user/login")
    fun apiLogin(@FieldMap params: HashMap<String, String>): Observable<LoginResponse>

    @FormUrlEncoded
    @POST("user/register")
    fun apiRegistrationSocila(@FieldMap params: HashMap<String, String>): Observable<LoginResponse>

    @Multipart
    @POST("user/register")
    fun apiRegistration(@PartMap params: HashMap<String, RequestBody>): Observable<LoginResponse>

    @POST("password/email")
    fun apiForgetPassword(@QueryMap params: HashMap<String, String>): Observable<ForgetPasswordResponse>

    @GET("performer_of_the_months")
    fun apiGetOfTheMonthData(): Observable<OfTheMonthResponse>

    @FormUrlEncoded
    @POST("add_user_activity")
    fun apiAddUserPoints(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @GET("get_video_library_categories")
    fun apiGetVideoCategories(): Observable<VideoCategoriesResponse>

    @GET("get_video_libraries")
    fun apiGetVideos(@QueryMap params: HashMap<String, String>): Observable<VideosListResponse>

    /* Jatin */
    @GET("get_all_recipes")
    fun apiGetRecipeList(@QueryMap params: HashMap<String, String?>): Observable<RecipeListReponse>


    @GET("get_recipe_details")
    fun apiGetRecipeDetails(@QueryMap params: HashMap<String, String?>): Observable<RecipeDetailsResponse>

    @FormUrlEncoded
    @POST("like_or_unlike_recipe")
    fun apiLikeUnlike(@FieldMap params: HashMap<String, Any>): Observable<LikeUnlikeResponse>

    //@FormUrlEncoded
    @GET("goddess_shop")
    fun apiShop(): Observable<ShopResponse>

    @FormUrlEncoded
    @POST("place_order")
    fun apiPlaceOrder(@FieldMap params: HashMap<String, String>): Observable<PlaceOrderResponse>

    @FormUrlEncoded
    @POST("place_order")
    fun apiPlaceOrder3(@FieldMap params: HashMap<String, String>): Observable<ResponseBody>

    @FormUrlEncoded
    @POST("place_order")
    fun apiPlaceOrderTwo(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @GET("get_goddess_community_count")
    fun apiGetGoddessCommunityCount(): Observable<GoddessCommunityCountResponse>

    @GET("get_goddess_community_opinions")
    fun apiGetGoddessCommunityOpinions(@QueryMap params: HashMap<String, String>): Observable<CommunityOpinionResponse>

    @FormUrlEncoded
    @POST("like_goddess_community_opinion")
    fun apiLikeUnlikeCommunityOpinioun(@FieldMap params: HashMap<String, Any>): Observable<LikeOpinionResponse>

    @GET("get_goddess_community_opinion_comments")
    fun apiGetCommunityOpinionComment(@QueryMap params: HashMap<String, Any?>): Observable<CommunityOpinionCommentListResponse>


    @FormUrlEncoded
    @POST("comment_goddess_community_opinion")
    fun apiAddCommunityOpinionComment(@FieldMap params: HashMap<String, Any>): Observable<BaseResponse>

    @Multipart
    @POST("add_new_goddess_community_opinion")
    fun apiAddCommunityOpinion(@PartMap params: HashMap<String, RequestBody>): Observable<BaseResponse>

    @Multipart
    @POST("edit_goddess_community_opinion")
    fun apiEditCommunityOpinion(@PartMap params: HashMap<String, RequestBody>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("delete_goddess_community_opinion")
    fun apiDeleteOpinion(@FieldMap params: HashMap<String, Any>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("user_subscriptions")
    fun apiVerifySubscription(@FieldMap params: HashMap<String, String>): Observable<UserSubscriptionResponse>

    @POST("check_user_subscriptions")
    fun apiGetUserSubscriptions(): Observable<UserSubscriptionResponse>

    @GET("user/{id}")
    fun apiGetMyProfile(@Path("id") id: Int): Observable<MyProfileResponse>

    @GET("user/{id}")
    fun apiGetMyProfileNew(@Path("id") id: Int): Observable<LoginResponse>

    @Multipart
    @POST("edit_profile")
    fun apiEditProfile(@PartMap params: HashMap<String, RequestBody>): Observable<EditProfileResponse>

    @FormUrlEncoded
    @POST("change_password")
    fun apiChangePassword(@FieldMap params: HashMap<String, Any>): Observable<BaseResponse>

    @GET("get_leaderboard")
    fun apiLeaderBoard(): Observable<LeaderBoardResponse>

    @FormUrlEncoded
    @POST("contactus")
    fun apiContactUs(@FieldMap params: HashMap<String, Any>): Observable<BaseResponse>


    @GET("get_user_activity")
    fun apiYourPoint(@QueryMap params: HashMap<String, Any>): Observable<YourPointsResponse>

    @GET("settings")
    fun apiAppSettings(): Observable<AppSettingsResponse>

    /*@POST("get_ephemeral_key")
    fun getEphemeralKey(@QueryMap params: HashMap<String, String>): Observable<ResponseBody>*/

    @POST("get_ephemeral_key")
    fun getEphemeralKey(@QueryMap params: HashMap<String, String>): Call<ResponseBody>

    @GET("my_address")
    fun apiMyAddressList(): Observable<MyAddressListResponse>

    @FormUrlEncoded
    @POST("delete_delivery_address")
    fun apiDeleteMyAddress(@FieldMap params: HashMap<String, Any?>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("add_new_delivery_address")
    fun apiAddAddress(@FieldMap params: HashMap<String, Any>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("edit_delivery_address")
    fun apiEditAddress(@FieldMap params: HashMap<String, Any>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("favourite_recipes")
    fun apiFavouritesRecipes(@FieldMap params: HashMap<String, Any>): Observable<FavouritesResponse>

    @FormUrlEncoded
    @POST("notification_settings")
    fun apiSettingNotification(@FieldMap params: HashMap<String, Any>): Observable<SettingResponse>

    @GET("getnotifications")
    fun apiGetNotification(@QueryMap params: HashMap<String, Any>): Observable<GetNotificationListResponse>

    @FormUrlEncoded
    @POST("removenotification")
    fun apiRemoveNotification(@FieldMap params: HashMap<String, Any>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("clearnotifications")
    fun apiClearNotification(@FieldMap params: HashMap<String, Any>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("order_list")
    fun apiGetOrderList(@FieldMap params: HashMap<String, Any>): Observable<OrderListResponse>

    @POST("user/cancel_account")
    fun apiCencelAccount(): Observable<BaseResponse>

    @POST("user/logout")
    fun apiLogOut(): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("updatedevicetoken")
    fun apiUpDateToken(@FieldMap params: HashMap<String, Any>): Observable<BaseResponse>

}