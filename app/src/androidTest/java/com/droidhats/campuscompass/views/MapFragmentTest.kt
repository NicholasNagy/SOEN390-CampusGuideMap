package com.droidhats.campuscompass.views

import android.view.View
import android.view.ViewGroup
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MapFragmentTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION"
        )

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)

        //Ensuring the app starts with splash_fragment
        if (navController.currentDestination?.id == R.id.splash_fragment) {

            //navigating to map_fragment - You can put either action id or destination id.
            //I chose the action id so I can check that it indeed takes you the specified destination id.
            navController.navigate(R.id.action_splashFragment_to_mapsActivity)

            //Checking if action id indeed took you to the correct destination id
            assertEquals(navController.currentDestination?.id!!, R.id.map_fragment)

            //Waiting 5 seconds for splash screen to load
            Thread.sleep(5000)

            //Checking if that action id did take you to map_fragment view
            onView(withId(R.id.coordinate_layout)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun test_SearchBar() {
        //Checks if the search bar with text "search" exists
        onView(
            allOf(
                withId(R.id.mt_placeholder), withText("Search"), isDisplayed()
            )
        ).check(matches(withText("Search")))

        //Checks if magnifying glass button is displayed
        onView(
            allOf(
                withId(R.id.mt_search), isDisplayed()
            )
        ).check(matches(isDisplayed()))

        //Checks if Search bar can be clicked
        onView(
            allOf(
                withId(R.id.mapFragSearchBar), isDisplayed()
            )
        ).perform(click())

        //Checks if text "Search" is displayed even after search bar was clicked
        onView(
            allOf(
                withId(R.id.search_src_text), withHint("Search"), isDisplayed()
            )
        ).check(matches(withHint("Search")))

        //Checks if info text that prompts to enter the text in the text field is displayed
        onView(
            allOf(
                withId(R.id.search_info), withText("Enter Street, Address, Concordia Classroom…"),
                isDisplayed()
            )
        ).check(matches(withText("Enter Street, Address, Concordia Classroom…")))

        //Checks if Autocomplete options for text "h" are displayed
        onView(
            allOf(
                withId(R.id.search_src_text), isDisplayed()
            )
        ).perform(ViewActions.typeText("h"), ViewActions.closeSoftKeyboard())

        Thread.sleep(1000)

        //Checks that if a Clear button is displayed next to the search field
        onView(
            allOf(
                withId(R.id.search_close_btn), withContentDescription("Clear query"),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))

        //selects the indoor location hall-167
        onView(
            allOf(
                withId(R.id.search_suggestions_card_view),
                withChild(
                    allOf(
                        withId(R.id.relative_layout1),
                        withChild(
                            allOf(
                                withId(R.id.search_suggestion),
                                withText("hall-167")
                            )
                        )
                    )
                ),
                isDisplayed()
            )
        ).perform(click())

        //check to go back to MapFragment
        Espresso.pressBack()

        //clicks on the search bar to do outdoor/indoor navigation
        onView(
            allOf(
                withId(R.id.mapFragSearchBar), isDisplayed()
            )
        ).perform(click())

        //Checks if Autocomplete options for text "h" are displayed
        onView(
            allOf(
                withId(R.id.search_src_text), isDisplayed()
            )
        ).perform(ViewActions.typeText("h"), ViewActions.closeSoftKeyboard())

        Thread.sleep(1000)

        //Performs click on the Set Navigation Button
        onView(
            allOf(
                withId(R.id.setNavigationPoint),
                withContentDescription("SetNavigation"),
                hasSibling(
                    allOf(
                        withId(R.id.relative_layout1),
                        withChild(
                            allOf(
                                withId(R.id.search_suggestion),
                                withText("hall-167")
                            )
                        )
                    )
                ),
                isDisplayed()
            )
        ).perform(click())

        //Checks if The "From" field displays text "From"
        onView(
            allOf(
                withId(R.id.search_src_text), withHint("From"),
                isDisplayed()
            )
        ).check(matches(withHint("From")))

        //Checks if Navigation button is displayed
        onView(
            allOf(
                withId(R.id.startNavigationButton), withContentDescription("Navigate"),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))

        //Checks if current location button is displayed
        onView(
            allOf(
                withId(R.id.myCurrentLocationFAB),
                isDisplayed()
            )
        ).perform(click())

        //Checks if "from" field is replaced by test "Your Location"
        onView(
            allOf(
                withId(R.id.search_src_text), withText("Your Current Location"), withHint("To"),
                isDisplayed()
            )
        ).check(matches(withText("Your Current Location")))

        //Checks if Clear button for secondary (To) searchbar is displayed
        onView(
            allOf(
                withId(R.id.secondarySearchBar),
                withChild(
                    allOf(
                        withId(R.id.search_bar),
                        withChild(
                            allOf(
                                withId(R.id.search_edit_frame),
                                withChild(
                                    allOf(
                                        withId(R.id.search_plate),
                                        withChild(
                                            withId(R.id.search_close_btn)
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))

        //Checks if Clear button for main (from) searchbar is displayed
        onView(
            allOf(
                withId(R.id.mainSearchBar),
                withChild(
                    allOf(
                        withId(R.id.search_bar),
                        withChild(
                            allOf(
                                withId(R.id.search_edit_frame),
                                withChild(
                                    allOf(
                                        withId(R.id.search_plate),
                                        withChild(
                                            withId(R.id.search_close_btn)
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))

        //Checking if driving option is displayed
        onView(withId(R.id.radio_transport_mode_driving)).check(matches(isDisplayed()))

        //Checking when the driving option is clicked, it's indeed selected
        onView(withId(R.id.radio_transport_mode_driving)).perform(click()).check(matches(isChecked()))

        //Checking if transit option is displayed
        onView(withId(R.id.radio_transport_mode_transit)).check(matches(isDisplayed()))

        //Checking when the transit option is clicked, it's indeed selected
        onView(withId(R.id.radio_transport_mode_transit)).perform(click()).check(matches(isChecked()))

        //Checking if walking option is displayed
        onView(withId(R.id.radio_transport_mode_walking)).check(matches(isDisplayed()))

        //Checking when the walking option is clicked, it's indeed selected
        onView(withId(R.id.radio_transport_mode_walking)).perform(click()).check(matches(isChecked()))

        //Checking if bicycle option is displayed
        onView(withId(R.id.radio_transport_mode_bicycle)).check(matches(isDisplayed()))

        //Checking when the bicycle option is clicked, it's indeed selected
        onView(withId(R.id.radio_transport_mode_bicycle)).perform(click()).check(matches(isChecked()))
    }

    //function that searches for restaurant Ganadara
    private fun searchOutdoorLocation() {

        // check if search bar exists and clicks on it
        onView(withId(R.id.mapFragSearchBar)).check(matches(isDisplayed())).perform(click())

        //Checking if action id indeed took you to the correct fragment
        onView(withId(R.id.search_fragment)).check(matches(isDisplayed()))

        //search for restaurant Ganadara
        onView(
            allOf(
                withId(R.id.search_src_text), isDisplayed()
            )
        ).perform(ViewActions.typeText("ganadara"), ViewActions.closeSoftKeyboard())

        //allow suggestions to load
        Thread.sleep(2000)

        //click on the Ganadara restaurant suggestion from list
        onView(allOf(withId(R.id.search_suggestion), withText("Ganadara"))).perform(click())
    }

    @Test
    fun test_PlaceInfoCard() {

        searchOutdoorLocation()

        //allow map to readjust view
        Thread.sleep(1500)

        //check that place info card is displayed
        onView(withId(R.id.place_card)).check(matches(isDisplayed()))

        //check that location name is displayed
        onView(withId(R.id.place_card_name)).check(matches(isDisplayed()))

        //check that location address is displayed
        onView(withId(R.id.place_card_category)).check(matches(isDisplayed()))

        //check that favorites button is displayed and click it
        onView(withId(R.id.place_card_favorites_button)).check(matches(isDisplayed())).perform(click())

        //Ensuring the text of the favorites button is Save
 //       onView(withId(R.id.place_card_favorites_button)).check(matches(withText("Save")))

        //check that close button is displayed
        onView(withId(R.id.place_card_close_button)).check(matches(isDisplayed()))

        //check that directions button is displayed
        onView(withId(R.id.place_card_directions_button)).check(matches(isDisplayed()))

        //Ensuring the text of the directions button is Directions
//       onView(withId(R.id.place_card_directions_button)).check(matches(withText("Directions")))

        //click on close button
        onView(withId(R.id.place_card_close_button)).perform(click())

        //allow place info card to close
        Thread.sleep(1000)

        //check if place info card really closed
        onView(withId(R.id.place_card)).check(matches(not(isDisplayed())))

        //repeat search for restaurant Ganadara in order to test navigation from the place info card
        searchOutdoorLocation()

        //allow map to readjust view
        Thread.sleep(1500)

        //check that favorites button is displayed and click it
        onView(withId(R.id.place_card_favorites_button)).check(matches(isDisplayed())).perform(click())

        //Ensuring the text of the favorites button is Saved
  //    onView(withId(R.id.place_card_favorites_button)).check(matches(withText("Saved")))

        //click on directions button
        onView(withId(R.id.place_card_directions_button)).perform(click())

        //allow search page to load
        //Thread.sleep(1500)

        //check if we are taken to search page
        onView(withId(R.id.search_fragment)).check(matches(isDisplayed()))

        //check if location name is set as the destination
        onView(allOf(withId(R.id.search_src_text), withText("Ganadara"))).check(matches(isDisplayed()))
    }

    @Test
    fun test_myPlaces(){
        //search for restaurant Ganadara
        searchOutdoorLocation()

        //allow map to readjust view
        Thread.sleep(1500)

        //check that favorites button is displayed and click it
        onView(withId(R.id.place_card_favorites_button)).check(matches(isDisplayed())).perform(click())

        //Clicks the navbar
        onView(
            allOf(
                withId(R.id.mt_nav),
                childAtPosition(
                    allOf(
                        withId(R.id.root),
                        childAtPosition(
                            withId(R.id.mt_container),
                            0
                        )
                    ),
                    3
                ),
                isDisplayed())).perform(click())
        //checks that the my places option is visible in the nav bar and clicks it
        onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed())).perform(click())
        Thread.sleep(2000)

        //Click on favorites location item
        onView(withId(R.id.favorites_location_item)).perform(click())

        //Clicks the navbar
        onView(
            allOf(
                withId(R.id.mt_nav),
                childAtPosition(
                    allOf(
                        withId(R.id.root),
                        childAtPosition(
                            withId(R.id.mt_container),
                            0
                        )
                    ),
                    3
                ),
                isDisplayed())).perform(click())
        //checks that the my places option is visible in the nav bar and clicks it
        onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed())).perform(click())
        Thread.sleep(2000)

        //click on navigation button
        onView(withId(R.id.setNavigationPoint)).perform(click())

        //click on start navigation button
        onView(withId(R.id.startNavigationButton)).perform(click())

        //allow navigation to load
        Thread.sleep(2000)

        //close instructions
        onView(withId(R.id.buttonCloseInstructions)).perform(click())

        //repeat search for restaurant Ganadara in order to test navigation from the place info card
        searchOutdoorLocation()

        //allow map to readjust view
        Thread.sleep(1500)

        //check that favorites button is displayed and click it
        onView(withId(R.id.place_card_favorites_button)).check(matches(isDisplayed())).perform(click())
    }

    @Test
    fun test_explore(){
        //Clicks the navbar
        onView(
            allOf(
                withId(R.id.mt_nav),
                childAtPosition(
                    allOf(
                        withId(R.id.root),
                        childAtPosition(
                            withId(R.id.mt_container),
                            0
                        )
                    ),
                    3
                ),
                isDisplayed())).perform(click())
        //checks that the explore option is visible in the nav bar and clicks it
        onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed())).perform(click())
        Thread.sleep(2000)

        //click on food button
        onView(withId(R.id.select_food_button)).perform(click())

        Thread.sleep(2000)
        //click on button menu explore
        onView(withId(R.id.button_explore)).perform(click())

        //click on drinks button
        onView(withId(R.id.select_drinks_button)).perform(click())

        Thread.sleep(2000)
        //click on button menu explore
        onView(withId(R.id.button_explore)).perform(click())

        //click on toggle button to switch campus
        onView(withId(R.id.toggleButton2)).perform(click())

        //click on toggle button to switch campus
        onView(withId(R.id.select_study_button)).perform(click())
    }

    @Test
    fun test_SwitchToggle() {

        //Checking if toggle button is displayed
        onView(withId(R.id.toggleButton)).check(matches(isDisplayed()))

        //Checking when the toggle is clicked, it's indeed checked
        onView(withId(R.id.toggleButton)).perform(click()).check(matches(isChecked()))
        Thread.sleep(2000)

        //Ensuring the text of the toggle just clicked is indeed SWG
        onView(withId(R.id.toggleButton)).check(matches(withText("LOY")))

        //Checking when the toggle is clicked again, it's indeed not checked
        onView(withId(R.id.toggleButton)).perform(click()).check(matches(isNotChecked()))
        Thread.sleep(2000)

        //Ensuring the text of the toggle just clicked is indeed LOY
        onView(withId(R.id.toggleButton)).check(matches(withText("SGW")))
    }

    @Test
    fun test_additionalMenuBar() {

        BottomSheetBehavior.from(activityRule.activity.bottom_sheet).state =
            BottomSheetBehavior.STATE_EXPANDED

        //Setting a delay to allow the bottom sheet to load
        Thread.sleep(1000)

        //Checking if building image is displayed
        onView(withId(R.id.building_image)).check(matches(isDisplayed()))

        //Checking if separator bar is displayed
        onView(withId(R.id.separator_bar)).check(matches(isDisplayed()))

        //Checking if building name is displayed
        onView(withId(R.id.bottom_sheet_building_name)).check(matches(isDisplayed()))

        //Checking if building address is displayed
        onView(withId(R.id.bottom_sheet_building_address)).check(matches(isDisplayed()))

        //Checking if opening hours are displayed
        onView(withId(R.id.bottom_sheet_open_hours)).check(matches(isDisplayed()))

        //Checking if services are displayed
        onView(withId(R.id.bottom_sheet_services)).check(matches(isDisplayed()))

        //Checking if departments are displayed
        onView(withId(R.id.bottom_sheet_departments)).check(matches(isDisplayed()))

        //Checking if the direction button is displayed and named Directions
        onView(withId(R.id.bottom_sheet_directions_button)).check(matches(isDisplayed()))
            .check(matches(withText("Directions")))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}