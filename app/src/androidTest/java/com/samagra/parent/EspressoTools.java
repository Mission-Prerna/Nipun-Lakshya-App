package com.samagra.parent;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.AllOf;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;


public final class EspressoTools {
    private static final int ATTEMPTS = 10000;
    private static final long WAITING_TIME = 50;

//    public static ViewAction waitId(final int viewId, final long millis) {
//        return new ViewAction() {
//            @Override
//            public Matcher<View> getConstraints() {
//                return isRoot();
//            }
//
//            @Override
//            public String getDescription() {
//                return "wait for a specific view with id <" + viewId + "> during " + millis + " millis.";
//            }
//
//            @Override
//            public void perform(final UiController uiController, final View view) {
//                uiController.loopMainThreadUntilIdle();
//                final long startTime = System.currentTimeMillis();
//                final long endTime = startTime + millis;
//                final Matcher<View> viewMatcher = withId(viewId);
//
//                do {
//                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
//                        // found view with required ID
//                        if (viewMatcher.matches(child)) {
//                            return;
//                        }
//                    }
//
//                    uiController.loopMainThreadForAtLeast(50);
//                }
//                while (System.currentTimeMillis() < endTime);
//
//                // timeout happens
//                throw new PerformException.Builder()
//                        .withActionDescription(this.getDescription())
//                        .withViewDescription(HumanReadables.describe(view))
//                        .withCause(new TimeoutException())
//                        .build();
//            }
//        };
//    }

    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    public static ViewInteraction waitForElementUntilDisplayed(int id, String className) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {
                ViewInteraction layout = onView(
                        AllOf.allOf(ViewMatchers.withId(id),
                                childAtPosition(
                                        childAtPosition(
                                                withClassName(is(className)),
                                                2),
                                        2),
                                isDisplayed()));
                return layout;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction waitForTextInputDisplayed(int id, int index) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {

                ViewInteraction textInputEditText = onView(withIndex(withId(id), index));

                return textInputEditText;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction waitForButtonDisplay(int id, int childid, String withString, String classname, int postion, int childposition) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {
                ViewInteraction appCompatButton = onView(
                        allOf(withId(id), withText(withString),
                                childAtPosition(
                                        allOf(withId(childid),
                                                childAtPosition(
                                                        withClassName(is(classname)),
                                                        postion)),
                                        childposition),
                                isDisplayed()));
                return appCompatButton;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction waitForNextButtonDisplay(int id, int childid, String withString, int classid, int postion, int childposition) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {
                ViewInteraction appCompatButton = onView(
                        allOf(withId(id), withText(withString),
                                childAtPosition(
                                        allOf(withId(childid),
                                                childAtPosition(
                                                        withId(classid),
                                                        postion)),
                                        childposition),
                                isDisplayed()));
                return appCompatButton;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction EditInputDisplayed(int id) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {
                ViewInteraction appCompatEditText = onView(Matchers.allOf(withId(id))).check(matches(isDisplayed()));

                return appCompatEditText;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction ButtonDisplayandClick(int id, String withString, String classname, int postion, int childposition) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {
                ViewInteraction appCompatButton = onView(Matchers.allOf(withId(id), withText(withString),childAtPosition(
                        childAtPosition(
                                withClassName(is(classname)),
                                postion),
                        childposition)));
                appCompatButton.check(matches(isDisplayed()));

                return appCompatButton;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction TextViewDisplayandClick(int id, String withString) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {
                ViewInteraction appCompatTextView = onView(Matchers.allOf(withId(id),withText(withString))).check(matches(isDisplayed()));

                return appCompatTextView;

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction ButtonDisplaywithID(int id, String withString, int classid, int postion, int childposition) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {
                ViewInteraction appCompatButton = onView(
                        allOf(withId(id), withText(withString),
                                childAtPosition(
                                        childAtPosition(
                                                withId(classid),
                                                postion),
                                        childposition),
                                isDisplayed()));

                return appCompatButton;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction DisplaywithContentDescription(String withString, int classid,int childclassid, int postion, int childposition) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {

                ViewInteraction appCompatImageButton = onView(
                        allOf(withContentDescription(withString),
                                childAtPosition(
                                        allOf(withId(classid),
                                                childAtPosition(
                                                        withId(childclassid),
                                                        postion)),
                                        childposition),
                                isDisplayed()));
                return appCompatImageButton;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction FabDisplayandClick(int id, int childid,int classid, int postion, int childposition) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {
                ViewInteraction floatingActionButton = onView(
                        Matchers.allOf(withId(id),
                                childAtPosition(
                                        Matchers.allOf(withId(childid),
                                                childAtPosition(
                                                        withId(classid),
                                                        postion)),
                                        childposition),
                                isDisplayed()));
                return floatingActionButton;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static DataInteraction ListAdapterDisplayandClick(int id, int classid, int postion, int childposition) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {
                DataInteraction layout = onData(anything())
                        .inAdapterView(Matchers.allOf(withId(id),
                                childAtPosition(
                                        withId(classid),
                                        postion)))
                        .atPosition(childposition);
                return layout;

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ViewInteraction DisplaywithText(String withString, int classid,String classname, int postion, int childposition) {
        int i = 0;
        while (i++ < ATTEMPTS) {
            try {

                ViewInteraction appCompatRadioButton = onView(
                        Matchers.allOf(withText(withString),
                                childAtPosition(
                                        Matchers.allOf(withId(classid),
                                                childAtPosition(
                                                        withClassName(is(classname)),
                                                        postion)),
                                        childposition),
                                isDisplayed()));
                return appCompatRadioButton;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    private static Matcher<View> withIndex(Matcher<View> viewMatcher, final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

}
