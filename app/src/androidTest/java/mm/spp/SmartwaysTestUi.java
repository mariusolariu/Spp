package mm.spp;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import mm.spp.root.MapsActivity;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by molariu on 12/30/2017.
 */

@RunWith(AndroidJUnit4.class)
public class SmartwaysTestUi {

  //load the activity
  @Rule
  public ActivityTestRule<MapsActivity> mapsActivityActivityTestRule = new ActivityTestRule<>(MapsActivity.class);

  @Test
  public void testAtoBLocationResult(){
    onView(withId(R.id.startACTV)).perform(typeText("Sibot"));
    onView(withId(R.id.destACTV)).perform(typeText("Balomiru"));
    onView(withId(R.id.distanceTV)).check(matches(allOf(withText("5.3 km"), isDisplayed())));
  }

  @Test
  public  void  testImageViewsPresent(){
    onView(withId(R.id.timeIV)).check(matches(isDisplayed()));
  }
}

