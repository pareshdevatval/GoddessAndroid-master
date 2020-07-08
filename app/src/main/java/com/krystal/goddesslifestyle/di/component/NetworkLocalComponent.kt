package com.krystal.goddesslifestyle.di.component

import com.krystal.goddesslifestyle.MyPlanActivity
import com.krystal.goddesslifestyle.ui.WaveActivity
import com.krystal.goddesslifestyle.ui.activity.*
import com.krystal.goddesslifestyle.ui.community.ActivityComments
import com.krystal.goddesslifestyle.ui.community.GoddessCommunityOpinionsFragment
import com.krystal.goddesslifestyle.ui.community.GoddessCommunityFragment
import com.krystal.goddesslifestyle.ui.main_activity.*
import com.krystal.goddesslifestyle.ui.of_the_month.GoddessOfTheMonthFragment
import com.krystal.goddesslifestyle.ui.of_the_month.OfTheMonthActivity
import com.krystal.goddesslifestyle.ui.of_the_month.RecipeOfTheMonthFragment
import com.krystal.goddesslifestyle.ui.of_the_month.TeacherOfTheMonthFragment
import com.krystal.goddesslifestyle.ui.profile.ChangesPasswordActivity
import com.krystal.goddesslifestyle.ui.profile.EditProfileActivity
import com.krystal.goddesslifestyle.ui.profile.MyProfileActivity
import com.krystal.goddesslifestyle.ui.profile.YogaPointsActivity
import com.krystal.goddesslifestyle.ui.profile.yoga_point_fragment.YourPointFragment
import com.krystal.goddesslifestyle.ui.recipe.*
import com.krystal.goddesslifestyle.ui.recipe_details.DetailsFragment
import com.krystal.goddesslifestyle.ui.recipe_details.HowToCookFragment
import com.krystal.goddesslifestyle.ui.recipe_details.IngredientsFragment
import com.krystal.goddesslifestyle.ui.recipe_details.RecipeDetailsActivity
import com.krystal.goddesslifestyle.ui.shop.*
import com.krystal.goddesslifestyle.ui.subscription.SubscriptionActivity
import com.krystal.goddesslifestyle.ui.video_library.VideoDetailsListActivity
import com.krystal.goddesslifestyle.ui.video_library.VideoListingFragment
import dagger.Component

//Created by imobdev-rujul on 2/1/19
@Component(dependencies = [LocalDataComponent::class, NetworkComponent::class])
interface NetworkLocalComponent {
    fun injectSplashActivity(splashActivity: SplashActivity)
    fun injectTutorialActivity(tutorialActivity: TutorialActivity)
    fun injectLoginActivity(loginActivity: LoginActivity)
    fun injectForgetPasswordActivity(forgetPasswordActivity: ForgetPasswordActivity)
    fun injectSignUpActivity(signUpActivity: SignUpActivity)
    fun injectProductDetailsActivity(productDetailsActivity: ProductDetailsActivity)
    fun injectCartActivity(cartActivity: CartActivity)
    fun injectOrderReviewActivity(orderReviewActivity: OrderReviewActivity)
    fun injectPaymentActivity(paymentActivity: PaymentActivity)
    fun injectWaveActivity(waveActivity: WaveActivity)


    fun injectMainActivity(mainActivity: MainActivity)
    fun injectOfTheMonthActivity(ofTheMonthActivity: OfTheMonthActivity)
    fun injectVideoDetailsListActivity(videoDetailsListActivity: VideoDetailsListActivity)

    fun injectSubscriptionsActivity(subscriptionActivity: SubscriptionActivity)
    fun injectMyPlansActivity(myPlanActivity: MyPlanActivity)

    fun injectHomeFragment(homeFragment: HomeFragment)
    fun injectRecipesFragment(recipesFragment: RecipesFragment)
    fun injectVideoListingFragment(videoListingFragment: VideoListingFragment)

    fun injectShopFragment(shopFragment: ShopFragment)

    fun injectTodaysPracticePagerFragment(todaysPracticePagerFagment: TodaysPracticePagerFagment)
    fun injectTodaysMealPagerFragment(todaysMealPagerFragment: TodaysMealPagerFragment)
    fun injectTodaysJournalPagerFragment(todaysJournalPagerFagment: TodaysJournalPagerFagment)

    fun injectGoddessOfTheMonthFragment(goddessOfTheMonthFragment: GoddessOfTheMonthFragment)
    fun injectRecipeOfTheMonthFragment(recipeOfTheMonthFragment: RecipeOfTheMonthFragment)
    fun injectTeacherOfTheMonthFragment(teacherOfTheMonthFragment: TeacherOfTheMonthFragment)

    /* Jatin*/
    fun injectBreakfastFragment(breakfastFragment: BreakfastFragment)
    fun injectLunchFragment(lunchFragment: LunchFragment)
    fun injectDinnerFragment(dinnerFragment: DinnerFragment)
    fun injectDessertFragment(dessertFragment: DessertFragment)
    fun injectSnacksFragment(snacksFragment: SnacksFragment)
    fun injectDetailsFragment(detailsFragment: DetailsFragment)
    fun injectIngredientsFragment(ingredientsFragment: IngredientsFragment)
    fun injectHowToCookFragment(howToCookFragment: HowToCookFragment)
    fun injectRecipeDetailsActivity(recipeDetailsActivity: RecipeDetailsActivity)

    fun injectHowToAddRecipeActivity(howToAddRecipeActivity: HowToAddRecipeActivity)

    fun injectGoddessCommunityFragment(goddessCommunityFragment: GoddessCommunityFragment)
    fun injectCommunityDetailsFragment(communityDetailsFragment: GoddessCommunityOpinionsFragment)
    fun injectCommentsActivity(comments: ActivityComments)

    /*keshu odedara*/
    fun injectMyProfileActivity(myProfileActivity: MyProfileActivity)
    fun injectEditProfileActivity(editProfileActivity: EditProfileActivity)
    fun injectChangesPasswordActivity(changesPasswordActivity: ChangesPasswordActivity)
    fun injectFavouriteRecipesActivity(favouriteRecipesActivity: FavouriteRecipesActivity)
    fun injectNotificationActivity(notificationActivity: NotificationActivity)
    fun injectFAQActivity(faqActivity: FAQActivity)
    fun injectSettingActivity(settingActivity: SettingActivity)
    fun injectContactUsActivity(contactUsActivity: ContactUsActivity)
    fun injectOrderListActivity(orderListActivity: OrderListActivity)
    fun injectFilterActivity(filterActivity: FilterActivity)
    fun injectOrderStatusActivity(orderStatusActivity: OrderStatusActivity)
    fun injectWebViewActivity(webViewActivity: WebViewActivity)
    fun injectMyAddressListActivity(myAddressListActivity: MyAddressListActivity)
    fun injectOrderDetailActivity(orderDetailActivity: OrderDetailActivity)
    fun injectAddAddressActivity(addAddressActivity: AddAddressActivity)
    fun injectEditAddressActivity(editAddressActivity: EditAddressActivity)
    fun injectYogaPointsActivity(yogaPointsActivity: YogaPointsActivity)
    fun injectLeaderBoardActivity(leaderBoardActivity: LeaderBoardActivity)
    fun injectYourPointFragment(yourPointFragment: YourPointFragment)

    fun injectSelectCityStateActivity(activity:SelectCityStateActivity)
}